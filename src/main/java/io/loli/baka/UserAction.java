package io.loli.baka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UserAction implements Runnable {

    private String name;
    private String passwd;

    private int kfbAdd = 0;

    private UserStatus status;

    private CloseableHttpClient client = HttpTools.getClient();
    private String totalKfb = "";
    private String chengzhang = "";
    private String shenmi = "";
    private String zaixian = "";

    private TextAreaMessageSender logger = null;

    public UserAction(String name, String passwd, TextAreaMessageSender logger) {
        this.logger = logger;
        logger.setUser(this);
        this.name = name;
        this.passwd = passwd;
        this.login();
        this.startRefresh();
        status = new UserStatus(this);
    }

    public void clickAdThenBox() {
        if (timerService.isShutdown()) {
            logger.log("结束获取kfb");
            return;
        }
        this.clickAd();
        logger.log("已点广告");
        int kfb = this.clickBox();
        if (kfb > 0)
            logger.log("获取了" + kfb + "KFB");
        else {
            if (timerService.isShutdown()) {
                logger.log("结束获取kfb");
                return;
            }
            logger.log("现在还不能点盒子，将于10分钟后再试");
            timerService.schedule(this, 10, TimeUnit.MINUTES);
            return;
        }
        kfbAdd += kfb;
        logger.log("一共获取了" + kfbAdd + "KFB");
        updateStatus();
        timerService.schedule(this, 301, TimeUnit.MINUTES);
    }

    private void updateStatus() {
        String str = HttpTools.get(client, BakaConfig.INDEX);
        totalKfb = findString(str, "拥有(\\d+)KFB");
        chengzhang = findString(str, "成长(\\d+)点");
        shenmi = findString(str, "神秘(\\d+)级");
        zaixian = findString(str, "在线(\\d+)分钟");

        logger.log("您现在的KFB:" + totalKfb);
        logger.log("您现在的神秘:" + shenmi);
        logger.log("您现在的成长:" + chengzhang);
        logger.log("您现在的在线分钟:" + zaixian);
    }

    protected int clickBox() {
        String boxLink = getBoxLink();
        if (boxLink.equals("")) {
            return 0;
        }
        String boxStr = HttpTools.get(client, BakaConfig.INDEX + "/" + boxLink);
        String output = findString(boxStr, "获得了(\\d+)KFB的奖励");
        if ("".equals(output)) {
            return 0;
        }
        int kfb = Integer.parseInt(output);
        return kfb;
    }

    public String getBoxLink() {
        String boxStr = HttpTools.get(client, BakaConfig.BOX_PAGE);
        Document doc = Jsoup.parse(boxStr);
        int size = doc.select(".box1").size();
        if (size == 0) {
            logger.err("盒子获取失败");
            return "";
        } else {
            Elements eles = doc.select(".box1").get(0).select("a");
            int boxSize = eles.size();
            int selectIndex = new Random().nextInt(boxSize);
            Element ele = eles.get(selectIndex);
            return ele.attr("href");
        }
    }

    protected String clickAd() {
        String fullAdLink = BakaConfig.INDEX + "/" + this.getAdLink();
        return HttpTools.get(client, fullAdLink);
    }

    protected String getAdLink() {
        String indexStr = HttpTools.get(client, BakaConfig.INDEX);
        String adLink = findString(indexStr, "(diy_ad_move.php\\?[0-9a-zA-Z=&]+)\"");
        return adLink;
    }

    private static String findString(String html, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(html);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public boolean login() {
        index();
        String loginStr = doLogin(client, name, passwd);
        boolean success = loginStr.contains("您已经顺利登录") || loginStr.contains("重复");
        if (!success) {
            logger.err("用户名密码不正确");
        } else {
            logger.log("您已经成功登陆");
        }
        return success;
    }

    public String index() {
        return HttpTools.get(client, BakaConfig.INDEX + "/index.php");
    }

    public static String doLogin(CloseableHttpClient client, String pwuser, String pwpwd) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.addAll(Arrays.asList(new NameValuePair[] { new BasicNameValuePair("pwuser", pwuser),
            new BasicNameValuePair("pwpwd", pwpwd), new BasicNameValuePair("hideid", "0"),
            new BasicNameValuePair("cktime", "0"), new BasicNameValuePair("jumpurl", "index.php"),
            new BasicNameValuePair("step", "2"), new BasicNameValuePair("lgt", "1") }));

        return HttpTools.post(client, BakaConfig.LOGIN_POST, params);
    }

    public void logout() {

    }

    public void checkLogin() {

    }

    public void buyVip() {

    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public void startRefresh() {
        if (!timerService.isShutdown())
            timerService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    index();
                }
            }, 1, 3, TimeUnit.MINUTES);
        else {
            logger.log("结束刷新");
        }
    }

    @Override
    public void run() {
        this.clickAdThenBox();
    }

    public static ScheduledExecutorService timerService = Executors.newScheduledThreadPool(20);

    public void close() {
        try {
            new Thread() {
                public void run() {
                    timerService.shutdown();
                    logger.log("线程结束");
                }
            }.start();
        } catch (Exception e) {
            logger.err(e.getMessage());
        }
    }

    public String getUserName() {
        return name;
    }

}
