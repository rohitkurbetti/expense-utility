package com.example.expenseutility.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.example.expenseutility.TransactionQueue;
import com.example.expenseutility.utility.SmsNotificationUtils;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String message = sms.getMessageBody();

                    if (message.contains("Debit INR") || message.contains("INR") || message.contains("XX7794")) {
                        // Save to queue
                        TransactionQueue.getInstance(context).addMessage(message);

                        // Show/update notification
                        SmsNotificationUtils.showInputNotification(context, message, (int) System.currentTimeMillis());
                    }
                }
            }
        }
    }
}
