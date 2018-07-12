package com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.common.utils.TCUtils;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.model.Video;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.model.VideoUrl;
import com.tencent.rtmp.TXBitrateItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ted on 2015/8/4.
 * MediaController
 */
public class MediaController extends FrameLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private ImageView mPlayImg;//播放按钮
    private SeekBar mProgressSeekBar;//播放进度条
    private TextView mTimeTxt1, mTimeTxt2;//播放时间
    private ImageView mExpandImg;//最大化播放按钮
    private TextView mResolutionTxt;//清晰度

    private MediaControlImpl mMediaControl;
    private String mScanVodUrl;
    private ArrayList<String> mTitleStrList;
    private ArrayList<String> mSupportedBitrates;
    private ArrayList<TXBitrateItem> mSource;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
        if (isFromUser)
            mMediaControl.onProgressTurn(ProgressState.DOING, progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mMediaControl.onProgressTurn(ProgressState.START, 0);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mMediaControl.onProgressTurn(ProgressState.STOP, 0);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.pause) {
            mMediaControl.onPlayTurn();
        } else if (view.getId() == R.id.expand) {
            mMediaControl.onPageTurn();
        } else if (view.getId() == R.id.resolutionTxt) {
            mMediaControl.onResolutionTurn();
        }
    }

    public void initVideoList(ArrayList<Video> videoList) {
        ArrayList<String> name = new ArrayList<>();
        for (Video video : videoList) {
            name.add(video.getVideoName());
        }
    }

    public void initPlayVideo(Video video) {
        ArrayList<String> format = new ArrayList<>();
        for (VideoUrl url : video.getVideoUrl()) {
            format.add(url.getFormatName());
        }
    }


    /***
     * 强制横屏模式
     */
    public void forceLandscapeMode() {
        mExpandImg.setVisibility(INVISIBLE);
        mResolutionTxt.setVisibility(INVISIBLE);
    }


    public void setProgressBar(int progress, int secondProgress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        if (secondProgress < 0) secondProgress = 0;
        if (secondProgress > 100) secondProgress = 100;
        mProgressSeekBar.setProgress(progress);
        mProgressSeekBar.setSecondaryProgress(secondProgress);
    }

    public void setPlayState(PlayState playState) {
        mPlayImg.setImageResource(playState.equals(PlayState.PLAY) ? R.drawable.ic_vod_pause_normal : R.drawable.ic_vod_play_normal);
    }

    public void setPageType(PageType pageType) {
        mExpandImg.setVisibility(pageType.equals(PageType.EXPAND) ? GONE : VISIBLE);
        mResolutionTxt.setVisibility(pageType.equals(PageType.SHRINK) ? GONE : VISIBLE);
    }

    public void setPlayProgressTxt(int nowSecond, int allSecond) {
        mTimeTxt1.setText(TCUtils.formattedTime(nowSecond / 1000));
        mTimeTxt2.setText(TCUtils.formattedTime(allSecond / 1000));
    }

    public void playFinish(int allTime) {
        mProgressSeekBar.setProgress(0);
        setPlayProgressTxt(0, allTime);
        setPlayState(PlayState.PAUSE);
    }

    public void setMediaControl(MediaControlImpl mediaControl) {
        mMediaControl = mediaControl;
    }

    public MediaController(Context context) {
        super(context);
        initView(context);
    }

    public MediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.biz_video_media_controller, this);
        mPlayImg = (ImageView) findViewById(R.id.pause);
        mProgressSeekBar = (SeekBar) findViewById(R.id.media_controller_progress);
        mTimeTxt1 = (TextView) findViewById(R.id.time_pos);
        mTimeTxt2 = (TextView) findViewById(R.id.time_duration);
        mExpandImg = (ImageView) findViewById(R.id.expand);
        mResolutionTxt = (TextView) findViewById(R.id.resolutionTxt);
        initData();
    }

    private void initData() {
        mTitleStrList = new ArrayList<String>();
        mTitleStrList.add("流畅");
        mTitleStrList.add("高清");
        mTitleStrList.add("超清");
        mTitleStrList.add("原画");
        mSupportedBitrates = new ArrayList<String>();
        mProgressSeekBar.setOnSeekBarChangeListener(this);
        mPlayImg.setOnClickListener(this);
        mResolutionTxt.setOnClickListener(this);
        mExpandImg.setOnClickListener(this);
        setPageType(PageType.SHRINK);
        setPlayState(PlayState.PAUSE);

        mResolutionTxt.setText(mTitleStrList.get(0));
    }

    @SuppressLint("SimpleDateFormat")
    private String formatPlayTime(long time) {
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(new Date(time));
    }

    public void setPlayUrl(String vodUrl) {
        mScanVodUrl = vodUrl;
    }

    public void updateUI() {
        setPlayState(PlayState.PLAY);
    }

    public void setDataSource(ArrayList<TXBitrateItem> supportedBitrates) {
        mSource = supportedBitrates;
        if (supportedBitrates == null || supportedBitrates.size() == 0) {
            mResolutionTxt.setText(mTitleStrList.get(1));
            return;
        }
        for (int i = 0; i < supportedBitrates.size(); i++) {
            if (i < mTitleStrList.size())
                mSupportedBitrates.add(mTitleStrList.get(i));
        }
    }

    public void updateResolutionTxt(int index) {
        if (mSource == null || mSource.size() == 0) {
            return;
        }
        int j = 0;
        for (int i = 0; i < mSource.size(); i++) {
            TXBitrateItem item = mSource.get(i);
            if (item.index == index) {
                j = i;
            }
        }
        if (j < mSupportedBitrates.size())
            mResolutionTxt.setText(mSupportedBitrates.get(j));
    }

    /**
     * 播放样式 展开、缩放
     */
    public enum PageType {
        EXPAND, SHRINK
    }

    /**
     * 播放状态 播放 暂停
     */
    public enum PlayState {
        PLAY, PAUSE
    }

    public enum ProgressState {
        START, DOING, STOP
    }


    public interface MediaControlImpl {
        void onPlayTurn();

        void onPageTurn();

        void onProgressTurn(ProgressState state, int progress);

        void onResolutionTurn();

        void alwaysShowController();
    }

}
