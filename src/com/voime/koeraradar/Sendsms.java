package com.voime.koeraradar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Sendsms extends Activity {
	public static final String PREFS_NAME = "koeraradar";
	public String rihma_nr;
	private String sms = "?LOC";
	private String multisms = "?TRC"; // ?TRC_05_10 saadab tagasi 10 korda iga 5 minuti tagant
	public EditText mKorda;
	public EditText mAeg;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendsms);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		rihma_nr = prefs.getString("number", null);
		if (rihma_nr == null) {	
			Toast.makeText(getApplicationContext(), "SMS ei saadeta, kuna pole rihma numbrit määratud!", Toast.LENGTH_LONG).show();
		}
    	mKorda = (EditText)findViewById(R.id.editText1);
    	mAeg = (EditText)findViewById(R.id.editText2); 
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    String korda = settings.getString("mKorda", null);
	    if (korda != null) {
	    	mKorda.setText(korda);
	    }
	    String aeg = settings.getString("mAeg", null);
	    if (aeg != null) {
	        mAeg.setText(aeg);
	    }
	}
	// SMS sõnumi reaalne saatmine
	public void sending (String text) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(rihma_nr, null, text, null, null);
		Toast.makeText(getApplicationContext(), "Sending SMS: " + text, Toast.LENGTH_LONG).show();
	}
	// kui vajutati ühekordse SMS-i nuppu
    public void sendMessage(View view) {
        // Do something in response to button click
		if (rihma_nr != null) {			;
			Toast.makeText(getApplicationContext(), "Saadan ühe SMS-i koera asukoha uuendamiseks\n" + rihma_nr, Toast.LENGTH_LONG).show();
			// sõnumi saatmine
			sending(sms);
		}
    }
    // kui vajutati mitmekordse SMS-i nuppu
    public void sendMulti(View view) {
        // Do something in response to button click
 	
    	String text;
		if (rihma_nr != null) {
			Toast.makeText(getApplicationContext(), "Saadan mitmekordse SMS-i koera asukoha uuendamiseks\n" + rihma_nr, Toast.LENGTH_LONG).show();
			// sõnumi saatmine
			text=multisms + '_' + mAeg.getText().toString() + '_' + mKorda.getText().toString();
			sending(text);
		}
    }
    protected void onStop(){
       super.onStop();
       // väljade salvestamine
       SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
       SharedPreferences.Editor editor = settings.edit();
       editor.putString("mKorda",  mKorda.getText().toString());
       editor.putString("mAeg",  mAeg.getText().toString());       
       editor.commit();
     }
}