package com.niurenjob.bullman.ShortVideo.play;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.liteav.basic.log.TXCLog;
import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.common.activity.QRCodeScanActivity;
import com.niurenjob.bullman.ShortVideo.common.utils.TCConstants;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.util.DensityUtil;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.view.MediaController;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.view.SuperVideoPlayer;
import com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.view.VodRspData;
import com.niurenjob.bullman.ShortVideo.videopublish.server.GetVideoInfoListListener;
import com.niurenjob.bullman.ShortVideo.videopublish.server.VideoDataMgr;
import com.niurenjob.bullman.ShortVideo.videopublish.server.VideoInfo;
import com.tencent.rtmp.TXPlayerAuthBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by annidy on 2017/12/27.
 */

public class NewVodPlayerActivity extends Activity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "NewVodPlayerActivity";
    private SuperVideoPlayer mSuperVideoPlayer;
    private ImageView mPlayBtnView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private NewVodListAdapter mAdapter;
    private ImageView mIvAdd;
    private Button mBtnScan;
//    private ArrayList<TXPlayerAuthParam> mVodList;
    private TXPlayerAuthParam mCurrentFileIDParam;
    private LinearLayout mLyTop;
    private ImageView mIvBack;

    private boolean isPlayDefaultVideo = true; // true:播放默认视频, false:播放从demo上传入口上传的视频
    private String videoIdFrom;
    private String titleFrom;
    private GetVideoInfoListListener mGetVideoInfoListListener;

    private NewVodListAdapter.OnItemClickLitener mOnItemClickListener = new NewVodListAdapter.OnItemClickLitener() {
        @Override
        public void onItemClick(int position,String title,String url) {
            playVideoWithUrl(url);
            mSuperVideoPlayer.updateUI(title);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_vod);
        initView();
        checkPermission();
        getWindow().addFlags(WindowManager.LayoutParams.
                FLAG_KEEP_SCREEN_ON);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
            }
        }
    }

    private void initView() {
        mLyTop = (LinearLayout) findViewById(R.id.layout_top);
        mSuperVideoPlayer = (SuperVideoPlayer) findViewById(R.id.video_player_item_1);
        mSuperVideoPlayer.setVideoPlayCallback(mVideoPlayCallback);

        mPlayBtnView = (ImageView) findViewById(R.id.play_btn);
        mPlayBtnView.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new NewVodListAdapter(this);
        mAdapter.setOnItemClickLitener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        mIvBack = (ImageView)findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(this);

        mIvAdd = (ImageView) findViewById(R.id.iv_add);
        mIvAdd.setOnClickListener(this);

        mBtnScan = (Button) findViewById(R.id.btnScan);
        mBtnScan.setOnClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_list);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);

