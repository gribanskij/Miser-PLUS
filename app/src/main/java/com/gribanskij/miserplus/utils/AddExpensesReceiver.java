package com.gribanskij.miserplus.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.gribanskij.miserplus.R;

/**
 * Created by SESA175711 on 28.11.2017.
 */

public class AddExpensesReceiver extends BroadcastReceiver {


    public static String ACTION_ADD_EXPENSES = "com.gribanskij.miserplus.ADD_EXPENSES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_ADD_EXPENSES)) {
            NotificationUtils.remindUserAddExpenses(context);
        } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (sharedPreferences.getBoolean(context.getResources().getString(R.string.pref_notification_key), false)) {
                NotificationUtils.setAlarm(context);
            }
        }
    }
}
