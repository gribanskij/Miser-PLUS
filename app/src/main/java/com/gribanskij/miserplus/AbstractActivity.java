package com.gribanskij.miserplus;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


public abstract class AbstractActivity extends AppCompatActivity implements BaseFragment.Callbacks {


    public static final String START_DATE = "start_date";
    public static final String FINAL_DATE = "final_date";
    public static final String TYPE = "type";
    public static final String MONTH = "month";
    public static final String CATEGORY_NAME = "name";
    public static final String CATEGORY_ID = "category_id";
    public static final String TRANSACTION = "transaction";
    public static final String ACCOUNT_ID = "account_id";
    public static final String BUDGET_ADD = "budget";

    // constants for swishing fragments interface
    public static final int BUDGET_DASHBOARD_FRAGMENT = 0;
    public static final int BUDGET_CATEGORY_FRAGMENT = 1;
    public static final int BUDGET_DETAIL_FRAGMENT = 2;


    protected abstract BaseFragment createFragment(Bundle savedInstanceState);

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_master;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.dash_container);
        if (fragment == null) {
            fragment = createFragment(savedInstanceState);
            fm.beginTransaction().add(R.id.dash_container, fragment).commit();
        }
    }
}
