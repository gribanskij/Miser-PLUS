package com.gribanskij.miserplus.dashboard_screen;


import android.app.Activity;
import android.app.AlarmManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.GraphLoader;
import com.gribanskij.miserplus.LabelFormatter;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.add_screen.AddActivity;
import com.gribanskij.miserplus.budget_screen.BudgetActivity;
import com.gribanskij.miserplus.graph_screen.GraphActivity;
import com.gribanskij.miserplus.help_screen.HelpActivity;
import com.gribanskij.miserplus.settings.SettingsActivity;
import com.gribanskij.miserplus.sql_base.MiserContract;
import com.gribanskij.miserplus.utils.DataRangeFragment;
import com.gribanskij.miserplus.utils.TimeUtils;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.gribanskij.miserplus.AbstractActivity.FINAL_DATE;
import static com.gribanskij.miserplus.AbstractActivity.START_DATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends BaseFragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String LOG_TAG = DashboardFragment.class.getSimpleName();
    private static final int DATE_RANGE = 2;


    private static final int DATA_SUM_COST = 10;
    private static final int DATA_SUM_COST_MONTH = 11;
    private static final int DATA_SUM_INCOME = 20;
    private static final int DATA_SUM_INCOME_MONTH = 21;
    private static final int ACCOUNT_AMOUNT = 30;
    private static final int ACCOUNT_NAME = 40;
    private static final int BARGRAPH_DATA_SET_COST = 50;
    private static final int BARGRAPH_DATA_SET_INCOME = 60;

    private static final int DATA_BUDGET_SUM_COST = 70;
    private static final int DATA_BUDGET_SUM_INCOME = 80;

    private static final String URL_APP = "https://play.google.com/store/apps/details?id=com.gribanskij.miserplus";
    private static final String URI_APP = "market://details?id=com.gribanskij.miserplus";

    private final int CARD = 0;
    private final int NAME = 1;
    private final int SUM = 2;

    private final String TAG_ACCOUNT_NAME = "name";
    private final String TAG_ACCOUNT_ID = "id";
    private final String TAG_ACCOUNT_SUM = "sum";
    private final String TAG_ACCOUNT_CATEGORY = "category";


    private List<List<View>> accounts;
    private Long start_date;
    private Long final_date;
    private SimpleDateFormat dateFormat;
    private NumberFormat numberFormat;

    private TextView income_view_sum;
    private TextView cost_view_sum;
    private String currency;
    private TextView budget_cost_left;
    private TextView budget_income_left;

    private ProgressBar progress_cost;
    private ProgressBar progress_income;

    private float data_sum_cost;
    private float data_sum_income;
    private float data_budget_sum_cost;
    private float data_budget_sum_income;

    private GraphView cost_graph;
    private GraphView income_graph;
    private GraphLoaderCallBack callback;
    private AlarmManager alarmManager;


    public DashboardFragment() {
    }

    public static BaseFragment newInstance(long start_date, long final_date) {
        BaseFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putLong(START_DATE, start_date);
        args.putLong(FINAL_DATE, final_date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(DATA_SUM_COST, null, this);
        getLoaderManager().initLoader(DATA_SUM_INCOME, null, this);
        getLoaderManager().initLoader(ACCOUNT_AMOUNT, null, this);
        getLoaderManager().initLoader(ACCOUNT_NAME, null, this);
        getLoaderManager().initLoader(DATA_BUDGET_SUM_COST, null, this);
        getLoaderManager().initLoader(DATA_BUDGET_SUM_INCOME, null, this);
        getLoaderManager().initLoader(DATA_SUM_COST_MONTH, null, this);
        getLoaderManager().initLoader(DATA_SUM_INCOME_MONTH, null, this);

        callback = new GraphLoaderCallBack();
        getLoaderManager().initLoader(BARGRAPH_DATA_SET_COST, null, callback);
        //getLoaderManager().initLoader(BARGRAPH_DATA_SET_INCOME, null, callback);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            start_date = savedInstanceState.getLong(START_DATE);
            final_date = savedInstanceState.getLong(FINAL_DATE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                start_date = arguments.getLong(START_DATE);
                final_date = arguments.getLong(FINAL_DATE);
            }
        }
        dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        currency = sharedPreferences.getString(getString(R.string.pref_currency_key), getString(R.string.pref_currency_default_RUB_value));


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dashboard_main, container, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isAdsDisabled = sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false);

        if (!isAdsDisabled) {

            FrameLayout layout = v.findViewById(R.id.admob_container);
            AdView adView = new AdView(getActivity());
            adView.setAdSize(AdSize.SMART_BANNER);
            //adView.setAdUnitId(getString(R.string.test_banner_id));
            adView.setAdUnitId(getString(R.string.dashboard_banner_ID));
            layout.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }

        cost_graph = v.findViewById(R.id.cost_graphView);
        income_graph = v.findViewById(R.id.income_graphView);

        budget_cost_left = v.findViewById(R.id.budget_cost_sum_left);
        budget_income_left = v.findViewById(R.id.budget_income_sum_left);

        progress_income = v.findViewById(R.id.progress_income);
        progress_cost = v.findViewById(R.id.progress_cost);

        progress_cost.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.colorAccentForProgress),
                android.graphics.PorterDuff.Mode.SRC_IN);

        progress_income.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.colorAccentForProgress),
                android.graphics.PorterDuff.Mode.SRC_IN);


        DefaultLabelFormatter formatter = new LabelFormatter(getActivity());
        cost_graph.getGridLabelRenderer().setLabelFormatter(formatter);
        cost_graph.getGridLabelRenderer().setHorizontalLabelsColor(
                ContextCompat.getColor(getContext(), R.color.colorGray));
        cost_graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        cost_graph.getGridLabelRenderer().setHighlightZeroLines(false);

        cost_graph.getViewport().setXAxisBoundsManual(true);
        cost_graph.getViewport().setMaxX(6);

        income_graph.getGridLabelRenderer().setLabelFormatter(formatter);
        income_graph.getGridLabelRenderer().setHorizontalLabelsColor(
                ContextCompat.getColor(getContext(), R.color.colorGray));
        income_graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        income_graph.getGridLabelRenderer().setHighlightZeroLines(false);

        income_graph.getViewport().setXAxisBoundsManual(true);
        income_graph.getViewport().setMaxX(6);


        //CardView graphCost = v.findViewById(R.id.cardView3);
        CardView graphIncome = v.findViewById(R.id.cardGraphIncome);
        graphIncome.setVisibility(View.GONE);

        cost_graph.setOnClickListener(new GraphListener());
        income_graph.setOnClickListener(new GraphListener());


        cost_view_sum = v.findViewById(R.id.cost_sum_textView);
        income_view_sum = v.findViewById(R.id.income_sum_textView);

        CardView cost_card = v.findViewById(R.id.cardView2);
        CardView income_card = v.findViewById(R.id.cardView);


        accounts = new ArrayList<>();

        List<View> card1 = new ArrayList<>();
        List<View> card2 = new ArrayList<>();
        List<View> card3 = new ArrayList<>();
        List<View> card4 = new ArrayList<>();

        accounts.add(card1);
        accounts.add(card2);
        accounts.add(card3);
        accounts.add(card4);

        card1.add(v.findViewById(R.id.cardView4));
        card1.add(v.findViewById(R.id.account_name1));
        card1.add(v.findViewById(R.id.account_sum1));

        card2.add(v.findViewById(R.id.cardView5));
        card2.add(v.findViewById(R.id.account_name2));
        card2.add(v.findViewById(R.id.account_sum2));

        card3.add(v.findViewById(R.id.cardView7));
        card3.add(v.findViewById(R.id.account_name3));
        card3.add(v.findViewById(R.id.account_sum3));

        card4.add(v.findViewById(R.id.cardView6));
        card4.add(v.findViewById(R.id.account_name4));
        card4.add(v.findViewById(R.id.account_sum4));


        for (List account : accounts) {

            View cardView = (View) account.get(CARD);
            Bundle tag = new Bundle();
            cardView.setTag(tag);
            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Bundle info = (Bundle) view.getTag();
                    DialogFragment dialog = AccountDialog.create(
                            info.getInt(TAG_ACCOUNT_CATEGORY),
                            info.getString(TAG_ACCOUNT_NAME, "?"));
                    FragmentManager manager = getFragmentManager();
                    dialog.show(manager, "dialog");
                    return true;
                }
            });

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Bundle info = (Bundle) view.getTag();
                    getCallback().onCategorySelected(
                            start_date,
                            final_date,
                            info.getInt(TAG_ACCOUNT_CATEGORY),
                            info.getString(TAG_ACCOUNT_NAME));
                }
            });
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        updateToolBar(start_date, final_date);


        RadioButton set = v.findViewById(R.id.set_radioButton);
        RadioButton day = v.findViewById(R.id.day_radioButton);
        RadioButton week = v.findViewById(R.id.week_radioButton);
        RadioButton month = v.findViewById(R.id.month_radioButton);


        View.OnClickListener range_button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.set_radioButton: {
                        FragmentManager manager = getFragmentManager();
                        DialogFragment fragment = new DataRangeFragment();
                        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                        fragment.setTargetFragment(DashboardFragment.this, DATE_RANGE);
                        fragment.show(manager, LOG_TAG);
                        break;
                    }
                    case R.id.day_radioButton: {
                        start_date = TimeUtils.getBegin_day();
                        final_date = TimeUtils.getEnd_day();
                        updateToolBar(start_date, final_date);
                        restartLoaderSum();
                        break;
                    }
                    case R.id.week_radioButton: {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        String temp = sharedPreferences.getString(getString(R.string.pref_week_key), getString(R.string.pref_week_default_value));
                        int first_day_week = Integer.parseInt(temp);
                        start_date = TimeUtils.getBegin_week(first_day_week);
                        final_date = TimeUtils.getEnd_week(first_day_week);
                        updateToolBar(start_date, final_date);
                        restartLoaderSum();
                        break;
                    }
                    case R.id.month_radioButton: {
                        start_date = TimeUtils.getBegin_month();
                        final_date = TimeUtils.getEnd_month();
                        updateToolBar(start_date, final_date);
                        restartLoaderSum();
                        break;
                    }
                }
            }
        };

        set.setOnClickListener(range_button_listener);
        week.setOnClickListener(range_button_listener);
        day.setOnClickListener(range_button_listener);
        month.setOnClickListener(range_button_listener);
        month.setChecked(true);


        cost_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getCallback().onCategorySelected(MiserContract.TYPE_COST, start_date, final_date);

            }
        });

        income_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getCallback().onCategorySelected(MiserContract.TYPE_INCOME, start_date, final_date);

            }
        });

        FloatingActionButton actionButton = v.findViewById(R.id.action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddActivity.class);
                startActivity(intent);
            }
        });

        View.OnClickListener budgetCardClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle parameters = new Bundle();
                //parameters.putInt(AbstractActivity.TYPE,MiserContract.TYPE_COST);
                //parameters.putInt(AbstractActivity.MONTH,getCurrentMonth());
                //getCallback().onFragmentChange(BUDGET_DASHBOARD_FRAGMENT,parameters);
                Intent intent = new Intent(getActivity(), BudgetActivity.class);
                startActivity(intent);
            }
        };


        CardView budgetCard = v.findViewById(R.id.cardGraphBudget);
        budgetCard.setOnClickListener(budgetCardClick);

        return v;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_rating:
                onClickRateThisApp();
                return true;
            case R.id.menu_item_info:
                intent = new Intent(getActivity(), HelpActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_options, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(START_DATE, start_date);
        outState.putLong(FINAL_DATE, final_date);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        long date_start_month = TimeUtils.getBegin_month();
        long date_end_month = TimeUtils.getEnd_month();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String count_cost_categories = sharedPreferences.getString(getString(R.string.pref_cost_quantity_key),
                getString(R.string.pref_cost_quantity_10_value));
        String count_income_categories = sharedPreferences.getString(getString(R.string.pref_income_quantity_key),
                getString(R.string.pref_income_quantity_4_value));

        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String sortOrder = null;
        String[] selectionArg = null;

        switch (id) {
            case DATA_SUM_COST:
                uri = MiserContract.TransactionEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " +
                        MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                        MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(start_date), Long.toString(final_date),
                        Integer.toString(MiserContract.TYPE_COST), count_cost_categories};
                break;

            case DATA_SUM_COST_MONTH:

                uri = MiserContract.TransactionEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " +
                        MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                        MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(date_start_month), Long.toString(date_end_month),
                        Integer.toString(MiserContract.TYPE_COST), count_cost_categories};
                break;

            case DATA_SUM_INCOME:
                uri = MiserContract.TransactionEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " +
                        MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                        MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(start_date), Long.toString(final_date),
                        Integer.toString(MiserContract.TYPE_INCOME), count_income_categories};
                break;

            case DATA_SUM_INCOME_MONTH:
                uri = MiserContract.TransactionEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " +
                        MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                        MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(date_start_month), Long.toString(date_end_month),
                        Integer.toString(MiserContract.TYPE_INCOME), count_income_categories};
                break;

            case DATA_BUDGET_SUM_COST:
                uri = MiserContract.BudgetEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")"};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(getCurrentMonth()),
                        Integer.toString(MiserContract.TYPE_COST), count_cost_categories};
                break;

            case DATA_BUDGET_SUM_INCOME:
                uri = MiserContract.BudgetEntry.CONTENT_URI;
                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")"};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(getCurrentMonth()),
                        Integer.toString(MiserContract.TYPE_INCOME), count_income_categories};
                break;

            case ACCOUNT_AMOUNT:
                uri = MiserContract.AccountEntry.CONTENT_URI;
                projection = new String[]{
                        MiserContract.AccountEntry.Cols._ID,
                        MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT,
                        MiserContract.AccountEntry.Cols.CATEGORY_ID

                };
                sortOrder = MiserContract.AccountEntry.Cols.CATEGORY_ID + " ASC";
                break;

            case ACCOUNT_NAME:
                uri = MiserContract.NameEntry.CONTENT_URI;
                projection = new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME};
                selection = MiserContract.NameEntry.Cols.TYPE + " = ? ";
                selectionArg = new String[]{Integer.toString(MiserContract.TYPE_ACCOUNTS)};
                break;

        }
        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Float sum;
        final int SUM_ = 0;


        switch (loader.getId()) {

            case DATA_SUM_COST: {
                if (data.moveToFirst()) {
                    sum = data.getFloat(SUM_);
                    cost_view_sum.setText(numberFormat.format(sum));
                } else {
                    cost_view_sum.setText(numberFormat.format(0));
                }
                break;
            }
            case DATA_SUM_INCOME: {
                if (data.moveToFirst()) {
                    sum = data.getFloat(SUM_);
                    income_view_sum.setText(numberFormat.format(sum));
                } else {
                    income_view_sum.setText(numberFormat.format(0));
                }
                break;
            }


            case DATA_SUM_COST_MONTH:
                if (data.moveToFirst()) {
                    data_sum_cost = data.getFloat(SUM_);
                } else {
                    data_sum_cost = 0f;
                }
                updateBudgetLeft();
                break;

            case DATA_SUM_INCOME_MONTH:
                if (data.moveToFirst()) {
                    data_sum_income = data.getFloat(SUM_);
                } else {
                    data_sum_income = 0f;
                }
                updateBudgetLeft();
                break;

            case ACCOUNT_AMOUNT: {

                int amount_index = data.getColumnIndex(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT);
                int id_index = data.getColumnIndex(MiserContract.AccountEntry.Cols._ID);
                int categoryId_index = data.getColumnIndex(MiserContract.AccountEntry.Cols.CATEGORY_ID);


                data.moveToFirst();
                for (List account : accounts) {

                    View cardView = (View) account.get(CARD);
                    Bundle info = (Bundle) cardView.getTag();

                    float sum_account = data.getFloat(amount_index);
                    info.putFloat(TAG_ACCOUNT_SUM, sum_account);

                    int _id = data.getInt(id_index);
                    info.putInt(TAG_ACCOUNT_ID, _id);

                    int category = data.getInt(categoryId_index);
                    info.putInt(TAG_ACCOUNT_CATEGORY, category);

                    ((TextView) account.get(SUM)).setText(numberFormat.format(sum_account));

                    data.moveToNext();
                }
                break;
            }
            case ACCOUNT_NAME: {

                int name_index = data.getColumnIndex(MiserContract.NameEntry.Cols.CATEGORY_NAME);
                data.moveToFirst();
                for (List account : accounts) {

                    String account_name = data.getString(name_index);
                    ((TextView) account.get(NAME)).setText(account_name);
                    View cardView = (View) account.get(CARD);
                    Bundle tag = (Bundle) cardView.getTag();
                    tag.putString(TAG_ACCOUNT_NAME, account_name);
                    data.moveToNext();
                }
                break;
            }
            case DATA_BUDGET_SUM_COST:

                if (data.moveToFirst()) {
                    data_budget_sum_cost = data.getFloat(SUM_);
                } else {
                    data_budget_sum_cost = 0f;
                }

                updateBudgetLeft();
                break;

            case DATA_BUDGET_SUM_INCOME:

                if (data.moveToFirst()) {
                    data_budget_sum_income = data.getFloat(SUM_);
                } else {
                    data_budget_sum_income = 0f;
                }

                updateBudgetLeft();
                break;
        }
    }

    private void updateBudgetLeft() {

        if (data_budget_sum_income != 0) {
            float temp_income = data_budget_sum_income - data_sum_income;
            budget_income_left.setText(numberFormat.format(temp_income));
            progress_income.setProgress(Math.round(data_sum_income * 100 / data_budget_sum_income));
        } else {
            budget_income_left.setText(getString(R.string.na));
            progress_income.setProgress(0);
        }

        if (data_budget_sum_cost != 0) {
            float temp_cost = data_budget_sum_cost - data_sum_cost;
            if (temp_cost < 0)
                budget_cost_left.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            else
                budget_cost_left.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBlue));
            budget_cost_left.setText(numberFormat.format(temp_cost));
            progress_cost.setProgress(Math.round(data_sum_cost * 100 / data_budget_sum_cost));

        } else {
            budget_cost_left.setText(R.string.na);
            progress_cost.setProgress(0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void updateToolBar(Long start_date, Long final_date) {

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(R.string.dashboard);

            StringBuilder subtitle = new StringBuilder().append(dateFormat.format(new Date(start_date)));
            subtitle.append(" - ").append(dateFormat.format(new Date(final_date))).append("   ");
            subtitle.append(currency);

            activity.getSupportActionBar().setSubtitle(subtitle);
        }
    }

    private void restartLoaderSum() {
        getLoaderManager().restartLoader(DATA_SUM_COST, null, this);
        getLoaderManager().restartLoader(DATA_SUM_INCOME, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == DATE_RANGE) {
            Date date = new Date();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            start_date = sharedPreferences.getLong(getString(R.string.pref_start_date_calendar_key), date.getTime());
            final_date = sharedPreferences.getLong(getString(R.string.pref_final_date_calendar_key), date.getTime());
            updateToolBar(start_date, final_date);
            restartLoaderSum();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals(getString(R.string.pref_currency_key))) {
            currency = sharedPreferences.getString(getString(R.string.pref_currency_key),
                    getString(R.string.pref_currency_default_RUB_value));
            updateToolBar(start_date, final_date);
            return;
        }
        if (s.equals(getString(R.string.pref_cost_quantity_key))) {
            getLoaderManager().restartLoader(DATA_SUM_COST, null, this);
            getLoaderManager().restartLoader(BARGRAPH_DATA_SET_COST, null, callback);
            return;
        }

        if (s.equals(getString(R.string.pref_income_quantity_key))) {
            getLoaderManager().restartLoader(DATA_SUM_INCOME, null, this);
            getLoaderManager().restartLoader(BARGRAPH_DATA_SET_INCOME, null, callback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
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

    private int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH);
    }

    private class GraphLoaderCallBack implements android.support.v4.app.LoaderManager.LoaderCallbacks<BarGraphSeries<DataPoint>> {

        @Override
        public Loader<BarGraphSeries<DataPoint>> onCreateLoader(int id, Bundle args) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            int type = 0;

            String category_count = "0";

            switch (id) {
                case BARGRAPH_DATA_SET_COST: {

                    category_count = sharedPreferences.getString(
                            getString(R.string.pref_cost_quantity_key),
                            getString(R.string.pref_cost_quantity_10_value));

                    type = MiserContract.TYPE_COST;

                    break;
                }
                case BARGRAPH_DATA_SET_INCOME: {

                    category_count = sharedPreferences.getString(
                            getString(R.string.pref_income_quantity_key),
                            getString(R.string.pref_income_quantity_4_value));
                    type = MiserContract.TYPE_INCOME;

                    break;
                }
            }

            return new GraphLoader(getContext(), type, category_count, MiserContract.TransactionEntry.CONTENT_URI);
        }

        @Override
        public void onLoadFinished(Loader<BarGraphSeries<DataPoint>> loader, BarGraphSeries<DataPoint> series) {

            int id = loader.getId();

            switch (id) {

                case BARGRAPH_DATA_SET_COST: {
                    cost_graph.removeAllSeries();
                    cost_graph.addSeries(series);
                    break;
                }

                case BARGRAPH_DATA_SET_INCOME: {
                    income_graph.removeAllSeries();
                    income_graph.addSeries(series);
                    break;
                }
            }

        }


        @Override
        public void onLoaderReset(Loader<BarGraphSeries<DataPoint>> loader) {
            cost_graph.removeAllSeries();
            income_graph.removeAllSeries();
        }
    }

    private class GraphListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), GraphActivity.class);

            if (view.getId() == R.id.income_graphView) {
                intent.putExtra(AbstractActivity.TYPE, MiserContract.TYPE_INCOME);
            } else {
                intent.putExtra(AbstractActivity.TYPE, MiserContract.TYPE_COST);
            }
            startActivity(intent);
        }
    }
}
