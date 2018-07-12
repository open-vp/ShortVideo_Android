package com.niurenjob.bullman.ShortVideo;

import android.support.multidex.MultiDexApplication;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.ugc.TXUGCBase;


public class DemoApplication extends MultiDexApplication {

//    private RefWatcher mRefWatcher;
    private static DemoApplication instance;
    String ugcLicenceUrl = "http://license.vod2.myqcloud.com/license/v1/5574621b64807ec49b7cf3274e0afb21/TXUgcSDK.licence";
    String ugcKey = "595f5502cad3a4908a044779619a7f62";

    @Override
    public void onCreate() {

        super.onCreate();

        instance = this;

        TXLiveBase.setConsoleEnabled(true);
        TXLiveBase.setAppID("1252463788");
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(TXLiveBase.getSDKVersionStr());
        CrashReport.initCrashReport(getApplicationContext(),strategy);

        TXUGCBase.getInstance().setLicence(instance, ugcLicenceUrl, ugcKey);

//        File file = getFilesDir();
//        Log.w("DemoApplication", "load:" + file.getAbsolutePath());
//        TXLiveBase.setLibraryPath(file.getAbsolutePath());
        //测试代码
//        TCHttpEngine.getInstance().initContext(getApplicationContext());
//        mRefWatcher = LeakCanary.install(this);
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        DemoApplication application = (DemoApplication) context.getApplicationContext();
//        return application.mRefWatcher;
//    }

    public static DemoApplication getApplication() {
        return instance;
    }

}
