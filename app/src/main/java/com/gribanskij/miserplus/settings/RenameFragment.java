package com.gribanskij.miserplus.settings;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.sql_base.MiserContract;

import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;


public class RenameFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String DIALOG_EDIT = "edit";
    private static final int CURSOR_INCOME_CATEGORY_NAMES = 10;
    private static final int CURSOR_COST_CATEGORY_NAMES = 20;
    private static final int CURSOR_ACCOUNT_CATEGORY_NAMES = 30;

    private CursorAdapter mAdapter;
    private int switch_cursor_title;

    public RenameFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            switch_cursor_title = getArguments().getInt(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, 0);
        }

        if (savedInstanceState != null) {
            switch_cursor_title = savedInstanceState.getInt(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.rename_fragment, container, false);
        ListView mListView = v.findViewById(R.id.list_rename_fragment);
        mListView.setDivider(null);
        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
                new String[]{MiserContract.NameEntry.Cols.CATEGORY_NAME}, new int[]{android.R.id.text1},
                FLAG_REGISTER_CONTENT_OBSERVER);
        mListView.setAdapter(mAdapter);


        AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                TextView view = v.findViewById(android.R.id.text1);
                String oldName = view.getText().toString();
                FragmentManager manager = getFragmentManager();
                RenameCategoryDialog dialog = RenameCategoryDialog.newInstance(id, oldName);
                dialog.show(manager, DIALOG_EDIT);

            }

        };

        mListView.setOnItemClickListener(mMessageClickedHandler);


        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity.getSupportActionBar() != null) {
            ActionBar actionBar = appCompatActivity.getSupportActionBar();

            switch (switch_cursor_title) {
                case MiserContract.TYPE_ACCOUNTS: {
                    actionBar.setTitle(getString(R.string.title_account));
                    break;
                }
                case MiserContract.TYPE_COST: {
                    actionBar.setTitle(getString(R.string.title_cost));
                    break;
                }
                case MiserContract.TYPE_INCOME: {
                    actionBar.setTitle(getString(R.string.title_income));
                    break;
                }
                default:
                    break;
            }
        }
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switch (switch_cursor_title) {
            case MiserContract.TYPE_ACCOUNTS: {
                getLoaderManager().initLoader(CURSOR_ACCOUNT_CATEGORY_NAMES, null, this);
                break;
            }
            case MiserContract.TYPE_COST: {
                getLoaderManager().initLoader(CURSOR_COST_CATEGORY_NAMES, null, this);
                break;
            }
            case MiserContract.TYPE_INCOME: {
                getLoaderManager().initLoader(CURSOR_INCOME_CATEGORY_NAMES, null, this);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        String[] projection = new String[]{MiserContract.NameEntry.Cols._ID, MiserContract.NameEntry.Cols.CATEGORY_NAME};
        String selection = MiserContract.NameEntry.Cols.TYPE + " = ? " + "AND " + MiserContract.NameEntry.Cols.CATEGORY_ID + "< ?";
        String sortOrder = null;
        String[] selectionArg = null;
        Uri uri = MiserContract.NameEntry.CONTENT_URI;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        switch (id) {
            case CURSOR_ACCOUNT_CATEGORY_NAMES: {
                String accounts = preferences.getString(getString(R.string.pref_account_quantity_key), getString(R.string.pref_account_quantity_4_value));
                selectionArg = new String[]{Integer.toString(MiserContract.TYPE_ACCOUNTS), accounts};
                break;
            }
            case CURSOR_COST_CATEGORY_NAMES: {
                String cost = preferences.getString(getString(R.string.pref_cost_quantity_key), getString(R.string.pref_cost_quantity_10_value));
                selectionArg = new String[]{Integer.toString(MiserContract.TYPE_COST), cost};
                break;
            }
            case CURSOR_INCOME_CATEGORY_NAMES: {
                String income = preferences.getString(getString(R.string.pref_income_quantity_key), getString(R.string.pref_income_quantity_4_value));
                selectionArg = new String[]{Integer.toString(MiserContract.TYPE_INCOME), income};
                break;
            }
            default: {
                break;
            }
        }

        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, switch_cursor_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
