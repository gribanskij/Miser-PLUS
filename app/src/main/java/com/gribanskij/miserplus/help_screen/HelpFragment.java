package com.gribanskij.miserplus.help_screen;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.gribanskij.miserplus.AbstractActivity;
import com.gribanskij.miserplus.BaseFragment;
import com.gribanskij.miserplus.R;


public class HelpFragment extends BaseFragment {


    private boolean isBudget;


    public HelpFragment() {
    }

    public static BaseFragment newInstance(boolean isBudget) {
        BaseFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putBoolean(AbstractActivity.BUDGET_ADD, isBudget);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            isBudget = savedInstanceState.getBoolean(AbstractActivity.BUDGET_ADD);
        } else {
            Bundle arg = getArguments();
            if (arg != null) {
                isBudget = arg.getBoolean(AbstractActivity.BUDGET_ADD);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            //NavUtils.navigateUpFromSameTask(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info, container, false);
        ExpandableListView expandableListView = view.findViewById(R.id.expandableList_info);

        AppCompatActivity activity = (AppCompatActivity) getActivity();


        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        String[] grope_names;
        String[] gropes;

        if (!isBudget) {

            grope_names = getResources().getStringArray(R.array.questions);
            gropes = getResources().getStringArray(R.array.answers);

        } else {
            grope_names = getResources().getStringArray(R.array.questions_budget);
            gropes = getResources().getStringArray(R.array.answers_budget);
        }


        expandableListView.setAdapter(new AdapterInfo(getContext(), gropes, grope_names));
        return view;
    }

    private class AdapterInfo extends BaseExpandableListAdapter {

        private Context context;
        private String[] gropes;
        private String[] gropes_name;

        private AdapterInfo(Context context, String[] gropes, String[] gropes_names) {

            this.context = context;
            this.gropes = gropes;
            this.gropes_name = gropes_names;

        }

        @Override
        public int getGroupCount() {

            if (gropes_name != null) {
                return gropes_name.length;
            }
            return 0;
        }

        @Override
        public int getChildrenCount(int i) {
            return 1;
        }

        @Override
        public Object getGroup(int i) {
            return gropes_name[i];
        }

        @Override
        public Object getChild(int i, int i1) {
            return gropes[i];
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.question_view, viewGroup, false);
            }

            TextView textGroup = view.findViewById(R.id.questeion_view);
            textGroup.setText(gropes_name[i]);

            return view;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.answer_view, viewGroup, false);
            }

            TextView textChild = view.findViewById(R.id.answer_text);
            textChild.setText(gropes[i]);

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }
    }
}
