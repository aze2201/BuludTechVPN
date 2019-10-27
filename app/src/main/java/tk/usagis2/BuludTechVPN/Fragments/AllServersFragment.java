package tk.usagis2.BuludTechVPN.Fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;

import java.lang.reflect.Field;

import tk.usagis2.BuludTechVPN.Activities.DashBoard;
import tk.usagis2.BuludTechVPN.Adapters.DividerItemDecoration;
import tk.usagis2.BuludTechVPN.Adapters.AllServersAdapter;
import tk.usagis2.BuludTechVPN.R;

/**
 * Created by UsagiS2 on 24/03/2016.
 */
public class AllServersFragment extends Fragment implements IVPNFragmentCallbacksForAdapter, IVPNFragmentCallbacksForActivity{
    public static final int KEY_CODE_CONNECT = 1;
    public static final int KEY_CODE_SAVE_TO = 2;
    public static final int KEY_CODE_SHARE = 3;
    public static final int KEY_CODE_CONNECT_THIS = 4;
    public static final int KEY_CODE_CONNECT_3RD = 5;
    public static final int KEY_CODE_SAVE_TO_SD = 6;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private AllServersAdapter adapter;
    private IVPNActivityCallbacks callbacks;
    private int openedPos = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_all_servers, container, false);
        recyclerView = v.findViewById(R.id.recycler_view_all_servers);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Item Decorator:
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));

        // Adapter:
        adapter = new AllServersAdapter(getContext(), callbacks.getVPNClasses(DashBoard.ALL_SERVERS), this);
        adapter.setMode(Attributes.Mode.Single);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(onScrollListener);

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setDistanceToTriggerSync(500);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callbacks.refreshLoadCSV();
            }
        });

        ViewTreeObserver vto = swipeRefreshLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Calculate the trigger distance.
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                Float mDistanceToTriggerSync = Math.min(
                        ((View) swipeRefreshLayout.getParent()).getHeight() * 0.6f,
                        150 * metrics.density);

                try {
                    // Set the internal trigger distance using reflection.
                    Field field = SwipeRefreshLayout.class.getDeclaredField("mDistanceToTriggerSync");
                    field.setAccessible(true);
                    field.setFloat(swipeRefreshLayout, mDistanceToTriggerSync);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Only needs to be done once so remove listener.
                ViewTreeObserver obs = swipeRefreshLayout.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void refreshData(){
        adapter.setDataSet(callbacks.getVPNClasses(DashBoard.ALL_SERVERS));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void clearData(){
        adapter.clearDataSet();
        adapter.notifyDataSetChanged();
    }

    private boolean loading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            //close opened view
            closeOpenedView();

            //callback to Activity for stuff
            if(callbacks != null)
                callbacks.onListScroll(dy);

            //check for scroll down and load more
            if(dy > 0)
            {
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                if (loading)
                {
                    if ( (visibleItemCount + pastVisibleItems) >= totalItemCount){
                        loading = false;
                        if(adapter.loadMoreData()){
                            loading = true;
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    };

    private void closeOpenedView(){
        if(openedPos >= 0){
            View v = recyclerView.getLayoutManager().findViewByPosition(openedPos);
            if(v != null){
                SwipeLayout swipeLayout = v.findViewById(R.id.swipe_layout);
                if(swipeLayout != null)
                    swipeLayout.close(true);
            }
            openedPos = -1;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        callbacks = (IVPNActivityCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onClickEvent(View view, int position, boolean doOpen) {
        if(doOpen){
            openedPos = position;
        }else{
            openedPos = -1;
        }
    }

    @Override
    public void onClickToButton(int position, int keyCode) {
        switch (keyCode){
            case KEY_CODE_CONNECT:
                callbacks.startVPN(adapter.getDataSetAtPosition(position), DashBoard.FORCE_START.None);
                //closeOpenedView();
                break;

            case KEY_CODE_SAVE_TO:
                callbacks.saveVPN(adapter.getDataSetAtPosition(position));
                //closeOpenedView();
                break;

            case KEY_CODE_CONNECT_THIS:
                callbacks.startVPN(adapter.getDataSetAtPosition(position), DashBoard.FORCE_START.Internal);
                //closeOpenedView();
                break;

            case KEY_CODE_CONNECT_3RD:
                callbacks.startVPN(adapter.getDataSetAtPosition(position), DashBoard.FORCE_START.ThirdParty);
                //closeOpenedView();
                break;

            case KEY_CODE_SHARE:
                callbacks.shareVPN(adapter.getDataSetAtPosition(position));
                break;

            case KEY_CODE_SAVE_TO_SD:
                callbacks.saveToSd(adapter.getDataSetAtPosition(position));
                break;
        }
    }

    @Override
    public boolean onClickFilterButton() {
        boolean rs = adapter.filterData();
        adapter.notifyDataSetChanged();
        loading = true;
        return rs;
    }

    @Override
    public boolean finishRefreshData() {
        if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
            return true;
        }
        return false;
    }

    @Override
    public void addAdsHeightToListView(int adsHeight) {
        if(recyclerView != null){
            recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight() , adsHeight);
            recyclerView.setClipToPadding(true);
        }
    }
}