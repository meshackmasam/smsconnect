package com.citywebtechnologies.smsconnect.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.citywebtechnologies.smsconnect.service.SMSConnectService;

public class SMSConnectReceiver extends BroadcastReceiver {
	
	private Context context;

	public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if (intent.getAction().equals(ACTION)) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] messages = new SmsMessage[pdus.length];
				
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}
				
				for (SmsMessage message : messages) {
					String strMessageFrom = message.getDisplayOriginatingAddress();
					String strMessageBody = message.getDisplayMessageBody();
					long timestamp = message.getTimestampMillis();
					forwardSMSToService(strMessageFrom, strMessageBody,timestamp);
				}
			}
		}
	}
	
	private void forwardSMSToService(String originatingAddress, String messageBody,long timestamp) {
		Intent intent = new Intent(context, SMSConnectService.class);
		intent.putExtra("address", originatingAddress);
		intent.putExtra("sms", messageBody);
		intent.putExtra("date", timestamp);
		//context.startService(intent);
	}

}
