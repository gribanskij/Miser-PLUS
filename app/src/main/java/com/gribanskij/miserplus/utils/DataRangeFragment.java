package com.gribanskij.miserplus.utils;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gribanskij.miserplus.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class DataRangeFragment extends DialogFragment {

    private static final String LOG_TAG = DataRangeFragment.class.getSimpleName();

    private static final int REQEST_DATE_FROM = 0;
    private static final int REQEST_DATE_TO = 1;


    private TextView dateViewFrom;
    private TextView dateViewTo;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    public DataRangeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.date_range_fragment, container, false);


        dateViewFrom = v.findViewById(R.id.date_view_from);
        dateViewTo = v.findViewById(R.id.date_view_to);

        Button buttonOK = v.findViewById(R.id.ok_range);
        Button buttonCancel = v.findViewById(R.id.cancel_range);

        ImageButton buttonFrom = v.findViewById(R.id.date_from);
        ImageButton buttonTo = v.findViewById(R.id.date_to);

        Date date = new Date();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        long beginDate = sharedPreferences.getLong(getString(R.string.pref_start_date_calendar_key), date.getTime());
        long endDate = sharedPreferences.getLong(getString(R.string.pref_final_date_calendar_key), date.getTime());

        dateViewFrom.setText(dateFormat.format(new Date(beginDate)));
        dateViewTo.setText(dateFormat.format(new Date(endDate)));

        buttonFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DialogFragment fragment = new DatePickerFragment();
                fragment.setTargetFragment(DataRangeFragment.this, REQEST_DATE_FROM);
                fragment.show(manager, LOG_TAG);
            }
        });

        buttonTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DialogFragment fragment = new DatePickerFragment();
                fragment.setTargetFragment(DataRangeFragment.this, REQEST_DATE_TO);
                fragment.show(manager, LOG_TAG);
            }
        });


        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResult(Activity.RESULT_OK);
                dismiss();
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQEST_DATE_FROM) {
            Long mDate = data.getLongExtra(DatePickerFragment.EXTRA_DATE, 0);
            dateViewFrom.setText(dateFormat.format(new Date(mDate)));
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(getString(R.string.pref_start_date_calendar_key), mDate);
            editor.apply();

        } else {
            if (requestCode == REQEST_DATE_TO) {
                Long mDate = data.getLongExtra(DatePickerFragment.EXTRA_DATE, 0);
                dateViewTo.setText(dateFormat.format(new Date(mDate)));
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(getString(R.string.pref_final_date_calendar_key), mDate);
                editor.apply();
            }
        }
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
        }
    }
}
