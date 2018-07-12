package com.niurenjob.bullman.ShortVideo.common.utils;

/**
 * 静态函数
 */
public class TCConstants {

    /**
     * UGC小视频录制信息
     */
    public static final String VIDEO_RECORD_TYPE = "type";
    public static final String VIDEO_RECORD_RESULT = "result";
    public static final String VIDEO_RECORD_DESCMSG = "descmsg";
    public static final String VIDEO_RECORD_VIDEPATH = "path";
    public static final String VIDEO_RECORD_COVERPATH = "coverpath";
    public static final String VIDEO_RECORD_DURATION = "duration";
    public static final String VIDEO_RECORD_RESOLUTION = "resolution";

    /**
     * UGC 编辑的的参数
     */
    public static final String VIDEO_EDITER_PATH = "key_video_editer_path"; // 路径的key

    public static final int VIDEO_RECORD_TYPE_PUBLISH = 1;   // 推流端录制
    public static final int VIDEO_RECORD_TYPE_PLAY = 2;   // 播放端录制
    public static final int VIDEO_RECORD_TYPE_UGC_RECORD = 3;   // 短视频录制
    public static final int VIDEO_RECORD_TYPE_EDIT = 4;   // 短视频编辑


    /**
     * 用户可见的错误提示语
     */
    public static final String ERROR_MSG_NET_DISCONNECTED = "网络异常，请检查网络";

    // UGCEditer
    public static final String ACTION_UGC_SINGLE_CHOOSE = "com.tencent.qcloud.xiaozhibo.single";
    public static final String ACTION_UGC_MULTI_CHOOSE = "com.tencent.qcloud.xiaozhibo.multi";

    public static final String INTENT_KEY_SINGLE_CHOOSE = "single_video";
    public static final String INTENT_KEY_MULTI_CHOOSE = "multi_video";
    public static final String INTENT_KEY_MULTI_PIC_CHOOSE = "multi_pic";
    public static final String INTENT_KEY_MULTI_PIC_LIST = "pic_list"; // 图片列表

    public static final String INTENT_KEY_TX_VIDEO_INFO = "key_tx_video_info";

    public static final String DEFAULT_MEDIA_PACK_FOLDER = "txrtmp";      // UGC编辑器输出目录

    // 上传常量
    public static final String PLAYER_DEFAULT_VIDEO = "play_default_video";
    public static final String PLAYER_VIDEO_ID = "video_id";
    public static final String PLAYER_VIDEO_NAME = "video_name";

    // 短视频licence名称
    public static final String UGC_LICENCE_NAME = "TXUgcSDK.licence";

    // 点播的信息
    public static final String VOD_APPID = "1256468886";
    public static final String VOD_APPKEY = "1973fcc2b70445af8b51053d4f9022bb";
}
