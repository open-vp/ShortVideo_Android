package com.openvp.shortVideo.model;

/**
 * Created by shortVideo on 2017/4/1.
 * https://github.com/openvp
 * openvp@gmail.com
 */

public class OnlyCompressOverBean {

    private boolean succeed;

    private String videoPath;

    private String picPath;

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
}
