package com.citywebtechnologies.smsconnect.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.citywebtechnologies.smsconnect.RestClient;
import com.citywebtechnologies.smsconnect.db.Datasource;
import com.citywebtechnologies.smsconnect.model.ConnectSMS;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

public class DownloadAndSendSMSService extends Service {

    private Datasource ds;
    private ConnectSMS sms;

    private static final String TAG = "Download service";

    public DownloadAndSendSMSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
        Log.d(TAG, "Entered download servie onStartCommand method");

        RequestParams params = new RequestParams();
        params.add("cmd", "fetch");
        params.add("limit", "10");
        params.put("offset", "0");

        RestClient.post("sms.php", params,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        Log.d(TAG,"SMS to send "+response.toString());

                        try {
                            JSONArray smsJarry = response.getJSONArray("messages");
                            List<ConnectSMS> connectSMSes = getMessagesFromJsonResponse(smsJarry);

                            for (ConnectSMS sms : connectSMSes) {
                                Log.d(TAG, "SMS => " + sms.toString());
                                sms.setAddress("0"+sms.getAddress());
                                sms.setSendStatus(0);
                                sms.setDateReceived(Calendar.getInstance().getTimeInMillis());
                                ds.createMessage(sms);
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

        return super.onStartCommand(intent, flags, startId);
    }


    private List<ConnectSMS> getMessagesFromJsonResponse(JSONArray jsonArray) throws JSONException {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss");
        gsonBuilder.setFieldNamingStrategy(new FieldNamingStrategy() {

            @Override
            public String translateName(Field field) {
                if (field.getName().equals("id"))
                    return "smsId";

                if (field.getName().equals("address"))
                    return "smsRecipient";

                if (field.getName().equals("message"))
                    return "smsBody";

                return field.getName();
            }
        });
        Gson gson = gsonBuilder.create();
        Type listType = new TypeToken<List<ConnectSMS>>() {
        }.getType();
        return gson.fromJson(jsonArray.toString(), listType);
    }
}
