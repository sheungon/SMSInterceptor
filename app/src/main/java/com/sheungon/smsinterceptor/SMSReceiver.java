package com.sheungon.smsinterceptor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.sheungon.smsinterceptor.util.Log;

/**
 * Listen for incoming SMS
 *
 * @author John
 */
public class SMSReceiver extends BroadcastReceiver {

    public static final String PDUS = "pdus";


    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case Telephony.Sms.Intents.SMS_RECEIVED_ACTION: {

                SmsMessage[] smsMessages;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                } else {
                    final Bundle bundle = intent.getExtras();
                    if (bundle != null) {

                        final Object[] pdus = (Object[]) intent.getSerializableExtra(PDUS);

                        int pduCount = pdus.length;
                        smsMessages = new SmsMessage[pduCount];

                        for (int i = 0; i < pduCount; i++) {
                            byte[] pdu = (byte[]) pdus[i];
                            //noinspection deprecation
                            smsMessages[i] = SmsMessage.createFromPdu(pdu);
                        }
                    } else {
                        Log.e("No bundle SMS received action?!");
                        return;
                    }
                }

                // Log the SMS
                for (SmsMessage smsMessage : smsMessages) {
                    String phoneNumber = smsMessage.getDisplayOriginatingAddress();
                    Log.d("Got SMS from [" + phoneNumber + "] : " + smsMessage.getDisplayMessageBody());
                }

                // TODO send the SMS to server

            } break;
        }
    }
}
