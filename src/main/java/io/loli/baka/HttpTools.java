package io.loli.baka;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class HttpTools {

    public static CloseableHttpClient getClient() {
        return HttpClients.createDefault();
    }

    /**
     * 发出post请求
     */
    public static String post(CloseableHttpClient httpclient, String postUrl, List<NameValuePair> params) {

        HttpPost hp = new HttpPost(postUrl);
        CloseableHttpResponse response = null;
        String result = null;
        try {
            hp.setEntity(new UrlEncodedFormEntity(params, "GBK"));
            hp.setHeader(new BasicHeader("User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.120 Safari/537.36"));
            response = httpclient.execute(hp);
            result = EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String get(CloseableHttpClient httpclient, String getUrl) {
        HttpGet httpget = new HttpGet(getUrl);
        HttpResponse response;
        String result = null;
        try {
            httpget
                .setHeader(new BasicHeader("User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.120 Safari/537.36"));
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
        } catch (IOException e) {
        }
        return result;
    }
}
