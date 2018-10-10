package com.gribanskij.miserplus.budget_screen;

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

import java.util.Calendar;


public class BudgetActivity extends AppCompatActivity {


    private PagerAdapter adapter;
    private ViewPager viewPager;
    private int month;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            month = savedInstanceState.getInt(AbstractActivity.MONTH, getCurrentMonth());
        } else month = getCurrentMonth();

        setContentView(R.layout.budget_activity);
        adapter = new MyPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(month);

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


        getSupportActionBar().setTitle(R.string.budgeting);


        FloatingActionButton actionButton = findViewById(R.id.action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                intent.putExtra(AbstractActivity.TYPE, 0);
                intent.putExtra(AbstractActivity.BUDGET_ADD, true);
                intent.putExtra(AbstractActivity.MONTH, viewPager.getCurrentItem());
                startActivity(intent);
            }
        });
    }

    private int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH);
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewPager.setCurrentItem(month);

    }

    @Override
    protected void onPause() {
        super.onPause();
        month = viewPager.getCurrentItem();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AbstractActivity.MONTH, viewPager.getCurrentItem());
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        String[] months = getResources().getStringArray(R.array.months);

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int month) {
            return BudgetFragment.newInstance(0, month);
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
