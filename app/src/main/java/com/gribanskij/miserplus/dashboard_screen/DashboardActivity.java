package com.gribanskij.miserplus.dashboard_screen;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.categories_screen.CategoryFragment;
import com.gribanskij.miserplus.detail_screen.DetailFragment;
import com.gribanskij.miserplus.detail_screen_account.DetailAccountFragment;
import com.gribanskij.miserplus.util.IabHelper;
import com.gribanskij.miserplus.util.IabResult;
import com.gribanskij.miserplus.util.Inventory;
import com.gribanskij.miserplus.util.Purchase;
import com.gribanskij.miserplus.utils.AddExpensesReceiver;
import com.gribanskij.miserplus.utils.TimeUtils;

import java.util.Calendar;

import static android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;


public class DashboardActivity extends AbstractActivity {

    private static final String LOG_TAG = DashboardActivity.class.getSimpleName();
    private static final int RC_REQUEST = 10004;
    private final String SKU_DISABLE_ADS = "sku_ads_disabling";

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {

            if (result.isFailure()) return;

            if (inv.hasPurchase(SKU_DISABLE_ADS)) {
                SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.disable_adMob_key), true);
                editor.apply();
            }
        }
    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {

            if (result.isFailure()) {
                Toast.makeText(getApplicationContext(), getString(R.string.info_error_ads_disabling),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (info.getSku().equals(SKU_DISABLE_ADS)) {
                Toast.makeText(getApplicationContext(), getString(R.string.info_disable_ads),
                        Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.disable_adMob_key), true);
                editor.apply();
            }
        }
    };
    private IabHelper mHelper;

    @Override
    protected BaseFragment createFragment(Bundle savedInstanceState) {
        long start_current_month = TimeUtils.getBegin_month();
        long final_current_month = TimeUtils.getEnd_month();
        return DashboardFragment.newInstance(start_current_month, final_current_month);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);


        //Start 9:00 pm notification every day.

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        if (!sharedPreferences.getBoolean((getString(R.string.pref_isNotification_key)), false)) {

            Intent intent = new Intent();
            intent.setAction(AddExpensesReceiver.ACTION_ADD_EXPENSES);
            intent.setClass(this, AddExpensesReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    178, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 21);
            calendar.set(Calendar.MINUTE, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.pref_isNotification_key), true);
            editor.apply();
        }


        if (!sharedPreferences.getBoolean(getString(R.string.pref_isRate_key), false)) {
            int time = sharedPreferences.getInt(getString(R.string.pref_time_to_rate_key), 0);
            SharedPreferences.Editor editor_ = sharedPreferences.edit();
            editor_.putInt(getString(R.string.pref_time_to_rate_key), ++time);
            editor_.putBoolean(getString(R.string.pref_isRate_key), false);
            editor_.apply();
        }

        boolean isAdsDisabled = sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false);
        if (!isAdsDisabled) {
            MobileAds.initialize(this, getString(R.string.ADMOB_APP_ID));
        }

        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.base64_1));
        builder.append(getString(R.string.base64_2));
        builder.append(getString(R.string.base64_3));
        builder.append(getString(R.string.base64_4));
        String base64EncodedPublicKey;
        base64EncodedPublicKey = builder.toString();

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d(LOG_TAG, "Problem setting up In-app Billing: " + result);
                    return;
                }
                Log.d(LOG_TAG, "Setup successful. Querying inventory.");

                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.d(LOG_TAG, "Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }


    @Override
    public void onCategorySelected(int type, long start_date, long final_date) {

        int container = R.id.dash_container;
        //if (findViewById(R.id.detail_container) != null) {
        //    container = R.id.detail_container;
        //}

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BaseFragment fragment = CategoryFragment.newInstance(type, start_date, final_date);
        ft.replace(container, fragment);
        ft.addToBackStack(null);
        ft.setTransition(TRANSIT_FRAGMENT_OPEN);
        ft.commit();

    }

    @Override
    public void onCategorySelected(int type, long start_date, long final_date, int categoryID, String categoryName) {

        int container = R.id.dash_container;
        //if (findViewById(R.id.detail_container) != null) {
        //    container = R.id.detail_container;
        //}

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BaseFragment fragment = DetailFragment.newInstance(type, start_date, final_date, categoryID, categoryName);
        ft.replace(container, fragment);
        ft.addToBackStack(null);
        ft.setTransition(TRANSIT_FRAGMENT_OPEN);
        ft.commit();

    }

    @Override
    public void onCategorySelected(long start_date, long final_date, int categoryID, String categoryName) {

        int container = R.id.dash_container;
        //if (findViewById(R.id.detail_container) != null) {
        //    container = R.id.detail_container;
        //}
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BaseFragment fragment = DetailAccountFragment.newInstance(start_date, final_date, categoryID, categoryName);
        ft.replace(container, fragment);
        ft.addToBackStack(null);
        ft.setTransition(TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public void onFragmentChange(int frg, Bundle parameters) {

        switch (frg) {

            case BUDGET_DASHBOARD_FRAGMENT:
                //if (parameters != null) {
                //    int month = parameters.getInt(AbstractActivity.MONTH, getCurrentMonth());
                //    int type = parameters.getInt(AbstractActivity.TYPE, MiserContract.TYPE_COST);
                //    int container = R.id.dash_container;
                //    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                //    BaseFragment fragment = BudgetFragment.newInstance(type, month);
                //    ft.replace(container, fragment);
                //    ft.addToBackStack(null);
                //    ft.setTransition(TRANSIT_FRAGMENT_OPEN);
                //    ft.commit();
                //}
                break;

            case BUDGET_CATEGORY_FRAGMENT:
                break;

            case BUDGET_DETAIL_FRAGMENT:
                break;

            default:
                break;

        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.disable_ads: {

                SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
                if (sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false))
                    return true;

                try {
                    mHelper.launchPurchaseFlow(this, SKU_DISABLE_ADS, RC_REQUEST, mPurchaseFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.info_error_ads_disabling),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (mHelper == null || !mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;
    }

    private int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH);
    }
}
