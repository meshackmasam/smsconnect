package com.citywebtechnologies.smsconnect.receiver;

import com.citywebtechnologies.smsconnect.service.DownloadAndSendSMSService;
import com.citywebtechnologies.smsconnect.utils.CommonUtility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	private static String TAG = "Alarm receiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Alarm has been triggered");
		if (CommonUtility.isNetworkConnectionAvailable(context)) {
			context.startService(new Intent(context, DownloadAndSendSMSService.class));
		} else {
			Log.i(TAG, "No internet connectivity");
		}
	}
}
