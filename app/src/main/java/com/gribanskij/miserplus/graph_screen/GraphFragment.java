package com.gribanskij.miserplus.graph_screen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.GraphLoader;
import com.gribanskij.miserplus.LabelFormatter;
import com.gribanskij.miserplus.R;
import com.gribanskij.miserplus.sql_base.MiserContract;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import static com.gribanskij.miserplus.AbstractActivity.TYPE;

/**
 * Created by santy on 07.10.2017.
 */

public class GraphFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<BarGraphSeries<DataPoint>> {


    private static final int GRAPH_LOADER = 70;

    private int mType;
    private GraphView graphView;

    public GraphFragment() {
    }


    public static GraphFragment newInstance(int type) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mType = savedInstanceState.getInt(TYPE);
        } else {
            if (getArguments() != null) {
                mType = getArguments().getInt(TYPE);
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.graph_screen_main, container, false);

        graphView = v.findViewById(R.id.graphView_screen);
        DefaultLabelFormatter formatter = new LabelFormatter(getActivity());
        graphView.getGridLabelRenderer().setLabelFormatter(formatter);
        graphView.getViewport().setMinX(6);
        graphView.getViewport().setMaxX(12);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getGridLabelRenderer().setHighlightZeroLines(false);
        graphView.getGridLabelRenderer().setHorizontalLabelsColor(ContextCompat.getColor(getContext(), R.color.colorGray));
        graphView.getGridLabelRenderer().setVerticalLabelsColor(ContextCompat.getColor(getContext(), R.color.colorGray));


        //graphView.getViewport().setScrollable(true);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(GRAPH_LOADER, null, this);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TYPE, mType);
    }

    @Override
    public Loader<BarGraphSeries<DataPoint>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        String category_count = "0";

        switch (id) {
            case GRAPH_LOADER: {

                if (mType == MiserContract.TYPE_COST) {
                    category_count = sharedPreferences.getString(
                            getString(R.string.pref_cost_quantity_key),
                            getString(R.string.pref_cost_quantity_10_value));
                } else {
                    category_count = sharedPreferences.getString(
                            getString(R.string.pref_income_quantity_key),
                            getString(R.string.pref_income_quantity_4_value));

                }
                break;
            }
        }

        return new GraphLoader(getContext(), mType, category_count, MiserContract.TransactionEntry.CONTENT_URI);
    }

    @Override
    public void onLoadFinished(Loader<BarGraphSeries<DataPoint>> loader, BarGraphSeries<DataPoint> data) {

        int id = loader.getId();

        switch (id) {
            case GRAPH_LOADER: {
                graphView.removeAllSeries();
                data.setSpacing(5);
                data.setAnimated(true);
                data.setColor(ContextCompat.getColor(getContext(), R.color.colorSeries));
                data.setOnDataPointTapListener(new DataSeriesListener());
                //data.setDrawValuesOnTop(true);
                //data.setValuesOnTopColor(Color.RED);
                if (mType == MiserContract.TYPE_COST) {
                    data.setTitle(getString(R.string.expenses_12));
                } else {
                    data.setTitle(getString(R.string.income_12));
                }
                graphView.addSeries(data);
                graphView.getLegendRenderer().setVisible(true);
                graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<BarGraphSeries<DataPoint>> loader) {
        graphView.removeAllSeries();
    }

    private class DataSeriesListener implements OnDataPointTapListener {

        @Override
        public void onTap(Series series, DataPointInterface dataPoint) {
            Toast.makeText(getActivity(), Double.toString(dataPoint.getY()), Toast.LENGTH_SHORT).show();
        }
    }

}
