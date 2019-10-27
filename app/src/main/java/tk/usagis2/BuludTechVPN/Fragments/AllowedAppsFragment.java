package tk.usagis2.BuludTechVPN.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tk.usagis2.BuludTechVPN.Activities.DashBoard;
import tk.usagis2.BuludTechVPN.Adapters.AllowedAppsAdapter;
import tk.usagis2.BuludTechVPN.Adapters.DividerItemDecoration;
import tk.usagis2.BuludTechVPN.R;
import tk.usagis2.BuludTechVPN.Utils.Utils;

/**
 * Created by Usagi on 7/30/2016.
 */
public class AllowedAppsFragment extends Fragment implements IForcedHeightToListView{

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private AllowedAppsAdapter adapter;
    private AppCompatSpinner connectTypeSpinner;
    private Switch excludeSwitch;

    private IVPNActivityCallbacks callbacks;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_allowed_apps, container, false);

        excludeSwitch = v.findViewById(R.id.exclude_this_app_switch);
        excludeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Utils.putPref(DashBoard.PREF_EXCLUDE_THIS_APP, isChecked, AllowedAppsFragment.this.getContext());
                startFilter(null, true);
            }
        });

        connectTypeSpinner = v.findViewById(R.id.filter_app_connect_type_spinner);
        connectTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                boolean enabledAll = pos == 0;
                Utils.putPref(DashBoard.PREF_ENABLED_ALL, enabledAll, AllowedAppsFragment.this.getContext());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        recyclerView = v.findViewById(android.R.id.list);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        adapter = new AllowedAppsAdapter(getContext(), null);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(onScrollListener);
        loadSettings();
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppLoadTask task = new AppLoadTask(getContext());
        task.execute();
    }

    private class AppLoadTask extends AsyncTask<Void, Integer, List<ApplicationInfo>>{
        private Context context;
        AppLoadTask(Context context){
            this.context = context;
        }

        @Override
        protected List<ApplicationInfo> doInBackground(Void... voids) {
            return loadAppList(context);
        }

        @Override
        protected void onPostExecute(List<ApplicationInfo> applicationInfos) {
            super.onPostExecute(applicationInfos);
            adapter.setDataSet(applicationInfos);
            adapter.notifyDataSetChanged();
        }
    }

    public List<ApplicationInfo> loadAppList(Context context){
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        int androidSystemUid = 0;
        ApplicationInfo system;
        final List<ApplicationInfo> apps = new ArrayList<>();

        try {
            system = pm.getApplicationInfo("android", PackageManager.GET_META_DATA);
            androidSystemUid = system.uid;
            apps.add(system);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        for (ApplicationInfo app : installedPackages) {
            if (pm.checkPermission(Manifest.permission.INTERNET, app.packageName) == PackageManager.PERMISSION_GRANTED
                    && app.uid != androidSystemUid) {
                apps.add(app);
            }
        }

        Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pm));
        return apps;
    }

    private void loadSettings(){
        boolean enableAllApps = Utils.getPref(DashBoard.PREF_ENABLED_ALL, true, getContext());
        connectTypeSpinner.setSelection(enableAllApps?0:1);

        boolean excludeThisApp = Utils.getPref(DashBoard.PREF_EXCLUDE_THIS_APP, true, getContext());
        excludeSwitch.setChecked(excludeThisApp);

        String tickedApps = Utils.getPref(DashBoard.TICKED_APP_PACKAGES, "", getContext());
        if(tickedApps.equals("")) return;
        adapter.setCheckedBoxes(Utils.getPackageNamesFromString(tickedApps));
    }

    public boolean onClickFilterButton(String filterString) {
        startFilter(filterString, false);
        return true;
    }

    private void startFilter(final String filterString, final boolean useOldFilter){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(useOldFilter)
                    adapter.filterData();
                else
                    adapter.filterData(filterString);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        loading = true;
                    }
                });
            }
        }).start();
    }

    public void addAdsHeightToListView(int adsHeight) {
        if(recyclerView != null){
            recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight() , adsHeight);
            recyclerView.setClipToPadding(true);
        }
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
}
