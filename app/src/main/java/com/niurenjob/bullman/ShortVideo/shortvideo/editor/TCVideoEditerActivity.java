package com.niurenjob.bullman.ShortVideo.editor;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.common.activity.videopreview.TCVideoPreviewActivity;
import com.niurenjob.bullman.ShortVideo.common.utils.FileUtils;
import com.niurenjob.bullman.ShortVideo.common.utils.TCConstants;
import com.niurenjob.bullman.ShortVideo.common.widget.VideoWorkProgressFragment;
import com.niurenjob.bullman.ShortVideo.editor.bgm.TCBGMSettingFragment;
import com.niurenjob.bullman.ShortVideo.editor.bubble.TCBubbleViewInfoManager;
import com.niurenjob.bullman.ShortVideo.editor.bubble.TCWordEditActivity;
import com.niurenjob.bullman.ShortVideo.editor.common.TCToolsView;
import com.niurenjob.bullman.ShortVideo.editor.common.widget.videotimeline.VideoProgressController;
import com.niurenjob.bullman.ShortVideo.editor.common.widget.videotimeline.VideoProgressView;
import com.niurenjob.bullman.ShortVideo.editor.cutter.TCCutterFragment;
import com.niurenjob.bullman.ShortVideo.editor.filter.TCStaticFilterFragment;
import com.niurenjob.bullman.ShortVideo.editor.motion.TCMotionFragment;
import com.niurenjob.bullman.ShortVideo.editor.paster.TCPasterActivity;
import com.niurenjob.bullman.ShortVideo.editor.paster.TCPasterViewInfoManager;
import com.niurenjob.bullman.ShortVideo.editor.time.TCTimeFragment;
import com.niurenjob.bullman.ShortVideo.editor.transition.TCTransitionFragment;
import com.niurenjob.bullman.ShortVideo.editor.utils.PlayState;
import com.niurenjob.bullman.ShortVideo.editor.utils.TCEditerUtil;
import com.niurenjob.bullman.ShortVideo.videorecord.TCVideoSettingActivity;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by hans on 2017/11/6.
 */

