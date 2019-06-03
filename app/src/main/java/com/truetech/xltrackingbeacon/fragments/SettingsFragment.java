package com.truetech.xltrackingbeacon.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;


import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import com.truetech.xltrackingbeacon.R;

import java.util.regex.Pattern;

import static com.truetech.xltrackingbeacon.Utils.Util.getStringFromPref;
import static com.truetech.xltrackingbeacon.Utils.Util.showDialog;

public class SettingsFragment extends PreferenceFragmentCompat {



    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        initEditText();
    }


    @Override
    public void onStart() {
        super.onStart();
//            setPreferenceScreen(null);
//            addPreferencesFromResource(R.xml.settings);
//            initEditText();
    }

    private void initListenerEditText(final EditTextPreference p, final String title){
        p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newValueStr=((String)newValue).trim();
                try {
                    Integer.parseInt(newValueStr);
                    String newTitle = title+newValueStr;
                    preference.setTitle(newTitle);
                } catch (Exception e) {
                    String title = getString(R.string.title_dialog_preference);
                    String message = getString(R.string.text_dialog_preference);
                    String butOk = getString(R.string.ok);
                    showDialog(getActivity(), title, message, butOk, null);
                    return  false;
                }
                return true;
            }
        });
    }

    private void initEditText(){
        try {
            String strServer=getString(R.string.server);
            String strPort=getString(R.string.port);
            String strRegData=getString(R.string.time_reg_data);
            String strSendData=getString(R.string.time_send_data);
            String strMinDistance=getString(R.string.min_distance_provider);

            EditTextPreference server= (EditTextPreference) findPreference(getString(R.string.key_server));
            server.setTitle(strServer+getStringFromPref(R.string.key_server));


            EditTextPreference port= (EditTextPreference) findPreference(getString(R.string.key_port));
            port.setTitle(strPort+getStringFromPref(R.string.key_port));

            EditTextPreference regData= (EditTextPreference) findPreference(getString(R.string.key_reg_data));
            regData.setTitle(strRegData+getStringFromPref(R.string.key_reg_data));

            EditTextPreference sendData= (EditTextPreference) findPreference(getString(R.string.key_send_data));
            sendData.setTitle(strSendData+getStringFromPref(R.string.key_send_data));

            EditTextPreference minDistance= (EditTextPreference) findPreference(getString(R.string.key_min_distance_provider));
            minDistance.setTitle(strMinDistance+getStringFromPref(R.string.key_min_distance_provider));

            initListenerEditTextServer(server,strServer);
            initListenerEditText(port,strPort);
            initListenerEditText(regData,strRegData);
            initListenerEditText(sendData,strSendData);
            initListenerEditText(minDistance,strMinDistance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initListenerEditTextServer(final EditTextPreference p, final String title){
        p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newValueStr=((String)newValue).trim();
                try {
                    if(!Pattern.matches("([0-9]{1,3}[\\.]){3}[0-9]{1,3}", newValueStr)) throw  new Exception();
                    String newTitle = title+newValueStr;
                    preference.setTitle(newTitle);
                } catch (Exception e) {
                    String title = getString(R.string.title_dialog_preference);
                    String message = getString(R.string.text_dialog_preference);
                    String butOk = getString(R.string.ok);
                    showDialog(getActivity(), title, message, butOk, null);
                    return  false;
                }
                return true;
            }
        });
    }

}
