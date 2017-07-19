package com.citywebtechnologies.smsconnect;

import android.content.Context;
import android.util.Log;

import com.citywebtechnologies.smsconnect.utils.CommonUtility;
import com.citywebtechnologies.smsconnect.utils.Constants;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class RestClient {
    private static String TAG = "Rest Client url";
    public static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params,
                           AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params,
                            AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    public static String getAbsoluteUrl(Context context, String url) {
        String _url = CommonUtility.getSmsServer(context) + "/" + url;
        Log.i(TAG, _url);
        return _url;
    }
}
