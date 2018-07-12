package com.niurenjob.bullman.ShortVideo.play.net;

/**
 * Created by annidy on 2017/12/8.
 */

public interface TXCVodPlayerNetListener {
    /**
     * 播放信息查询成功
     * @param netApi
     */
    void onNetSuccess(TXCVodPlayerNetApi netApi, TXPlayInfoResponse playInfo);

    /**
     * 播放信息查询失败
     * @param netApi
     * @param reason 失败原因
     * @param code   错误码
     */
    void onNetFailed(TXCVodPlayerNetApi netApi, String reason, int code);
}
