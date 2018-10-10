package com.gribanskij.miserplus.utils;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupManager;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;


/**
 * Created by sesa175711 on 11.11.2016.
 */
public class TheBackupAgent extends BackupAgentHelper {

    public static final String DB_NAME = "miserBase.db";

    public static void requestBackup(Context context) {
        BackupManager bm = new BackupManager(context);
        bm.dataChanged();
    }

    @Override
    public void onCreate() {
        FileBackupHelper fileBackupHelper = new FileBackupHelper(this, "../databases/" + DB_NAME);
        addHelper(DB_NAME, fileBackupHelper);
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, getPackageName() + "_preferences");
        addHelper("prefs", helper);
    }
}
