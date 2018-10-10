package com.gribanskij.miserplus.dashboard_screen;


import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.asynctask.AsyncQueryTask;
import com.gribanskij.miserplus.sql_base.MiserContract;


public class AccountDialog extends DialogFragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = AccountDialog.class.getSimpleName();
    private static final int TOKEN_SIMPLE_UPDATE = 40;
    private static final int ACCOUNT_CATEGORY_NAME_LOADER = 101;
    private static final String ARG1 = "id";
    private static final String ARG2 = "name";


    private EditText mSumEdit;
    private int account_id;
    private SimpleCursorAdapter simpleCursorAdapter;

    public static AccountDialog create(int account_id, String name) {
        AccountDialog accountDialog = new AccountDialog();
        Bundle arg = new Bundle();
        arg.putInt(ARG1, account_id);
        arg.putString(ARG2, name);
        accountDialog.setArguments(arg);
        return accountDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String accountCategoryName;

        if (getArguments() != null) {
            account_id = getArguments().getInt(ARG1);
            accountCategoryName = getArguments().getString(ARG2);
        } else {
            account_id = 0;
            accountCategoryName = "";
        }


        View view = LayoutInflater.from(getActivity()).inflate(R.layout.account_dialog, null);
        final Spinner spinner = view.findViewById(R.id.spinner_account_transfer);
        final CheckBox checkBox = view.findViewById(R.id.checkbox_account);
        mSumEdit = view.findViewById(R.id.account_sum_edit);

        simpleCursorAdapter = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item, null, new String[]{"name"}, new int[]{android.R.id.text1}, 0);
        simpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(simpleCursorAdapter);
        spinner.setEnabled(false);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    spinner.setEnabled(true);
                    mSumEdit.setHint(R.string.summ_transfer);
                } else {
                    spinner.setEnabled(false);
                    mSumEdit.setHint(getResources().getString(R.string.account_hint));
                }
            }
        });

        return new AlertDialog.Builder(getActivity()).setView(view).setTitle(accountCategoryName).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                float mSum;

                if (mSumEdit.getText().toString().equals("")) {
                    mSum = 0F;
                } else {
                    mSum = Float.parseFloat(mSumEdit.getText().toString());
                }

                if (checkBox.isChecked()) {
                    int mAccount_source = spinner.getSelectedItemPosition();

                    AddThread addThread = new AddThread(getContext().getContentResolver(), account_id, mSum, mAccount_source);
                    addThread.start();

                } else {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT, mSum);
                    String selection = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
                    String[] selectionArg = new String[]{Integer.toString(account_id)};

                    AsyncQueryTask mAsyncTask = new AsyncQueryTask(getContext().getContentResolver(), getContext());
                    mAsyncTask.startUpdate(TOKEN_SIMPLE_UPDATE, null,
                            MiserContract.AccountEntry.CONTENT_URI,
                            contentValues, selection, selectionArg);
                }

            }
        })
                .setNegativeButton(android.R.string.cancel, null).create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ACCOUNT_CATEGORY_NAME_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MiserContract.NameEntry.CONTENT_URI;
        String[] projection = new String[]{MiserContract.NameEntry.Cols._ID, MiserContract.NameEntry.Cols.CATEGORY_NAME};
        String selection =
                MiserContract.NameEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.NameEntry.Cols.CATEGORY_ID + " < ?";
        String[] selectionArg = new String[]{Integer.toString(MiserContract.TYPE_ACCOUNTS), getString(R.string.pref_account_quantity_4_value)};
        String sortOrder = null;
        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        simpleCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        simpleCursorAdapter.swapCursor(null);
    }

    private static class AddThread extends Thread {

        private static final String LOG_TAG = AccountDialog.AddThread.class.getSimpleName();
        private final int ACCOUNT_SUM = 0;
        private ContentResolver contentResolver;
        private int accountId;
        private float sumForAdd;
        private int accountIdSource;


        private AddThread(ContentResolver contentResolver, int accountId, float sumForAdd, int accountIdSource) {
            this.contentResolver = contentResolver;
            this.accountId = accountId;
            this.sumForAdd = sumForAdd;
            this.accountIdSource = accountIdSource;
        }

        @Override
        public void run() {

            final int SUM_ACCOUNT = 0;
            Uri uri = MiserContract.AccountEntry.CONTENT_URI;

            float sumSource;
            float oldSum;

            String[] projection = new String[]{MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT};
            String selection = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
            String[] selectionArgs = new String[]{Integer.toString(accountIdSource)};
            Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                sumSource = cursor.getFloat(SUM_ACCOUNT);
                cursor.close();
            } else return;


            if (sumSource < sumForAdd) {
                sumForAdd = sumSource;
                sumSource = 0;
            } else {
                sumSource = sumSource - sumForAdd;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT, sumSource);
            selectionArgs = new String[]{Integer.toString(accountIdSource)};
            String where = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
            contentResolver.update(uri, contentValues, where, selectionArgs);
            contentValues.clear();


            projection = new String[]{MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT};
            selection = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
            selectionArgs = new String[]{Integer.toString(accountId)};
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                oldSum = cursor.getFloat(SUM_ACCOUNT);
                cursor.close();
            } else return;

            sumForAdd = oldSum + sumForAdd;

            contentValues.put(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT, sumForAdd);
            selectionArgs = new String[]{Integer.toString(accountId)};
            where = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
            contentResolver.update(uri, contentValues, where, selectionArgs);
        }
    }
}
