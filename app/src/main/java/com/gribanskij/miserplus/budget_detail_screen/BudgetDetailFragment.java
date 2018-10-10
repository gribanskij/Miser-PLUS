package com.gribanskij.miserplus.budget_detail_screen;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.add_screen.AddActivity;
import com.gribanskij.miserplus.asynctask.AsyncQueryTask;
import com.gribanskij.miserplus.edit_screen.EditActivity;
import com.gribanskij.miserplus.edit_screen.EditFragment;
import com.gribanskij.miserplus.help_screen.HelpActivity;
import com.gribanskij.miserplus.sql_base.MiserContract;

import java.text.NumberFormat;
import java.util.Locale;


public class BudgetDetailFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int BUDGET_CATEGORY_DETAIL_LOADER = 700;
    private static final int TOKEN_DELETE_BUDGET_ITEM = 11;

    private int budget_type;
    private int budget_month;
    private int category_id;
    private TextView emptyView;
    private RecyclerDetailAdapter mAdapter;
    private NumberFormat numberFormat;
    private String[] months;

    private Paint p = new Paint();
    private Snackbar s;


    public BudgetDetailFragment() {
    }

    public static BudgetDetailFragment newInstance(int type, int month, int category_id) {
        BudgetDetailFragment fragment = new BudgetDetailFragment();
        Bundle args = new Bundle();
        args.putInt(AbstractActivity.TYPE, type);
        args.putInt(AbstractActivity.MONTH, month);
        args.putInt(AbstractActivity.CATEGORY_ID, category_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(BUDGET_CATEGORY_DETAIL_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            budget_type = savedInstanceState.getInt(AbstractActivity.TYPE);
            budget_month = savedInstanceState.getInt(AbstractActivity.MONTH);
            category_id = savedInstanceState.getInt(AbstractActivity.CATEGORY_ID);
        } else {
            if (getArguments() != null) {
                budget_type = getArguments().getInt(AbstractActivity.TYPE);
                budget_month = getArguments().getInt(AbstractActivity.MONTH);
                category_id = getArguments().getInt(AbstractActivity.CATEGORY_ID);
            }
        }

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);
        months = getResources().getStringArray(R.array.months);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.budget_detail_fragment, container, false);

        RecyclerView mRecyclerView = v.findViewById(R.id.detail_recycler);
        emptyView = v.findViewById(R.id.emptyView);
        mAdapter = new RecyclerDetailAdapter(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);


        FrameLayout frame = v.findViewById(R.id.frame);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isAdsDisabled = sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false);

        if (!isAdsDisabled) {
            FrameLayout ads_container = v.findViewById(R.id.admob_container);
            AdView adView = new AdView(getActivity());
            adView.getHeight();
            adView.setAdSize(AdSize.SMART_BANNER);
            //adView.setAdUnitId(getString(R.string.test_banner_id));
            adView.setAdUnitId(getString(R.string.dashboard_banner_ID));
            ads_container.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else frame.setVisibility(View.GONE);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                Bundle budgetItem = (Bundle) viewHolder.itemView.getTag();

                if (swipeDir == ItemTouchHelper.LEFT) {
                    s = Snackbar.make(viewHolder.itemView, R.string.remove_action, Snackbar.LENGTH_LONG);
                    s.setAction(R.string.undo_action, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            s.dismiss();
                        }
                    });
                    s.addCallback(new SBlistener(budgetItem.getInt(EditFragment._ID)));
                    s.show();

                } else {
                    mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    Intent intent = new Intent(getActivity(), EditActivity.class);
                    intent.putExtra(AbstractActivity.TRANSACTION, budgetItem);
                    startActivity(intent);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        p.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mode_edit_white_24dp);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    } else {
                        p.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white_24dp);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        c.drawBitmap(icon, null, icon_dest, p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(mRecyclerView);


        FloatingActionButton floatingActionButton = v.findViewById(R.id.fab_detail);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddActivity.class);
                intent.putExtra(AbstractActivity.BUDGET_ADD, true);
                intent.putExtra(AbstractActivity.TYPE, budget_type);
                intent.putExtra(AbstractActivity.MONTH, budget_month);
                intent.putExtra(AbstractActivity.CATEGORY_ID, category_id);
                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_option_budget, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_item_info:
                intent = new Intent(getActivity(), HelpActivity.class);
                intent.putExtra(AbstractActivity.BUDGET_ADD, true);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AbstractActivity.MONTH, budget_month);
        outState.putInt(AbstractActivity.TYPE, budget_type);
        outState.putInt(AbstractActivity.CATEGORY_ID, category_id);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String sortOrder = null;
        String[] selectionArg = null;

        switch (id) {

            case BUDGET_CATEGORY_DETAIL_LOADER: {
                uri = MiserContract.BudgetEntry.CONTENT_URI;

                projection = new String[]{
                        MiserContract.BudgetEntry.Cols._ID,
                        MiserContract.BudgetEntry.Cols.BUDGET_DESCRIPTION,
                        MiserContract.BudgetEntry.Cols.BUDGET_SUM,
                        MiserContract.BudgetEntry.Cols.BUDGET_MONTH,
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID,
                        MiserContract.BudgetEntry.Cols.TYPE,
                        MiserContract.BudgetEntry.Cols.BUDGET_INSERT_DATE};

                selection = MiserContract.BudgetEntry.Cols.BUDGET_MONTH + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.TYPE + " = ? AND " +
                        MiserContract.BudgetEntry.Cols.CATEGORY_ID + " = ? ";

                selectionArg = new String[]{Integer.toString(budget_month),
                        Integer.toString(budget_type), Integer.toString(category_id)};
                sortOrder = MiserContract.BudgetEntry.Cols.BUDGET_INSERT_DATE + " ASC";
                break;
            }
        }
        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case BUDGET_CATEGORY_DETAIL_LOADER: {
                mAdapter.swapCursor(data);
                if (data != null && data.moveToFirst())
                    emptyView.setVisibility(View.GONE);
                else
                    emptyView.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    private class RecyclerDetailAdapter extends RecyclerView.Adapter<DetailHolder> {


        private Cursor mCursor;
        private Context mContext;

        RecyclerDetailAdapter(Context mContext) {
            this.mContext = mContext;
        }

        public Cursor getCursor() {
            return mCursor;
        }


        private Cursor swapCursor(Cursor newCursor) {

            if (mCursor == newCursor) {
                return null;
            }
            Cursor temp = mCursor;
            mCursor = newCursor;
            if (newCursor != null) {
                notifyDataSetChanged();
            }
            return temp;
        }

        @Override
        public DetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View vhView = LayoutInflater.from(mContext).inflate(R.layout.view_holder, parent, false);
            return new DetailHolder(vhView);
        }

        @Override
        public void onBindViewHolder(DetailHolder holder, int position) {

            int description = mCursor.getColumnIndex(MiserContract.BudgetEntry.Cols.BUDGET_DESCRIPTION);
            int amount = mCursor.getColumnIndex(MiserContract.BudgetEntry.Cols.BUDGET_SUM);
            int month = mCursor.getColumnIndex(MiserContract.BudgetEntry.Cols.BUDGET_MONTH);
            int id = mCursor.getColumnIndex(MiserContract.BudgetEntry.Cols._ID);
            int type = mCursor.getColumnIndex(MiserContract.BudgetEntry.Cols.TYPE);
            int category = mCursor.getColumnIndex(MiserContract.BudgetEntry.Cols.CATEGORY_ID);
            int insertDate = mCursor.getColumnIndex(MiserContract.BudgetEntry.Cols.BUDGET_INSERT_DATE);


            if (mCursor.moveToPosition(position)) {

                Bundle transaction = new Bundle();
                transaction.putInt(EditFragment.CATEGORY, mCursor.getInt(category));
                transaction.putInt(EditFragment._TYPE, mCursor.getInt(type));
                transaction.putInt(EditFragment._ID, mCursor.getInt(id));
                transaction.putLong(EditFragment.DATE, mCursor.getLong(insertDate));
                transaction.putFloat(EditFragment.SUM, mCursor.getFloat(amount));
                transaction.putString(EditFragment.DESCRIPTION, mCursor.getString(description));
                transaction.putInt(EditFragment.ACCOUNT, 0);
                transaction.putBoolean(EditFragment.BUDGET_ITEM, true);

                holder.mDescription.setText(mCursor.getString(description));
                holder.mSum.setText(numberFormat.format(mCursor.getFloat(amount)));
                holder.mDate.setText(months[mCursor.getInt(month)]);
                //holder.mAccount.setText(mCursor.getString(account_name));

                holder.itemView.setTag(transaction);

            }
        }

        @Override
        public int getItemCount() {
            if (mCursor == null) return 0;
            return mCursor.getCount();
        }

        @Override
        public long getItemId(int position) {
            if (mCursor != null && mCursor.moveToPosition(position)) {
                return mCursor.getLong(mCursor.getColumnIndex(MiserContract.BudgetEntry.Cols._ID));
            }
            return RecyclerView.NO_ID;
        }

        public void removeItem(int position) {
            notifyItemRemoved(position);
        }
    }


    private class DetailHolder extends RecyclerView.ViewHolder {

        TextView mDescription;
        TextView mSum;
        TextView mDate;
        //TextView mAccount;

        private DetailHolder(View view) {
            super(view);
            mDescription = view.findViewById(R.id.vh_description);
            mSum = view.findViewById(R.id.vh_sum);
            mDate = view.findViewById(R.id.vh_date);
            //mAccount = view.findViewById(R.id.vh_account);
        }
    }

    private class SBlistener extends BaseTransientBottomBar.BaseCallback<Snackbar> {

        private int id;

        private SBlistener(int id) {
            this.id = id;
        }

        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT) {
                String stringId = Integer.toString(id);
                Uri uri = MiserContract.BudgetEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();
                AsyncQueryTask mAsyncTask = new AsyncQueryTask(getContext().getContentResolver(), getContext());
                mAsyncTask.startDelete(TOKEN_DELETE_BUDGET_ITEM, null, uri, null, null);
            }
        }
    }
}
