package com.niurenjob.bullman.ShortVideo.play.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by annidy on 2017/12/13.
 */

public class TXPlayInfoResponse {
    protected JSONObject response;
    public TXPlayInfoResponse(JSONObject response) {
        this.response = response;
    }

    /**
     * 获取服务器下发的播放地址
     * @return 播放地址
     */
    public String playUrl() {
        if (getMasterPlayList() != null) {
            return getMasterPlayList().url;
        }
        if (getStreamList().size() != 0) {
            return getStreamList().get(0).url;
        }
        if (getSource() != null) {
            return getSource().url;
        }
        return null;
    }


    /**
     * 获取封面图片
     * @return 图片url
     */
    public String coverUrl() {
        try {
            JSONObject coverInfo = response.getJSONObject("coverInfo");
            if (coverInfo != null) {
                return coverInfo.getString("coverUrl");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<TXPlayInfoStream> getStreamList() {
        ArrayList<TXPlayInfoStream> streamList = new ArrayList<>();
        try {
            JSONArray transcodeList = response.getJSONObject("videoInfo").getJSONArray("transcodeList");
            if (transcodeList != null) {
                for(int i = 0; i < transcodeList.length(); i++) {
                    JSONObject transcode = transcodeList.getJSONObject(i);

                    TXPlayInfoStream stream = new TXPlayInfoStream();
                    stream.url = transcode.getString("url");
                    stream.duration = transcode.getInt("duration");
                    stream.width = transcode.getInt("width");
                    stream.height = transcode.getInt("height");
                    stream.size = transcode.getInt("size");
                    stream.bitrate = transcode.getInt("bitrate");

                    streamList.add(stream);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return streamList;
    }

    public TXPlayInfoStream getSource() {
        try {
            JSONObject sourceVideo = response.getJSONObject("videoInfo").getJSONObject("sourceVideo");

            TXPlayInfoStream stream = new TXPlayInfoStream();
            stream.url = sourceVideo.getString("url");
            stream.duration = sourceVideo.getInt("duration");
            stream.width = sourceVideo.getInt("width");
            stream.height = sourceVideo.getInt("height");
            stream.size = sourceVideo.getInt("size");
            stream.bitrate = sourceVideo.getInt("bitrate");

            return stream;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TXPlayInfoStream getMasterPlayList() {
        try {
            JSONObject masterPlayList = response.getJSONObject("videoInfo").getJSONObject("masterPlayList");

            TXPlayInfoStream stream = new TXPlayInfoStream();
            stream.url = masterPlayList.getString("url");

            return stream;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取视频名称
     * @return
     */
    public String name() {
        try {
            JSONObject basicInfo = response.getJSONObject("videoInfo").getJSONObject("basicInfo");
            if (basicInfo != null) {
                return basicInfo.getString("name");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取视频描述
     * @return
     */
    public String description() {
        try {
            JSONObject basicInfo = response.getJSONObject("videoInfo").getJSONObject("basicInfo");
            if (basicInfo != null) {
                return basicInfo.getString("description");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