//        mVodList = new ArrayList<TXPlayerAuthParam>();
        getData();
        playVideo();
        initData();
    }

    private void getData() {
        isPlayDefaultVideo = getIntent().getBooleanExtra(TCConstants.PLAYER_DEFAULT_VIDEO, true);
        if(isPlayDefaultVideo){
            return;
        }
        mCurrentFileIDParam = new TXPlayerAuthParam();
        mCurrentFileIDParam.appId = TCConstants.VOD_APPID;

        videoIdFrom = getIntent().getStringExtra(TCConstants.PLAYER_VIDEO_ID);
        titleFrom = getIntent().getStringExtra(TCConstants.PLAYER_VIDEO_NAME);
        if( !TextUtils.isEmpty(videoIdFrom) ){
            mCurrentFileIDParam.fileId = videoIdFrom;
            mSuperVideoPlayer.updateUI(titleFrom);
            playVideoWithFileId(mCurrentFileIDParam);
        }
        initGetVideoInfoListListener();
        if( !isPlayDefaultVideo ){
            mBtnScan.setVisibility(View.GONE);
            mIvAdd.setVisibility(View.GONE);
        }
    }

    private void initGetVideoInfoListListener() {
        mGetVideoInfoListListener = new GetVideoInfoListListener() {
            @Override
            public void onGetVideoInfoList(List<VideoInfo> videoInfoList) {
                loadVideoInfoList(videoInfoList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFail(int errCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NewVodPlayerActivity.this, "获取已上传的视频列表失败", Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        };
    }

    private void loadVideoInfoList(List<VideoInfo> videoInfoList) {
        if (videoInfoList == null || videoInfoList.size() == 0)
            return;
//        mVodList.clear();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
            }
        });
        for (VideoInfo videoInfo : videoInfoList) {
            TXPlayerAuthParam param = new TXPlayerAuthParam();
            param.appId = TCConstants.VOD_APPID;
            param.fileId = videoInfo.fileId;
            TXCLog.i(TAG, " fileId:" + param.fileId);
            mSuperVideoPlayer.addVodInfo(param);
//            mVodList.add(param);
        }

//        mSuperVideoPlayer.setVideoListInfo(mVodList);
//        mSuperVideoPlayer.getNextInfo();

        if(TextUtils.isEmpty(mCurrentFileIDParam.fileId)){
            final VideoInfo videoInfo = videoInfoList.get(0);
            mCurrentFileIDParam.fileId = videoInfo.fileId;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSuperVideoPlayer.updateUI(videoInfo.name);
                }
            });
            playVideoWithFileId(mCurrentFileIDParam);
        }
    }

    private void playVideoWithFileId(TXPlayerAuthParam param) {
        TXPlayerAuthBuilder authBuilder = new TXPlayerAuthBuilder();
        try {
            if (!TextUtils.isEmpty(param.appId)) {
                authBuilder.setAppId(Integer.parseInt(param.appId));
            }
            String fileId = param.fileId;
            authBuilder.setFileId(fileId);

            if (mSuperVideoPlayer != null) {
                mSuperVideoPlayer.playFileID(authBuilder);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSuperVideoPlayer.loadVideo();
                    }
                });
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入正确的AppId和FileId", Toast.LENGTH_SHORT).show();
        }
    }

    private void playVideoWithUrl(String url) {
        if (mSuperVideoPlayer != null) {
            mSuperVideoPlayer.setPlayUrl(url);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSuperVideoPlayer.loadVideo();
                }
            });
        }
    }

    private void initData() {
        if(isPlayDefaultVideo){
            TXPlayerAuthParam param2 = new TXPlayerAuthParam();
            param2.appId = "1252463788";
            param2.fileId = "4564972819219071568";
            mSuperVideoPlayer.addVodInfo(param2);

            TXPlayerAuthParam param3 = new TXPlayerAuthParam();
            param3.appId = "1252463788";
            param3.fileId = "4564972819219071668";
            mSuperVideoPlayer.addVodInfo(param3);

            TXPlayerAuthParam param4 = new TXPlayerAuthParam();
            param4.appId = "1252463788";
            param4.fileId = "4564972819219071679";
            mSuperVideoPlayer.addVodInfo(param4);

            TXPlayerAuthParam param5 = new TXPlayerAuthParam();
            param5.appId = "1252463788";
            param5.fileId = "4564972819219081699";
            mSuperVideoPlayer.addVodInfo(param5);

        }else{
            VideoDataMgr.getInstance().setGetVideoInfoListListener(mGetVideoInfoListListener);
            VideoDataMgr.getInstance().getVideoList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSuperVideoPlayer != null) {
            mSuperVideoPlayer.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSuperVideoPlayer != null) {
            mSuperVideoPlayer.onPause();
        }
    }

    private SuperVideoPlayer.VideoPlayCallbackImpl mVideoPlayCallback = new SuperVideoPlayer.VideoPlayCallbackImpl() {
        @Override
        public void onCloseVideo() {
            mSuperVideoPlayer.onDestroy();
            mPlayBtnView.setVisibility(View.VISIBLE);
            mSuperVideoPlayer.setVisibility(View.GONE);
            resetPageToPortrait();
        }

        @Override
        public void onSwitchPageType() {
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mLyTop.setVisibility(View.VISIBLE);
                mSuperVideoPlayer.setPageType(MediaController.PageType.SHRINK);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mLyTop.setVisibility(View.GONE);
                mSuperVideoPlayer.setPageType(MediaController.PageType.EXPAND);
            }
        }

        @Override
        public void onPlayFinish() {
            mPlayBtnView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onBack() {
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mLyTop.setVisibility(View.VISIBLE);
                mSuperVideoPlayer.setPageType(MediaController.PageType.SHRINK);
            } else {
                finish();
            }
        }

        @Override
        public void onLoadVideoInfo(VodRspData data) {
            mAdapter.addVideoInfo(data);
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                showAddDialog();
                break;
            case R.id.btnScan:
                scan();
                break;
            case R.id.play_btn:
                if (mCurrentFileIDParam != null){
                    playVideoWithFileId(mCurrentFileIDParam);
                }
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString("result"))) {
            return;
        }
        String result = data.getExtras().getString("result");
        if (requestCode == 200) {
            EditText editText = (EditText) findViewById(R.id.editText);
            editText.setText(result);
        } else if (requestCode == 100) {
            if (mSuperVideoPlayer != null) {
                mSuperVideoPlayer.updateUI("新播放的视频");
                mSuperVideoPlayer.setPlayUrl(result);
            }
        }
    }

    private void scan() {
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        startActivityForResult(intent, 100);
    }

    private void showAddDialog() {
        AlertDialog.Builder customizeDialog = new AlertDialog.Builder(this);
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_vod_player_fileid, null);
        customizeDialog.setTitle("请设置AppID和FileID");
        customizeDialog.setView(dialogView);

        final EditText etAppId = (EditText) dialogView.findViewById(R.id.et_appid);
        final EditText etFileId = (EditText) dialogView.findViewById(R.id.et_fileid);

        customizeDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        customizeDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TXPlayerAuthParam param = new TXPlayerAuthParam();
                        param.appId = etAppId.getText().toString();
                        param.fileId = etFileId.getText().toString();

                        try {
                            if (TextUtils.isEmpty(param.appId)) {
                                Toast.makeText(NewVodPlayerActivity.this, "请输入正确的AppId和FileId", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            int appid = Integer.parseInt(param.appId);
                            String fileId = param.fileId;
                            if(TextUtils.isEmpty(fileId)){
                                Toast.makeText(NewVodPlayerActivity.this, "请输入正确的AppId和FileId", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(NewVodPlayerActivity.this, "请输入正确的AppId和FileId", Toast.LENGTH_SHORT).show();
                            return;
                        }
//                        mVodList.add(param);

                        if (mSuperVideoPlayer != null) {
//                            mSuperVideoPlayer.setVideoPlayInfoCallback(mPlayInfoCallback);
                            mSuperVideoPlayer.addVodInfo(param);
                        }
                    }
                });
        customizeDialog.show();
    }

