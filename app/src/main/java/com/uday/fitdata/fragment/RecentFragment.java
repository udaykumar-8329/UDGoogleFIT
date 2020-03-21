package com.uday.fitdata.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uday.fitdata.R;
import com.uday.fitdata.adapter.AnimatedLayoutManager;
import com.uday.fitdata.adapter.WorkoutListViewAdapter;
import com.uday.fitdata.animation.ItemAnimator;
import com.uday.fitdata.database.CacheManager;
import com.uday.fitdata.database.DataManager;
import com.uday.fitdata.model.Workout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Chris Black
 *
 * Displays historical entries in a vertical list.
 */
@SuppressWarnings("WeakerAccess") // Butterknife requires public reference of injected views
public class RecentFragment extends BaseFragment implements WorkoutListViewAdapter.OnItemClickListener {

    public static final String TAG = "RecentFragment";

    private DataManager.IDataManager dataReceiver;
    private CacheManager.ICacheManager cacheReceiver;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private WorkoutListViewAdapter adapter;

    public static RecentFragment create() {
        return new RecentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new AnimatedLayoutManager(this.getActivity(), 1));

        adapter = new WorkoutListViewAdapter(this.getActivity(), cacheReceiver.getCursor());
        adapter.setHasStableIds(true);
        adapter.setOnItemClickListener(this);

        mRecyclerView.setItemAnimator(new ItemAnimator());
        mRecyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if(activity instanceof DataManager.IDataManager) {
            dataReceiver = (DataManager.IDataManager)activity;
        }
        if(activity instanceof CacheManager.ICacheManager) {
            cacheReceiver = (CacheManager.ICacheManager)activity;
        }
    }

    public void swapCursor(Cursor cursor) {
        adapter.swapCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    /**
     * Clear callback on detach to prevent null reference errors after the view has been
     */
    @Override
    public void onDetach() {
        super.onDetach();
        dataReceiver = null;
        cacheReceiver = null;
    }

    @Override
    public void onItemClick(View view, Workout viewModel) {
        if (dataReceiver != null) {
            dataReceiver.removeData(viewModel);
        }
    }
}
