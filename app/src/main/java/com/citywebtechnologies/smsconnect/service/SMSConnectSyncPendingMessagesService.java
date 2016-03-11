package com.citywebtechnologies.smsconnect.service;

import java.util.Calendar;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import com.citywebtechnologies.smsconnect.RestClient;
import com.citywebtechnologies.smsconnect.db.DBOpenHelper;
import com.citywebtechnologies.smsconnect.db.Datasource;
import com.citywebtechnologies.smsconnect.model.ConnectSMS;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SMSConnectSyncPendingMessagesService extends Service {

    private Datasource ds;
    private ConnectSMS sms;

    private static String TAG = "Emaktaba Sync service";

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
        Log.d(TAG, "Entered sms sync service onStartCommand method");

        String selection = DBOpenHelper.MSG_COLUMN_SENT_STATUS + " = 0";
        String orderBy = DBOpenHelper.MSG_COLUMN_ID + " DESC";
        List<ConnectSMS> pendingSms = ds.findFilterdMessages(selection, orderBy);
        int pendingSMSCount = pendingSms.size();
        if (pendingSMSCount > 0) {
            for (int i = 0; i < pendingSMSCount; i++) {

                final long smsId = pendingSms.get(i).getId();
                final long smsId2 = pendingSms.get(i).getRec();
                sms = new ConnectSMS();
                sms.setId(pendingSms.get(i).getId());
                sms.setAddress(pendingSms.get(i).getAddress());
                sms.setMessage(pendingSms.get(i).getMessage());
                sms.setSendStatus(pendingSms.get(i).getSentStatus());
                sms.setDateReceived(pendingSms.get(i).getDateReceived());

                String senderName = sms.getAddress();
                final String senderNumber = sms.getAddress();
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
                sendMessage(sms.getAddress(), sms.getMessage());
                ds.updateMessageSendStatus(sms);
                updateServerSendStatus(smsId,smsId2);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else
            Log.d(TAG, "All is okay");

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateServerSendStatus(final long smsId,final long smsId2) {
        RequestParams params = new RequestParams();
        params.add("cmd", "update");
        params.add("status", "sent");
        params.put("sms_id", "" + smsId2);

        Log.d(TAG, "Request parameters " + params.toString());

        RestClient.post("sms.php", params,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        String replyMessage = "";
                        sms = new ConnectSMS();
                        sms.setId(smsId);
                        sms.setDateSent(Calendar.getInstance().getTimeInMillis());
                        sms.setSendStatus(1);
                        Log.d(TAG, "Update status from Stock Sync Pending " + ds.updateMessageSendStatus(sms));
                        try {
                            if (response.getString("status").equalsIgnoreCase("success")) {
                                Log.d(TAG, "Success " + response.toString());
                                ds.updateMessageSendStatus(sms);
                            } else {
                                Log.d(TAG, "Failed " + response.toString());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable e) {
                        super.onFailure(statusCode, headers, responseBody, e);
                        Log.d(TAG, "Error code" + statusCode + ", Response body " + responseBody);
                    }
                });
    }

    protected void sendMessage(String senderNumber, String replyMessage) {
        Log.d(TAG, "Reply message: " + replyMessage + ", number: " + senderNumber);
        SmsManager.getDefault().sendTextMessage(senderNumber, null, replyMessage, null, null);
    }
}
