package com.gribanskij.miserplus;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

import com.gribanskij.miserplus.sql_base.MiserContract;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class GraphLoader extends AsyncTaskLoader<BarGraphSeries<DataPoint>> {


    private final int MONTHS_IN_YEAR = 12;
    private final ForceLoadContentObserver mObserver;
    private Context context;
    private int type;
    private Cursor mCursor;
    private String category_count;
    private BarGraphSeries<DataPoint> barGraphSeries;
    private Uri uri;
    private CancellationSignal mCancellationSignal;


    public GraphLoader(Context context, int type, String category_count, Uri uri) {
        super(context);
        this.context = context;
        this.type = type;
        this.category_count = category_count;
        this.uri = uri;
        mObserver = new ForceLoadContentObserver();
    }


    @Override
    protected void onStartLoading() {
        if (barGraphSeries != null) {
            deliverResult(barGraphSeries);
        }

        if (takeContentChanged() || barGraphSeries == null) {
            forceLoad();
        }

    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }


    @Override
    public void deliverResult(BarGraphSeries<DataPoint> data) {

        if (isReset()) return;

        if (isStarted()) {
            super.deliverResult(data);
        }

    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(BarGraphSeries<DataPoint> data) {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (mCursor != null) {
            mCursor.close();
        }
        onStopLoading();
        barGraphSeries = null;
    }

    @Override
    public BarGraphSeries<DataPoint> loadInBackground() {

        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }

        try {
            List intervals = getMonthIntervals();
            DataPoint[] series = getDataPointSet(type, intervals);
            barGraphSeries = new BarGraphSeries<>(series);
            barGraphSeries.setSpacing(5);
            barGraphSeries.setColor(ContextCompat.getColor(getContext(), R.color.colorSeries));
            return barGraphSeries;

        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }

    private List<Month> getMonthIntervals() {
        List<Month> m = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, -6);

        for (int a = 0; a <= 6; a++) {
            long start = calendar.getTimeInMillis();
            calendar.add(Calendar.MONTH, 1);
            long end = calendar.getTimeInMillis();
            Month month = new Month(start, end);
            m.add(month);
        }
        return m;
    }

    private DataPoint[] getDataPointSet(int type, List<Month> date_set) {

        String[] projection = new String[]{"SUM" + "(" + MiserContract.TransactionEntry.Cols.AMOUNT + ")"};
        String selection = MiserContract.TransactionEntry.Cols.DATE + " >=  ? AND " +
                MiserContract.TransactionEntry.Cols.DATE + " < ? AND " +
                MiserContract.TransactionEntry.Cols.TYPE + " = ? AND " +
                MiserContract.TransactionEntry.Cols.CATEGORY_ID + " < ?";
        ContentResolver resolver = context.getContentResolver();

        String[] arg = new String[]{
                Long.toString(0),
                Long.toString(1),
                Integer.toString(type),
                category_count};

        mCursor = resolver.query(uri, projection, selection, arg, null);
        if (mCursor != null) mCursor.registerContentObserver(mObserver);


        DataPoint[] dataPoints = new DataPoint[date_set.size()];
        int count = 0;
        int sum = 0;

        for (Month m : date_set) {
            long start_date = m.getStart();
            long final_date = m.getFinal();

            String[] selectionArg = new String[]{
                    Long.toString(start_date),
                    Long.toString(final_date),
                    Integer.toString(type),
                    category_count
            };
            Cursor cursor = resolver.query(uri, projection, selection, selectionArg, null);

            if (cursor != null && cursor.moveToFirst()) {
                sum = cursor.getInt(0);
                cursor.close();
            }
            DataPoint point = new DataPoint(count, sum);
            dataPoints[count] = point;
            count++;
            sum = 0;
        }
        return dataPoints;
    }

    private class Month {

        private long start_month_day;
        private long end_month_day;

        private Month(long start, long end) {
            this.start_month_day = start;
            this.end_month_day = end;
        }

        public long getStart() {
            return start_month_day;
        }

        public long getFinal() {
            return end_month_day;
        }

    }

}
