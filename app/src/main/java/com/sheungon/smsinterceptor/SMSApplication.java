package com.sheungon.smsinterceptor;

import android.app.Application;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.sheungon.smsinterceptor.service.SMSInterceptorSettings;
import com.sheungon.smsinterceptor.util.FileUtil;
import com.sheungon.smsinterceptor.util.Log;
import com.sheungon.smsinterceptor.util.LogcatUtil;
import com.sheungon.smsinterceptor.util.PrivatePrefUtil;

import java.io.File;

/**
 * @author John
 */
public class SMSApplication extends Application {

    public static final String LOG_TAG = "SMSInterceptor";

    private static SMSApplication _instance;


    @NonNull
    public static SMSApplication getInstance() {
        return _instance;
    }

    @Override
    public void onCreate() {

        _instance = this;

        super.onCreate();

        Log.setDefaultLogTag(LOG_TAG);

        PrivatePrefUtil.init(this);

        File logFile = FileUtil.getLogFile();
        if (logFile != null) {
            LogcatUtil.startLogcatAt(this, logFile);
        }

        updateReceiver();
    }

    private void updateReceiver() {

        if (TextUtils.isEmpty(SMSInterceptorSettings.getServerApi()) ||
                TextUtils.isEmpty(SMSInterceptorSettings.getServerBaseUrl())) {
            SMSReceiver.enableReceiver(false);
        }
    }
}
