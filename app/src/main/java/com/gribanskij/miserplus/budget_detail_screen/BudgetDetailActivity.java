package com.gribanskij.miserplus.budget_detail_screen;


import android.content.Intent;
import android.os.Bundle;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;


public class BudgetDetailActivity extends AbstractActivity {


    private int month;
    private String categoryName;
    private int category_id;
    private int type;

    @Override
    protected BaseFragment createFragment(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            month = savedInstanceState.getInt(AbstractActivity.MONTH, 0);
            categoryName = savedInstanceState.getString(AbstractActivity.CATEGORY_NAME);
            category_id = savedInstanceState.getInt(AbstractActivity.CATEGORY_ID, 0);
            type = savedInstanceState.getInt(AbstractActivity.TYPE, 0);
        } else {
            Intent intent = getIntent();
            month = intent.getIntExtra(AbstractActivity.MONTH, 0);
            categoryName = intent.getStringExtra(AbstractActivity.CATEGORY_NAME);
            category_id = intent.getIntExtra(AbstractActivity.CATEGORY_ID, 0);
            type = intent.getIntExtra(AbstractActivity.TYPE, 0);
        }

        return BudgetDetailFragment.newInstance(type, month, category_id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            categoryName = savedInstanceState.getString(AbstractActivity.CATEGORY_NAME);
        getSupportActionBar().setTitle(categoryName);
    }

    @Override
    public void onCategorySelected(int type, long start_date, long final_date) {

    }

    @Override
    public void onCategorySelected(int type, long start_date, long final_date, int categoryID, String categoryName) {

    }

    @Override
    public void onCategorySelected(long start_date, long final_date, int categoryID, String categoryName) {

    }

    @Override
    public void onFragmentChange(int fragment, Bundle parameters) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AbstractActivity.MONTH, month);
        outState.putString(AbstractActivity.CATEGORY_NAME, categoryName);
        outState.putInt(AbstractActivity.TYPE, type);
        outState.putInt(AbstractActivity.CATEGORY_ID, category_id);
    }
}
