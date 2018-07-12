package com.niurenjob.bullman.ShortVideo.editor.utils;

import android.content.Context;

import com.tencent.ugc.TXVideoEditConstants;

/**
 * Created by yuejiaoli on 2017/10/16.
 * Demo层保存视频配置
 */
public class TCVideoConfig {

    private static final String TAG = "TCVideoConfig";
    private static TCVideoConfig sInstance;
    private final Context mContext;
    private TXVideoEditConstants.TXVideoInfo mVideoInfo;

    public static TCVideoConfig getInstance(Context context) {
        if (sInstance == null)
            sInstance = new TCVideoConfig(context);
        return sInstance;
    }

    private TCVideoConfig(Context context) {
        mContext = context.getApplicationContext();
    }

    public void setVideoInfo(TXVideoEditConstants.TXVideoInfo videoInfo) {
        mVideoInfo = videoInfo;
    }

    public TXVideoEditConstants.TXVideoInfo getVideoInfo() {
        return mVideoInfo;
    }
}
