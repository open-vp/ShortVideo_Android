package com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.model.Video;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.model.VideoUrl;

import java.util.ArrayList;

/**
 * Created by Ted on 2015/8/4.
 * MediaController
 */
public class MediaToolbar extends FrameLayout implements View.OnClickListener {

    private ImageView mDanmukuBtn;
    private boolean mDanmukuOn;
    private MediaToolbarImpl mMediaToolbarImpl;
    private ImageView mMoreBtn;
    private ImageView mBackBtn;
    private ImageView mSnapshotBtn;
    private TextView mTitle;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.danmuku) {
            mDanmukuOn = !mDanmukuOn;
            if (mDanmukuOn) {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_danmuku_on);
                mDanmukuBtn.setImageBitmap(bmp);
            } else {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_danmuku_off);
                mDanmukuBtn.setImageBitmap(bmp);
            }
            mMediaToolbarImpl.onDanmaku(mDanmukuOn);
        } else if (view.getId() == R.id.snapshoot) {
            mMediaToolbarImpl.onSnapshoot();
        } else if (view.getId() == R.id.menu_more) {
            mMediaToolbarImpl.onMoreSetting();
        } else if (view.getId() == R.id.back_pl) {
            mMediaToolbarImpl.onBack();
        }
    }


    public void setMediaControl(MediaToolbarImpl mediaControl) {
        mMediaToolbarImpl = mediaControl;
    }

    public MediaToolbar(Context context) {
        super(context);
        initView(context);
    }

    public MediaToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public MediaToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.biz_video_media_toolbar, this);

        mDanmukuBtn = (ImageView) findViewById(R.id.danmuku);
        mMoreBtn = (ImageView) findViewById(R.id.menu_more);
        mBackBtn = (ImageView) findViewById(R.id.back_pl);
        mSnapshotBtn = (ImageView) findViewById(R.id.snapshoot);
        mTitle = (TextView) findViewById(R.id.title);

        mDanmukuBtn.setOnClickListener(this);
        mMoreBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        mSnapshotBtn.setOnClickListener(this);
        initData();
    }

    private void initData() {

    }

    public void udpateTitle(String title) {
        if (mTitle != null) {
            mTitle.setText(title);
        }
    }


    public interface MediaToolbarImpl {
        void onDanmaku(boolean on);

        void onSnapshoot();

        void onMoreSetting();

        void onBack();
    }

}
