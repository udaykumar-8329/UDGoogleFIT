package com.uday.fitdata.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.uday.fitdata.R;

import com.uday.fitdata.Utilities;
import com.uday.fitdata.activity.IMainActivityCallback;
import com.uday.fitdata.adapter.RecyclerViewAdapter;
import com.uday.fitdata.animation.ItemAnimator;
import com.uday.fitdata.database.CacheManager;
import com.uday.fitdata.model.Workout;
import com.uday.fitdata.service.CacheResultReceiver;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Chris Black
 *
 * Used to display a page of data within the view pager.
 */
public class PageFragment extends BaseFragment implements RecyclerViewAdapter.OnItemClickListener, CacheResultReceiver.Receiver {

    private static final String TAG = "PageFragment";
    private static final String ARG_PAGE = "ARG_PAGE";
    private static final String ARG_FILTER = "ARG_FILTER";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerViewAdapter adapter;
    private boolean needsRefresh = true;
    private int mPage;
    private String mFilterText = "";
    private CacheResultReceiver mReciever;
    protected IMainActivityCallback mCallback;
    protected Handler mHandler;
    protected Runnable mRunnable;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    public static PageFragment create(int page, String filterText) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString(ARG_FILTER, filterText);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        mFilterText = getArguments().getString(ARG_FILTER);
        mReciever = new CacheResultReceiver(new Handler());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);

        ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this.getActivity(), 2));

        List<Workout> items = new ArrayList<>();

        adapter = new RecyclerViewAdapter(items, Utilities.getTimeFrameText(Utilities.TimeFrame.values()[mPage - 1]));
        adapter.setOnItemClickListener(this);
        filter();
        mRecyclerView.setItemAnimator(new ItemAnimator());
        mRecyclerView.setAdapter(adapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.contentView);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //CacheManager.getReport(Utilities.TimeFrame.values()[mPage - 1], mReciever, getActivity());
                if (mPage < 4) {
                    //UserPreferences.setLastSync(getActivity(), Utilities.getTimeFrameStart(Utilities.TimeFrame.values()[mPage - 1]));
                }
                mCallback.quickDataRead();
            }
        });
        mSwipeRefreshLayout.setEnabled(false);

        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if(activity instanceof IMainActivityCallback) {
            mCallback = (IMainActivityCallback)activity;
        }
    }

    /**
     * Clear callback on detach to prevent null reference errors after the view has been
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public void setFilterText(String text) {
        if(mFilterText.equals(text)) {
            return;
        }
        mFilterText = text;
        filter();
    }

    /**
     * Triggered when data changes in the DataManger by calls to quickDataRead
     */
    public void refreshData() {
        mReciever.setReceiver(this);
        mSwipeRefreshLayout.setRefreshing(true);
        CacheManager.getReport(Utilities.TimeFrame.values()[mPage - 1], mReciever, getActivity());
    }

    public void filter() {
        adapter.filter(mFilterText);
    }

    @Override
    public void onResume() {
        super.onResume();
        mReciever.setReceiver(this);
        //mCallback.quickDataRead(); // TODO: Use timeframe instead
        CacheManager.getReport(Utilities.TimeFrame.values()[mPage - 1], mReciever, getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        mReciever.setReceiver(null);
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(false);
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }
    }

    public void setSwipeToRefreshEnabled(boolean enabled) {
        mSwipeRefreshLayout.setEnabled(enabled);
    }

    @Override
    public void onItemClick(View view, Workout viewModel) {
        mCallback.launch(view.findViewById(R.id.image), viewModel);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        final List<Workout> workoutList = resultData.getParcelableArrayList("workoutList");
        needsRefresh = true;
        if (adapter.getItemCount() == 0) {
            needsRefresh = false;
            adapter.setItems(workoutList, Utilities.getTimeFrameText(Utilities.TimeFrame.values()[mPage - 1]));
            filter();
        }
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (needsRefresh) {
                    adapter.setItems(workoutList, Utilities.getTimeFrameText(Utilities.TimeFrame.values()[mPage - 1]));
                    filter();
                }
                // Update the UI
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        };
        mHandler.postDelayed(mRunnable, 1000);


    }
}

