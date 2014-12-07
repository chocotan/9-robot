function updateKfb() {
    $.get("/",
    function(result) {
        var kfb = result.match(/(拥有(\d+)KFB)/ig);
        log("现在" + kfb);
    })
};
function log(info) {
    var date = new Date();
    var status = $("#robot_status");
    var hour = date.getHours();
    var min = date.getMinutes();
    if (hour < 10) hour = "0" + hour;
    if (min < 10) min = "0" + min;
    status.append(hour + ":" + min + "-" + info + "\n");
    if (status.length) status.get(0).scrollTop = status.get(0).scrollHeight;
}
function clickbox() {
    var iframe = $("#box");
    if (iframe.size() > 0) {
        iframe = $("#box").get(0);
    } else {
        if ($("#robot_status").size() == 0) {
            $("body").append("<div style='position:fixed;bottom:0;right:0;'><textarea id='robot_status' style='height:100px;'></textarea></div>");
        }
        log("开始挂机");
        iframe = $("<iframe src='/kf_smbox.php' id='box' name='box' style='display:none'></iframe>");
        $("body").append(iframe);
        $(iframe).load(function() {
            var boxes = $(this).contents().find(".box1").find("a");
            var index = Math.floor(Math.random() * boxes.size());
            $.get($(boxes.get(index)).attr("href"),
            function(result) {
                if (result.indexOf("请等待") >= 0) {
                    log("现在还不能点盒子，将于10分钟后重试");
                    setTimeout(
                    function() {
                        clickbox()
                    },600000)
                } else {
                    var kfb = result.match(/获得了(\d+)KFB的奖励/ig);
                    log("成功获取KFB:" + kfb + ", 将于300分钟后重试");
                    setTimeout(
                    function() {
                        clickbox()
                    },1000 * 60 * 300)
                }
                updateKfb();
            });
        })
        iframe = iframe.get(0);
    }
    iframe.src = '/kf_smbox.php';
}
function clickAd() {
    $.get("/",
    function(result) {
        var ad_link = $(".clear").next().find("a").attr("href");console.log(ad_link);
        if (ad_link) {
            $.ajax({
                url: "/" + ad_link,
                success: function() {
                    clickbox();
                },
                error: function() {
                    clickbox();
                }
            });

        }
    });
}
clickAd();
setInterval(
function() {
    $.get("/");
},180000);