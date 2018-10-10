package com.gribanskij.miserplus.graph_screen;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;


/**
 * Created by santy on 07.10.2017.
 */

public class GraphActivity extends AbstractActivity {


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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Intent intent = getIntent();
        int mType = intent.getIntExtra(AbstractActivity.TYPE, 0);
        return GraphFragment.newInstance(mType);
    }
}
