package com.gribanskij.miserplus.detail_screen;


import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
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
import com.gribanskij.miserplus.sql_base.MiserContract;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DetailFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final String LOG_TAG = DetailFragment.class.getSimpleName();

    public static final int CATEGORY_DATA_LOADER = 15;
    private static final int TOKEN_DELETE = 10;
    private int mCategory;
    private int mType;
    private long mDateFrom;
    private long mDateTo;
    private String categoryName;
    private NumberFormat numberFormat;
    private SimpleDateFormat dateFormat;
    private RecyclerDetailAdapter mAdapter;
    private TextView emptyView;

    private Paint p = new Paint();
    private Snackbar s;


    public DetailFragment() {
    }

    public static BaseFragment newInstance(int type, long start_date, long final_date, int categoryID, String categoryName) {
        BaseFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(AbstractActivity.CATEGORY_NAME, categoryName);
        args.putInt(AbstractActivity.CATEGORY_ID, categoryID);
        args.putInt(AbstractActivity.TYPE, type);
        args.putLong(AbstractActivity.START_DATE, start_date);
        args.putLong(AbstractActivity.FINAL_DATE, final_date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mCategory = savedInstanceState.getInt(AbstractActivity.CATEGORY_ID);
            mType = savedInstanceState.getInt(AbstractActivity.TYPE);
            mDateFrom = savedInstanceState.getLong(AbstractActivity.START_DATE);
            mDateTo = savedInstanceState.getLong(AbstractActivity.FINAL_DATE);
            categoryName = savedInstanceState.getString(AbstractActivity.CATEGORY_NAME);
        } else {
            mCategory = getArguments().getInt(AbstractActivity.CATEGORY_ID);
            mType = getArguments().getInt(AbstractActivity.TYPE);
            mDateFrom = getArguments().getLong(AbstractActivity.START_DATE);
            mDateTo = getArguments().getLong(AbstractActivity.FINAL_DATE);
            categoryName = getArguments().getString(AbstractActivity.CATEGORY_NAME);
        }

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);
        dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.detail_fragment, container, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isAdsDisabled = sharedPreferences.getBoolean(getString(R.string.disable_adMob_key), false);

        FrameLayout frame = view.findViewById(R.id.frame);


        if (!isAdsDisabled) {
            FrameLayout ads_container = view.findViewById(R.id.admob_container);
            AdView adView = new AdView(getActivity());
            adView.getHeight();
            adView.setAdSize(AdSize.SMART_BANNER);
            //adView.setAdUnitId(getString(R.string.test_banner_id));
            adView.setAdUnitId(getString(R.string.dashboard_banner_ID));
            ads_container.addView(adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            //FrameLayout buffer = view.findViewById(R.id.frame);
            //AdView empty = new AdView(getActivity());
            //empty.setAdSize(AdSize.SMART_BANNER);
            //buffer.addView(empty);
            //adView.loadAd(((DashboardActivity) getActivity()).getAdRequest());
        } else frame.setVisibility(View.GONE);


        Toolbar toolbar = view.findViewById(R.id.toolbar);
        emptyView = view.findViewById(R.id.emptyView);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(categoryName);
            StringBuilder subtitle = new StringBuilder().append(dateFormat.format(new Date(mDateFrom)));
            subtitle.append(" - ").append(dateFormat.format(new Date(mDateTo))).append("   ");
            String currency = sharedPreferences.getString(getString(R.string.pref_currency_key),
                    getString(R.string.pref_currency_default_RUB_value));
            subtitle.append(currency);
            actionBar.setSubtitle(subtitle);
        }


        final RecyclerView mRecyclerView = view.findViewById(R.id.detail_recycler);
        mAdapter = new RecyclerDetailAdapter(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                Bundle transaction = (Bundle) viewHolder.itemView.getTag();
                if (swipeDir == ItemTouchHelper.LEFT) {
                    s = Snackbar.make(viewHolder.itemView, R.string.remove_action, Snackbar.LENGTH_LONG);
                    s.setAction(R.string.undo_action, view1 -> {
                        mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        s.dismiss();
                    });


                    s.addCallback(new SnackBarListener(
                            transaction.getLong(EditFragment._ID),
                            transaction.getInt(EditFragment.ACCOUNT),
                            transaction.getFloat(EditFragment.SUM),
                            transaction.getInt(EditFragment._TYPE)));
                    s.show();

                } else {
                    mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    Intent intent = new Intent(getActivity(), EditActivity.class);
                    intent.putExtra(AbstractActivity.TRANSACTION, transaction);
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


        FloatingActionButton floatingActionButton = view.findViewById(R.id.fab_detail);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddActivity.class);
                intent.putExtra(AbstractActivity.TYPE, mType);
                intent.putExtra(AbstractActivity.CATEGORY_ID, mCategory);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CATEGORY_DATA_LOADER, null, this);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        outState.putLong(AbstractActivity.START_DATE, mDateFrom);
        outState.putLong(AbstractActivity.FINAL_DATE, mDateTo);
        outState.putInt(AbstractActivity.CATEGORY_ID, mCategory);
        outState.putInt(AbstractActivity.TYPE, mType);
        outState.putString(AbstractActivity.CATEGORY_NAME, categoryName);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (!preferences.getBoolean(getString(R.string.pref_isRate_key), false)) {
            int time = preferences.getInt(getString(R.string.pref_time_to_rate_key), 0);
            if (time > 20) {
                FragmentManager manager = getFragmentManager();
                DialogFragment fragment = new RateApp();
                fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                fragment.show(manager, LOG_TAG);
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String sortOrder = null;
        String[] selectionArg = null;


        switch (id) {

            case CATEGORY_DATA_LOADER: {
                uri = MiserContract.TransactionEntry.CONTENT_URI;

                projection = new String[]{
                        MiserContract.TransactionEntry.Cols._ID,
                        MiserContract.TransactionEntry.Cols.DESCRIPTION,
                        MiserContract.TransactionEntry.Cols.AMOUNT,
                        MiserContract.TransactionEntry.Cols.DATE,
                        MiserContract.TransactionEntry.Cols.CATEGORY_ID,
                        MiserContract.TransactionEntry.Cols.ACCOUNT_NAME,
                        MiserContract.TransactionEntry.Cols.ACCOUNT,
                        MiserContract.TransactionEntry.Cols.TYPE
                };
                selection = MiserContract.TransactionEntry.Cols.DATE + " >= ? AND " + MiserContract.TransactionEntry.Cols.DATE +
                        " < ? AND " + MiserContract.TransactionEntry.Cols.CATEGORY_ID +
                        " = ? AND " + MiserContract.TransactionEntry.Cols.TYPE + " = ? ";
                selectionArg = new String[]{Long.toString(mDateFrom), Long.toString(mDateTo), Integer.toString(mCategory),
                        Integer.toString(mType)};
                sortOrder = MiserContract.TransactionEntry.Cols.DATE + " DESC";
                break;
            }
        }
        return new CursorLoader(getContext(), uri, projection, selection, selectionArg, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case CATEGORY_DATA_LOADER: {
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

            int description = mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols.DESCRIPTION);
            int amount = mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols.AMOUNT);
            int date = mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols.DATE);
            int id = mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols._ID);
            int account_name = mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols.ACCOUNT_NAME);
            int account = mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols.ACCOUNT);
            int type = mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols.TYPE);
            int category = mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols.CATEGORY_ID);


            if (mCursor.moveToPosition(position)) {

                Bundle transaction = new Bundle();
                transaction.putInt(EditFragment.CATEGORY, mCursor.getInt(category));
                transaction.putInt(EditFragment._TYPE, mCursor.getInt(type));
                transaction.putLong(EditFragment._ID, mCursor.getLong(id));
                transaction.putLong(EditFragment.DATE, mCursor.getLong(date));
                transaction.putFloat(EditFragment.SUM, mCursor.getFloat(amount));
                transaction.putInt(EditFragment.ACCOUNT, mCursor.getInt(account));
                transaction.putString(EditFragment.DESCRIPTION, mCursor.getString(description));
                transaction.putBoolean(EditFragment.BUDGET_ITEM, false);

                holder.mDescription.setText(mCursor.getString(description));
                holder.mSum.setText(numberFormat.format(mCursor.getFloat(amount)));
                holder.mDate.setText(DateFormat.getDateInstance().format(mCursor.getLong(date)));
                holder.mAccount.setText(mCursor.getString(account_name));
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
                return mCursor.getLong(mCursor.getColumnIndex(MiserContract.TransactionEntry.Cols._ID));
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
        TextView mAccount;

        private DetailHolder(View view) {
            super(view);
            mDescription = view.findViewById(R.id.vh_description);
            mSum = view.findViewById(R.id.vh_sum);
            mDate = view.findViewById(R.id.vh_date);
            mAccount = view.findViewById(R.id.vh_account);

        }
    }

    private class SnackBarListener extends BaseTransientBottomBar.BaseCallback<Snackbar> {

        private long id;
        private int account;
        private float sum;
        private int type;

        private SnackBarListener(long id, int account, float sum, int type) {
            this.id = id;
            this.sum = sum;
            this.account = account;
            this.type = type;
        }

        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {

            if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT) {
                String stringId = Long.toString(id);
                Uri uri = MiserContract.TransactionEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();
                AsyncQueryTask mAsyncTask = new AsyncQueryTask(getContext().getContentResolver(), getContext());
                mAsyncTask.startDelete(TOKEN_DELETE, null, uri, null, null);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        double oldAccountSum;
                        double newAccountSum;
                        ContentResolver resolver = getActivity().getContentResolver();
                        Uri uri = MiserContract.AccountEntry.CONTENT_URI;

                        String[] projection = new String[]{MiserContract.AccountEntry.Cols._ID, MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT};
                        String selection = MiserContract.AccountEntry.Cols.CATEGORY_ID + " = ? ";
                        String[] selectionArgs = new String[]{Integer.toString(account)};

                        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);

                        if (cursor != null && cursor.moveToFirst()) {
                            oldAccountSum = cursor.getDouble(cursor.getColumnIndex(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT));
                            id = cursor.getInt(cursor.getColumnIndex(MiserContract.AccountEntry.Cols._ID));
                            cursor.close();
                        } else {
                            return;
                        }

                        if (type == MiserContract.TYPE_COST) {
                            newAccountSum = oldAccountSum + sum;
                        } else {
                            newAccountSum = oldAccountSum - sum;
                            if (newAccountSum < 0) {
                                newAccountSum = 0F;
                            }
                        }
                        Uri accountUri = MiserContract.AccountEntry.CONTENT_URI;
                        accountUri = accountUri.buildUpon().appendPath(Long.toString(id)).build();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MiserContract.AccountEntry.Cols.ACCOUNT_AMOUNT, newAccountSum);
                        resolver.update(accountUri, contentValues, null, null);
                    }
                });
                thread.start();
            }
        }
    }
}
