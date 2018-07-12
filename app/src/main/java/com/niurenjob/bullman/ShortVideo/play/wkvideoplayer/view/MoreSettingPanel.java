package com.niurenjob.bullman.ShortVideo.play.wkvideoplayer.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import com.niurenjob.bullman.ShortVideo.R;

/**
 * Created by annidy on 2018/1/2.
 */

public class MoreSettingPanel extends FrameLayout implements View.OnClickListener {

    private static final float MAX_BRIGNTNESS = 255;
    private Context mContext;
    private SeekBar mAudioSeek;
    private SeekBar mLightSeek;
    private Button mBtnSpeed1;
    private Button mBtnSpeed125;
    private Button mBtnSpeed15;
    private Button mBtnSpeed2;
    private Switch mBtnMirror;
    private AudioManager mAudioManager;

    public MoreSettingPanel(Context context) {
        super(context);
        mContext = context;

        initView(context);
    }

    public MoreSettingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        initView(context);
    }

    public MoreSettingPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.biz_video_media_more_setting_panel, this);

        mAudioSeek = (SeekBar) findViewById(R.id.seekBar_audio);
        mLightSeek = (SeekBar) findViewById(R.id.seekBar_light);

        mBtnSpeed1 = (Button) findViewById(R.id.btn_speed1);
        mBtnSpeed125 = (Button) findViewById(R.id.btn_speed125);
        mBtnSpeed15 = (Button) findViewById(R.id.btn_speed15);
        mBtnSpeed2 = (Button) findViewById(R.id.btn_speed2);
        mBtnMirror = (Switch) findViewById(R.id.switch_btn_mirror);

        mBtnSpeed1.setOnClickListener(this);
        mBtnSpeed125.setOnClickListener(this);
        mBtnSpeed15.setOnClickListener(this);
        mBtnSpeed2.setOnClickListener(this);
        mBtnMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mImp != null) {
                    mImp.onMirrorChange(isChecked);
                }
            }
        });

        mAudioSeek.setOnSeekBarChangeListener(mAudioSeekListener);
        mLightSeek.setOnSeekBarChangeListener(mLightSeekListener);

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        updateCurrentVolume();
        updateCurrentLight();
    }

    private void updateCurrentLight() {
        Activity activity = (Activity) mContext;
        Window window = activity.getWindow();

        WindowManager.LayoutParams params = window.getAttributes();
        if (params.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            params.screenBrightness = getActivityBrightness((Activity) mContext);
            window.setAttributes(params);
            if (params.screenBrightness == -1){
                mLightSeek.setProgress(100);
                return;
            }
            mLightSeek.setProgress((int) (params.screenBrightness * 100));
        }
    }

    private SeekBar.OnSeekBarChangeListener mAudioSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateVolumeProgress(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void updateVolumeProgress(int progress) {
        float percentage = (float) progress / mAudioSeek.getMax();

        if (percentage < 0 || percentage > 1)
            return;

        if (mAudioManager != null) {
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int newVolume = (int) (percentage * maxVolume);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        }
    }

    private void updateCurrentVolume() {
        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        float percentage = (float) curVolume / maxVolume;

        final int progress = (int) (percentage * mAudioSeek.getMax());
        mAudioSeek.setProgress(progress);
    }

    private SeekBar.OnSeekBarChangeListener mLightSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateBrightProgress(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * 设置当前activity的屏幕亮度
     *
     * @param paramFloat 0-1.0f
     * @param activity   需要调整亮度的activity
     */
    public static void setActivityBrightness(float paramFloat, Activity activity) {
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams params = localWindow.getAttributes();
        params.screenBrightness = paramFloat;
        localWindow.setAttributes(params);
    }

    private void updateBrightProgress(int progress) {
        Activity activity = (Activity) mContext;
        Window window = activity.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = progress * 1.0f / 100;
        if (params.screenBrightness > 1.0f) {
            params.screenBrightness = 1.0f;
        }
        if (params.screenBrightness <= 0.01f) {
            params.screenBrightness = 0.01f;
        }

        window.setAttributes(params);
        mLightSeek.setProgress(progress);
    }

    /**
     * 获取当前activity的屏幕亮度
     *
     * @param activity 当前的activity对象
     * @return 亮度值范围为0-0.1f，如果为-1.0，则亮度与全局同步。
     */
    public static float getActivityBrightness(Activity activity) {
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams params = localWindow.getAttributes();
        return params.screenBrightness;
    }

    private float getScreenBrightness(Context context) {
        float result = 1;
        int value = 0;
        ContentResolver cr = context.getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        result = (float) value / MAX_BRIGNTNESS;
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_speed1:
                mBtnSpeed1.setTextColor(mContext.getResources().getColor(R.color.colorTintRed));
                mBtnSpeed125.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed15.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed2.setTextColor(mContext.getResources().getColor(R.color.white));
                if (mImp != null) {
                    mImp.onRateChange(1.0f);
                }
                break;
            case R.id.btn_speed125:
                mBtnSpeed1.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed125.setTextColor(mContext.getResources().getColor(R.color.colorTintRed));
                mBtnSpeed15.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed2.setTextColor(mContext.getResources().getColor(R.color.white));
                if (mImp != null) {
                    mImp.onRateChange(1.25f);
                }
                break;
            case R.id.btn_speed15:
                mBtnSpeed1.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed125.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed15.setTextColor(mContext.getResources().getColor(R.color.colorTintRed));
                mBtnSpeed2.setTextColor(mContext.getResources().getColor(R.color.white));
                if (mImp != null) {
                    mImp.onRateChange(1.5f);
                }
                break;
            case R.id.btn_speed2:
                mBtnSpeed1.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed125.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed15.setTextColor(mContext.getResources().getColor(R.color.white));
                mBtnSpeed2.setTextColor(mContext.getResources().getColor(R.color.colorTintRed));
                if (mImp != null) {
                    mImp.onRateChange(2f);
                }
                break;
        }
    }

    private MoreSettingChangeImpl mImp;

    public void setMoreSettingChangeControl(MoreSettingChangeImpl imp) {
        mImp = imp;
    }

    public interface MoreSettingChangeImpl {
        void onRateChange(float rate);

        void onMirrorChange(boolean mirror);
    }
}
