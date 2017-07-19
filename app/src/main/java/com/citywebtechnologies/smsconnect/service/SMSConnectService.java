package com.citywebtechnologies.smsconnect.service;

import java.util.Calendar;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.citywebtechnologies.smsconnect.R;
import com.citywebtechnologies.smsconnect.RestClient;
import com.citywebtechnologies.smsconnect.db.Datasource;
import com.citywebtechnologies.smsconnect.model.ConnectSMS;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SMSConnectService extends Service {

    private String originatingAddress;
    private String messageBody;
    private long timestamp;
    private Datasource ds;
    private ConnectSMS sms;
    private Context context = this;

    private static String TAG = "Emaktaba Service";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        ds = new Datasource(getApplicationContext());
        ds.open();
    }

    @Override
    public void onDestroy() {
        ds.close();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Entered service onStartCommand method");

        if (intent != null) {
            originatingAddress = intent.getStringExtra("address");
            messageBody = intent.getStringExtra("sms");
            timestamp = intent.getExtras().getLong("date");

            sms = new ConnectSMS();
            sms.setAddress(originatingAddress);
            sms.setMessage(messageBody);
            sms.setSendStatus(0);
            sms.setDateReceived(timestamp);
            sms = ds.createMessage(sms);

            String senderName = originatingAddress;
            final String senderNumber = originatingAddress;

            String keyword = null;
            String[] msgParts = null;
            String msgContent = null;
            if (sms.getMessage().contains(" ")) {
                keyword = sms.getMessage().substring(0,
                        sms.getMessage().indexOf(' '));
                msgParts = sms.getMessage().split(" ", 2);
                if (msgParts.length >= 2)
                    msgContent = msgParts[1];
            } else {
                keyword = sms.getMessage();
                msgContent = keyword;
            }

            RequestParams params = new RequestParams();
            params.put("sender_name", senderName);
            params.put("sender_number", senderNumber);
            params.add("keyword", keyword);
            params.add("message_content", msgContent);
            params.put("date", timestamp + "");

            Log.i(TAG, "Request parametes " + params.toString());

            RestClient.post(RestClient.getAbsoluteUrl(context,"sms.php"), params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    String replyMessage = "";
                    sms.setDateSent(Calendar.getInstance().getTimeInMillis());
                    sms.setSendStatus(1);
                    Log.d(TAG, "message updated with status " + ds.updateMessageSendStatus(sms));
                    try {
                        replyMessage = response.getString("message");
                        if (response.getString("status")
                                .equalsIgnoreCase("success")) {
                            Log.d(TAG, "Success " + response.toString());
                        } else {
                            Log.d(TAG, "Failed " + response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    /*if (!TextUtils.isEmpty(replyMessage))
                        sendReplySMS(senderNumber, replyMessage);*/
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      String responseBody, Throwable e) {
                    Log.d(TAG, "Error code" + statusCode + ", Response body " + responseBody);

                    super.onFailure(statusCode, headers, responseBody, e);
                }
            });

        } else {
            Log.d(TAG, "No intent extras data was empty");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    protected void sendReplySMS(String senderNumber, String replyMessage) {
        Log.d(TAG, "Reply message: " + replyMessage + ", number: " + senderNumber);
        SmsManager.getDefault().sendTextMessage(senderNumber, null, replyMessage, null, null);
    }
}
