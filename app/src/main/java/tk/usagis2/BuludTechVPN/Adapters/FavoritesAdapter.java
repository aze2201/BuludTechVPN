package tk.usagis2.BuludTechVPN.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.blinkt.openvpn.core.OpenVPNService;
import tk.usagis2.BuludTechVPN.Activities.DashBoard;
import tk.usagis2.BuludTechVPN.CSV.VPNClass;
import tk.usagis2.BuludTechVPN.Fragments.FavoritesFragment;
import tk.usagis2.BuludTechVPN.Fragments.IVPNFragmentCallbacksForAdapter;
import tk.usagis2.BuludTechVPN.R;
import tk.usagis2.BuludTechVPN.Utils.SortComparators.CountryComparator;
import tk.usagis2.BuludTechVPN.Utils.SortComparators.HostNameComparator;
import tk.usagis2.BuludTechVPN.Utils.SortComparators.IPComparator;
import tk.usagis2.BuludTechVPN.Utils.SortComparators.TotalTrafficComparator;
import tk.usagis2.BuludTechVPN.Utils.SortComparators.SessionComparator;
import tk.usagis2.BuludTechVPN.Utils.SortComparators.SpeedComparator;
import tk.usagis2.BuludTechVPN.Utils.SortComparators.UpTimeComparator;
import tk.usagis2.BuludTechVPN.Utils.Utils;

/**
 * Created by UsagiS2 on 24/03/2016.
 */
public class FavoritesAdapter extends RecyclerSwipeAdapter<FavoritesAdapter.SimpleViewHolder> {

    public static int PRELOAD_NUMBER = 30;
    private Context context;
    private List<VPNClass> dataSet;
    private List<VPNClass> displaySet;
    private int numOfLoadedVPNs = 0;
    private IVPNFragmentCallbacksForAdapter clickCallback;
    private static SimpleViewHolder viewHolder;

    public class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener{
        SwipeLayout swipeLayout;
        ImageView imageFlag;
        TextView textIp;
        TextView textHostName;
        TextView textSpeed;
        TextView textTraffic;
        TextView textUpTime;
        TextView textSessions;
        SwipeLayout.Status status;
        Button btnConnect;
        Button btnDelete;
        boolean doOpen;
        private Context context;
        public SimpleViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            swipeLayout = itemView.findViewById(R.id.swipe_layout);
            imageFlag = itemView.findViewById(R.id.top_country_icon);
            textIp = itemView.findViewById(R.id.top_ip);
            textHostName = itemView.findViewById(R.id.top_host_name);
            textSpeed = itemView.findViewById(R.id.top_speed);
            textTraffic = itemView.findViewById(R.id.top_traffic);
            textUpTime = itemView.findViewById(R.id.top_up_time);
            textSessions = itemView.findViewById(R.id.top_sessions);
            btnConnect = itemView.findViewById(R.id.bottom_button_connect);
            btnDelete = itemView.findViewById(R.id.bottom_button_delete);

            btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickCallback.onClickToButton(SimpleViewHolder.this.getAdapterPosition(), FavoritesFragment.KEY_CODE_CONNECT);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickCallback.onClickToButton(SimpleViewHolder.this.getAdapterPosition(), FavoritesFragment.KEY_CODE_DELETE);
                }
            });
            swipeLayout.setOnClickListener(this);
            swipeLayout.setSwipeEnabled(false);
            swipeLayout.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            status = swipeLayout.getOpenStatus();
            if(status == SwipeLayout.Status.Open){
                swipeLayout.close(true);
                doOpen = false;
            }else if (status == SwipeLayout.Status.Close){
                refreshDragEdge();
                swipeLayout.open(true);
                doOpen = true;
            }
            clickCallback.onClickEvent(view, this.getAdapterPosition(), doOpen);
        }

        public void refreshDragEdge(){
            int itemDirection = Utils.getSettingSharePref("settings_general_list_item_direction", 1, this.context);
            if(itemDirection == 1){
                swipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);
            }else{
                swipeLayout.setDragEdge(SwipeLayout.DragEdge.Left);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            MenuItem connectVPN = contextMenu.add(context.getResources().getString(R.string.context_connect));
            MenuItem connect3rdVPN = contextMenu.add(context.getResources().getString(R.string.context_connect_3rd));
            MenuItem editVPN = contextMenu.add(context.getResources().getString(R.string.context_edit));
            MenuItem shareVPN = contextMenu.add(context.getResources().getString(R.string.context_share));
            MenuItem saveToSD = contextMenu.add(context.getResources().getString(R.string.context_sdcard));
            MenuItem deleteVPN = contextMenu.add(context.getResources().getString(R.string.context_delete));

            connectVPN.setOnMenuItemClickListener(this);
            connect3rdVPN.setOnMenuItemClickListener(this);
            editVPN.setOnMenuItemClickListener(this);
            shareVPN.setOnMenuItemClickListener(this);
            saveToSD.setOnMenuItemClickListener(this);
            deleteVPN.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(menuItem.getTitle().equals(context.getResources().getString(R.string.context_connect)) ){
                clickCallback.onClickToButton(SimpleViewHolder.this.getAdapterPosition(), FavoritesFragment.KEY_CODE_CONNECT_THIS);
            }else if(menuItem.getTitle().equals(context.getResources().getString(R.string.context_connect_3rd))) {
                clickCallback.onClickToButton(SimpleViewHolder.this.getAdapterPosition(), FavoritesFragment.KEY_CODE_CONNECT_3RD);
            }else if(menuItem.getTitle().equals(context.getResources().getString(R.string.context_edit))) {
                clickCallback.onClickToButton(SimpleViewHolder.this.getAdapterPosition(), FavoritesFragment.KEY_CODE_EDIT);
            }else if(menuItem.getTitle().equals(context.getResources().getString(R.string.context_share))){
                clickCallback.onClickToButton(SimpleViewHolder.this.getAdapterPosition(), FavoritesFragment.KEY_CODE_SHARE);
            }else if(menuItem.getTitle().equals(context.getResources().getString(R.string.context_delete))){
                clickCallback.onClickToButton(SimpleViewHolder.this.getAdapterPosition(), FavoritesFragment.KEY_CODE_DELETE);
            }else if(menuItem.getTitle().equals(context.getResources().getString(R.string.context_sdcard))){
                clickCallback.onClickToButton(SimpleViewHolder.this.getAdapterPosition(), FavoritesFragment.KEY_CODE_SAVE_TO_SD);
            }
            return true;
        }
    }

    //true = load done, false = need load more
    public boolean loadMoreData(){
        int totalSize = displaySet.size();
        if(numOfLoadedVPNs >= totalSize)
            return false;

        if(PRELOAD_NUMBER == 0){
            numOfLoadedVPNs = totalSize;
            return true;
        }

        numOfLoadedVPNs += PRELOAD_NUMBER;
        if(numOfLoadedVPNs > totalSize)
            numOfLoadedVPNs = totalSize;
        return true;
    }

    public boolean filterData(){
        if(dataSet == null ){
            dataSet = new ArrayList<>();
        }

        //filter
        String filterByCountry = Utils.getPref(DashBoard.PREF_P2_FILTER_COUNTRY, "", context);
        String filterByHostName = Utils.getPref(DashBoard.PREF_P2_FILTER_HOSTNAME, "", context);
        String filterByIP = Utils.getPref(DashBoard.PREF_P2_FILTER_IP, "", context);

        List<VPNClass> tempSet = new ArrayList<>();
        boolean gottaAdd;
        if(!(filterByCountry.isEmpty() && filterByHostName.isEmpty() && filterByIP.isEmpty())){
            for (VPNClass dataItem : dataSet) {
                gottaAdd = true;
                if (!filterByCountry.isEmpty() && !dataItem.CountryLong.toLowerCase().contains(filterByCountry.toLowerCase())) {
                    gottaAdd = false;
                }

                if (!filterByHostName.isEmpty() && !dataItem.HostName.toLowerCase().contains(filterByHostName.toLowerCase())) {
                    gottaAdd = false;
                }

                if (!filterByIP.isEmpty() && !dataItem.IP.toLowerCase().contains(filterByIP.toLowerCase())) {
                    gottaAdd = false;
                }

                if(gottaAdd)
                    tempSet.add(dataItem);
            }
        }else{
            tempSet = dataSet;
        }

        //sort
        int sortBy = Utils.getPref(DashBoard.PREF_P2_SORT_BY, 0, context);
        int sortOrderInt = Utils.getPref(DashBoard.PREF_P2_SORT_ORDER, 0, context);
        boolean sortOrder;
        switch (sortOrderInt){
            case 1:
                sortOrder = true;
                break;
            case 2:
                sortOrder = false;
                break;
            default:
                displaySet(tempSet);
                return true;
        }

        Comparator<VPNClass> comparator;
        switch (sortBy){
            case 0:
                comparator = null;
                break;
            case 1:
                comparator = new SpeedComparator(sortOrder);
                break;
            case 2:
                comparator = new UpTimeComparator(sortOrder);
                break;
            case 3:
                comparator = new TotalTrafficComparator(sortOrder);
                break;
            case 4:
                comparator = new SessionComparator(sortOrder);
                break;
            case 5:
                comparator = new CountryComparator(sortOrder);
                break;
            case 6:
                comparator = new HostNameComparator(sortOrder);
                break;
            case 7:
                comparator = new IPComparator(sortOrder);
                break;
            default:
                comparator = null;
                break;
        }

        if(comparator != null){
            Collections.sort(tempSet, comparator);
        }
        displaySet(tempSet);
        return true;
    }

    private void displaySet(List<VPNClass> tempSet){
        displaySet = tempSet;
        if(this.displaySet == null){
            numOfLoadedVPNs = 0; return;
        }
        if(PRELOAD_NUMBER == 0){
            numOfLoadedVPNs = this.displaySet.size();
        }else{
            if(this.displaySet.size() < PRELOAD_NUMBER){
                numOfLoadedVPNs = this.displaySet.size();
            }else{
                numOfLoadedVPNs = PRELOAD_NUMBER;
            }
        }
    }

    public VPNClass getDataSetAtPosition(int position){
        return displaySet.get(position);
    }

    public void setDataSet(List<VPNClass> dataSet){
        this.dataSet = dataSet;
        filterData();
    }

    public FavoritesAdapter(Context context, List<VPNClass> objects, IVPNFragmentCallbacksForAdapter callback) {
        this.context = context;
        this.dataSet = objects;
        clickCallback = callback;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_item_favorite, parent, false);
        viewHolder =  new SimpleViewHolder(view, context);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder viewHolder, int position) {
        VPNClass item = displaySet.get(position);

        viewHolder.textIp.setText(item.IP);
        viewHolder.textHostName.setText(item.HostName);
        viewHolder.textTraffic.setText(item.TotalTraffic == Integer.MIN_VALUE ? "-":  OpenVPNService.humanReadableByteCount((long)item.TotalTraffic, false, context.getResources()));
        viewHolder.textSessions.setText(Utils.getDisplayInt(item.NumberOfSessions));
        viewHolder.textSpeed.setText(Utils.getHumanReadableByteCount(item.Speed, false));
        viewHolder.textUpTime.setText(Utils.getDisplayTime(item.UpTime/1000));
        if(item.CountryShort != null){
            Glide.with(context).load(Utils.getFlagDrawable(context, item.CountryShort.toUpperCase()))
                    .into(viewHolder.imageFlag);
        }else{
            Glide.with(context).load(R.mipmap.unknown)
                    .into(viewHolder.imageFlag);
        }

        mItemManger.bindView(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        if(displaySet == null) return 0;
        return numOfLoadedVPNs;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

}
