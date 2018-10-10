package com.gribanskij.miserplus.budget_category_screen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.add_screen.AddActivity;
import com.gribanskij.miserplus.sql_base.MiserContract;

public class BudgetCategoryActivity extends AppCompatActivity {


    private static final String LOG_TAG = BudgetCategoryActivity.class.getSimpleName();


    private PagerAdapter adapter;
    private int budgetMonth;
    private int budgetType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            budgetMonth = savedInstanceState.getInt(AbstractActivity.MONTH, 0);
            budgetType = savedInstanceState.getInt(AbstractActivity.TYPE, 0);
        } else {
            Intent intent = getIntent();
            budgetMonth = intent.getIntExtra(AbstractActivity.MONTH, 0);
            budgetType = intent.getIntExtra(AbstractActivity.TYPE, 0);
        }

        setContentView(R.layout.budget_activity);

        FrameLayout frame = findViewById(R.id.frame);


        adapter = new BudgetCategoryActivity.MyPagerAdapter(getSupportFragmentManager());

        final ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(budgetMonth);

        if (budgetType == MiserContract.TYPE_COST) {
            getSupportActionBar().setTitle(R.string.budgeting_cat_cost);
        } else {
            getSupportActionBar().setTitle(R.string.budgeting_cat_income);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAdsDisabled = sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false);

        if (!isAdsDisabled) {

            FrameLayout layout = findViewById(R.id.admob_container);
            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.SMART_BANNER);
            //adView.setAdUnitId(getString(R.string.test_banner_id));
            adView.setAdUnitId(getString(R.string.dashboard_banner_ID));
            layout.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }


        FloatingActionButton actionButton = findViewById(R.id.action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                intent.putExtra(AbstractActivity.TYPE, budgetType);
                intent.putExtra(AbstractActivity.BUDGET_ADD, true);
                intent.putExtra(AbstractActivity.MONTH, viewPager.getCurrentItem());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AbstractActivity.MONTH, budgetMonth);
        outState.putInt(AbstractActivity.TYPE, budgetType);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        String[] months = getResources().getStringArray(R.array.months);

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int month) {
            return BudgetCategoryFragment.newInstance(month, budgetType);
        }

        @Override
        public int getCount() {
            return months.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int month) {
            return months[month];
        }
    }
}
