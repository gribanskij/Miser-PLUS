package com.gribanskij.miserplus.edit_screen;

import android.content.Intent;
import android.os.Bundle;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;


public class EditActivity extends AbstractActivity {

    @Override
    public BaseFragment createFragment(Bundle bundle) {
        Intent intent = getIntent();
        Bundle transaction = intent.getBundleExtra(AbstractActivity.TRANSACTION);
        return EditFragment.newInstance(transaction);
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
