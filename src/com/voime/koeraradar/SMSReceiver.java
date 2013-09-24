package com.voime.koeraradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String str = "sms";
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("SMS_RE");
            broadcastIntent.putExtra("sms_re", str);
            context.sendBroadcast(broadcastIntent);
        }
    }
}