package com.gribanskij.miserplus.edit_screen;


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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.asynctask.AsyncQueryTask;
import com.gribanskij.miserplus.sql_base.MiserContract;
import com.gribanskij.miserplus.utils.DatePickerFragment;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class EditFragment extends BaseFragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditFragment.class.getSimpleName();


    public static final String _ID = "_idm";
    public static final String DATE = "date";
    public static final String SUM = "sum";
    public static final String DESCRIPTION = "discr";
    public static final String _TYPE = "_type";
    public static final String CATEGORY = "category";
    public static final String ACCOUNT = "account";

    public static final String BUDGET_ITEM = "typeItem";


    private static final int REQEST_DATE = 0;
    private static final int TOKEN_UPDATE = 21;
    private static final int TOKEN_UPDATE_ACCOUNT = 22;
    private static final int ACCOUNT_CATEGORY_NAME_LOADER = 10;
    private static final int INCOME_CATEGORY_NAME_LOADER = 20;
    private static final int COST_CATEGORY_NAME_LOADER = 30;

    private long mDate;


    private Bundle transaction;

    private SimpleCursorAdapter adapter_category_cost;
    private SimpleCursorAdapter adapter_category_income;
    private SimpleCursorAdapter adapter_category_account;
    private TextView textView_date;
    private Spinner category_spinner;
    private Spinner type_spinner;
    private Spinner account_spinner;
    private EditText sum_edittext;
    private EditText descrip_edittext;
    private NumberFormat numberFormat;


    public EditFragment() {
    }


    public static EditFragment newInstance(Bundle transaction) {
        EditFragment fragment = new EditFragment();
        fragment.setArguments(transaction);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(AbstractActivity.TRANSACTION, transaction);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            transaction = savedInstanceState.getBundle(AbstractActivity.TRANSACTION);
        } else {
            if (getArguments() != null) {
                transaction = getArguments();
            }
        }

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);

        adapter_category_cost = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                null, new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME}, new int[]{android.R.id.text1}, 0);
        adapter_category_cost.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter_category_income = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                null, new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME}, new int[]{android.R.id.text1}, 0);
        adapter_category_income.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter_category_account = new SimpleCursorAdapter(getContext(), android.R.layout.simple_spinner_item,
                null, new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME}, new int[]{android.R.id.text1}, 0);
        adapter_category_account.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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

        View v = inflater.inflate(R.layout.edit_main, container, false);
        ImageButton ok_button = v.findViewById(R.id.save_button);
        sum_edittext = v.findViewById(R.id.sum_edittext_);
        descrip_edittext = v.findViewById(R.id.descrip_edittext);
        textView_date = v.findViewById(R.id.calendar_textview);
        type_spinner = v.findViewById(R.id.type_spiner);
        category_spinner = v.findViewById(R.id.category_spiner);
        account_spinner = v.findViewById(R.id.account_spiner);
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        TextView title_card = v.findViewById(R.id.add_type);

        type_spinner.setEnabled(false);
        account_spinner.setEnabled(false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) {
            bar.setTitle(R.string.edit_title);
            bar.setDisplayHomeAsUpEnabled(true);
        }

        if (transaction.getBoolean(BUDGET_ITEM)) {
            title_card.setText(getString(R.string.title_add_card_budget));
        } else {
            title_card.setText(getString(R.string.title_add_card));
        }


        descrip_edittext.setText(transaction.getString(DESCRIPTION));

        //String sum = numberFormat.format(transaction.getFloat(SUM));
        String sum = Float.toString(transaction.getFloat(SUM));
        sum_edittext.setText(sum);
        mDate = transaction.getLong(DATE);
        textView_date.setText(DateFormat.getDateInstance().format(new Date(transaction.getLong(DATE))));
        type_spinner.setSelection(transaction.getInt(_TYPE));
        account_spinner.setAdapter(adapter_category_account);

        if (transaction.getInt(_TYPE) == MiserContract.TYPE_COST) {
            category_spinner.setAdapter(adapter_category_cost);
        } else {
            category_spinner.setAdapter(adapter_category_income);
        }

        textView_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = new DatePickerFragment();
                dialog.setTargetFragment(EditFragment.this, REQEST_DATE);
                dialog.show(manager, LOG_TAG);
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

                if (!transaction.getBoolean(EditFragment.BUDGET_ITEM)) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MiserContract.TransactionEntry.Cols.TYPE, mType);
                    contentValues.put(MiserContract.TransactionEntry.Cols.CATEGORY_ID, mCategory);
                    contentValues.put(MiserContract.TransactionEntry.Cols.DESCRIPTION, mDescription);
                    contentValues.put(MiserContract.TransactionEntry.Cols.AMOUNT, mSum);
                    contentValues.put(MiserContract.TransactionEntry.Cols.DATE, mDate);
                    contentValues.put(MiserContract.TransactionEntry.Cols.ACCOUNT, mAccount);
                    contentValues.put(MiserContract.TransactionEntry.Cols.ACCOUNT_NAME, mAccount_name);
                    contentValues.put(MiserContract.TransactionEntry.Cols.CATEGORY_NAME, mCategory_name);

                    Uri uri = MiserContract.TransactionEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(Long.toString(transaction.getInt(_ID))).build();

                    AsyncQueryTask mAsyncTask = new AsyncQueryTask(getContext().getContentResolver(), getContext());
                    mAsyncTask.startUpdate(TOKEN_UPDATE, null, uri, contentValues, null, null);

                    Thread mThread = new EditThread(mAccount, mSum, mType, transaction, getActivity().getContentResolver());
                    mThread.start();
                } else {

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(mDate);
                    int mMonth = calendar.get(Calendar.MONTH);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MiserContract.BudgetEntry.Cols.TYPE, mType);
                    contentValues.put(MiserContract.BudgetEntry.Cols.CATEGORY_ID, mCategory);
                    contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_DESCRIPTION, mDescription);
                    contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_SUM, mSum);
                    contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_INSERT_DATE, mDate);
                    contentValues.put(MiserContract.BudgetEntry.Cols.BUDGET_MONTH, mMonth);


                    Uri uri = MiserContract.BudgetEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(Long.toString(transaction.getInt(_ID))).build();

                    AsyncQueryTask mAsyncTask = new AsyncQueryTask(getContext().getContentResolver(), getContext());
                    mAsyncTask.startUpdate(TOKEN_UPDATE, null, uri, contentValues, null, null);
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
            mDate = data.getLongExtra(DatePickerFragment.EXTRA_DATE, 0);
            textView_date.setText(DateFormat.getDateInstance().format(new Date(mDate)));
        }
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
                account_spinner.post(new Runnable() {
                    @Override
                    public void run() {
                        account_spinner.setSelection(transaction.getInt(ACCOUNT));
                    }
                });
                break;
            }
            case INCOME_CATEGORY_NAME_LOADER: {
                adapter_category_income.swapCursor(data);
                if (transaction.getInt(_TYPE) == MiserContract.TYPE_INCOME) {
                    category_spinner.post(new Runnable() {
                        @Override
                        public void run() {
                            category_spinner.setSelection(transaction.getInt(CATEGORY));
                        }
                    });
                }

                break;
            }
            case COST_CATEGORY_NAME_LOADER: {
                adapter_category_cost.swapCursor(data);
                if (transaction.getInt(_TYPE) == MiserContract.TYPE_COST) {
                    category_spinner.post(new Runnable() {
                        @Override
                        public void run() {
                            category_spinner.setSelection(transaction.getInt(CATEGORY));
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

    private static class EditThread extends Thread {


        private int newAccount;
        private float newSum;
        private int newType;
        private Bundle transaction;
        private ContentResolver contentResolver;

        private EditThread(int newAccount, float newSum, int newType, Bundle transaction, ContentResolver contentResolver) {
            this.newAccount = newAccount;
            this.newSum = newSum;
            this.newType = newType;
            this.transaction = transaction;
            this.contentResolver = contentResolver;
        }

        @Override
        public void run() {

            int oldAcount = transaction.getInt(ACCOUNT);
            float oldSum = transaction.getFloat(SUM);
            int oldType = transaction.getInt(_TYPE);


            if (newAccount == oldAcount && newType == oldType && newSum == oldSum) return;

            float sum = getAccountSum(oldAcount);
            float difSum = oldSum - newSum;

            if (oldType == MiserContract.TYPE_COST) {
                sum = sum + difSum;
            } else {
                sum = sum - difSum;
            }

            if (sum < 0) sum = 0F;
            updateAccountSum(oldAcount, sum);



            /*

            int kType = oldType - newType;
            int kAccount = oldAcount - newAccount;

            if (kAccount != 0){
                float sum = getAccountSum(newAccount);
                kAccount = 1;
                if (newType == MiserContract.TYPE_COST) kAccount = -1;
                sum = sum + newSum * kAccount;
                if (sum < 0)sum = 0F;
                updateAccountSum(newAccount,sum);

                sum = getAccountSum(oldAcount);
                kAccount = -1;
                if (oldType == MiserContract.TYPE_COST) kAccount = 1;
                sum = sum + oldSum * kAccount;
                if (sum < 0)sum = 0F;
                updateAccountSum(oldAcount,sum);
                return;
            }

            if (kType != 0) {
                kType = 1;
                float sum = getAccountSum(oldAcount);
                if (newType == MiserContract.TYPE_COST) kType = -1;
                sum = sum + oldSum * kType + newSum *kType;
                if (sum < 0) sum = 0F;
                updateAccountSum(oldAcount, sum);
            }

            */
        }

        private float getAccountSum(int account) {

            float accountSum = -1;
            Uri uri = MiserContract.AccountEntry.CONTENT_URI;

            String[] projection = new String[]{MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT};
            String selection = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
            String[] selectionArgs = new String[]{Integer.toString(account)};

            Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                accountSum = cursor.getFloat(cursor.getColumnIndex(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT));
                cursor.close();
            }

            return accountSum;
        }

        private int updateAccountSum(int account, float sum) {

            Uri uri = MiserContract.AccountEntry.CONTENT_URI;
            String[] selectionArgs = new String[]{Integer.toString(account)};
            String where = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MiserContract.TransactionEntry.Cols.AMOUNT, sum);
            int a = contentResolver.update(uri, contentValues, where, selectionArgs);
            return a;
        }
    }
}
