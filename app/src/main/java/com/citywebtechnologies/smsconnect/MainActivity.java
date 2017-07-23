package com.citywebtechnologies.smsconnect;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.citywebtechnologies.smsconnect.db.DBOpenHelper;
import com.citywebtechnologies.smsconnect.db.Datasource;
import com.citywebtechnologies.smsconnect.model.ConnectSMS;
import com.citywebtechnologies.smsconnect.service.DownloadAndSendSMSService;
import com.citywebtechnologies.smsconnect.service.SMSConnectSyncPendingMessagesService;
import com.citywebtechnologies.smsconnect.utils.CommonUtility;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.citywebtechnologies.smsconnect.utils.CommonUtility.getWaitSeconds;

public class MainActivity extends Activity {

    private Context context = this;
    //private List<ConnectSMS> connectSMSList;
    private Datasource ds;
    @Bind(R.id.smsList)
    ListView smsListView;
    @Bind(R.id.tv_progress)
    TextView tv_progress;
    private ConnectSMSListAdapter adapter;
    Button b;
    Boolean running = false;
    private static String TAG = "Main Activity";
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh(intent.getBooleanExtra("refresh",false));
            Log.d(TAG, "refreshing - - " + intent.toString());
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        registerReceiver(broadcastReceiver, new IntentFilter(DownloadAndSendSMSService.BROADCAST_ACTION));
        registerReceiver(broadcastReceiver, new IntentFilter(SMSConnectSyncPendingMessagesService.BROADCAST_ACTION));
        //intent = new Intent(this, DownloadAndSendSMSService.class);
        ds = new Datasource(context);
        ds.open();
        /*smsListView = (ListView) findViewById(R.id.smsList);
        smsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> rootView, View v,
                                    int position, long id) {
                //connectSMSList.get(position);
            }
        });*/

        //bind refresh button click
        b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                refresh(true);
                v.setClickable(true);
            }
        });
        CommonUtility.setAlarm(context);
        startService(new Intent(context, SMSConnectSyncPendingMessagesService.class));
        adapter = new ConnectSMSListAdapter(context, new ArrayList<ConnectSMS>());
        smsListView.setAdapter(adapter);
        //refresh(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //Toast.makeText(this, "ADD!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    protected void refresh(boolean runServices) {



        final String[] permissions = new String[]
                {Manifest.permission.BROADCAST_SMS,Manifest.permission.SEND_SMS};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }




        if (running)
            return;
        running = true;
        if (runServices){
            startService(new Intent(context,DownloadAndSendSMSService.class));
            startService(new Intent(context, SMSConnectSyncPendingMessagesService.class));
        }


        new Thread(new Runnable() {
            public void run() {
                String orderBy = DBOpenHelper.MSG_COLUMN_ID + " DESC";
                final List<ConnectSMS> connectSMSList = ds.findFilterdMessages(null,orderBy);
                final int pendingCount = ds.count(9);
                final int failedCount = ds.count(-1,9);
                final int queueCount = ds.count(99,100);
                final int sentCount = ds.count(1,2);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        adapter.updateAdapter(connectSMSList);

                    }

                });
                    Log.i(TAG, "Found " + connectSMSList.size() + " records");

                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            tv_progress.setText("Sent:" + sentCount + " Queue:" + queueCount +
                                    " Pending:" + pendingCount + " Failed:" + failedCount);
                            if (!CommonUtility.canDownloadMessages(context)){
                                Toast.makeText(context, "Downloading sms has being turned off", Toast.LENGTH_SHORT).show();
                            }

                            if (!CommonUtility.canSendMessages(context)){
                                Toast.makeText(context, "Sending sms has being turned off", Toast.LENGTH_SHORT).show();
                            }

                            if (connectSMSList.size() == 0){
                                Toast.makeText(context, "No records found \n This could mean remote " +
                                                "server has no sms or remote server not queryed yet",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }

                    });

                   Log.d(TAG, "No records found");

                running=false;


            }
        }).start();



        }
                //reload view on resume
        @Override
    protected void onResume() {
        ds.open();
        super.onResume();
            getWaitSeconds(context);
        registerReceiver(broadcastReceiver, new IntentFilter(DownloadAndSendSMSService.BROADCAST_ACTION));
        registerReceiver(broadcastReceiver, new IntentFilter(SMSConnectSyncPendingMessagesService.BROADCAST_ACTION));
        refresh(true);
    }

    @Override
    protected void onPause() {
        ds.close();
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        ds.close();
        super.onDestroy();
    }

}
