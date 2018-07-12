package com.niurenjob.bullman.ShortVideo.play.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tencent.liteav.basic.log.TXCLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by annidy on 2017/12/8.
 */

public class TXCVodPlayerNetApi {
    private static final String TAG = "TXCVodPlayerNetApi";

    //http://183.60.81.104/getplayinfo/v2/1255566655/4564972818519602447?&t=5c08d9fa&us=someus&sign=65b202bc855c0981da719f2d8df85859%22
    private final String        BASE_URL = "http://playvideo.qcloud.com/getplayinfo/v2";
    private final String        BASE_URLS= "https://playvideo.qcloud.com/getplayinfo/v2";
    private final int SUCCESS   = 0;
    private final int FAILED    = 1;


    protected TXCVodPlayerNetListener mListener;
    protected TXPlayInfoResponse mPlayInfo;
    private Thread mThread;
    private boolean mIsHttps;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (mListener == null)
                return;
            switch (msg.what){
                case SUCCESS:
                    mListener.onNetSuccess(TXCVodPlayerNetApi.this, (TXPlayInfoResponse) msg.obj);
                    break;
                case FAILED:
                    mListener.onNetFailed(TXCVodPlayerNetApi.this, (String)msg.obj, msg.arg1);
                    break;
                default:
                    break;
            }
        };
    };

    public int getPlayInfo(final int appId, final String fileId, final String timeout, final String us, final int exper, final String sign) {
        if (appId == 0 || fileId == null)
            return -1;
        if ((timeout != null || exper > 0) && sign == null)
            return -1;

        mThread = new Thread("getPlayInfo") {
            public void run() {
                try {
                    Looper.prepare();
                    String urlStr;
                    if (mIsHttps) {
                        urlStr = String.format("%s/%d/%s", BASE_URL, appId, fileId);
                    } else {
                        urlStr = String.format("%s/%d/%s", BASE_URLS, appId, fileId);
                    }
                    String query = makeQueryString(timeout, us, exper, sign);
                    if (query != null) {
                        urlStr = urlStr + "?" + query;
                    }
                    URL url = new URL(urlStr);

                    TXCLog.d(TAG, "getplayinfo: "+urlStr);

                    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.connect();

                    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream stream = httpConnection.getInputStream();
                        BufferedReader buffer = new BufferedReader(
                                new InputStreamReader(stream));
                        String s;
                        StringBuilder output = new StringBuilder();
                        while ((s = buffer.readLine()) != null)
                            output.append(s);

                        parseJson(output.toString());
                    } else {
                        httpFailed("请求失败", -1);
//                        parseJson(test);
                    }
                } catch (JSONException ex) {
                    httpFailed("格式错误", -2);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    httpFailed("请求失败", -2);
                }
            }
        };
        mThread.start();
        return 0;
    }

    private String makeQueryString(String timeout, String us, int exper, String sign) {
        StringBuilder str = new StringBuilder();
        if (timeout != null) {
            str.append("t=" + timeout + "&");
        }
        if (us != null) {
            str.append("us=" + us + "&");
        }
        if (sign != null) {
            str.append("sign="+sign+"&");
        }
        if (exper >=0) {
            str.append("exper="+exper+"&");
        }
        if (str.length() > 1) {
            str.deleteCharAt(str.length()-1);
        }
        return str.toString();
    }

    protected void httpFailed(String reason, int code) {
        Message msg = new Message();
        msg.what = FAILED;
        msg.arg1 = code;
        msg.obj = reason;
        mHandler.sendMessage(msg);
    }

    private void parseJson(final String jsonStr) throws JSONException
    {
        JSONObject jsonObj = new JSONObject(jsonStr);
        int code = jsonObj.getInt("code");
        if (code != 0) {
            String message = jsonObj.getString("message");
            TXCLog.e(TAG, message);
            httpFailed(message, code);
            return;
        }

        mPlayInfo = new TXPlayInfoResponse(jsonObj);
        if (mPlayInfo.playUrl() == null) {
            httpFailed("无播放地址", -3);
        } else {
            Message msg = new Message();
            msg.what = SUCCESS;
            msg.obj = mPlayInfo;
            mHandler.sendMessage(msg);
        }
    }

    public void setListener(TXCVodPlayerNetListener listener) {
        this.mListener = listener;
    }

    public TXPlayInfoResponse getPlayInfo() {
        return this.mPlayInfo;
    }

    public void setHttps(boolean https) {
        mIsHttps = https;
    }

    private String test = "{\"code\":0,\"message\":\"\",\"playerInfo\":{\"playerId\":\"0\",\"name\":\"初始播放器\",\"defaultVideoClassification\":\"SD\",\"videoClassification\":[{\"id\":\"FLU\",\"name\":\"流畅\",\"definitionList\":[10,510,210,610,10046,710]},{\"id\":\"SD\",\"name\":\"标清\",\"definitionList\":[20,520,220,620,10047,720]},{\"id\":\"HD\",\"name\":\"高清\",\"definitionList\":[30,530,230,630,10048,730]},{\"id\":\"FHD\",\"name\":\"全高清\",\"definitionList\":[40,540,240,640,10049,740]},{\"id\":\"2K\",\"name\":\"2K\",\"definitionList\":[70,570,270,670,370,770]},{\"id\":\"4K\",\"name\":\"4K\",\"definitionList\":[80,580,280,680,380,780]}],\"logoLocation\":\"1\",\"logoPic\":\"\",\"logoUrl\":\"\"},\"coverInfo\":{\"coverUrl\":\"http://1255566655.vod2.myqcloud.com/7e9cee55vodtransgzp1255566655/8f5fbff14564972818519602447/coverBySnapshot/1513156403_1311093072.100_0.jpg?t=5c08d9fa&us=someus&sign=95f34beb353fe32cfe7f8b5e79cc28b1\"},\"imageSpriteInfo\":{\"imageSpriteList\":[{\"definition\":10,\"height\":80,\"width\":142,\"totalCount\":4,\"imageUrls\":[\"http://1255566655.vod2.myqcloud.com/ca754badvodgzp1255566655/8f5fbff14564972818519602447/imageSprite/1513156058_533711271_00001.jpg?t=5c08d9fa&us=someus&sign=79449db4e1fb05a3becfa096613659c3\"],\"webVttUrl\":\"http://1255566655.vod2.myqcloud.com/ca754badvodgzp1255566655/8f5fbff14564972818519602447/imageSprite/1513156058_533711271.vtt?t=5c08d9fa&us=someus&sign=79449db4e1fb05a3becfa096613659c3\"}]},\"videoInfo\":{\"sourceVideo\":{\"url\":\"http://1255566655.vod2.myqcloud.com/ca754badvodgzp1255566655/8f5fbff14564972818519602447/uAnXX0OMLSAA.wmv?t=5c08d9fa&us=someus&sign=659af5dd3f27eb92dc4ed74eb561daa4\",\"definition\":0,\"duration\":30,\"floatDuration\":30.093000411987305,\"size\":26246026,\"bitrate\":6134170,\"height\":720,\"width\":1280,\"container\":\"asf\",\"md5\":\"\",\"videoStreamList\":[{\"bitrate\":5942130,\"height\":720,\"width\":1280,\"codec\":\"vc1\",\"fps\":29}],\"audioStreamList\":[{\"samplingRate\":44100,\"bitrate\":192040,\"codec\":\"wmav2\"}]},\"mas©terPlayList1\":{\"idrAligned\":false,\"url\":\"http://1255566655.vod2.myqcloud.com/7e9cee55vodtransgzp1255566655/8f5fbff14564972818519602447/master_playlist.m3u8?t=5c08d9fa&us=someus&sign=66290475b7182c89193f03b8f74a979d\",\"definition\":10000,\"md5\":\"23ecc2cfe4cb7c8f87af41993ba8c09c\"},\"transcodeList\":[{\"url\":\"http://1255566655.vod2.myqcloud.com/7e9cee55vodtransgzp1255566655/8f5fbff14564972818519602447/v.f220.m3u8?t=5c08d9fa&us=someus&sign=66290475b7182c89193f03b8f74a979d\",\"definition\":220,\"duration\":30,\"floatDuration\":30.08329963684082,\"size\":796,\"bitrate\":646036,\"height\":360,\"width\":640,\"container\":\"hls,applehttp\",\"md5\":\"dce044407826b4d809c16b6d1a9e9f13\",\"videoStreamList\":[{\"bitrate\":607449,\"height\":360,\"width\":640,\"codec\":\"h264\",\"fps\":24}],\"audioStreamList\":[{\"samplingRate\":44100,\"bitrate\":38587,\"codec\":\"aac\"}]},{\"url\":\"http://1255566655.vod2.myqcloud.com/7e9cee55vodtransgzp1255566655/8f5fbff14564972818519602447/v.f230.m3u8?t=5c08d9fa&us=someus&sign=66290475b7182c89193f03b8f74a979d\",\"definition\":230,\"duration\":30,\"floatDuration\":30.04170036315918,\"size\":802,\"bitrate\":1224776,\"height\":720,\"width\":1280,\"container\":\"hls,applehttp\",\"md5\":\"f07bb0be9e2fee967d87b6c310d3c036\",\"videoStreamList\":[{\"bitrate\":1186189,\"height\":720,\"width\":1280,\"codec\":\"h264\",\"fps\":24}],\"audioStreamList\":[{\"samplingRate\":44100,\"bitrate\":38587,\"codec\":\"aac\"}]},{\"url\":\"http://1255566655.vod2.myqcloud.com/7e9cee55vodtransgzp1255566655/8f5fbff14564972818519602447/v.f240.m3u8?t=5c08d9fa&us=someus&sign=66290475b7182c89193f03b8f74a979d\",\"definition\":240,\"duration\":30,\"floatDuration\":0,\"size\":809,\"bitrate\":2866685,\"height\":1080,\"width\":1920,\"container\":\"hls,applehttp\",\"md5\":\"ff8190cf07afceb8ed053b198453e954\",\"videoStreamList\":[{\"bitrate\":2828098,\"height\":1080,\"width\":1920,\"codec\":\"h264\",\"fps\":24}],\"audioStreamList\":[{\"samplingRate\":44100,\"bitrate\":38587,\"codec\":\"aac\"}]},{\"url\":\"http://1255566655.vod2.myqcloud.com/7e9cee55vodtransgzp1255566655/8f5fbff14564972818519602447/v.f210.m3u8?t=5c08d9fa&us=someus&sign=66290475b7182c89193f03b8f74a979d\",\"definition\":210,\"duration\":30,\"floatDuration\":30.08329963684082,\"size\":788,\"bitrate\":358908,\"height\":180,\"width\":320,\"container\":\"hls,applehttp\",\"md5\":\"5fa784e0a588c51dc2d7428ad6787079\",\"videoStreamList\":[{\"bitrate\":320321,\"height\":180,\"width\":320,\"codec\":\"h264\",\"fps\":24}],\"audioStreamList\":[{\"samplingRate\":44100,\"bitrate\":38587,\"codec\":\"aac\"}]},{\"url\":\"http://1255566655.vod2.myqcloud.com/7e9cee55vodtransgzp1255566655/8f5fbff14564972818519602447/v.f10.mp4?t=5c08d9fa&us=someus&sign=66290475b7182c89193f03b8f74a979d\",\"definition\":10,\"duration\":30,\"floatDuration\":30.139400482177734,\"size\":1169168,\"bitrate\":303916,\"height\":180,\"width\":320,\"container\":\"mov,mp4,m4a,3gp,3g2,mj2\",\"md5\":\"85002ed20125acf150d014b192cf39a0\",\"videoStreamList\":[{\"bitrate\":255698,\"height\":180,\"width\":320,\"codec\":\"h264\",\"fps\":24}],\"audioStreamList\":[{\"samplingRate\":44100,\"bitrate\":48218,\"codec\":\"aac\"}]},{\"url\":\"http://1255566655.vod2.myqcloud.com/7e9cee55vodtransgzp1255566655/8f5fbff14564972818519602447/v.f20.mp4?t=5c08d9fa&us=someus&sign=66290475b7182c89193f03b8f74a979d\",\"definition\":20,\"duration\":30,\"floatDuration\":30.139400482177734,\"size\":2158411,\"bitrate\":566647,\"height\":360,\"width\":640,\"container\":\"mov,mp4,m4a,3gp,3g2,mj2\",\"md5\":\"cba3630e5f92325041da7fee336246b6\",\"videoStreamList\":[{\"bitrate\":518429,\"height\":360,\"width\":640,\"codec\":\"h264\",\"fps\":24}],\"audioStreamList\":[{\"samplingRate\":44100,\"bitrate\":48218,\"codec\":\"aac\"}]}]}}";
}
