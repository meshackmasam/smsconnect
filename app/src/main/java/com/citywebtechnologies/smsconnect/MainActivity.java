package com.citywebtechnologies.smsconnect;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.citywebtechnologies.smsconnect.db.DBOpenHelper;
import com.citywebtechnologies.smsconnect.db.Datasource;
import com.citywebtechnologies.smsconnect.model.ConnectSMS;
import com.citywebtechnologies.smsconnect.service.SMSConnectSyncPendingMessagesService;
import com.citywebtechnologies.smsconnect.utils.CommonUtility;

public class MainActivity extends Activity {

    private Context context = this;
    private List<ConnectSMS> connectSMSList;
    private Datasource ds;
    private ListView smsListView;
    private ConnectSMSListAdapter adapter;

    private static String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smsListView = (ListView) findViewById(R.id.smsList);

        ds = new Datasource(context);
        ds.open();
        String orderBy = DBOpenHelper.MSG_COLUMN_ID + " DESC";
        connectSMSList = ds.findFilterdMessages(null, orderBy);

        if (connectSMSList.size() > 0) {
            Log.i(TAG, "Found " + connectSMSList.size() + " records");
            adapter = new ConnectSMSListAdapter(context, connectSMSList);
            smsListView.setAdapter(adapter);
        } else
            Log.d(TAG, "No records found");

        smsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> rootView, View v,
                                    int position, long id) {
                connectSMSList.get(position);
                Toast.makeText(context, "Not implemented for now",
                        Toast.LENGTH_LONG).show();
            }
        });

        CommonUtility.setAlarm(context);
        startService(new Intent(context, SMSConnectSyncPendingMessagesService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_resend:
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        ds.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        ds.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ds.close();
        super.onDestroy();
    }

}
