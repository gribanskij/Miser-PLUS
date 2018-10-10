package com.gribanskij.miserplus.help_screen;

import android.os.Bundle;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;

/**
 * Created by SESA175711 on 18.10.2017.
 */

public class HelpActivity extends AbstractActivity {
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
    protected BaseFragment createFragment(Bundle savedInstanceState) {

        return HelpFragment.newInstance(getIntent().getBooleanExtra(AbstractActivity.BUDGET_ADD, false));
    }
}
