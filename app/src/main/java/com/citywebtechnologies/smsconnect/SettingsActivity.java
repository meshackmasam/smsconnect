package com.citywebtechnologies.smsconnect;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.citywebtechnologies.smsconnect.db.DBOpenHelper;
import com.citywebtechnologies.smsconnect.db.Datasource;

/**
 * Created by MESHACK on 7/18/2017.
 */

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        Context context;
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            context = getActivity();
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);



            Preference button = getPreferenceManager().findPreference("key_clear_sms");
            if (button != null) {
                button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        Datasource ds;

                        ds = new Datasource(context);
                        ds.open();

                        String selection = DBOpenHelper.MSG_COLUMN_SENT_STATUS + " = 1 ";
                        int d = ds.delete(selection);
                        arg0.setSummary(d + " messages deleted");
                        ds.close();
                        return true;
                    }
                });
            }

            Preference button2 = getPreferenceManager().findPreference("key_clear_all_sms");
            if (button2 != null) {
                button2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        Datasource ds;

                        ds = new Datasource(context);
                        ds.open();

                        String selection = DBOpenHelper.MSG_COLUMN_SENT_STATUS + " > -2 ";
                        int d = ds.delete(selection);
                        arg0.setSummary(d + " messages deleted");
                        ds.close();
                        return true;
                    }
                });
            }


        }
    }

}
