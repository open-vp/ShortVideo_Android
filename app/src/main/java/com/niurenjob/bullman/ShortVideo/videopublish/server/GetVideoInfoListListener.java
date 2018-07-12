package com.niurenjob.bullman.ShortVideo.videopublish.server;

import java.util.List;

/**
 * Created by vinsonswang on 2018/3/29.
 */

public interface GetVideoInfoListListener {
    void onGetVideoInfoList(List<VideoInfo> videoInfoList);

    void onFail(int errCode);
}
