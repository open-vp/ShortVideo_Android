package com.niurenjob.bullman.ShortVideo.joiner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.common.activity.videopreview.TCVideoPreviewActivity;
import com.niurenjob.bullman.ShortVideo.common.utils.TCConstants;
import com.niurenjob.bullman.ShortVideo.common.widget.VideoWorkProgressFragment;
import com.niurenjob.bullman.ShortVideo.choose.TCVideoFileInfo;
import com.niurenjob.bullman.ShortVideo.editor.utils.TCEditerUtil;
import com.niurenjob.bullman.ShortVideo.view.DialogUtil;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoInfoReader;
import com.tencent.ugc.TXVideoJoiner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TCVideoJoinerPreviewActivity extends FragmentActivity implements View.OnClickListener, TXVideoJoiner.TXVideoPreviewListener, TXVideoJoiner.TXVideoJoinerListener {
    private static final String TAG = TCVideoJoinerPreviewActivity.class.getSimpleName();

    static final int STATE_NONE = 0;
    static final int STATE_PLAY = 1;
    static final int STATE_PAUSE = 2;
    static final int STATE_TRANSCODE = 3;
    private int mCurrentState = STATE_NONE;

    private ArrayList<TCVideoFileInfo> mTCVideoFileInfoList;

    private TextView mBtnDone;
    private ImageButton mBtnPlay;
    private FrameLayout mVideoView;
    private ProgressBar mLoadProgress;

    private String mVideoOutputPath;
    private ArrayList<String> mVideoSourceList;

    private TXVideoJoiner mTXVideoJoiner;
    private TXVideoInfoReader mVideoInfoReader;
    private TXVideoEditConstants.TXJoinerResult mResult;
    private TXVideoEditConstants.TXVideoInfo mTXVideoInfo;

    private BackGroundHandler mHandler;
    private final int MSG_LOAD_VIDEO_INFO = 1000;
    private final int MSG_RET_VIDEO_INFO = 1001;
    private HandlerThread mHandlerThread;
    private VideoWorkProgressFragment mWorkProgressDialog;
    private boolean mIsStopManually;//标记是否手动停止
    private Context mContext;
    private DialogUtil mDialogUtil;
    private TCVideoFileInfo mTCVideoFileInfo;
    private TXVideoEditConstants.TXVideoInfo videoInfo;
    private TXVideoEditConstants.TXVideoInfo videoInfo2;

    class BackGroundHandler extends Handler {

        public BackGroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_VIDEO_INFO:
                    videoInfo = mVideoInfoReader.getVideoFileInfo(mTCVideoFileInfoList.get(0).getFilePath());
                    if (mTCVideoFileInfoList.size() > 1) {
                        videoInfo2 = mVideoInfoReader.getVideoFileInfo(mTCVideoFileInfoList.get(1).getFilePath());
                    }
                    if (videoInfo == null) {
                        mLoadProgress.setVisibility(View.GONE);
                        mBtnDone.setClickable(true);

                        mDialogUtil.showDialog(mContext, "视频合成失败", "暂不支持Android 4.3以下的系统" , null);
                        return;
                    }
                    if (mMainHandler != null) {
                        Message mainMsg = new Message();
                        mainMsg.what = MSG_RET_VIDEO_INFO;
                        mainMsg.obj = videoInfo;
                        mMainHandler.sendMessage(mainMsg);
                    }
                    break;
            }

        }
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RET_VIDEO_INFO:
                    mTXVideoInfo = (TXVideoEditConstants.TXVideoInfo) msg.obj;

                    TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
                    param.videoView = mVideoView;
                    param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
                    mTXVideoJoiner.initWithPreview(param);

                    int ret = mTXVideoJoiner.setVideoPathList(mVideoSourceList);
                    if (ret == 0) {
                        mTXVideoJoiner.startPlay();

                        mCurrentState = STATE_PLAY;
                        mLoadProgress.setVisibility(View.GONE);
                        mBtnDone.setClickable(true);
                        mBtnPlay.setClickable(true);
                        mBtnPlay.setImageResource(mCurrentState == STATE_PLAY ? R.drawable.ic_pause : R.drawable.ic_play);
                    } else if (ret == TXVideoEditConstants.ERR_UNSUPPORT_VIDEO_FORMAT) {
                        DialogUtil.showDialog(mContext, "视频合成失败", "本机型暂不支持此视频格式", null);
                    }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_joiner_preview);

        initViews();
        initData();
    }

    private void initViews() {
        mBtnDone = (TextView) findViewById(R.id.btn_done);
        mBtnDone.setClickable(false);
        mBtnPlay = (ImageButton) findViewById(R.id.btn_play);
        mVideoView = (FrameLayout) findViewById(R.id.video_view);

        LinearLayout backLL = (LinearLayout) findViewById(R.id.back_ll);
        backLL.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
        mBtnDone.setClickable(false);
        mBtnPlay.setClickable(false);

        mLoadProgress = (ProgressBar) findViewById(R.id.progress_load);

        initWorkProgressPopWin();
    }

    private void initData() {
        mContext = TCVideoJoinerPreviewActivity.this;
        mDialogUtil = new DialogUtil();

        mTCVideoFileInfoList = (ArrayList<TCVideoFileInfo>) getIntent().getSerializableExtra(TCConstants.INTENT_KEY_MULTI_CHOOSE);
        if (mTCVideoFileInfoList == null || mTCVideoFileInfoList.size() == 0) {
            finish();
            return;
        }
        mHandlerThread = new HandlerThread("LoadData");
        mHandlerThread.start();
        mHandler = new BackGroundHandler(mHandlerThread.getLooper());

        mTXVideoJoiner = new TXVideoJoiner(this);
        mTXVideoJoiner.setTXVideoPreviewListener(this);

        mVideoInfoReader = TXVideoInfoReader.getInstance();

        mVideoSourceList = new ArrayList<>();
        for (int i = 0; i < mTCVideoFileInfoList.size(); i++) {
            mVideoSourceList.add(mTCVideoFileInfoList.get(i).getFilePath());
        }
        mHandler.sendEmptyMessage(MSG_LOAD_VIDEO_INFO);
    }

    private void initWorkProgressPopWin() {
        if (mWorkProgressDialog == null) {
            mWorkProgressDialog = new VideoWorkProgressFragment();
            mWorkProgressDialog.setOnClickStopListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBtnDone.setClickable(true);
                    mBtnDone.setEnabled(true);
                    Toast.makeText(TCVideoJoinerPreviewActivity.this, "取消视频合成", Toast.LENGTH_SHORT).show();
                    mWorkProgressDialog.dismiss();
                    mWorkProgressDialog.setProgress(0);
                    if (mTXVideoJoiner != null)
                        mTXVideoJoiner.cancel();
                    mCurrentState = STATE_NONE;
                }
            });
        }
        mWorkProgressDialog.setProgress(0);
    }

    private void createThumbFile(TXVideoEditConstants.TXVideoInfo videoInfo) {
        final TCVideoFileInfo fileInfo = mTCVideoFileInfoList.get(0);
        if (fileInfo == null)
            return;
        mBtnDone.setClickable(false);
        mBtnPlay.setClickable(false);
        AsyncTask<TXVideoEditConstants.TXVideoInfo, String, String> task = new AsyncTask<TXVideoEditConstants.TXVideoInfo, String, String>() {
            @Override
            protected String doInBackground(TXVideoEditConstants.TXVideoInfo... params) {
                String mediaFileName = fileInfo.getFileName();
                TXCLog.d(TAG, "fileName = " + mediaFileName);
                if (mediaFileName == null)
                    mediaFileName = fileInfo.getFilePath().substring(fileInfo.getFilePath().lastIndexOf("/"), fileInfo.getFilePath().lastIndexOf("."));
                if (mediaFileName.lastIndexOf(".") != -1) {
                    mediaFileName = mediaFileName.substring(0, mediaFileName.lastIndexOf("."));
                }

                String folder = mediaFileName;
                File appDir = new File(folder);
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }

                String fileName = "thumbnail" + ".jpg";
                File file = new File(appDir, fileName);
                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    if (params[0].coverImage != null)
                        params[0].coverImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileInfo.setThumbPath(file.getAbsolutePath());
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                startPreviewActivity(mResult);
                finish();
            }
        };
        task.execute(videoInfo);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_done:
                startTranscode();
                break;
            case R.id.back_ll:
                mTXVideoJoiner.stopPlay();
                mTXVideoJoiner.cancel();
                mTXVideoJoiner.setTXVideoPreviewListener(null);
                mTXVideoJoiner.setVideoJoinerListener(null);
                finish();
                break;
            case R.id.btn_play:
                mIsStopManually = !mIsStopManually;//取反
                playVideo();
                break;
        }
    }

    /**
     * 选择合成模式
     */
    private void showJoinModeDialog() {
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(TCVideoJoinerPreviewActivity.this, R.style.ConfirmDialogStyle);
        normalDialog.setMessage("选择合成模式");
        normalDialog.setCancelable(true);
        normalDialog.setPositiveButton("合成模式1", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startTranscode();
            }
        });
        normalDialog.setNegativeButton("合成模式2", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startPictureJoin();
            }
        });
        normalDialog.show();
    }

    private void startPictureJoin() {
        if (mCurrentState == STATE_PLAY || mCurrentState == STATE_PAUSE) {
            mTXVideoJoiner.setTXVideoPreviewListener(null);
            mTXVideoJoiner.stopPlay();
        }
        mBtnDone.setClickable(false);
        mBtnDone.setEnabled(false);
        Toast.makeText(this, "开始视频合成", Toast.LENGTH_SHORT).show();
        mWorkProgressDialog.setProgress(0);
        mWorkProgressDialog.setCancelable(false);
        mWorkProgressDialog.show(getSupportFragmentManager(), "progress_dialog");
        try {
            String outputPath = Environment.getExternalStorageDirectory() + File.separator + TCConstants.DEFAULT_MEDIA_PACK_FOLDER;
            File outputFolder = new File(outputPath);

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            String current = String.valueOf(System.currentTimeMillis() / 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String time = sdf.format(new Date(Long.valueOf(current + "000")));
            String saveFileName = String.format("TXVideo_%s.mp4", time);
            mVideoOutputPath = outputFolder + "/" + saveFileName;
            TXCLog.d(TAG, mVideoOutputPath);
            mTXVideoJoiner.setVideoJoinerListener(this);
            mCurrentState = STATE_TRANSCODE;

            //示例：以右边高度为准
            if (videoInfo != null && videoInfo2 != null) {
                TXVideoEditConstants.TXAbsoluteRect rect1 = new TXVideoEditConstants.TXAbsoluteRect();
                rect1.x = 0;                     //第一个视频的左上角位置
                rect1.y = 0;
                rect1.width = videoInfo.width;   //第一个视频的宽高
                rect1.height = videoInfo.height;

                TXVideoEditConstants.TXAbsoluteRect rect2 = new TXVideoEditConstants.TXAbsoluteRect();
                rect2.x = rect1.x + rect1.width; //第2个视频的左上角位置
                rect2.y = 0;
                rect2.width = videoInfo2.width;  //第2个视频的宽高
                rect2.height = videoInfo2.height;

                List<TXVideoEditConstants.TXAbsoluteRect> list = new ArrayList<>();
                list.add(rect1);
                list.add(rect2);
                mTXVideoJoiner.setSplitScreenList(list, videoInfo.width + videoInfo2.width, videoInfo2.height); //第2，3个param：两个视频合成画布的宽高
                mTXVideoJoiner.splitJoinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mCurrentState == STATE_NONE) {//说明是合成被取消
            playVideo();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsStopManually) { //非手动停止
            if (mCurrentState == STATE_PAUSE) {
                mTXVideoJoiner.resumePlay();
                mCurrentState = STATE_PLAY;
            }
            mBtnPlay.setImageResource(mCurrentState == STATE_PLAY ? R.drawable.ic_pause : R.drawable.ic_play);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCurrentState == STATE_PLAY) {
            mTXVideoJoiner.pausePlay();
            mCurrentState = STATE_PAUSE;
        } else if (mCurrentState == STATE_PAUSE) {
            mIsStopManually = false;
        } else if (mCurrentState == STATE_TRANSCODE) {
            mTXVideoJoiner.cancel();
            mCurrentState = STATE_NONE;
        }
        mBtnPlay.setImageResource(mCurrentState == STATE_PLAY ? R.drawable.ic_pause : R.drawable.ic_play);

        mBtnDone.setClickable(true);
        mBtnDone.setEnabled(true);

        if (mWorkProgressDialog != null && mWorkProgressDialog.isAdded()) {
            mWorkProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private void playVideo() {
        if (mCurrentState == STATE_NONE) {
            mTXVideoJoiner.startPlay();
            mCurrentState = STATE_PLAY;
        } else if (mCurrentState == STATE_PLAY) {
            mTXVideoJoiner.pausePlay();
            mCurrentState = STATE_PAUSE;
        } else if (mCurrentState == STATE_PAUSE) {
            mTXVideoJoiner.resumePlay();
            mCurrentState = STATE_PLAY;
        } else if (mCurrentState == STATE_TRANSCODE) {
            //do nothing
        }
        mBtnPlay.setImageResource(mCurrentState == STATE_PLAY ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWorkProgressDialog != null) {
            mWorkProgressDialog.setOnClickStopListener(null);
            if (mWorkProgressDialog.isAdded()) {
                mWorkProgressDialog.dismiss();
            }
        }
        if (mTXVideoJoiner != null) {
            if (mCurrentState == STATE_PLAY || mCurrentState == STATE_PAUSE) {
                mTXVideoJoiner.stopPlay();
            } else if (mCurrentState == STATE_TRANSCODE) {
                mTXVideoJoiner.cancel();
            }
            mTXVideoJoiner.setTXVideoPreviewListener(null);
            mTXVideoJoiner.setVideoJoinerListener(null);
        }

        if (mHandler != null) {
            mHandler.removeMessages(MSG_LOAD_VIDEO_INFO);
            mHandler.getLooper().quit();
            mHandler = null;
        }
    }

    public void startTranscode() {
        if (mCurrentState == STATE_PLAY || mCurrentState == STATE_PAUSE) {
            mTXVideoJoiner.stopPlay();
        }
        mBtnPlay.setImageResource(R.drawable.ic_play);
        mBtnDone.setClickable(false);
        mBtnDone.setEnabled(false);
        Toast.makeText(this, "开始视频合成", Toast.LENGTH_SHORT).show();
        mWorkProgressDialog.setProgress(0);
        mWorkProgressDialog.setCancelable(false);
        mWorkProgressDialog.show(getSupportFragmentManager(), "progress_dialog");
        mWorkProgressDialog.setCanCancel(true);
        try {
            mVideoOutputPath = TCEditerUtil.generateVideoPath();
            mTXVideoJoiner.setVideoJoinerListener(this);
            mCurrentState = STATE_TRANSCODE;
            mTXVideoJoiner.joinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_540P, mVideoOutputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewProgress(int time) {
        TXCLog.d(TAG, "setProgessIsReverse curPos = " + time);
    }

    @Override
    public void onPreviewFinished() {
        TXCLog.d(TAG, "onPreviewFinished");
        mTXVideoJoiner.startPlay();
    }

    @Override
    public void onJoinProgress(float progress) {
        final int prog = (int) (progress * 100);
        TXCLog.d(TAG, "composer progress = " + prog);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWorkProgressDialog.setProgress(prog);
            }
        });
    }

    @Override
    public void onJoinComplete(final TXVideoEditConstants.TXJoinerResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWorkProgressDialog.dismiss();
                mBtnDone.setClickable(true);
                mBtnDone.setEnabled(true);
                if (result.retCode == TXVideoEditConstants.JOIN_RESULT_OK) {
                    if (mTXVideoInfo != null) {
                        mResult = result;
                        createThumbFile(mTXVideoInfo);
                    } else {
                        finish();
                    }
                } else {
                    TXVideoEditConstants.TXJoinerResult ret = result;
                    mDialogUtil.showDialog(mContext, "视频合成失败", result.descMsg, null);
                }
                mCurrentState = STATE_NONE;
            }
        });
    }

    private void startPreviewActivity(TXVideoEditConstants.TXJoinerResult result) {
        Intent intent = new Intent(getApplicationContext(), TCVideoPreviewActivity.class);
        intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_PLAY);
        intent.putExtra(TCConstants.VIDEO_RECORD_RESULT, result.retCode);
        intent.putExtra(TCConstants.VIDEO_RECORD_DESCMSG, result.descMsg);
        intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
        intent.putExtra(TCConstants.VIDEO_RECORD_COVERPATH, mTCVideoFileInfoList.get(0).getThumbPath());
        intent.putExtra(TCConstants.VIDEO_RECORD_DURATION, TXVideoInfoReader.getInstance().getVideoFileInfo(mVideoOutputPath).duration);
        startActivity(intent);
    }

}