public class TCVideoEditerActivity extends FragmentActivity implements
        TCToolsView.OnItemClickListener,
        View.OnClickListener,
        TCVideoEditerWrapper.TXVideoPreviewListenerWrapper,
        TXVideoEditer.TXVideoGenerateListener {
    private static final String TAG = "TCVideoEditerActivity";

    private TCVideoEditerWrapper wrapper;
    // 短视频SDK获取到的视频信息
    private TXVideoEditer mTXVideoEditer;                   // SDK接口类
    /**
     * 布局相关
     */
    private LinearLayout mLlBack;                           // 左上角返回
    private FrameLayout mVideoPlayerLayout;                 // 视频承载布局
    private ImageButton mIbPlay;                            // 播放按钮
    private TextView mTvDone;
    private TCToolsView mToolsView;                         // 底部工具栏

    private VideoWorkProgressFragment mWorkLoadingProgress; // 生成视频的等待框


    private Fragment mCurrentFragment,                      // 标记当前的Fragment
            mCutterFragment,                                // 裁剪的Fragment
            mTimeFragment,                                  // 时间特效的Fragment
            mStaticFilterFragment,                          // 静态滤镜的Fragment
            mMotionFragment,                                // 动态滤镜的Fragment
            mBGMSettingFragment,                            // BGM设置的Fragment
            mTransitionFragment;                            // 转场的Fragment

    private int mCurrentState = PlayState.STATE_NONE;       // 播放器当前状态

    private String mVideoOutputPath;                        // 视频输出路径
    private int mVideoResolution = -1;                      // 分辨率类型（如果是从录制过来的话才会有，这参数）

    private long mVideoDuration;                            // 视频的总时长
    private long mPreviewAtTime;                            // 当前单帧预览的时间

    private TXPhoneStateListener mPhoneListener;            // 电话监听

    private KeyguardManager mKeyguardManager;
    private int mVideoFrom;
    public boolean isPreviewFinish;

    /**
     * 缩略图进度条相关
     */
    private VideoProgressView mVideoProgressView;
    private VideoProgressController mVideoProgressController;
    private VideoProgressController.VideoProgressSeekListener mVideoProgressSeekListener = new VideoProgressController.VideoProgressSeekListener() {
        @Override
        public void onVideoProgressSeek(long currentTimeMs) {
            TXCLog.i(TAG, "onVideoProgressSeek, currentTimeMs = " + currentTimeMs);

            previewAtTime(currentTimeMs);
        }

        @Override
        public void onVideoProgressSeekFinish(long currentTimeMs) {
            TXCLog.i(TAG, "onVideoProgressSeekFinish, currentTimeMs = " + currentTimeMs);

            previewAtTime(currentTimeMs);
        }
    };
    private String mRecordProcessedPath;
    private int mCustomBitrate;
    private int beautyLevel = 0;
    private int whiteLevel = 0;
    private long t1;
    private long t2;
    private boolean mIsPicCombine; // 图片合成
    private List<String> picPathList;
    private ArrayList<Bitmap> bitmapList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        t1 = System.currentTimeMillis();
        setContentView(R.layout.activity_video_editer2);
        wrapper = TCVideoEditerWrapper.getInstance();
        wrapper.addTXVideoPreviewListenerWrapper(this);

        mIsPicCombine = getIntent().getBooleanExtra(TCConstants.INTENT_KEY_MULTI_PIC_CHOOSE, false);
        if(mIsPicCombine){
            picPathList = getIntent().getStringArrayListExtra(TCConstants.INTENT_KEY_MULTI_PIC_LIST);
            decodeFileToBitmap(picPathList);
            mTXVideoEditer = new TXVideoEditer(this);
            wrapper.setEditer(mTXVideoEditer);
            int result = mTXVideoEditer.setPictureList(bitmapList, 20);
            if(result == TXVideoEditConstants.PICTURE_TRANSITION_FAILED){
                Toast.makeText(this, "图片设置异常，结束编辑", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            // 注意：
            // 1、接口调用顺序：setPictureList在前，setPicTransferType在后，必须顺序调用
            // 1、图片转视频的时长需要设置转场类型后获取，因为不同的转场类型时长会不一样
            // 2、宽高信息按照第一张图片的宽高读取，在加片尾水印的时候算归一化坐标用到。
            mVideoDuration = mTXVideoEditer.setPictureTransition(TXVideoEditConstants.TX_TRANSITION_TYPE_LEFT_RIGHT_SLIPPING);
            TXVideoEditConstants.TXVideoInfo txVideoInfo = new TXVideoEditConstants.TXVideoInfo();
            txVideoInfo.duration = mVideoDuration;
            txVideoInfo.width = bitmapList.get(0).getWidth();
            txVideoInfo.height = bitmapList.get(0).getHeight();
            wrapper.setTXVideoInfo(txVideoInfo);
        }else{
            mTXVideoEditer = wrapper.getEditer();
            if (mTXVideoEditer == null || wrapper.getTXVideoInfo() == null) {
                Toast.makeText(this, "状态异常，结束编辑", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            mVideoDuration = wrapper.getTXVideoInfo().duration;
        }
        wrapper.setCutterStartTime(0, mVideoDuration);
        mVideoResolution = getIntent().getIntExtra(TCConstants.VIDEO_RECORD_RESOLUTION, -1);
        mCustomBitrate = getIntent().getIntExtra(TCVideoSettingActivity.RECORD_CONFIG_BITE_RATE, 0);

        mVideoFrom = getIntent().getIntExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_EDIT);
        // 录制经过预处理的视频路径，在编辑后需要删掉录制源文件
        mRecordProcessedPath = getIntent().getStringExtra(TCConstants.VIDEO_EDITER_PATH);

        initViews();
        initPhoneListener();
        initVideoProgressLayout();
        previewVideo();// 开始预览视频
        mKeyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    }

    private void decodeFileToBitmap(List<String> picPathList) {
        bitmapList = new ArrayList<>();
        for(int i = 0; i < picPathList.size(); i++){
            String filePath = picPathList.get(i);
            Bitmap bitmap = TCEditerUtil.decodeSampledBitmapFromFile(filePath, 720, 1280);
            bitmapList.add(bitmap);
            TCVideoEditerWrapper.getInstance().addThumbnailBitmap(0, bitmap);
        }
    }

    private void initPhoneListener() {
        //设置电话监听
        if (mPhoneListener == null) {
            mPhoneListener = new TXPhoneStateListener(this);
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private void initViews() {
        mToolsView = (TCToolsView) findViewById(R.id.editer_tools_view);
        mToolsView.setOnItemClickListener(this);
        mLlBack = (LinearLayout) findViewById(R.id.editer_back_ll);
        mLlBack.setOnClickListener(this);
        mTvDone = (TextView) findViewById(R.id.editer_tv_done);
        mTvDone.setOnClickListener(this);
        mVideoPlayerLayout = (FrameLayout) findViewById(R.id.editer_fl_video);

        mIbPlay = (ImageButton) findViewById(R.id.editer_ib_play);
        mIbPlay.setOnClickListener(this);
    }

    private long getCutterStartTime() {
        return wrapper.getCutterStartTime();
    }

    private long getCutterEndTime() {
        return wrapper.getCutterEndTime();
    }

    /**
     * ==========================================SDK播放器生命周期==========================================
     */

    private void previewVideo() {
        showCutterFragment();
        initPlayerLayout();         // 初始化预览视频布局
        startPlay(getCutterStartTime(), getCutterEndTime());  // 开始播放
    }


    private void initVideoProgressLayout() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int screenWidth = point.x;
        mVideoProgressView = (VideoProgressView) findViewById(R.id.editer_video_progress_view);
        mVideoProgressView.setViewWidth(screenWidth);
        if(!mIsPicCombine){
            List<Bitmap> thumbnailList = TCVideoEditerWrapper.getInstance().getAllThumbnails();
            mVideoProgressView.setThumbnailData(thumbnailList);
        }else{
            if(bitmapList != null){
                mVideoProgressView.setThumbnailData(bitmapList);
            }
        }
        mVideoProgressController = new VideoProgressController(mVideoDuration);
        mVideoProgressController.setVideoProgressView(mVideoProgressView);
        mVideoProgressController.setVideoProgressSeekListener(mVideoProgressSeekListener);
        mVideoProgressController.setVideoProgressDisplayWidth(screenWidth);

    }

    private void initPlayerLayout() {
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mVideoPlayerLayout;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);
    }

    /**
     * 调用mTXVideoEditer.previewAtTime后，需要记录当前时间，下次播放时从当前时间开始
     * x
     *
     * @param timeMs
     */
    public void previewAtTime(long timeMs) {
        pausePlay();
        isPreviewFinish = false;
        mTXVideoEditer.previewAtTime(timeMs);
        mPreviewAtTime = timeMs;
        mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
    }

    /**
     * 给子Fragment调用 （子Fragment不在意Activity中对于播放器的生命周期）
     */
    public void restartPlay() {
        stopPlay();
        startPlay(getCutterStartTime(), getCutterEndTime());
    }

    public void startPlay(long startTime, long endTime) {
        mTXVideoEditer.startPlayFromTime(startTime, endTime);
        mCurrentState = PlayState.STATE_PLAY;
        isPreviewFinish = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIbPlay.setImageResource(R.drawable.ic_pause);
            }
        });
    }


    public void resumePlay() {
        if (mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoEditer.resumePlay();
            mCurrentState = PlayState.STATE_RESUME;
            mIbPlay.setImageResource(R.drawable.ic_pause);

        }
    }

    public void pausePlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            mTXVideoEditer.pausePlay();
            mCurrentState = PlayState.STATE_PAUSE;
            mIbPlay.setImageResource(R.drawable.ic_play);
        }
    }

    public void stopPlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY ||
                mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoEditer.stopPlay();
            mCurrentState = PlayState.STATE_STOP;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIbPlay.setImageResource(R.drawable.ic_play);
                }
            });
        }
    }


    /**
     * ==========================================activity生命周期==========================================
     */

    @Override
    protected void onRestart() {
        super.onRestart();
        TXCLog.i(TAG, "onRestart");
        // 在oppo r9s上，锁屏后，按电源键进入解锁状态（屏保画面），也会走onRestart和onResume。因此做个保护
        if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
            initPlayerLayout();
//            startPlayAccordingState(getCutterStartTime(), getCutterEndTime());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TXCLog.i(TAG, "onResume");
        if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
            restartPlay();
        }
        t2 = System.currentTimeMillis();
        Log.d("CostTime", "编辑界面UI显示时间：t2:" + String.valueOf(t2 - t1));
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlay();
        // 若当前处于生成状态，离开当前activity，直接停止生成
        if (mCurrentState == PlayState.STATE_GENERATE) {
            stopGenerate();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoneListener != null) {
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
        if (mTXVideoEditer != null) {
            stopPlay();
            mTXVideoEditer.setVideoGenerateListener(null);
            //编辑完成后，销毁资源，避免影响处理下一个视频
            mTXVideoEditer.release();
            mTXVideoEditer = null;
        }
        // 清除对TXVideoEditer的引用以及相关配置
        TCVideoEditerWrapper.getInstance().removeTXVideoPreviewListenerWrapper(this);
        TCVideoEditerWrapper.getInstance().clear();

        // 清空保存的气泡字幕参数 （避免下一个视频混入上一个视频的气泡设定
        TCBubbleViewInfoManager.getInstance().clear();
        // 清空保存的贴纸参数
        TCPasterViewInfoManager.getInstance().clear();
        if (mWorkLoadingProgress != null) {
            mWorkLoadingProgress.setOnClickStopListener(null);
        }
    }


    /**
     * ==========================================SDK回调==========================================
     */
    @Override // 预览进度回调
    public void onPreviewProgressWrapper(final int timeMs) {
        // 视频的进度回调是异步的，如果不是处于播放状态，那么无需修改进度
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVideoProgressController.setCurrentTimeMs(timeMs);
                }
            });
        }
    }

    @Override // 预览完成回调
    public void onPreviewFinishedWrapper() {
        TXCLog.d(TAG, "---------------onPreviewFinished-----------------");
        isPreviewFinish = true;
        stopPlay();
        if ((mMotionFragment != null && mMotionFragment.isAdded() && !mMotionFragment.isHidden()) ||
                (mTimeFragment != null && mTimeFragment.isAdded() && !mTimeFragment.isHidden())) {
            // 处于动态滤镜或者时间特效界面,忽略 不做任何操作
        } else {
            // 如果当前不是动态滤镜界面或者时间特效界面，那么会自动开始重复播放
            startPlay(getCutterStartTime(), getCutterEndTime());
        }
    }


    /**
     * 创建缩略图，并跳转至视频预览的Activity
     */
    private void createThumbFile(final TXVideoEditConstants.TXGenerateResult result) {
        AsyncTask<Void, String, String> task = new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                File outputVideo = new File(mVideoOutputPath);
                if (!outputVideo.exists())
                    return null;
                Bitmap bitmap = TXVideoInfoReader.getInstance().getSampleImage(0, mVideoOutputPath);
                if (bitmap == null)
                    return null;
                String mediaFileName = outputVideo.getAbsolutePath();
                if (mediaFileName.lastIndexOf(".") != -1) {
                    mediaFileName = mediaFileName.substring(0, mediaFileName.lastIndexOf("."));
                }
                String folder = Environment.getExternalStorageDirectory() + File.separator + TCConstants.DEFAULT_MEDIA_PACK_FOLDER + File.separator + mediaFileName;
                File appDir = new File(folder);
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }

                String fileName = "thumbnail" + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return file.getAbsolutePath();
            }

            @Override
            protected void onPostExecute(String s) {
                if (mVideoFrom == TCConstants.VIDEO_RECORD_TYPE_UGC_RECORD) {
                    FileUtils.deleteFile(mRecordProcessedPath);
                }
                startPreviewActivity(result, s);
            }

        };
        task.execute();
    }

    private void startPreviewActivity(TXVideoEditConstants.TXGenerateResult result, String thumbPath) {
        Intent intent = new Intent(getApplicationContext(), TCVideoPreviewActivity.class);
        intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_EDIT);
        intent.putExtra(TCConstants.VIDEO_RECORD_RESULT, result.retCode);
        intent.putExtra(TCConstants.VIDEO_RECORD_DESCMSG, result.descMsg);
        intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
        if (thumbPath != null)
            intent.putExtra(TCConstants.VIDEO_RECORD_COVERPATH, thumbPath);
        intent.putExtra(TCConstants.VIDEO_RECORD_DURATION, getCutterEndTime() - getCutterStartTime());
        startActivity(intent);
        finish();
    }

    /**
     * ==========================================工具栏的点击回调==========================================
     */


    private void showCutterFragment() {
        if (mCutterFragment == null) {
            mCutterFragment = new TCCutterFragment();
        }
        showFragment(mCutterFragment, "cutter_fragment");
    }


    @Override
    public void onClickTime() {
        if(mIsPicCombine){
            showTransitionFragment();
        }else {
            showTimeFragment();
        }
    }

    @Override
    public void onClickCutter() {
        showCutterFragment();
    }

    private void showTimeFragment() {
        if (mTimeFragment == null) {
            mTimeFragment = new TCTimeFragment();
        }
        showFragment(mTimeFragment, "time_fragment");
    }

    private void showTransitionFragment() {
        if (mTransitionFragment == null) {
            mTransitionFragment = new TCTransitionFragment();
        }
        showFragment(mTransitionFragment, "transition_fragment");
    }

    @Override
    public void onClickStaticFilter() {
        if (mStaticFilterFragment == null) {
            mStaticFilterFragment = new TCStaticFilterFragment();
        }
        showFragment(mStaticFilterFragment, "static_filter_fragment");
    }

    @Override
    public void onClickMotionFilter() {
        if (mMotionFragment == null) {
            mMotionFragment = new TCMotionFragment();
        }
        showFragment(mMotionFragment, "motion_fragment");
    }

    @Override
    public void onClickBGM() {
        if (mBGMSettingFragment == null) {
            mBGMSettingFragment = new TCBGMSettingFragment();
        }
        showFragment(mBGMSettingFragment, "bgm_setting_fragment");
    }

    @Override
    public void onClickPaster() {
        stopPlay();
        Intent intent = new Intent(this, TCPasterActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickBubbleWord() {
        stopPlay();
        Intent intent = new Intent(this, TCWordEditActivity.class);
        startActivity(intent);
    }


    private void showFragment(Fragment fragment, String tag) {
        if (fragment == mCurrentFragment) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        if (!fragment.isAdded()) {
            transaction.add(R.id.editer_fl_container, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        mCurrentFragment = fragment;
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editer_back_ll:// 返回
                finish();
                break;
            case R.id.editer_tv_done:// 开始生成
                startGenerateVideo();
                break;
            case R.id.editer_ib_play:// 播放
                playVideo(false);
                break;
        }
    }

    /**
     * 如果是滤镜特效的界面调用：
     * 1、在播放状态下，按住滤镜不会停止播放
     * 2、播放到末尾了，按住时，不会重新播放
     * @param isMotionFilter
     */
    public void playVideo(boolean isMotionFilter) {
        TXCLog.i(TAG, "editer_ib_play clicked, mCurrentState = " + mCurrentState);
        if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP) {
            startPlay(getCutterStartTime(), getCutterEndTime());
        } else if ( (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) && !isMotionFilter) {
            pausePlay();
        } else if (mCurrentState == PlayState.STATE_PAUSE) {
            resumePlay();
        } else if (mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            if( (mPreviewAtTime >= getCutterEndTime() || mPreviewAtTime <= getCutterStartTime()) && !isMotionFilter){
                startPlay(getCutterStartTime(), getCutterEndTime());
            }else if( !TCVideoEditerWrapper.getInstance().isReverse() ){
                startPlay(mPreviewAtTime, getCutterEndTime());
            }else{
                startPlay(getCutterStartTime(), mPreviewAtTime);
            }
        }
    }

    /**
     * =========================================视频生成相关==========================================
     */
    private void startGenerateVideo() {
        stopPlay(); // 停止播放

        // 处于生成状态
        mCurrentState = PlayState.STATE_GENERATE;
        // 防止
        mTvDone.setEnabled(false);
        mTvDone.setClickable(false);
        // 生成视频输出路径
        mVideoOutputPath = TCEditerUtil.generateVideoPath();

        mIbPlay.setImageResource(R.drawable.ic_play);

        if (mWorkLoadingProgress == null) {
            initWorkLoadingProgress();
        }
        mWorkLoadingProgress.setProgress(0);
        mWorkLoadingProgress.setCancelable(false);
        mWorkLoadingProgress.show(getSupportFragmentManager(), "progress_dialog");

        // 添加片尾水印
        addTailWaterMark();

        mTXVideoEditer.setCutFromTime(getCutterStartTime(), getCutterEndTime());
        mTXVideoEditer.setVideoGenerateListener(this);

        if (mCustomBitrate != 0) { // 是否自定义码率
            mTXVideoEditer.setVideoBitrate(mCustomBitrate);
        }
        if (mVideoResolution == -1) {// 默认情况下都将输出720的视频
            mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
        } else if (mVideoResolution == TXRecordCommon.VIDEO_RESOLUTION_360_640) {
            mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_360P, mVideoOutputPath);
        } else if (mVideoResolution == TXRecordCommon.VIDEO_RESOLUTION_540_960) {
            mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
        } else if (mVideoResolution == TXRecordCommon.VIDEO_RESOLUTION_720_1280) {
            mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
        }
    }

    private void stopGenerate() {
        if (mCurrentState == PlayState.STATE_GENERATE) {
            mTvDone.setEnabled(true);
            mTvDone.setClickable(true);
            mWorkLoadingProgress.dismiss();
            Toast.makeText(TCVideoEditerActivity.this, "取消视频生成", Toast.LENGTH_SHORT).show();
            mWorkLoadingProgress.setProgress(0);
            mCurrentState = PlayState.STATE_NONE;
            if (mTXVideoEditer != null) {
                mTXVideoEditer.cancel();
            }
        }
    }

    /**
     * 添加片尾水印
     */
    private void addTailWaterMark() {

        TXVideoEditConstants.TXVideoInfo info = TCVideoEditerWrapper.getInstance().getTXVideoInfo();

        Bitmap tailWaterMarkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tcloud_logo);
        float widthHeightRatio = tailWaterMarkBitmap.getWidth() / (float) tailWaterMarkBitmap.getHeight();

        TXVideoEditConstants.TXRect txRect = new TXVideoEditConstants.TXRect();
        txRect.width = 0.25f; // 归一化的片尾水印，这里设置了一个固定值，水印占屏幕宽度的0.25。
        // 后面根据实际图片的宽高比，计算出对应缩放后的图片的宽度：txRect.width * videoInfo.width 和高度：txRect.width * videoInfo.width / widthHeightRatio，然后计算出水印放中间时的左上角位置
        txRect.x = (info.width - txRect.width * info.width) / (2f * info.width);
        txRect.y = (info.height - txRect.width * info.width / widthHeightRatio) / (2f * info.height);

        mTXVideoEditer.setTailWaterMark(tailWaterMarkBitmap, txRect, 3);
    }


    @Override // 生成进度回调
    public void onGenerateProgress(final float progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWorkLoadingProgress.setProgress((int) (progress * 100));
            }
        });
    }

    @Override // 生成完成
    public void onGenerateComplete(final TXVideoEditConstants.TXGenerateResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
                    // 生成成功
                    createThumbFile(result);
                } else {
                    Toast.makeText(TCVideoEditerActivity.this, result.descMsg, Toast.LENGTH_SHORT).show();
                }
                mTvDone.setEnabled(true);
                mTvDone.setClickable(true);
                mCurrentState = PlayState.STATE_NONE;
            }
        });
    }

    /**
     * ==========================================进度条==========================================
     */
    private void initWorkLoadingProgress() {
        if (mWorkLoadingProgress == null) {
            mWorkLoadingProgress = new VideoWorkProgressFragment();
            mWorkLoadingProgress.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopGenerate();
                }
            });
        }
        mWorkLoadingProgress.setProgress(0);
    }

    public VideoProgressController getVideoProgressViewController() {
        return mVideoProgressController;
    }


    /*********************************************监听电话状态**************************************************/
    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TCVideoEditerActivity> mEditer;

        public TXPhoneStateListener(TCVideoEditerActivity editer) {
            mEditer = new WeakReference<TCVideoEditerActivity>(editer);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TCVideoEditerActivity activity = mEditer.get();
            if (activity == null) return;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:  //电话等待接听
                case TelephonyManager.CALL_STATE_OFFHOOK:  //电话接听
                    // 生成状态 取消生成
                    if (activity.mCurrentState == PlayState.STATE_GENERATE) {
                        activity.stopGenerate();
                    }
                    // 直接停止播放
                    activity.stopPlay();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }
}
