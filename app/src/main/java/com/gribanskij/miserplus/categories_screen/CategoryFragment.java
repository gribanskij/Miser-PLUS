package com.gribanskij.miserplus.categories_screen;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.add_screen.AddActivity;
import com.gribanskij.miserplus.help_screen.HelpActivity;
import com.gribanskij.miserplus.settings.SettingsActivity;
import com.gribanskij.miserplus.sql_base.MiserContract;
import com.gribanskij.miserplus.utils.DataRangeFragment;
import com.gribanskij.miserplus.utils.TimeUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.gribanskij.miserplus.AbstractActivity.CATEGORY_ID;
import static com.gribanskij.miserplus.AbstractActivity.FINAL_DATE;
import static com.gribanskij.miserplus.AbstractActivity.START_DATE;
import static com.gribanskij.miserplus.AbstractActivity.TYPE;


public class CategoryFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String LOG_TAG = CategoryFragment.class.getSimpleName();
    private static final int DATE_RANGE = 0;
    private static final int ADD_TYPE = 1;
    private static final int CATEGORY_NAME_LOADER = 11;
    private static final int CATEGORY_SUM_LOADER = 21;
    private static final String URL_APP = "https://play.google.com/store/apps/details?id=com.gribanskij.miserplus";
    private static final String URI_APP = "market://details?id=com.gribanskij.miserplus";
    private int type;
    private List<Categories> categoriesList;
    private long start_date;
    private long final_date;
    private RecycleAdapter adapter;
    private NumberFormat numberFormat;
    private SimpleDateFormat dateFormat;
    private String currency;
    private String maxCategories;
    private RecyclerView recyclerView;
    private PieChart structure_graph;
    private int[] colors;
    private List<PieEntry> entries;


    public CategoryFragment() {
    }

    public static BaseFragment newInstance(int type, long start_date, long final_date) {
        BaseFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putLong(START_DATE, start_date);
        args.putLong(FINAL_DATE, final_date);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        if (savedInstanceState != null) {
            start_date = savedInstanceState.getLong(START_DATE);
            final_date = savedInstanceState.getLong(FINAL_DATE);
            type = savedInstanceState.getInt(TYPE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                start_date = arguments.getLong(START_DATE);
                final_date = arguments.getLong(FINAL_DATE);
                type = arguments.getInt(TYPE);
            }
        }
        dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        currency = sharedPreferences.getString(getString(R.string.pref_currency_key), getString(R.string.pref_currency_default_RUB_value));


        if (type == MiserContract.TYPE_COST) {
            maxCategories = sharedPreferences.getString(getString(R.string.pref_cost_quantity_key),
                    getString(R.string.pref_cost_quantity_10_value));

        } else {
            maxCategories = sharedPreferences.getString(getString(R.string.pref_income_quantity_key),
                    getString(R.string.pref_cost_quantity_4_value));
        }

        categoriesList = getCategoryList(maxCategories);
        adapter = new RecycleAdapter(categoriesList);


        entries = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            entries.add(new PieEntry(0f, ""));
        }

        colors = new int[16];
        colors[0] = ContextCompat.getColor(getActivity(), R.color.colorRed);
        colors[1] = ContextCompat.getColor(getActivity(), R.color.colorGreen);
        colors[2] = ContextCompat.getColor(getActivity(), R.color.colorDeepPurple);
        colors[3] = ContextCompat.getColor(getActivity(), R.color.colorCyan);
        colors[4] = ContextCompat.getColor(getActivity(), R.color.colorPink);
        colors[5] = ContextCompat.getColor(getActivity(), R.color.colorIndigo);
        colors[6] = ContextCompat.getColor(getActivity(), R.color.colorTeal);
        colors[7] = ContextCompat.getColor(getActivity(), R.color.colorPurple);
        colors[8] = ContextCompat.getColor(getActivity(), R.color.colorLime);
        colors[9] = ContextCompat.getColor(getActivity(), R.color.colorYellow);
        colors[10] = ContextCompat.getColor(getActivity(), R.color.colorOrange);
        colors[11] = ContextCompat.getColor(getActivity(), R.color.colorBrown);
        colors[12] = ContextCompat.getColor(getActivity(), R.color.colorBlueGray);
        colors[13] = ContextCompat.getColor(getActivity(), R.color.colorDeepOrange);
        colors[14] = ContextCompat.getColor(getActivity(), R.color.colorLightGreen);
        colors[15] = ContextCompat.getColor(getActivity(), R.color.colorlightBlue);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CATEGORY_SUM_LOADER, null, this);
        getLoaderManager().initLoader(CATEGORY_NAME_LOADER, null, this);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.category_fragment, container, false);

        FrameLayout frame = v.findViewById(R.id.frame);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isAdsDisabled = sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false);


        if (!isAdsDisabled) {
            FrameLayout layout = v.findViewById(R.id.admob_container);
            AdView adView = new AdView(getActivity());
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(getString(R.string.dashboard_banner_ID));
            //adView.setAdUnitId(getString(R.string.test_banner_id));
            layout.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else frame.setVisibility(View.GONE);


        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);


        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setDisplayHomeAsUpEnabled(true);
            if (type == MiserContract.TYPE_COST) {
                actionBar.setTitle(getResources().getString(R.string.title_cost));
            } else {
                actionBar.setTitle(getResources().getString(R.string.title_income));
            }
        }

        upDateActionBar(start_date, final_date);

        recyclerView = v.findViewById(R.id.category_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);


        FloatingActionButton fab = v.findViewById(R.id.action_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddActivity.class);
                intent.putExtra(TYPE, type);
                intent.putExtra(CATEGORY_ID, 0);
                startActivity(intent);
            }
        });

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
                        fragment.setTargetFragment(CategoryFragment.this, DATE_RANGE);
                        fragment.show(manager, LOG_TAG);
                        break;
                    }
                    case R.id.day_radioButton: {
                        start_date = TimeUtils.getBegin_day();
                        final_date = TimeUtils.getEnd_day();
                        upDateActionBar(start_date, final_date);
                        restartLoader();
                        break;
                    }
                    case R.id.week_radioButton: {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        String temp = sharedPreferences.getString(getString(R.string.pref_week_key), getString(R.string.pref_week_default_value));
                        int first_day_week = Integer.parseInt(temp);
                        start_date = TimeUtils.getBegin_week(first_day_week);
                        final_date = TimeUtils.getEnd_week(first_day_week);
                        upDateActionBar(start_date, final_date);
                        restartLoader();
                        break;
                    }
                    case R.id.month_radioButton: {
                        start_date = TimeUtils.getBegin_month();
                        final_date = TimeUtils.getEnd_month();
                        upDateActionBar(start_date, final_date);
                        restartLoader();
                        break;
                    }
                }
            }
        };

        set.setOnClickListener(range_button_listener);
        week.setOnClickListener(range_button_listener);
        day.setOnClickListener(range_button_listener);
        month.setOnClickListener(range_button_listener);
        set.setChecked(true);


        structure_graph = v.findViewById(R.id.structure_graphView);
        PieDataSet sett = new PieDataSet(entries, "");
        sett.setValueTextSize(10);
        sett.setValueFormatter(new MyFormatter());
        sett.setColors(colors);
        sett.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.colorGrayGraph));
        PieData dataa = new PieData(sett);
        structure_graph.setData(dataa);
        structure_graph.getLegend().setEnabled(false);
        structure_graph.setCenterTextColor(ContextCompat.getColor(getActivity(), R.color.colorGrayGraph));
        structure_graph.getDescription().setText("");
        structure_graph.setUsePercentValues(true);
        structure_graph.setDrawCenterText(true);
        structure_graph.setDrawEntryLabels(false);
        structure_graph.setTransparentCircleRadius(50);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_options, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TYPE, type);
        outState.putLong(START_DATE, start_date);
        outState.putLong(FINAL_DATE, final_date);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String sortOrder = null;
        String[] selectionArg = null;

        switch (id) {

            case CATEGORY_SUM_LOADER: {
                uri = MiserContract.TransactionEntry.CONTENT_URI.buildUpon().
                        appendPath(MiserContract.TransactionEntry.Cols.CATEGORY_ID).build();

                projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT +
                        ")", MiserContract.TransactionEntry.Cols.CATEGORY_ID};
                selection = MiserContract.TransactionEntry.Cols.DATE +
                        " >= ? AND " + MiserContract.TransactionEntry.Cols.DATE +
                        " < ? AND " + MiserContract.TransactionEntry.Cols.TYPE +
                        " = ? AND " + MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Long.toString(start_date),
                        Long.toString(final_date), Integer.toString(type), maxCategories};
                sortOrder = MiserContract.TransactionEntry.Cols.CATEGORY_ID + " ASC";
                break;
            }
            case CATEGORY_NAME_LOADER: {

                uri = MiserContract.NameEntry.CONTENT_URI;

                projection = new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME,
                        MiserContract.NameEntry.Cols.CATEGORY_ID};
                selection = MiserContract.NameEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.NameEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(type), maxCategories};
                sortOrder = MiserContract.NameEntry.Cols.CATEGORY_ID + " ASC";
                break;
            }
        }

        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, sortOrder);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        float categories_sum = 0;
        String category_sum_graph;

        switch (loader.getId()) {
            case CATEGORY_SUM_LOADER: {
                int SUM = 0;
                for (Categories category : categoriesList) {
                    category.setCategory_sum(0F);
                }

                for (PieEntry entry : entries) {
                    entry.setY(categories_sum);
                }

                if (data.moveToFirst()) {
                    do {
                        Categories category = categoriesList.get(data.getInt(data.getColumnIndex(
                                MiserContract.TransactionEntry.Cols.CATEGORY_ID)));
                        category.setCategory_sum(data.getFloat(SUM));
                        PieEntry entry = entries.get(data.getInt(data.getColumnIndex(
                                MiserContract.TransactionEntry.Cols.CATEGORY_ID)));
                        entry.setY(data.getFloat(SUM));
                        categories_sum = categories_sum + data.getFloat(SUM);
                    } while (data.moveToNext());
                }
                adapter.notifyDataSetChanged();

                if (categories_sum == 0) {
                    category_sum_graph = getString(R.string.no_data_graph);
                } else {
                    category_sum_graph = numberFormat.format(categories_sum);
                }

                structure_graph.setCenterText(category_sum_graph);
                structure_graph.notifyDataSetChanged();
                structure_graph.animateY(3000, Easing.EasingOption.EaseOutBack);
                structure_graph.invalidate();


                break;
            }

            case CATEGORY_NAME_LOADER: {

                if (!data.moveToFirst()) {
                    break;
                } else {
                    do {
                        Categories category = categoriesList.get(data.getInt(
                                data.getColumnIndex(MiserContract.NameEntry.Cols.CATEGORY_ID)));
                        category.setCategory_name(data.getString(data.getColumnIndex(MiserContract.NameEntry.Cols.CATEGORY_NAME)));
                    } while (data.moveToNext());
                }
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(CATEGORY_SUM_LOADER, null, this);
    }


    private void upDateActionBar(Long start_date, Long end_date) {

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;
        StringBuilder subtitle = new StringBuilder().append(dateFormat.format(new Date(start_date)));
        subtitle.append(" - ").append(dateFormat.format(new Date(end_date))).append("   ");
        subtitle.append(currency);
        actionBar.setSubtitle(subtitle);
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
            upDateActionBar(start_date, final_date);
            restartLoader();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals(getString(R.string.pref_currency_key))) {
            currency = sharedPreferences.getString(getString(R.string.pref_currency_key),
                    getString(R.string.pref_currency_default_RUB_value));
            upDateActionBar(start_date, final_date);
            return;
        }
        if (type == MiserContract.TYPE_COST && s.equals(getString(R.string.pref_cost_quantity_key))) {

            maxCategories = sharedPreferences.getString(getString(R.string.pref_cost_quantity_key),
                    getString(R.string.pref_cost_quantity_10_value));
            categoriesList = getCategoryList(maxCategories);
            adapter = new RecycleAdapter(categoriesList);
            recyclerView.swapAdapter(adapter, true);

            getLoaderManager().restartLoader(CATEGORY_SUM_LOADER, null, this);
            getLoaderManager().restartLoader(CATEGORY_NAME_LOADER, null, this);
            return;
        }

        if (type == MiserContract.TYPE_INCOME && s.equals(getString(R.string.pref_income_quantity_key))) {

            maxCategories = sharedPreferences.getString(getString(R.string.pref_income_quantity_key),
                    getString(R.string.pref_cost_quantity_4_value));
            categoriesList = getCategoryList(maxCategories);
            adapter = new RecycleAdapter(categoriesList);
            recyclerView.swapAdapter(adapter, true);

            getLoaderManager().destroyLoader(CATEGORY_NAME_LOADER);
            getLoaderManager().destroyLoader(CATEGORY_SUM_LOADER);

            getLoaderManager().initLoader(CATEGORY_SUM_LOADER, null, this);
            getLoaderManager().initLoader(CATEGORY_NAME_LOADER, null, this);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    private List<Categories> getCategoryList(String maxCountCategories) {

        List<Categories> categoriesList = new ArrayList<>();
        int maxCategories_int = Integer.parseInt(maxCountCategories);
        for (int i = 0; i < maxCategories_int; i++) {
            categoriesList.add(new Categories("", 0));
        }
        return categoriesList;
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

    private class RecycleAdapter extends RecyclerView.Adapter<CategoryHolder> {
        private List<Categories> list;

        private RecycleAdapter(List<Categories> list) {
            this.list = list;
        }

        @Override
        public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.view_holder_category, parent, false);
            return new CategoryHolder(v);
        }

        @Override
        public void onBindViewHolder(CategoryHolder holder, int position) {
            Categories category = list.get(position);
            holder.mName.setText(category.getCategory_name());
            holder.mSum.setText(numberFormat.format(category.getCategory_sum()));
            holder.mColor.setBackgroundColor(colors[position]);
            holder.category = position;
        }

        @Override
        public int getItemCount() {
            if (list == null) {
                return 0;
            }
            return list.size();
        }
    }

    private class CategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mName;
        TextView mSum;
        int category;
        View mColor;

        private CategoryHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.category_name);
            mSum = view.findViewById(R.id.category_amount);
            mColor = view.findViewById(R.id.color_view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            String categoryName = mName.getText().toString();
            getCallback().onCategorySelected(type, start_date, final_date, category, categoryName);

        }
    }
}
