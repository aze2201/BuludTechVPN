package tk.usagis2.BuludTechVPN.Adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import tk.usagis2.BuludTechVPN.Activities.DashBoard;
import tk.usagis2.BuludTechVPN.R;
import tk.usagis2.BuludTechVPN.Utils.Utils;

/**
 * Created by Usagi on 7/30/2016.
 */
public class AllowedAppsAdapter  extends RecyclerView.Adapter<AllowedAppsAdapter.SimpleViewHolder> {

    public static int PRELOAD_NUMBER = 0;
    private Context context;
    private List<ApplicationInfo> dataSet;
    private List<ApplicationInfo> displaySet;
    private int numOfLoadedApps = 0;
    private static SimpleViewHolder viewHolder;
    private final PackageManager pm;
    private HashSet<String> checkedBoxes;
    private String oldStringFilter = "";
    public AllowedAppsAdapter(Context context, List<ApplicationInfo> objects) {
        this.context = context;
        this.dataSet = objects;

        pm = context.getPackageManager();
        checkedBoxes = new HashSet<>();
    }

    public boolean loadMoreData(){
        int totalSize = displaySet.size();
        if(numOfLoadedApps >= totalSize)
            return false;

        if(PRELOAD_NUMBER == 0){
            numOfLoadedApps = totalSize;
            return true;
        }

        numOfLoadedApps += PRELOAD_NUMBER;
        if(numOfLoadedApps > totalSize)
            numOfLoadedApps = totalSize;
        return true;
    }

    public void setCheckedBoxes(HashSet<String> checkedBoxes){
        this.checkedBoxes = checkedBoxes;
    }

    public boolean filterData(){
        return filterData(oldStringFilter);
    }

    public boolean filterData(String stringFilter) {
        if (dataSet == null) {
            dataSet = new ArrayList<>();
        }
        oldStringFilter = stringFilter;

        List<ApplicationInfo> tempSet = new ArrayList<>();
        if(stringFilter != null && !stringFilter.equals("")){
            for (ApplicationInfo dataItem : dataSet) {
                if(dataItem.loadLabel(pm).toString().toLowerCase().contains(stringFilter.toLowerCase())){
                    tempSet.add(dataItem);
                }
            }
        }else{
            tempSet.addAll(dataSet);
        }

        boolean excludeThisApp = Utils.getPref(DashBoard.PREF_EXCLUDE_THIS_APP, true, context);
        if(excludeThisApp){
            for (ApplicationInfo dataItem : tempSet) {
                if(dataItem.packageName.equals(context.getString(R.string.package_name))){
                    tempSet.remove(dataItem);
                    break;
                }
            }
        }

        displaySet(tempSet);
        return true;
    }

    private void displaySet(List<ApplicationInfo> tempSet){
        displaySet = tempSet;
        if(this.displaySet == null){
            numOfLoadedApps = 0; return;
        }
        if(PRELOAD_NUMBER == 0){
            numOfLoadedApps = this.displaySet.size();
        }else{
            if(this.displaySet.size() < PRELOAD_NUMBER){
                numOfLoadedApps = this.displaySet.size();
            }else{
                numOfLoadedApps = PRELOAD_NUMBER;
            }
        }
    }

    public ApplicationInfo getDataSetAtPosition(int position){
        return displaySet.get(position);
    }

    public void setDataSet(List<ApplicationInfo> dataSet){
        this.dataSet = dataSet;
        filterData(oldStringFilter);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.allowed_apps_item, parent, false);
        viewHolder =  new SimpleViewHolder(view);
        return viewHolder;
    }

    private void saveCheckedBoxes(){
        String fullStr = "";
        for (String data: checkedBoxes) {
            fullStr+= data + ",";
        }
        if(fullStr.lastIndexOf(",") > 0){
            fullStr = fullStr.substring(0, fullStr.length()-1);
        }
        Utils.putPref(DashBoard.TICKED_APP_PACKAGES, fullStr, context);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder viewHolder, int position) {
        ApplicationInfo item = displaySet.get(position);
        CharSequence appName = item.loadLabel(pm);
        if (TextUtils.isEmpty(appName))
            appName = item.packageName;
        viewHolder.appName.setText(appName);
        viewHolder.appIcon.setImageDrawable(item.loadIcon(pm));
        viewHolder.checkBox.setChecked(checkedBoxes.contains(item.packageName));
        viewHolder.checkBox.setTag(item.packageName);
    }

    @Override
    public int getItemCount() {
        if(displaySet == null) return 0;
        return numOfLoadedApps;
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder{
        public TextView appName;
        public ImageView appIcon;
        public Switch checkBox;
        public SimpleViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            appIcon = itemView.findViewById(R.id.app_icon);
            checkBox = itemView.findViewById(R.id.app_selected);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String packageName = (String)view.getTag();
                    if(packageName == null || packageName.equals("")) return;
                    if(checkBox.isChecked()){
                        if(!checkedBoxes.contains(packageName)){
                            checkedBoxes.add(packageName);
                            saveCheckedBoxes();
                        }
                    }else{
                        if(checkedBoxes.contains(packageName)){
                            checkedBoxes.remove(packageName);
                            saveCheckedBoxes();
                        }
                    }
                }
            });
        }
    }
}
