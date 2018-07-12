package com.niurenjob.bullman.ShortVideo.common.misc;

/**
 * Created by jac on 2017/11/17.
 * Copyright © 2013-2017 Tencent Cloud. All Rights Reserved.
 */

public class TextChatMsg {

    public static enum Aligment{
        LEFT(0),
        RIGHT(1),
        CENTER(2);
        final int aligment;
        Aligment(int aligment){
            this.aligment = aligment;
        }
    }

    private String name;
    private String time;
    private String msg;
    private Aligment aligment;

    public TextChatMsg() {
    }

    public TextChatMsg(String name, String time, String msg, Aligment aligment) {
        this.name = name;
        this.time = time;
        this.msg = msg;
        this.aligment = aligment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Aligment aligment() {
        return aligment;
    }

    public void setAligment(Aligment aligment) {
        this.aligment = aligment;
    }
}