//    private SuperVideoPlayer.OnPlayInfoCallback mPlayInfoCallback = new SuperVideoPlayer.OnPlayInfoCallback() {
//        @Override
//        public void onPlayInfoCallback(int ret) {
//            if (ret < 0) {
//                int index = mVodList.size() - 1;
//                if (index >= 0) {
//                    mVodList.remove(index);
//                }
//            }
//        }
//    };

    private void playVideo() {
        mPlayBtnView.setVisibility(View.GONE);
        mSuperVideoPlayer.setVisibility(View.VISIBLE);
        mSuperVideoPlayer.setAutoHideController(false);

        TXPlayerAuthParam param = new TXPlayerAuthParam();
        if(isPlayDefaultVideo){
            param.appId = "1252463788";
            param.fileId = "4564972819220421305";
            mSuperVideoPlayer.addVodInfo(param);
            playVideoWithFileId(param);
        }else if( !TextUtils.isEmpty(videoIdFrom) ){
            param.appId = TCConstants.VOD_APPID;
            param.fileId = videoIdFrom;
            mSuperVideoPlayer.addVodInfo(param);
            playVideoWithFileId(param);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSuperVideoPlayer != null) {
            mSuperVideoPlayer.onDestroy();
        }
        VideoDataMgr.getInstance().setGetVideoInfoListListener(null);
    }

    /***
     * 旋转屏幕之后回调
     *
     * @param newConfig newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null == mSuperVideoPlayer) return;
        /***
         * 根据屏幕方向重新设置播放器的大小
         */
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().invalidate();
            float height = DensityUtil.getWidthInPx(this);
            float width = DensityUtil.getHeightInPx(this);
            mSuperVideoPlayer.getLayoutParams().height = (int) width;
            mSuperVideoPlayer.getLayoutParams().width = (int) height;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            float width = DensityUtil.getWidthInPx(this);
            float height = DensityUtil.dip2px(this, 200.f);
            mSuperVideoPlayer.getLayoutParams().height = (int) height;
            mSuperVideoPlayer.getLayoutParams().width = (int) width;
        }
    }

    /***
     * 恢复屏幕至竖屏
     */
    private void resetPageToPortrait() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mSuperVideoPlayer.setPageType(MediaController.PageType.SHRINK);
        }
    }

    @Override
    public void onRefresh() {
        if(isPlayDefaultVideo){
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        VideoDataMgr.getInstance().getVideoList();
    }
}
