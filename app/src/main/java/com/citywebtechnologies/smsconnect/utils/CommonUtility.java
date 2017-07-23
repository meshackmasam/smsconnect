package com.citywebtechnologies.smsconnect.utils;

import java.util.Calendar;

import com.citywebtechnologies.smsconnect.receiver.AlarmReceiver;
import com.citywebtechnologies.smsconnect.service.SMSConnectSyncPendingMessagesService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class CommonUtility {

	public static boolean isNetworkConnectionAvailable(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}

	public static void setAlarm(Context context) {

		Intent intentAlarm = new Intent(context, AlarmReceiver.class);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 1,
				intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.MINUTE, 2);
		//some edits happened here

		Intent intent2 = new Intent(context, BroadcastReceiver.class);

		boolean alarmIsSet = (PendingIntent.getBroadcast(context, 0,intent2, PendingIntent.FLAG_NO_CREATE) != null);

		if (!alarmIsSet){
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), 100 * 1000, alarmIntent);
		}

	}

	public static boolean canDownloadMessages(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_download_sms",false);
	}
	public static boolean canSendMessages(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_send_sms",false);
	}
	public static String getSmsServer(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getString("key_sms_server",Constants.BASE_URL);
	}
	public static long getWaitSeconds(Context context){
		String v = PreferenceManager.getDefaultSharedPreferences(context).getString("key_wait_seconds","10");
		try {
			return Long.parseLong(v) * 1000;
		}catch (Exception ex){
			ex.printStackTrace();
			return 10*1000;
		}
	}
	// check battery level
}
