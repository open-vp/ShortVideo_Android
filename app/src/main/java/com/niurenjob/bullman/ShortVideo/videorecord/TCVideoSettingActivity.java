package com.niurenjob.bullman.ShortVideo.videorecord;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tencent.liteav.basic.log.TXCLog;
import com.niurenjob.bullman.ShortVideo.R;
import com.niurenjob.bullman.ShortVideo.common.utils.FileUtils;
import com.niurenjob.bullman.ShortVideo.common.utils.TCConstants;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.ugc.TXRecordCommon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * UGC小视频设置界面.
 */

public class TCVideoSettingActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TCVideoSettingActivity";

    public static final String RECORD_CONFIG_MAX_DURATION = "record_config_max_duration";
    public static final String RECORD_CONFIG_MIN_DURATION = "record_config_min_duration";
    public static final String RECORD_CONFIG_ASPECT_RATIO = "record_config_aspect_ratio";
    public static final String RECORD_CONFIG_RECOMMEND_QUALITY = "record_config_recommend_quality";
    public static final String RECORD_CONFIG_HOME_ORIENTATION = "record_config_home_orientation";
    public static final String RECORD_CONFIG_RESOLUTION = "record_config_resolution";
    public static final String RECORD_CONFIG_BITE_RATE = "record_config_bite_rate";
    public static final String RECORD_CONFIG_FPS = "record_config_fps";
    public static final String RECORD_CONFIG_GOP = "record_config_gop";
    public static final String RECORD_CONFIG_NEED_EDITER = "record_config_go_editer";

    private LinearLayout llBack;
    private View rlBiterate, rlFps, rlGop;
    private EditText etBitrate, etGop, etFps;
    private RadioGroup rgVideoQuality, rgVideoResolution, rgVideoAspectRatio;
    private RadioButton rbVideoQualitySD, rbVideoQualityHD, rbVideoQualitySSD, rbVideoQulityCustom,
            rbVideoResolution360p, rbVideoResolution540p, rbVideoResolution720p,
            rbVideoAspectRatio11, rbVideoAspectRatio34, rbVideoAspectRatio916;
    private TextView tvRecommendResolution, tvRecommendBitrate, tvRecommendFps, tvRecommendGop;
    private Button btnOK;

    private int mRecommendQuality = -1;
    private int mAspectRatio; // 视频比例
    private int mRecordResolution; // 录制分辨率
    private int mBiteRate = 2400; // 码率
    private int mFps = 20; // 帧率
    private int mGop = 3; // 关键帧间隔
    private CheckBox cbEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_settings);

        TXCLog.init();

        initData();

        initView();

        initListener();

        initViewStatus();

        checkPermission();

