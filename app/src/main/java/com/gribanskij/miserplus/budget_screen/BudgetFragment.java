package com.gribanskij.miserplus.budget_screen;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.budget_category_screen.BudgetCategoryActivity;
import com.gribanskij.miserplus.help_screen.HelpActivity;
import com.gribanskij.miserplus.sql_base.MiserContract;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class BudgetFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = BudgetFragment.class.getSimpleName();
    private static final String URL_APP = "https://play.google.com/store/apps/details?id=com.gribanskij.miserplus";
    private static final String URI_APP = "market://details?id=com.gribanskij.miserplus";

    private static final int DATA_BUDGET_SUM_COST_MONTH = 500;
    private static final int DATA_BUDGET_SUM_INCOME_MONTH = 510;

    private static final int DATA_BUDGET_SUM_COST_YTD = 540;
    private static final int DATA_BUDGET_SUM_INCOME_YTD = 550;

    private static final int DATA_BUDGET_SUM_INCOME_YEAR = 551;
    private static final int DATA_BUDGET_SUM_COST_YEAR = 552;

    private static final int DATA_SUM_COST_MONTH = 520;
    private static final int DATA_SUM_INCOME_MONTH = 530;

    private static final int DATA_SUM_COST_YTD = 560;
    private static final int DATA_SUM_INCOME_YTD = 570;


    private int budget_month;
    private int budget_type;
    private NumberFormat numberFormat;
    private NumberFormat numberFormatBalance;
    private SimpleDateFormat dateFormat;
    private String currency;
    private float fact_cost_data_ytd;
    private float fact_income_data_ytd;
    private float fact_cost_month_data;
    private float fact_income_month_data;
    private float budget_cost_month_data;
    private float budget_income_month_data;
    private float budget_cost_data_ytd;
    private float budget_income_data_ytd;
    private float budget_cost_data_year;
    private float budget_income_data_year;
    private TextView fact_cost_ytd;
    private TextView fact_income_ytd;
    private TextView fact_balance_month;
    private TextView fact_balance_ytd;
    private TextView budget_cost_month;
    private TextView budget_income_month;
    private TextView budget_balance_month;
    private TextView budget_cost_ytd;
    private TextView budget_income_ytd;
    private TextView budget_balance_ytd;
    private ProgressBar budget_cost_ytd_progress;
    private ProgressBar budget_income_ytd_progress;
    private ProgressBar fact_cost_progress_ytd;
    private ProgressBar fact_income_progress_ytd;

    public BudgetFragment() {
    }

    public static Fragment newInstance(int type, int month) {
        Fragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putInt(AbstractActivity.TYPE, type);
        args.putInt(AbstractActivity.MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(DATA_SUM_COST_MONTH, null, this);
        getLoaderManager().initLoader(DATA_SUM_INCOME_MONTH, null, this);
        getLoaderManager().initLoader(DATA_BUDGET_SUM_COST_MONTH, null, this);
        getLoaderManager().initLoader(DATA_BUDGET_SUM_INCOME_MONTH, null, this);
        getLoaderManager().initLoader(DATA_BUDGET_SUM_COST_YTD, null, this);
        getLoaderManager().initLoader(DATA_BUDGET_SUM_INCOME_YTD, null, this);
        getLoaderManager().initLoader(DATA_BUDGET_SUM_COST_YEAR, null, this);
        getLoaderManager().initLoader(DATA_BUDGET_SUM_INCOME_YEAR, null, this);
        getLoaderManager().initLoader(DATA_SUM_COST_YTD, null, this);
        getLoaderManager().initLoader(DATA_SUM_INCOME_YTD, null, this);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            budget_month = savedInstanceState.getInt(AbstractActivity.MONTH, Calendar.JANUARY);
            budget_type = savedInstanceState.getInt(AbstractActivity.TYPE, MiserContract.TYPE_COST);
        } else {
            Bundle arg = getArguments();
            if (arg != null) {
                budget_month = arg.getInt(AbstractActivity.MONTH, Calendar.JANUARY);
                budget_type = arg.getInt(AbstractActivity.TYPE, MiserContract.TYPE_COST);
            }
        }

        dateFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);
        numberFormatBalance = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormatBalance.setMaximumFractionDigits(1);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        currency = sharedPreferences.getString(getString(R.string.pref_currency_key), getString(R.string.pref_currency_default_RUB_value));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.budget_main, container, false);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isAdsDisabled = sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false);

        if (isAdsDisabled) {
            FrameLayout frame = v.findViewById(R.id.frame);
        }


        budget_cost_month = v.findViewById(R.id.viewBudgetCostMonth);
        budget_income_month = v.findViewById(R.id.viewBudgetIncomeMonth);
        //budget_balance_month = v.findViewById(R.id.viewBalanceBudgetMonth);

        //fact_balance_month = v.findViewById(R.id.viewBalanceBudgetMonthPerform);
        fact_balance_ytd = v.findViewById(R.id.balance_ytd_fact);

        budget_balance_ytd = v.findViewById(R.id.balance_budget_ytd);
        budget_income_ytd = v.findViewById(R.id.budget_income_sum_ytd);
        budget_cost_ytd = v.findViewById(R.id.budget_cost_sum_ytd);


        fact_cost_ytd = v.findViewById(R.id.view_cost_sum_ytd_fact);
        fact_income_ytd = v.findViewById(R.id.view_income_sum_ytd_fact);


        budget_cost_ytd_progress = v.findViewById(R.id.progress_budget_cost_sum_ytd);
        budget_income_ytd_progress = v.findViewById(R.id.progress_budget_income_sum_ytd);

        fact_cost_progress_ytd = v.findViewById(R.id.progress_cost_ytd_fact);
        fact_income_progress_ytd = v.findViewById(R.id.progress_income_ytd_fact);

        fact_income_progress_ytd.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.colorAccentForProgress),
                android.graphics.PorterDuff.Mode.SRC_IN);

        fact_cost_progress_ytd.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.colorAccentForProgress),
                android.graphics.PorterDuff.Mode.SRC_IN);

        budget_income_ytd_progress.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.colorAccentForProgress),
                android.graphics.PorterDuff.Mode.SRC_IN);

        budget_cost_ytd_progress.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.colorAccentForProgress),
                android.graphics.PorterDuff.Mode.SRC_IN);


        CardView costCard = v.findViewById(R.id.cardCostBudget);
        CardView incomeCard = v.findViewById(R.id.cardIncomeBudget);


        costCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BudgetCategoryActivity.class);
                intent.putExtra(AbstractActivity.TYPE, MiserContract.TYPE_COST);
                intent.putExtra(AbstractActivity.MONTH, budget_month);
                startActivity(intent);
            }
        });

        incomeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BudgetCategoryActivity.class);
                intent.putExtra(AbstractActivity.TYPE, MiserContract.TYPE_INCOME);
                intent.putExtra(AbstractActivity.MONTH, budget_month);
                startActivity(intent);
            }
        });


        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AbstractActivity.TYPE, budget_type);
        outState.putLong(AbstractActivity.MONTH, budget_month);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_option_budget, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.menu_item_info:
                intent = new Intent(getActivity(), HelpActivity.class);
                intent.putExtra(AbstractActivity.BUDGET_ADD, true);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onClickRateThisApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(URI_APP));
        if (!isActivityStarted(intent)) {
            intent.setData(Uri.parse(URL_APP));
            if (!isActivityStarted(intent)) {
                Toast.makeText(getContext(), R.string.market_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isActivityStarted(Intent aIntent) {
        try {
            startActivity(aIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String sortOrder = null;
        String[] selectionArg = null;


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String count_cost_categories = sharedPreferences.getString(getString(R.string.pref_cost_quantity_key),
                getString(R.string.pref_cost_quantity_10_value));
        String count_income_categories = sharedPreferences.getString(getString(R.string.pref_income_quantity_key),
                getString(R.string.pref_income_quantity_4_value));

        switch (id) {
            case DATA_SUM_COST_MONTH:
                uri = MiserContract.TransactionEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " +
                        MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                        MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(getBeginMonth(budget_month)), Long.toString(getEndMonth(budget_month)),
                        Integer.toString(MiserContract.TYPE_COST), count_cost_categories};
                break;

            case DATA_SUM_INCOME_MONTH:
                uri = MiserContract.TransactionEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " +
                        MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                        MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(getBeginMonth(budget_month)), Long.toString(getEndMonth(budget_month)),
                        Integer.toString(MiserContract.TYPE_INCOME), count_income_categories};
                break;


            case DATA_SUM_COST_YTD:
                uri = MiserContract.TransactionEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " +
                        MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                        MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(getBeginYear()), Long.toString(getYTDdata(budget_month)),
                        Integer.toString(MiserContract.TYPE_COST), count_cost_categories};

                break;

            case DATA_SUM_INCOME_YTD:
                uri = MiserContract.TransactionEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " +
                        MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                        MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(getBeginYear()), Long.toString(getYTDdata(budget_month)),
                        Integer.toString(MiserContract.TYPE_INCOME), count_income_categories};

                break;

            case DATA_BUDGET_SUM_COST_MONTH:
                uri = MiserContract.BudgetEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")"};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(budget_month),
                        Integer.toString(MiserContract.TYPE_COST), count_cost_categories};
                break;

            case DATA_BUDGET_SUM_INCOME_MONTH:
                uri = MiserContract.BudgetEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")"};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(budget_month),
                        Integer.toString(MiserContract.TYPE_INCOME), count_income_categories};
                break;

            case DATA_BUDGET_SUM_COST_YTD:
                uri = MiserContract.BudgetEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")"};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " <= ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(budget_month),
                        Integer.toString(MiserContract.TYPE_COST), count_cost_categories};
                break;

            case DATA_BUDGET_SUM_INCOME_YTD:
                uri = MiserContract.BudgetEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")"};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " <= ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(budget_month),
                        Integer.toString(MiserContract.TYPE_INCOME), count_income_categories};
                break;

            case DATA_BUDGET_SUM_COST_YEAR:
                uri = MiserContract.BudgetEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")"};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " <= ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(11),
                        Integer.toString(MiserContract.TYPE_COST), count_cost_categories};
                break;

            case DATA_BUDGET_SUM_INCOME_YEAR:
                uri = MiserContract.BudgetEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")"};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " <= ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(11),
                        Integer.toString(MiserContract.TYPE_INCOME), count_income_categories};
                break;


        }
        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Float sum;
        final int SUM_ = 0;

        switch (loader.getId()) {

            case DATA_BUDGET_SUM_COST_MONTH:
                if (data.moveToFirst()) {
                    budget_cost_month_data = data.getFloat(SUM_);
                    budget_cost_month.setText(numberFormat.format(budget_cost_month_data));
                } else {
                    budget_cost_month.setText(numberFormat.format(0));
                    budget_cost_month_data = 0f;
                }
                //budget_balance_month.setText(upDateBadgetBalance(budget_income_month_data, budget_cost_month_data));

                break;

            case DATA_BUDGET_SUM_INCOME_MONTH:
                if (data.moveToFirst()) {
                    budget_income_month_data = data.getFloat(SUM_);
                    budget_income_month.setText(numberFormat.format(budget_income_month_data));
                } else {
                    budget_income_month.setText(numberFormat.format(0));
                    budget_income_month_data = 0f;
                }
                //budget_balance_month.setText(upDateBadgetBalance(budget_income_month_data, budget_cost_month_data));
                break;

            case DATA_BUDGET_SUM_INCOME_YTD:
                if (data.moveToFirst()) {
                    budget_income_data_ytd = data.getFloat(SUM_);
                    budget_income_ytd.setText(numberFormat.format(budget_income_data_ytd));
                } else {
                    budget_income_ytd.setText(numberFormat.format(0));
                    budget_income_data_ytd = 0f;
                }
                budget_balance_ytd.setText(upDateBadgetBalance(budget_income_data_ytd, budget_cost_data_ytd));
                budget_income_ytd_progress.setProgress(upDateProgress(budget_income_data_ytd, budget_income_data_year));
                break;

            case DATA_BUDGET_SUM_COST_YTD:
                if (data.moveToFirst()) {
                    budget_cost_data_ytd = data.getFloat(SUM_);
                    budget_cost_ytd.setText(numberFormat.format(budget_cost_data_ytd));
                } else {
                    budget_cost_ytd.setText(numberFormat.format(0));
                    budget_cost_data_ytd = 0f;
                }
                budget_balance_ytd.setText(upDateBadgetBalance(budget_income_data_ytd, budget_cost_data_ytd));
                budget_cost_ytd_progress.setProgress(upDateProgress(budget_cost_data_ytd, budget_cost_data_year));
                break;

            case DATA_BUDGET_SUM_INCOME_YEAR:
                if (data.moveToFirst()) {
                    budget_income_data_year = data.getFloat(SUM_);
                } else {
                    budget_income_data_year = 0f;
                }
                budget_income_ytd_progress.setProgress(upDateProgress(budget_income_data_ytd, budget_income_data_year));
                fact_income_progress_ytd.setProgress(upDateProgress(fact_income_data_ytd, budget_income_data_year));

                break;

            case DATA_BUDGET_SUM_COST_YEAR:
                if (data.moveToFirst()) {
                    budget_cost_data_year = data.getFloat(SUM_);
                } else {
                    budget_cost_data_year = 0f;
                }
                fact_cost_progress_ytd.setProgress(upDateProgress(fact_cost_data_ytd, budget_cost_data_year));
                budget_cost_ytd_progress.setProgress(upDateProgress(budget_cost_data_ytd, budget_cost_data_year));
                break;

            case DATA_SUM_COST_MONTH:
                if (data.moveToFirst()) {
                    fact_cost_month_data = data.getFloat(SUM_);
                } else {
                    fact_cost_month_data = 0f;
                }
                //fact_balance_month.setText(upDateBadgetBalance(fact_income_month_data, fact_cost_month_data));
                break;

            case DATA_SUM_INCOME_MONTH:
                if (data.moveToFirst()) {
                    fact_income_month_data = data.getFloat(SUM_);
                } else {
                    fact_income_month_data = 0f;
                }
                //fact_balance_month.setText(upDateBadgetBalance(fact_income_month_data, fact_cost_month_data));
                break;

            case DATA_SUM_COST_YTD:

                if (data.moveToFirst()) {
                    fact_cost_data_ytd = data.getFloat(SUM_);
                    fact_cost_ytd.setText(numberFormat.format(fact_cost_data_ytd));
                } else {
                    fact_cost_ytd.setText(numberFormat.format(0));
                    fact_cost_data_ytd = 0f;
                }
                fact_balance_ytd.setText(upDateBadgetBalance(fact_income_data_ytd, fact_cost_data_ytd));
                fact_cost_progress_ytd.setProgress(upDateProgress(fact_cost_data_ytd, budget_cost_data_year));
                break;


            case DATA_SUM_INCOME_YTD:
                if (data.moveToFirst()) {
                    fact_income_data_ytd = data.getFloat(SUM_);
                    fact_income_ytd.setText(numberFormat.format(fact_income_data_ytd));
                } else {
                    fact_income_ytd.setText(numberFormat.format(0));
                    fact_income_data_ytd = 0f;
                }
                fact_balance_ytd.setText(upDateBadgetBalance(fact_income_data_ytd, fact_cost_data_ytd));
                fact_income_progress_ytd.setProgress(upDateProgress(fact_income_data_ytd, budget_income_data_year));
                break;


            default:

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private String upDateBadgetBalance(float income, float cost) {
        if (income == 0 || cost == 0) {
            return "0%";
        } else {
            float temp = ((income / cost) * 100 - 100);
            StringBuilder builder = new StringBuilder();
            if (temp > 0) {
                builder.append("+");
                builder.append(numberFormatBalance.format(temp));
                builder.append("%");
                return builder.toString();
            } else {
                builder.append(numberFormatBalance.format(temp));
                builder.append("%");
                return builder.toString();
            }
        }
    }

    private int upDateProgress(float ytd, float year) {
        if (ytd == 0 || year == 0) {
            return 0;
        }
        return Math.round((ytd * 100 / year));
    }

    private long getBeginYear() {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.MONTH, 0);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mCalendar.set(Calendar.HOUR, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        return mCalendar.getTimeInMillis();
    }

    private long getEndYear() {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(getBeginYear());
        mCalendar.add(Calendar.YEAR, 1);
        return mCalendar.getTimeInMillis();
    }

    private long getBeginMonth(int month) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mCalendar.set(Calendar.HOUR, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        return mCalendar.getTimeInMillis();
    }

    private long getEndMonth(int month) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(getBeginMonth(month));
        mCalendar.add(Calendar.MONTH, 1);
        return mCalendar.getTimeInMillis();
    }

    private long getYTDdata(int month) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(getBeginYear());
        mCalendar.add(Calendar.MONTH, month + 1);
        return mCalendar.getTimeInMillis();
    }
}
