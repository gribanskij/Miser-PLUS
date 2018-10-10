package com.gribanskij.miserplus.budget_category_screen;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.budget_detail_screen.BudgetDetailActivity;
import com.gribanskij.miserplus.categories_screen.Categories;
import com.gribanskij.miserplus.help_screen.HelpActivity;
import com.gribanskij.miserplus.sql_base.MiserContract;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetCategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = BudgetCategoryFragment.class.getSimpleName();


    private static final int CATEGORY_NAME_LOADER = 600;
    private static final int CATEGORY_SUM_LOADER = 610;
    private int budget_type;
    private int budget_month;
    private NumberFormat numberFormat;
    private String currency;
    private String maxCategories;
    private List<Categories> categoriesList;
    private RecycleAdapter adapter;

    public BudgetCategoryFragment() {
    }

    public static Fragment newInstance(int month, int type) {

        Fragment fragment = new BudgetCategoryFragment();
        Bundle args = new Bundle();
        args.putInt(AbstractActivity.TYPE, type);
        args.putInt(AbstractActivity.MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            budget_month = savedInstanceState.getInt(AbstractActivity.MONTH);
            budget_type = savedInstanceState.getInt(AbstractActivity.TYPE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                budget_type = arguments.getInt(AbstractActivity.TYPE);
                budget_month = arguments.getInt(AbstractActivity.MONTH);
            }
        }

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        currency = sharedPreferences.getString(getString(R.string.pref_currency_key),
                getString(R.string.pref_currency_default_RUB_value));

        if (budget_type == MiserContract.TYPE_COST) {
            maxCategories = sharedPreferences.getString(getString(R.string.pref_cost_quantity_key),
                    getString(R.string.pref_cost_quantity_10_value));
        } else {
            maxCategories = sharedPreferences.getString(getString(R.string.pref_income_quantity_key),
                    getString(R.string.pref_cost_quantity_4_value));
        }

        categoriesList = getCategoryList(maxCategories);
        adapter = new RecycleAdapter(categoriesList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CATEGORY_NAME_LOADER, null, this);
        getLoaderManager().initLoader(CATEGORY_SUM_LOADER, null, this);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.budget_category_fragment, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.budget_recycler_category);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        FrameLayout frame = v.findViewById(R.id.frame);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isAdsDisabled = sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false);

        if (isAdsDisabled) {
            frame.setVisibility(View.GONE);
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AbstractActivity.TYPE, budget_type);
        outState.putInt(AbstractActivity.MONTH, budget_month);
    }

    private List<Categories> getCategoryList(String maxCountCategories) {

        List<Categories> categoriesList = new ArrayList<>();
        int maxCategories_int = Integer.parseInt(maxCountCategories);
        for (int i = 0; i < maxCategories_int; i++) {
            categoriesList.add(new Categories("", 0));
        }
        return categoriesList;
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
                uri = MiserContract.BudgetEntry.CONTENT_URI.buildUpon().
                        appendPath(MiserContract.BudgetEntry.Cols.CATEGORY_ID).build();

                projection = new String[]{"SUM" + "(" + MiserContract.BudgetEntry.Cols.BUDGET_SUM + ")",
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID};
                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(budget_month),
                        Integer.toString(budget_type), maxCategories};
                sortOrder = MiserContract.BudgetEntry.Cols.CATEGORY_ID + " ASC";
                break;
            }
            case CATEGORY_NAME_LOADER: {
                uri = MiserContract.NameEntry.CONTENT_URI;
                projection = new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME,
                        MiserContract.NameEntry.Cols.CATEGORY_ID};
                selection = MiserContract.NameEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.NameEntry.Cols.CATEGORY_ID + " < ?";
                selectionArg = new String[]{Integer.toString(budget_type), maxCategories};
                sortOrder = MiserContract.NameEntry.Cols.CATEGORY_ID + " ASC";
                break;
            }
            default:
                Log.e(LOG_TAG, "LOADER ID ERROR IN onCREATE");
                break;
        }
        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
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
            case CATEGORY_SUM_LOADER: {
                int SUM = 0;
                for (Categories category : categoriesList) {
                    category.setCategory_sum(0F);
                }
                if (data.moveToFirst()) {
                    do {
                        Categories category = categoriesList.get(data.getInt(data.getColumnIndex(
                                MiserContract.BudgetEntry.Cols.CATEGORY_ID)));
                        category.setCategory_sum(data.getFloat(SUM));
                    } while (data.moveToNext());
                }
                adapter.notifyDataSetChanged();
                break;
            }
            default:
                Log.e(LOG_TAG, "LOADER ID ERROR IN onFINISHED");
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private class RecycleAdapter extends RecyclerView.Adapter<CategoryHolder> {
        private List<Categories> list;

        private RecycleAdapter(List<Categories> list) {
            this.list = list;
        }

        @Override
        public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.vh_budget_category, parent, false);
            return new CategoryHolder(v);
        }

        @Override
        public void onBindViewHolder(CategoryHolder holder, int position) {
            Categories category = list.get(position);
            holder.mName.setText(category.getCategory_name());
            holder.mSum.setText(numberFormat.format(category.getCategory_sum()));
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

        private CategoryHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.category_name);
            mSum = view.findViewById(R.id.category_amount);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            String categoryName = mName.getText().toString();
            Intent intent = new Intent(getActivity(), BudgetDetailActivity.class);

            intent.putExtra(AbstractActivity.CATEGORY_ID, category);
            intent.putExtra(AbstractActivity.MONTH, budget_month);
            intent.putExtra(AbstractActivity.TYPE, budget_type);
            intent.putExtra(AbstractActivity.CATEGORY_NAME, categoryName);

            startActivity(intent);
        }
    }
}
