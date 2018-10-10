package com.gribanskij.miserplus.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.gribanskij.miserplus.R;


/**
 * Created by santy on 29.09.2017.
 */

public class ChangeLanguageDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.change_language_info, null);
        return new AlertDialog.Builder(getActivity()).
                setView(v).setTitle(R.string.title_language_dialog).
                setPositiveButton(android.R.string.ok, null).create();
    }
}
