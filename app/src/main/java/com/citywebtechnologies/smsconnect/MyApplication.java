package com.citywebtechnologies.smsconnect;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.citywebtechnologies.smsconnect.db.DBOpenHelper;
import com.citywebtechnologies.smsconnect.db.Datasource;
import com.citywebtechnologies.smsconnect.service.DownloadAndSendSMSService;
import com.citywebtechnologies.smsconnect.service.SMSConnectSyncPendingMessagesService;

/**
 * Created by MESHACK on 7/18/2017.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        clearQueue();
        // Do something here.
    }

    public void clearQueue(){
        Datasource ds;

        ds = new Datasource(this);
        ds.open();
        int d = ds.clearQueue();
        if (d > 3){
            Toast.makeText(this, "Application was terminated while " + d
                    + " where on queue, this might lead to sending multiple messages.",
                    Toast.LENGTH_LONG).show();
        }
        ds.close();
    }

    public void refresh(){
        startService(new Intent(this,DownloadAndSendSMSService.class));
        startService(new Intent(this, SMSConnectSyncPendingMessagesService.class));
    }
}