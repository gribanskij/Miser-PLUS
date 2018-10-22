package com.gribanskij.miserplus.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.sql_base.MiserContract;
import com.gribanskij.miserplus.utils.NotificationUtils;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();

        if (appCompatActivity.getSupportActionBar() != null) {
            ActionBar actionBar = appCompatActivity.getSupportActionBar();
            actionBar.setTitle(R.string.action_settings);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_miser, rootKey);


        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);

            if (preference instanceof ListPreference) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            } else {
                if (preference instanceof PreferenceCategory) {
                    PreferenceCategory category = (PreferenceCategory) preference;
                    int count_1 = category.getPreferenceCount();
                    for (int a = 0; a < count_1; a++) {
                        Preference preference_ = category.getPreference(a);

                        if (preference_ instanceof ListPreference) {
                            String value = sharedPreferences.getString(preference_.getKey(), "");
                            setPreferenceSummary(preference_, value);
                        }
                    }
                }
            }
        }
    }

    private void setPreferenceSummary(Preference p, String value) {

        if (p instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) p;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }


    @Override
    public void onNavigateToScreen(PreferenceScreen pref) {
        super.onNavigateToScreen(pref);
        Fragment fragment = new SettingsFragment();
        int switch_title_cursor = 0;


        if (pref.getKey().equals(getString(R.string.prefscreen_account_key))) {
            fragment = new RenameFragment();
            switch_title_cursor = MiserContract.TYPE_ACCOUNTS;
        }

        if (pref.getKey().equals(getString(R.string.prefscreen_cost_key))) {
            fragment = new RenameFragment();
            switch_title_cursor = MiserContract.TYPE_COST;
        }
        if (pref.getKey().equals(getString(R.string.prefscreen_income_key))) {
            fragment = new RenameFragment();
            switch_title_cursor = MiserContract.TYPE_INCOME;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putInt(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, switch_title_cursor);
        fragment.setArguments(args);
        ft.replace(R.id.frame_for_fragment, fragment, pref.getKey());
        ft.addToBackStack(null);
        ft.setTransition(TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference preference = findPreference(key);
        if (preference != null) {
            if (preference instanceof ListPreference) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
        if (key.equals(getString(R.string.pref_language_key))) {

            DialogFragment dialog = new ChangeLanguageDialog();
            FragmentManager fragmentManager = getFragmentManager();
            dialog.show(fragmentManager, null);

        }
        if (key.equals(getString(R.string.pref_notification_key))) {
            if (sharedPreferences.getBoolean(getString(R.string.pref_notification_key), false)) {
                NotificationUtils.setAlarm(getActivity());
            } else {
                NotificationUtils.disableAlarm(getActivity());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
