package com.sheungon.smsinterceptor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.SparseArray;

import com.sheungon.smsinterceptor.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 * @author John
 */
public class SMSInterceptorService extends Service {

    public static final String PDUS = "pdus";

    private final SparseArray<Call<Object>> mRetrofitTasks = new SparseArray<>();


    @Override
    public void onCreate() {
        super.onCreate();

        // TODO Create Retrofit service here
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // TODO release retrofit service here
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final Object[] pdus;
        if (intent == null ||
                (pdus = (Object[]) intent.getSerializableExtra(PDUS)) == null) {
            boolean result = stopSelfResult(startId);
            Log.v("null intent? stop it [" + startId + "] : " + result);
            return START_REDELIVER_INTENT;
        }

        String action = intent.getAction();
        switch (action) {
            case Telephony.Sms.Intents.SMS_RECEIVED_ACTION:
                break;

            default:
                Log.i("Ignored start service with action : " + action);
                stopSelfResult(startId);
                return START_REDELIVER_INTENT;
        }

        if ((flags & START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY) {
            Call<Object> call = mRetrofitTasks.get(startId);
            if (call != null &&
                    !call.isExecuted()) {
                // The task is still pending to be executed or executing
                Log.i("Got task redelivered : " + startId);
                return START_REDELIVER_INTENT;
            }
        }

        SmsMessage[] smsMessages;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        } else {
            int pduCount = pdus.length;
            smsMessages = new SmsMessage[pduCount];

            for (int i = 0; i < pduCount; i++) {
                byte[] pdu = (byte[]) pdus[i];
                //noinspection deprecation
                smsMessages[i] = SmsMessage.createFromPdu(pdu);
            }
        }

        // Log the SMS
        for (SmsMessage smsMessage : smsMessages) {
            String phoneNumber = smsMessage.getDisplayOriginatingAddress();
            Log.d("Got SMS from [" + phoneNumber + "] : " + smsMessage.getDisplayMessageBody());
        }

        /*
        * TODO send SMS to server here. Remember to call stopSelfResult(startId) after sent the SMS to server
        * smsMessages to be sent
        * */
        /*Call<Object> call = null;
        call.enqueue(new SendSMSCallback(startId));
        mRetrofitTasks.put(startId, call);*/
        // FIXME remove this after implemented Retrofit
        boolean result = stopSelfResult(startId);
        if (result) {
            Log.d("SMSInterceptorService stopped.");
        }

        return START_REDELIVER_INTENT;
    }


    /////////////////////////////
    // Class and interface
    /////////////////////////////
    private class SendSMSCallback implements Callback<Object> {

        private final int mStartId;

        SendSMSCallback(int startId) {
            mStartId = startId;
        }

        @Override
        public void onResponse(Call<Object> call, Response<Object> response) {

            Log.e("SMS Send to server : " + mStartId);
            stopSelfService();
        }

        @Override
        public void onFailure(Call<Object> call, Throwable t) {

            Log.e("Error on sending SMS to server : " + mStartId + ", error : " + t.getMessage());
            stopSelfService();
        }

        private void stopSelfService() {

            mRetrofitTasks.remove(mStartId);
            boolean result = stopSelfResult(mStartId);
            if (result) {
                Log.d("SMSInterceptorService stopped.");
            }
        }
    }
}
