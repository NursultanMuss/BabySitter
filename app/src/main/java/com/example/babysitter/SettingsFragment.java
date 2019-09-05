package com.example.babysitter;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    public static final String KEY_PREF_PHONE_NUMBER = "pref_phone_number";
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_pref, rootKey);

    }

//    public void setSummaries(){
//        Preference stylePref = findPreference("pref_phone_number");
//    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences= getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

//        updateSummary();

//        Map<String, ?> preferencesMap = sharedPreferences.getAll();
//        for(Map.Entry<String,?> preferenceEntry : preferencesMap.entrySet()){
//            if(preferenceEntry instanceof EditTextPreference){
//                updateSummary((EditTextPreference) preferenceEntry);
//            }
//        }
    }

    @Override
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Map<String, ?> preferencesMap = sharedPreferences.getAll();
//        Object changedPreference = preferencesMap.get(key);
//        if(preferencesMap.get(key) instanceof EditTextPreference){
//            updateSummary((EditTextPreference) changedPreference);
//        }
        Preference pref = findPreference(key);
        if(pref instanceof EditTextPreference){
            EditTextPreference editTextPreference = (EditTextPreference)pref;
            pref.setSummary(editTextPreference.getText());
            Log.d("pref",editTextPreference.getSummary().toString());
        }
    }

//    private void updateSummary(){
//        Preference pref = findPreference(KEY_PREF_PHONE_NUMBER);
//        if(pref instanceof EditTextPreference){
//            EditTextPreference editTextPreference = (EditTextPreference)pref;
//            pref.setSummary(editTextPreference.getText());
//
//        };
//    }
}
