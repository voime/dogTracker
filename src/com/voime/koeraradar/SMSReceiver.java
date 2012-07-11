package com.voime.koeraradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	private String sms_start = "!LOC";
	@Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        Object messages[] = (Object[]) bundle.get("pdus");
        SmsMessage smsMessage[] = new SmsMessage[messages.length];
        for (int n = 0; n < messages.length; n++) {
            smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
        }
        
        String sms=smsMessage[0].getMessageBody();
        // show first message
        
        Toast.makeText(context, "Saadud SMS: " + sms, Toast.LENGTH_LONG).show();
        
        // kui algus on sama mis sms_start siis tuleks koera marker teha.
        
        //ShowMap.setDog(sms);
        
    }
}
