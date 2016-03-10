package com.citywebtechnologies.smsconnect.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.citywebtechnologies.smsconnect.service.DownloadAndSendSMSService;
import com.citywebtechnologies.smsconnect.utils.CommonUtility;

public class BootReceiver extends BroadcastReceiver {

	private static String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		CommonUtility.setAlarm(context);
		
		if (CommonUtility.isNetworkConnectionAvailable(context)) {
			context.startService(new Intent(context,
					DownloadAndSendSMSService.class));
		} else {
			Log.i(TAG, "No internet connectivity");
		}
		
	}
}
