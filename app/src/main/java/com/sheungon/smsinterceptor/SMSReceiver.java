package com.sheungon.smsinterceptor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Telephony;

import com.sheungon.smsinterceptor.service.SMSInterceptorService2;

/**
 * Listen for incoming SMS
 *
 * @author John
 */
public class SMSReceiver extends BroadcastReceiver {

    private static final ComponentName SMS_RECEIVER_COMPONENT = new ComponentName(SMSApplication.getInstance(), SMSReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case Telephony.Sms.Intents.SMS_RECEIVED_ACTION: {

                // Forward to service to handle
                SMSApplication app = SMSApplication.getInstance();
                intent.setClass(app, SMSInterceptorService2.class);
                app.startService(intent);

            } break;
        }
    }

    public static boolean isReceiverEnabled() {

        SMSApplication app = SMSApplication.getInstance();
        int status = app.getPackageManager().getComponentEnabledSetting(SMS_RECEIVER_COMPONENT);

        return status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static void enableReceiver(boolean enable) {

        SMSApplication app = SMSApplication.getInstance();

        int newState = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        app.getPackageManager().setComponentEnabledSetting(SMS_RECEIVER_COMPONENT,
                        newState,
                        PackageManager.DONT_KILL_APP);
    }
}
