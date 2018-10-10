package com.gribanskij.miserplus.settings;


import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.asynctask.AsyncQueryTask;
import com.gribanskij.miserplus.sql_base.MiserContract;


public class RenameCategoryDialog extends DialogFragment {

    private static final String NAME = "name";
    private static final String ID = "param2";

    private static final int TOKEN_CATEGORY_NAME = 50;
    private long _id;
    private String name;


    public RenameCategoryDialog() {
    }

    public static RenameCategoryDialog newInstance(long _ID, String name) {
        RenameCategoryDialog fragment = new RenameCategoryDialog();
        Bundle args = new Bundle();
        args.putLong(ID, _ID);
        args.putString(NAME, name);
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        if (getArguments() != null) {
            _id = getArguments().getLong(ID);
            name = getArguments().getString(NAME);
        }

        if (savedInstanceState != null) {
            _id = savedInstanceState.getLong(ID);
            name = savedInstanceState.getString(NAME);
        }

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_edit, null);
        final EditText editText = view.findViewById(R.id.edittext_name_category);

        return new AlertDialog.Builder(getActivity()).setView(view).setTitle(name).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String newName = editText.getText().toString();
                if (newName.length() == 0) {
                    return;
                }
                Uri uri = MiserContract.NameEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(Long.toString(_id)).build();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MiserContract.NameEntry.Cols.CATEGORY_NAME, newName);
                AsyncQueryTask mTask = new AsyncQueryTask(getContext().getContentResolver(), getContext());
                mTask.startUpdate(TOKEN_CATEGORY_NAME, null, uri, contentValues, null, null);

            }
        })
                .setNegativeButton(android.R.string.cancel, null).create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NAME, name);
        outState.putLong(ID, _id);
    }

}
