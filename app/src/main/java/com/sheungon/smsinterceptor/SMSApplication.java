package com.sheungon.smsinterceptor;

import android.app.Application;
import android.support.annotation.NonNull;

import com.sheungon.smsinterceptor.util.PrivatePrefUtil;

/**
 * @author John
 */
public class SMSApplication extends Application {

    private static SMSApplication _instance;


    @NonNull
    public static SMSApplication getInstance() {
        return _instance;
    }

    @Override
    public void onCreate() {

        _instance = this;

        super.onCreate();

        PrivatePrefUtil.init(this);
    }
}