//        copyLicenceToSdcard();
    }

    private void copyLicenceToSdcard() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sdcardFolder = getExternalFilesDir(null).getAbsolutePath();
                File sdcardLicenceFile = new File(sdcardFolder + File.separator + TCConstants.UGC_LICENCE_NAME);
                if(sdcardLicenceFile.exists()){
                    return;
                }
                try {
                    FileUtils.copyFromAssetToSdcard(TCVideoSettingActivity.this, TCConstants.UGC_LICENCE_NAME, sdcardFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
//            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
//                permissions.add(Manifest.permission.CAMERA);
//            }
//            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
//                permissions.add(Manifest.permission.RECORD_AUDIO);
//            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int ret : grantResults) {
                    if (ret != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void initData() {
        mRecommendQuality = -1;
    }

    private void initView() {
        llBack = (LinearLayout) findViewById(R.id.back_ll);

        rlBiterate = findViewById(R.id.rl_bite_rate);
        rlFps = findViewById(R.id.rl_fps);
        rlGop = findViewById(R.id.rl_gop);

        etBitrate = (EditText) findViewById(R.id.et_biterate);
        etFps = (EditText) findViewById(R.id.et_fps);
        etGop = (EditText) findViewById(R.id.et_gop);

        rgVideoQuality = (RadioGroup) findViewById(R.id.rg_video_quality);
        rgVideoResolution = (RadioGroup) findViewById(R.id.rg_video_resolution);
        rgVideoAspectRatio = (RadioGroup) findViewById(R.id.rg_video_aspect_ratio);

        rbVideoQualitySD = (RadioButton) findViewById(R.id.rb_video_quality_sd);
        rbVideoQualityHD = (RadioButton) findViewById(R.id.rb_video_quality_hd);
        rbVideoQualitySSD = (RadioButton) findViewById(R.id.rb_video_quality_super);
        rbVideoQulityCustom = (RadioButton) findViewById(R.id.rb_video_quality_custom);

        rbVideoResolution360p = (RadioButton) findViewById(R.id.rb_video_resolution_360p);
        rbVideoResolution540p = (RadioButton) findViewById(R.id.rb_video_resolution_540p);
        rbVideoResolution720p = (RadioButton) findViewById(R.id.rb_video_resolution_720p);

        rbVideoAspectRatio11 = (RadioButton) findViewById(R.id.rb_video_aspect_ratio_1_1);
        rbVideoAspectRatio34 = (RadioButton) findViewById(R.id.rb_video_aspect_ratio_3_4);
        rbVideoAspectRatio916 = (RadioButton) findViewById(R.id.rb_video_aspect_ratio_9_16);

        tvRecommendResolution = (TextView) findViewById(R.id.tv_recommend_resolution);
        tvRecommendBitrate = (TextView) findViewById(R.id.tv_recommend_bitrate);
        tvRecommendFps = (TextView) findViewById(R.id.tv_recommend_fps);
        tvRecommendGop = (TextView) findViewById(R.id.tv_recommend_gop);

        btnOK = (Button) findViewById(R.id.btn_ok);

        cbEdit = (CheckBox)findViewById(R.id.cb_edit);
    }

    private void initListener() {
        llBack.setOnClickListener(this);
        btnOK.setOnClickListener(this);

        etBitrate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    rlBiterate.setBackgroundResource(R.drawable.rect_bg_green);
                } else {
                    rlBiterate.setBackgroundResource(R.drawable.rect_bg_gray);
                }
            }
        });

        etFps.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    rlFps.setBackgroundResource(R.drawable.rect_bg_green);
                } else {
                    rlFps.setBackgroundResource(R.drawable.rect_bg_gray);
                }
            }
        });

        etGop.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    rlGop.setBackgroundResource(R.drawable.rect_bg_green);
                } else {
                    rlGop.setBackgroundResource(R.drawable.rect_bg_gray);
                }
            }
        });

        rgVideoAspectRatio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == rbVideoAspectRatio11.getId()) {
                    mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1;
                } else if (i == rbVideoAspectRatio34.getId()) {
                    mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4;
                } else {
                    mAspectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
                }
            }
        });

        rgVideoQuality.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == rbVideoQualitySD.getId()) {
                    mRecommendQuality = TXRecordCommon.VIDEO_QUALITY_LOW;
                    showRecommendQualitySet();
                    recommendQualitySD();
                    clearCustomBg();
                } else if (i == rbVideoQualityHD.getId()) {
                    mRecommendQuality = TXRecordCommon.VIDEO_QUALITY_MEDIUM;
                    showRecommendQualitySet();
                    recommendQualityHD();
                    clearCustomBg();
                } else if (i == rbVideoQualitySSD.getId()) {
                    mRecommendQuality = TXRecordCommon.VIDEO_QUALITY_HIGH;
                    showRecommendQualitySet();
                    recommendQualitySSD();
                    clearCustomBg();
                } else {
                    // 自定义
                    mRecommendQuality = -1;
                    showCustomQualitySet();
                }
            }
        });

        rgVideoResolution.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == rbVideoResolution360p.getId()) {
                    mRecordResolution = TXRecordCommon.VIDEO_RESOLUTION_360_640;
                } else if (i == rbVideoResolution540p.getId()) {
                    mRecordResolution = TXRecordCommon.VIDEO_RESOLUTION_540_960;
                } else {
                    mRecordResolution = TXRecordCommon.VIDEO_RESOLUTION_720_1280;
                }
            }
        });

        cbEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbEdit.setChecked(isChecked);
            }
        });
    }

    private void initViewStatus() {
        rbVideoResolution540p.setChecked(true);
        rbVideoAspectRatio916.setChecked(true);

        rbVideoQualityHD.setChecked(true);
    }

    private void recommendQualitySD() {
        tvRecommendResolution.setText("360p");
        tvRecommendBitrate.setText("2400");
        tvRecommendFps.setText("20");
        tvRecommendGop.setText("3");

        rbVideoResolution360p.setChecked(true);
    }

    private void recommendQualityHD() {
        tvRecommendResolution.setText("540p");
        tvRecommendBitrate.setText("6500");
        tvRecommendFps.setText("20");
        tvRecommendGop.setText("3");

        rbVideoResolution540p.setChecked(true);
    }

    private void recommendQualitySSD() {
        tvRecommendResolution.setText("720p");
        tvRecommendBitrate.setText("9600");
        tvRecommendFps.setText("20");
        tvRecommendGop.setText("3");

        rbVideoResolution720p.setChecked(true);
    }

    private void showCustomQualitySet() {
        rgVideoResolution.setVisibility(View.VISIBLE);
        etBitrate.setVisibility(View.VISIBLE);
        etFps.setVisibility(View.VISIBLE);
        etGop.setVisibility(View.VISIBLE);

        tvRecommendGop.setVisibility(View.GONE);
        tvRecommendResolution.setVisibility(View.GONE);
        tvRecommendBitrate.setVisibility(View.GONE);
        tvRecommendFps.setVisibility(View.GONE);
    }

    private void showRecommendQualitySet() {
        rgVideoResolution.setVisibility(View.GONE);
        etBitrate.setVisibility(View.GONE);
        etFps.setVisibility(View.GONE);
        etGop.setVisibility(View.GONE);

        tvRecommendGop.setVisibility(View.VISIBLE);
        tvRecommendResolution.setVisibility(View.VISIBLE);
        tvRecommendBitrate.setVisibility(View.VISIBLE);
        tvRecommendFps.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_ll:
                finish();
                break;
            case R.id.btn_ok:
                getConfigData();
                startVideoRecordActivity();
                break;
        }
    }

    private void clearCustomBg() {
        rlBiterate.setBackgroundResource(R.drawable.rect_bg_gray);
        rlFps.setBackgroundResource(R.drawable.rect_bg_gray);
        rlGop.setBackgroundResource(R.drawable.rect_bg_gray);
    }

    private void getConfigData() {
        // 使用提供的三挡质量设置，不需要传以下参数，sdk内部已定义
        if (mRecommendQuality != -1) {
            return;
        }

        String fps = etFps.getText().toString();
        String gop = etGop.getText().toString();
        String bitrate = etBitrate.getText().toString();

        if (!TextUtils.isEmpty(bitrate)) {
            try {
                mBiteRate = Integer.parseInt(bitrate);
                if (mBiteRate < 600) {
                    mBiteRate = 600;
                } else if (mBiteRate > 12000) {
                    mBiteRate = 12000;
                }
            } catch (NumberFormatException e) {
                TXCLog.e(TAG, "NumberFormatException");
            }
        } else {
            mBiteRate = 6500;
        }

        if (!TextUtils.isEmpty(fps)) {
            try {
                mFps = Integer.parseInt(fps);
                if (mFps < 15) {
                    mFps = 15;
                } else if (mFps > 30) {
                    mFps = 20;
                }
            } catch (NumberFormatException e) {
                TXCLog.e(TAG, "NumberFormatException");
            }
        } else {
            mFps = 20;
        }

        if (!TextUtils.isEmpty(gop)) {
            try {
                mGop = Integer.parseInt(gop);
                if (mGop < 1) {
                    mGop = 1;
                } else if (mGop > 10) {
                    mGop = 3;
                }
            } catch (NumberFormatException e) {
                TXCLog.e(TAG, "NumberFormatException");
            }
        } else {
            mGop = 3;
        }
    }

    private void startVideoRecordActivity() {
        Intent intent = new Intent(this, TCVideoRecordActivity.class);
        intent.putExtra(RECORD_CONFIG_MIN_DURATION, 5 * 1000);
        intent.putExtra(RECORD_CONFIG_MAX_DURATION, 60 * 1000);
        intent.putExtra(RECORD_CONFIG_ASPECT_RATIO, mAspectRatio);
        if (mRecommendQuality != -1) {
            // 提供的三挡设置
            intent.putExtra(RECORD_CONFIG_RECOMMEND_QUALITY, mRecommendQuality);
        } else {
            // 自定义设置
            intent.putExtra(RECORD_CONFIG_RESOLUTION, mRecordResolution);
            intent.putExtra(RECORD_CONFIG_BITE_RATE, mBiteRate);
            intent.putExtra(RECORD_CONFIG_FPS, mFps);
            intent.putExtra(RECORD_CONFIG_GOP, mGop);
        }
        intent.putExtra(RECORD_CONFIG_HOME_ORIENTATION, TXLiveConstants.VIDEO_ANGLE_HOME_DOWN); // 竖屏录制
        intent.putExtra(RECORD_CONFIG_NEED_EDITER, cbEdit.isChecked());
        startActivity(intent);
    }

}
