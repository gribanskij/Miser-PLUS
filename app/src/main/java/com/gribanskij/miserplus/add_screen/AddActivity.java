package com.gribanskij.miserplus.add_screen;

import android.content.Intent;
import android.os.Bundle;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.sql_base.MiserContract;

import java.util.Calendar;


public class AddActivity extends AbstractActivity {


    @Override
    public BaseFragment createFragment(Bundle bundle) {
        Intent intent = getIntent();
        int mType = intent.getIntExtra(AbstractActivity.TYPE, MiserContract.TYPE_COST);
        int mCategory = intent.getIntExtra(AbstractActivity.CATEGORY_ID, 0);
        boolean mBudget = intent.getBooleanExtra(AbstractActivity.BUDGET_ADD, false);
        int mMonth = intent.getIntExtra(AbstractActivity.MONTH, Calendar.JANUARY);
        return AddFragment.newInstance(mType, mCategory, mBudget, mMonth);
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

}
