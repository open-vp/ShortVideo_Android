package com.niurenjob.bullman.ShortVideo.play.net;

/**
 * Created by annidy on 2017/12/20.
 */

public class TXPlayInfoStream {
    String url;
    int height;
    int width;
    int size;
    int duration;
    int bitrate;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getHeight() {
        return height;
    }

    public int getDuration() {
        return duration;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
}
