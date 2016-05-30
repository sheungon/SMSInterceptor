package com.sheungon.smsinterceptor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.SparseArray;

import com.sheungon.smsinterceptor.service.dto.SMS;
import com.sheungon.smsinterceptor.service.dto.SMSGatewayReturnMessage;
import com.sheungon.smsinterceptor.util.Log;
import com.sheungon.smsinterceptor.util.SSLSelfSigningClientBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 * @author John
 */
public class SMSInterceptorService extends Service {

    public static final String PDUS = "pdus";

    private final SparseArray<Call<SMSGatewayReturnMessage>> mRetrofitTasks = new SparseArray<>();

    private SMSGatewayService mSMSGatewayService = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create Retrofit service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SMSInterceptorSettings.getServerBaseUrl())
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(SSLSelfSigningClientBuilder.createClient())
                .build();

        mSMSGatewayService = retrofit.create(SMSGatewayService.class);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("SMSInterceptorService onDestroy.");
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
            Call<SMSGatewayReturnMessage> call = mRetrofitTasks.get(startId);
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
        Map<String, StringBuilder> smsMap = new HashMap<>();
        for (SmsMessage smsMessage : smsMessages) {

            String phoneNumber = smsMessage.getDisplayOriginatingAddress();
            String smsPartialBody = smsMessage.getDisplayMessageBody();

            StringBuilder smsBodyBuilder = smsMap.get(phoneNumber);
            if (smsBodyBuilder == null) {
                smsBodyBuilder = new StringBuilder();
                smsMap.put(phoneNumber, smsBodyBuilder);
            }
            smsBodyBuilder.append(smsPartialBody);

            Log.d("Got SMS from [" + phoneNumber + "] : " + smsPartialBody);
        }

        Set<String> keySet = smsMap.keySet();
        if (keySet.isEmpty()) {
            Log.w("No sms ?!");
            stopSelfResult(startId);
        } else {

            Iterator<String> iterator = keySet.iterator();
            String firstPhoneNumber = iterator.next();
            forwardSMS(startId, smsMap, firstPhoneNumber);
        }

        return START_REDELIVER_INTENT;
    }

    private void forwardSMS(int startId,
                            @NonNull Map<String, StringBuilder> smsMap,
                            @NonNull String phoneNumber) {

        String smsBody = smsMap.remove(phoneNumber).toString();
        SMS incomingMessage = new SMS(phoneNumber, smsBody);
        incomingMessage.setAccountNumber(SMSInterceptorSettings.getAccountNumber());
        incomingMessage.setBankCode(SMSInterceptorSettings.getBankCode());

        // Send SMS to server here. stopSelfResult(startId) will be called after sent the SMS to server
        Call<SMSGatewayReturnMessage> call = mSMSGatewayService.receiveSms(SMSInterceptorSettings.getServerApi(), incomingMessage);
        mRetrofitTasks.put(startId, call);
        call.enqueue(new SendSMSCallback(startId, smsMap));
    }


    /////////////////////////////
    // Class and interface
    /////////////////////////////
    private class SendSMSCallback implements Callback<SMSGatewayReturnMessage> {

        private final int mStartId;
        @NonNull
        private final Map<String, StringBuilder> mPendingSmsMap;

        SendSMSCallback(int startId, @NonNull Map<String, StringBuilder> pendingSmsMap) {
            mStartId = startId;
            mPendingSmsMap = pendingSmsMap;
        }

        @Override
        public void onResponse(Call<SMSGatewayReturnMessage> call, Response<SMSGatewayReturnMessage> response) {

            Log.d("SMS Sent. startId[" + mStartId + "]");
            stopSelfService();
        }

        @Override
        public void onFailure(Call<SMSGatewayReturnMessage> call, Throwable t) {

            Log.e("Error on sending SMS to server ID[" + mStartId + "] error : " + t.getMessage());
            stopSelfService();
        }

        private void stopSelfService() {

            Set<String> keySet = mPendingSmsMap.keySet();
            if (keySet.isEmpty()) {
                mRetrofitTasks.remove(mStartId);
                boolean result = stopSelfResult(mStartId);
                if (result) {
                    Log.d("SMSInterceptorService stopped.");
                }
            } else {
                Iterator<String> iterator = keySet.iterator();
                String firstPhoneNumber = iterator.next();
                forwardSMS(mStartId, mPendingSmsMap, firstPhoneNumber);
            }
        }
    }

    private class NullOnEmptyConverterFactory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return new Converter<ResponseBody, Object>() {
                @Override
                public Object convert(ResponseBody body) throws IOException {
                    if (body.contentLength() == 0) return null;
                    return delegate.convert(body);
                }
            };
        }
    }
}
