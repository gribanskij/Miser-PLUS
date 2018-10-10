package com.gribanskij.miserplus;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by SESA175711 on 22.09.2017.
 */

public class BaseFragment extends Fragment {


    private Callbacks mCallback;

    public BaseFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callbacks) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    protected Callbacks getCallback() {
        return mCallback;
    }

    public interface Callbacks {

        void onCategorySelected(int type, long start_date, long final_date);

        void onCategorySelected(int type, long start_date, long final_date, int categoryID, String categoryName);

        void onCategorySelected(long start_date, long final_date, int categoryID, String categoryName);

        void onFragmentChange(int fragment, Bundle parameters);

    }
}


