package com.gribanskij.miserplus.add_screen;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.asynctask.AsyncQueryTask;
import com.gribanskij.miserplus.sql_base.MiserContract;
import com.gribanskij.miserplus.utils.DatePickerFragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.gribanskij.miserplus.AbstractActivity.BUDGET_ADD;
import static com.gribanskij.miserplus.AbstractActivity.CATEGORY_ID;
import static com.gribanskij.miserplus.AbstractActivity.MONTH;
import static com.gribanskij.miserplus.AbstractActivity.TYPE;


public class AddFragment extends BaseFragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = AddFragment.class.getSimpleName();

    public static final int REQEST_DATE = 0;
    public static final int TOKEN_INSERT = 20;
    public static final int TOKEN_INSERT_BUDGET = 30;
    private static final int ACCOUNT_CATEGORY_NAME_LOADER = 15;
    private static final int INCOME_CATEGORY_NAME_LOADER = 25;
    private static final int COST_CATEGORY_NAME_LOADER = 35;

    private static final int ONLY_THIS_MONTH = 0;

    private int mType;
    private int mCategoryID;
    private long mDate;
    private boolean isBudget;
    private int mMonth;
    private SimpleCursorAdapter adapter_category_cost;
    private SimpleCursorAdapter adapter_category_income;
    private SimpleCursorAdapter adapter_category_account;
    private ArrayAdapter adapter_each_month;
    private TextView textView_date;
    private Spinner category_spinner;
    private Spinner type_spinner;
    private Spinner account_spinner;
    private EditText sum_edittext;
    private EditText descrip_edittext;


    public AddFragment() {
    }


    public static AddFragment newInstance(int type, int categoryID, boolean isBudget, int month) {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putInt(CATEGORY_ID, categoryID);
        args.putBoolean(BUDGET_ADD, isBudget);
        args.putInt(MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TYPE, mType);
        outState.putInt(CATEGORY_ID, mCategoryID);
        outState.putBoolean(BUDGET_ADD, isBudget);
        outState.putInt(MONTH, mMonth);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mType = savedInstanceState.getInt(TYPE);
            mCategoryID = savedInstanceState.getInt(CATEGORY_ID);
            isBudget = savedInstanceState.getBoolean(BUDGET_ADD, false);
            mMonth = savedInstanceState.getInt(MONTH, Calendar.JANUARY);
        } else {
            if (getArguments() != null) {
                mType = getArguments().getInt(TYPE);
                mCategoryID = getArguments().getInt(CATEGORY_ID);
                isBudget = getArguments().getBoolean(BUDGET_ADD);
                mMonth = getArguments().getInt(MONTH);
            }
        }

        adapter_category_cost = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                null, new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME}, new int[]{android.R.id.text1}, 0);
        adapter_category_cost.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter_category_income = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                null, new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME}, new int[]{android.R.id.text1}, 0);
        adapter_category_income.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter_category_account = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                null, new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME}, new int[]{android.R.id.text1}, 0);
        adapter_category_account.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter_each_month = ArrayAdapter.createFromResource(getContext(), R.array.budget_interval,
                android.R.layout.simple_spinner_item);
        adapter_each_month.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(ACCOUNT_CATEGORY_NAME_LOADER, null, this);
        getLoaderManager().initLoader(INCOME_CATEGORY_NAME_LOADER, null, this);
        getLoaderManager().initLoader(COST_CATEGORY_NAME_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.add_main, container, false);
        ImageButton ok_button = v.findViewById(R.id.save_button);
        sum_edittext = v.findViewById(R.id.sum_edittext_);
        descrip_edittext = v.findViewById(R.id.descrip_edittext);
        textView_date = v.findViewById(R.id.calendar_textview);
        type_spinner = v.findViewById(R.id.type_spiner);
        category_spinner = v.findViewById(R.id.category_spiner);
        account_spinner = v.findViewById(R.id.account_spiner);
        TextView add_type = v.findViewById(R.id.add_type);
        add_type.setText(getString(R.string.title_add_card));
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.Create);
            bar.setDisplayHomeAsUpEnabled(true);
        }

        mDate = new Date().getTime();
        textView_date.setText(DateFormat.getDateInstance().format(new Date(mDate)));


        if (isBudget) {
            add_type.setText(getString(R.string.title_add_card_budget));
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, mMonth);
            mDate = calendar.getTimeInMillis();
            textView_date.setText(DateFormat.getDateInstance().format(new Date(mDate)));
        }

        type_spinner.setSelection(mType);


        if (isBudget) {
            account_spinner.setAdapter(adapter_each_month);
        } else {
            account_spinner.setAdapter(adapter_category_account);
        }

        if (mType == MiserContract.TYPE_COST) {
            category_spinner.setAdapter(adapter_category_cost);
        } else {
            category_spinner.setAdapter(adapter_category_income);
        }

        textView_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = new DatePickerFragment();
                dialog.setTargetFragment(AddFragment.this, REQEST_DATE);
                dialog.show(manager, LOG_TAG);
            }
        });

        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == MiserContract.TYPE_INCOME) {
                    category_spinner.setAdapter(adapter_category_income);
                } else {
                    category_spinner.setAdapter(adapter_category_cost);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int mType = type_spinner.getSelectedItemPosition();
                int mCategory = category_spinner.getSelectedItemPosition();
                int mAccount = account_spinner.getSelectedItemPosition();
                TextView name = (TextView) account_spinner.getSelectedView();
                String mAccount_name = name.getText().toString();

                name = (TextView) category_spinner.getSelectedView();
                String mCategory_name = name.getText().toString();


                float mSum;

                if (sum_edittext.getText().toString().length() == 0) {
                    mSum = 0F;
                } else {
                    mSum = Float.valueOf(sum_edittext.getText().toString());
                }
                String mDescription = descrip_edittext.getText().toString();
                if (mDescription.length() == 0) {
                    mDescription = getResources().getString(R.string.no_comments);
                }
                ContentValues contentValues = new ContentValues();

                if (!isBudget) {
                    contentValues.put(MiserContract.TransactionEntry.Cols.TYPE, mType);
                    contentValues.put(MiserContract.TransactionEntry.Cols.CATEGORY_ID, mCategory);
                    contentValues.put(MiserContract.TransactionEntry.Cols.DESCRIPTION, mDescription);
                    contentValues.put(MiserContract.TransactionEntry.Cols.AMOUNT, mSum);
                    contentValues.put(MiserContract.TransactionEntry.Cols.DATE, mDate);
                    contentValues.put(MiserContract.TransactionEntry.Cols.ACCOUNT, mAccount);
                    contentValues.put(MiserContract.TransactionEntry.Cols.ACCOUNT_NAME, mAccount_name);
                    contentValues.put(MiserContract.TransactionEntry.Cols.CATEGORY_NAME, mCategory_name);

                    AsyncQueryTask mAsyncTask = new AsyncQueryTask(getContext().getContentResolver(), getContext());
                    mAsyncTask.startInsert(TOKEN_INSERT, null, MiserContract.TransactionEntry.CONTENT_URI, contentValues);


                    Thread mThread = new AddThread(getContext().getContentResolver(), mAccount, mSum, mType);
                    mThread.start();

                } else {

                    if (mAccount == ONLY_THIS_MONTH) {

                        contentValues.clear();
                        contentValues.put(MiserContract.BudgetEntry.Cols.TYPE, mType);
                        contentValues.put(MiserContract.BudgetEntry.Cols.CATEGORY_ID, mCategory);
                        contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_DESCRIPTION, mDescription);
                        contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_SUM, mSum);
                        contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_INSERT_DATE, mDate);
                        contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_MONTH, mMonth);

                        AsyncQueryTask mAsyncTask = new AsyncQueryTask(getContext().getContentResolver(), getContext());
                        mAsyncTask.startInsert(TOKEN_INSERT_BUDGET, null, MiserContract.BudgetEntry.CONTENT_URI, contentValues);
                    } else {
                        Thread thread = new AddBudgetItems(getContext().getContentResolver(), mCategory, mSum, mType, mDescription);
                        thread.start();
                    }
                }
                getActivity().finish();
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQEST_DATE) {
            mDate = data.getLongExtra(com.gribanskij.miserplus.utils.DatePickerFragment.EXTRA_DATE, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mDate);
            mMonth = calendar.get(Calendar.MONTH);
            textView_date.setText(DateFormat.getDateInstance().format(new Date(mDate)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            //NavUtils.navigateUpFromSameTask(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        Uri uri = MiserContract.NameEntry.CONTENT_URI;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        String[] projection = new String[]{MiserContract.NameEntry.Cols._ID, MiserContract.NameEntry.Cols.CATEGORY_NAME};
        String selection = MiserContract.NameEntry.Cols.TYPE + " = ? " + "AND " +
                MiserContract.NameEntry.Cols.CATEGORY_ID + "< ?";
        String[] selectionArg = null;


        switch (id) {
            case ACCOUNT_CATEGORY_NAME_LOADER: {
                String accounts = preferences.getString(getString(R.string.pref_account_quantity_key), getString(R.string.pref_account_quantity_4_value));
                selectionArg = new String[]{Integer.toString(MiserContract.TYPE_ACCOUNTS), accounts};
                break;
            }
            case INCOME_CATEGORY_NAME_LOADER: {
                String income = preferences.getString(getString(R.string.pref_income_quantity_key), getString(R.string.pref_income_quantity_4_value));
                selectionArg = new String[]{Integer.toString(MiserContract.TYPE_INCOME), income};
                break;
            }
            case COST_CATEGORY_NAME_LOADER: {
                String cost = preferences.getString(getString(R.string.pref_cost_quantity_key), getString(R.string.pref_cost_quantity_10_value));
                selectionArg = new String[]{Integer.toString(MiserContract.TYPE_COST), cost};
                break;
            }
        }

        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case ACCOUNT_CATEGORY_NAME_LOADER: {
                adapter_category_account.swapCursor(data);
                break;
            }
            case INCOME_CATEGORY_NAME_LOADER: {
                adapter_category_income.swapCursor(data);
                if (mType == MiserContract.TYPE_INCOME) {
                    category_spinner.post(new Runnable() {
                        @Override
                        public void run() {
                            category_spinner.setSelection(mCategoryID);
                        }
                    });
                }

                break;
            }
            case COST_CATEGORY_NAME_LOADER: {
                adapter_category_cost.swapCursor(data);
                if (mType == MiserContract.TYPE_COST) {
                    category_spinner.post(new Runnable() {
                        @Override
                        public void run() {
                            category_spinner.setSelection(mCategoryID);
                        }
                    });
                }

                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ACCOUNT_CATEGORY_NAME_LOADER: {
                adapter_category_account.swapCursor(null);
                break;
            }
            case INCOME_CATEGORY_NAME_LOADER: {
                adapter_category_income.swapCursor(null);
                break;
            }
            case COST_CATEGORY_NAME_LOADER: {
                adapter_category_cost.swapCursor(null);
                break;
            }
        }

    }

    private static class AddThread extends Thread {

        private ContentResolver contentResolver;
        private int accountId;
        private float sum;
        private int type;
        private int id;


        private AddThread(ContentResolver contentResolver, int accountId, float sum, int type) {
            this.contentResolver = contentResolver;
            this.accountId = accountId;
            this.sum = sum;
            this.type = type;
        }

        @Override
        public void run() {
            double oldAccountSum;
            double newAccountSum;
            Uri uri = MiserContract.AccountEntry.CONTENT_URI;

            String[] projection = new String[]{MiserContract.AccountEntry.Cols._ID, MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT};
            String selection = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
            String[] selectionArgs = new String[]{Integer.toString(accountId)};

            Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                oldAccountSum = cursor.getDouble(cursor.getColumnIndex(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT));
                id = cursor.getInt(cursor.getColumnIndex(MiserContract.AccountEntry.Cols._ID));
                cursor.close();
            } else {
                return;
            }
            if (type == MiserContract.TYPE_COST) {
                newAccountSum = oldAccountSum - sum;
                if (newAccountSum < 0) newAccountSum = 0F;
            } else {
                newAccountSum = oldAccountSum + sum;
            }
            Uri accountUri = MiserContract.AccountEntry.CONTENT_URI;
            accountUri = accountUri.buildUpon().appendPath(Long.toString(id)).build();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT, newAccountSum);
            contentResolver.update(accountUri, contentValues, null, null);
        }
    }

    private class AddBudgetItems extends Thread {


        ContentResolver contentResolver;
        int categoryID;
        float sum;
        int type;
        String description;
        ContentValues contentValues = new ContentValues();


        private AddBudgetItems(ContentResolver contentResolver, int categoryID, float sum,
                               int type, String description) {
            this.categoryID = categoryID;
            this.contentResolver = contentResolver;
            this.description = description;
            this.type = type;
            this.sum = sum;
        }


        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            for (int i = 0; i < 12; i++) {
                calendar.set(Calendar.MONTH, i);
                contentValues.clear();
                contentValues.put(MiserContract.BudgetEntry.Cols.TYPE, type);
                contentValues.put(MiserContract.BudgetEntry.Cols.CATEGORY_ID, categoryID);
                contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_DESCRIPTION, description);
                contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_SUM, sum);
                contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_INSERT_DATE, calendar.getTimeInMillis());
                contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_MONTH, i);

                contentResolver.insert(MiserContract.BudgetEntry.CONTENT_URI, contentValues);

            }
        }
    }
}
