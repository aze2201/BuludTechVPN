package tk.usagis2.BuludTechVPN.Activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import at.markushi.ui.CircleButton;
import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.OpenVPNManagement;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;
import tk.usagis2.BuludTechVPN.Fragments.AllowedAppsFragment;
import tk.usagis2.BuludTechVPN.Fragments.IForcedHeightToListView;
import tk.usagis2.BuludTechVPN.Fragments.IVPNFragmentCallbacksForActivity;
import tk.usagis2.BuludTechVPN.CSV.VPNClass;
import tk.usagis2.BuludTechVPN.Databases.DatabaseManager;
import tk.usagis2.BuludTechVPN.Fragments.AllServersFragment;
import tk.usagis2.BuludTechVPN.Fragments.FavoritesFragment;
import tk.usagis2.BuludTechVPN.Fragments.IVPNActivityCallbacks;
import tk.usagis2.BuludTechVPN.Fragments.RetainFragment;
import tk.usagis2.BuludTechVPN.R;
import tk.usagis2.BuludTechVPN.Utils.Utils;
import tk.usagis2.BuludTechVPN.Utils.VPNClassToVPNProfileConverter;
import tk.usagis2.BuludTechVPN.Utils.ZoomOutSlideTransformer;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DashBoard extends AppCompatActivity implements
        RetainFragment.CSVLoadCallbacks,
        RetainFragment.DatabaseLoadCallbacks,
        IVPNActivityCallbacks,
        VPNClassToVPNProfileConverter.IFinishConvertCallback,
        VpnStatus.StateListener,
        VpnStatus.ByteCountListener
{
    //Fragment for retaining data + async task
    private final static String TAG_RETAIN_FRAGMENT = "retain_fragment";
    private final static int SHOW_ADS_TIME = 1500;
    private final static int SHOW_TIME_LONG = 800;
    private final static int HIDE_TIME_LONG = 700;
    private final static int SHOW_TIME_SHORT = 400;
    private final static int HIDE_TIME_SHORT = 400;

    public enum FORCE_START{
        None,
        Internal,
        ThirdParty
    }

    //TypeOfVPN
    public final static int ALL_SERVERS = 0;
    public final static int FAVORITES = 1;
    public final static int ALLOW_APP = 2;
    //Activity properties
    private final static int PAGE_NUM = 3;

    private RetainFragment retainFragment;
    private ActionBar actionBar;
    private View actionBarCustomView;
    private FragmentPagerItemAdapter adapter;
    private ViewPager viewPager;
    private int currentPagePosition;
    private RelativeLayout connectManagementLayout;
    private ScrollView filterLayout;
    private ScrollView filterAppLayout;
    private RelativeLayout clickPreventingLayout;
    private View loadingBar;
    private CircleButton btFilters;
    private Snackbar mainSnackBar = null;
    private AdView adView;

    //Management layout
    private ImageView managementIndicator;
    private TextView managementStatus;
    private TextView managementUp;
    private TextView managementUpSpeed;
    private TextView managementDown;
    private TextView managementDownSpeed;
    private TextView backBanner;

    //ActionBar Buttons
    SmartTabLayout viewPagerTab;

    //SHARED PREF STRINGS
    public static final String PREF_FILTER_COUNTRY = "PREF_FILTER_COUNTRY";
    public static final String PREF_FILTER_HOSTNAME = "PREF_FILTER_HOSTNAME";
    public static final String PREF_FILTER_IP = "PREF_FILTER_IP";
    public static final String PREF_FILTER_LAST_SELECTED = "PREF_FILTER_LAST_SELECTED";
    public static final String PREF_SORT_BY = "PREF_SORT_BY";
    public static final String PREF_SORT_ORDER = "PREF_SORT_ORDER";

    public static final String PREF_P2_FILTER_COUNTRY = "PREF_P2_FILTER_COUNTRY";
    public static final String PREF_P2_FILTER_HOSTNAME = "PREF_P2_FILTER_HOSTNAME";
    public static final String PREF_P2_FILTER_IP = "PREF_P2_FILTER_IP";
    public static final String PREF_P2_FILTER_LAST_SELECTED = "PREF_P2_FILTER_LAST_SELECTED";
    public static final String PREF_P2_SORT_BY = "PREF_P2_SORT_BY";
    public static final String PREF_P2_SORT_ORDER = "PREF_P2_SORT_ORDER";

    public static final String PREF_ENABLED_ALL = "PREF_ENABLED_ALL";
    public static final String PREF_EXCLUDE_THIS_APP = "PREF_EXCLUDE_THIS_APP";
    public static final String TICKED_APP_PACKAGES = "TICKED_APP_PACKAGES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        MobileAds.initialize(getApplicationContext(), getString(R.string.str_ads_app_id));

        initAds();
        initActionbar();
        initTabSwipe();
        initRetainFragment();
        initClickPreventingLayout();
        initConnectManagementLayout();
        initFilterLayout();
        initFilterAppLayout();
        loadStartPage();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void RemoveVPNStatus(){
        VpnStatus.removeStateListener(this);
        VpnStatus.removeByteCountListener(this);
    }

    private void initAds(){
        backBanner = (TextView)findViewById(R.id.ad_view_back_info);
        adView = (AdView) findViewById(R.id.ad_view);
        loadAds();
        lastTimeAds = System.currentTimeMillis();

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdClosed() {}

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if(backBanner!= null)
                    backBanner.setText(getString(R.string.ads_back_banner));
            }

            @Override
            public void onAdLeftApplication() {}

            @Override
            public void onAdOpened() {}
        });
    }

    private void loadAds(){
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
        RemoveVPNStatus();
    }

    private long lastTimeAds = 0;
    @Override
    protected void onResume() {
        super.onResume();

        //settings
        loadSettings();

        //ads
        if (adView != null) {
            adView.resume();
            if(lastTimeAds < System.currentTimeMillis()){
                if(!VpnStatus.isVPNActive()){
                    lastTimeAds = System.currentTimeMillis() + 120000;
                    loadAds();
                }
            }
        }

        //bind status
        BindVPNStatus();
    }

    private void BindVPNStatus(){
        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        //special case: open => boot completed => reopen management layout
        if(connectManagementLayout.getY() == startPositionInPx){
            if(VpnStatus.isVPNActive()){
                moveManagementLayoutUp();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        trimCache(this);
        super.onDestroy();
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir.exists()) {
                for (File f : dir.listFiles()) {
                    if(f.isFile() && Utils.getFileExt(f.getName()).equals(".ovpn")){
                        f.delete();
                    }
                }
            }

        } catch (Exception e) {
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            canManagementStart = true;

            //close keyboard first
            closeKeyboard();
            int topHeight = getPointOfView(viewPager).y;
            appHeight = getRealScreenSize(this).y - (getNavigationBarSize(this) + topHeight);

            adsHeight = adView.getHeight();
            startPositionInPx = appHeight - adsHeight;

            //get startHeight for filterLayout
            filterStartHeight = (int)actionBar.getCustomView().getY() - filterLayout.getHeight() - getResources().getDimensionPixelOffset(R.dimen.filter_top_offset);
            filterAppStartHeight = (int)actionBar.getCustomView().getY() - filterAppLayout.getHeight() - getResources().getDimensionPixelOffset(R.dimen.filter_top_offset);

            moveManagementLayoutToStartPosition();
            loadDataAfterShowUp();
        }
    }

    private void refreshPaddingForListViews(int adsHeight){
        IForcedHeightToListView callbacks = ((IForcedHeightToListView)adapter.getPage(ALL_SERVERS));
        if(callbacks != null)
            callbacks.addAdsHeightToListView(adsHeight);

        callbacks = ((IForcedHeightToListView)adapter.getPage(FAVORITES));
        if(callbacks != null)
            callbacks.addAdsHeightToListView(adsHeight);

        callbacks = ((IForcedHeightToListView)adapter.getPage(ALLOW_APP));
        if(callbacks != null)
            callbacks.addAdsHeightToListView(adsHeight);
    }

    private Point getPointOfView(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    public static int getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

//        // navigation bar on the right
//        if (appUsableSize.x < realScreenSize.x) {
//            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
//        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return realScreenSize.y - appUsableSize.y;
        }

        // navigation bar is not present
        return 0;
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            }
            catch (Exception e) {
                size.x = display.getWidth();
                size.y = display.getHeight();
            }
        }

        return size;
    }

    @Override
    public void onBackPressed() {
        if(isFilterShow){
            moveFilterLayoutUp();
            return;
        }

        if(isFilterAppShow){
            moveFilterAppLayoutUp();
            return;
        }
        super.onBackPressed();
    }

    //region LoadSettings

    private void loadSettings(){
        loadSaveFilter();
    }

    private void loadStartPage(){
        if(viewPager != null)
            viewPager.setCurrentItem(Utils.getSettingSharePref("settings_general_list_start_page", 0, this));
    }

    private void loadSaveFilter(){
        boolean doKeepFilter = Utils.getSettingSharePref("settings_general_save_filter", true, this);
        if(!doKeepFilter){
            resetFilter(ALL_SERVERS);
            resetFilter(FAVORITES);
        }
    }

    private ArrayList<Integer> loadedPage = null;
    private void loadDataAfterShowUp(){
        //reload
        loadedPage = new ArrayList<>();
        int wantedPage = viewPager.getCurrentItem();
        switch (wantedPage){
            case ALL_SERVERS:
                if(retainFragment != null && retainFragment.doReload())
                    tryToLoadCSVData(true);
                else
                    tryToLoadCSVData(false);
                loadedPage.add(ALL_SERVERS);
                break;
            case FAVORITES:
                tryToLoadDatabaseData(false);
                loadedPage.add(FAVORITES);
                break;
            case ALLOW_APP:
                break;
        }
    }

    //endregion

    //region ActionBar

    private void initActionbar(){
        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(myToolbar);
        actionBar = getSupportActionBar();
        if(actionBar == null){
            //Currently not support non actionBar TODO: later
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setTitle(getString(R.string.str_error));
            builder.setMessage(getString(R.string.str_action_bar_not_found));
            builder.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DashBoard.this.finish();
                }
            });
            builder.show();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        actionBarCustomView = inflater.inflate(R.layout.action_bar_customview, null);
        viewPagerTab = actionBarCustomView.findViewById(R.id.viewpager_tab);
        CircleButton btSettings = actionBarCustomView.findViewById(R.id.actionbar_settings);
        btFilters = actionBarCustomView.findViewById(R.id.actionbar_filters);

        btSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoard.this, Settings.class);
                DashBoard.this.startActivity(intent);
            }
        });
        btFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPagePosition == ALL_SERVERS || currentPagePosition == FAVORITES)
                    showHideFilterLayout();
                else if(currentPagePosition == ALLOW_APP)
                    showHideFilterAppLayout();
            }
        });

        actionBar.setCustomView(actionBarCustomView);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        myToolbar.setContentInsetsAbsolute(0,0);
    }

    //endregion

    //region Tabs/Fragments
    private void initTabSwipe(){
        final LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        final int finalActionBarHeight = actionBarHeight;

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setPageTransformer(true, new ZoomOutSlideTransformer());
        adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("", AllServersFragment.class)
                .add("", FavoritesFragment.class)
                .add("", AllowedAppsFragment.class)
                .create());

        viewPagerTab.setCustomTabView(new SmartTabLayout.TabProvider() {
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                View itemView = inflater.inflate(R.layout.action_bar_button, container, false);
                ImageView icon = itemView.findViewById(R.id.circle_button);

                if(finalActionBarHeight >= 0){
                    icon.setMinimumWidth(getResources().getDimensionPixelSize(R.dimen.action_bar_indicator_thickness));
                }else{
                    icon.setMinimumWidth((int)dpToPx(DashBoard.this, 40));
                }

                switch (position) {
                    case ALL_SERVERS:
                        icon.setImageResource(R.drawable.ic_cloud_queue_white_24dp);
                        break;
                    case FAVORITES:
                        icon.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                        break;
                    case ALLOW_APP:
                        icon.setImageResource(R.drawable.ic_call_split_white_24dp);
                        break;
                }
                return icon;
            }
        });

        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);
        viewPagerTab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(isFilterShow){
                    moveFilterLayoutUp();
                }

                if(isFilterAppShow){
                    moveFilterAppLayoutUp();
                }
            }

            @Override
            public void onPageSelected(int position) {
                currentPagePosition = position;
                if(loadedPage != null){
                    if(currentPagePosition == ALL_SERVERS && !loadedPage.contains(ALL_SERVERS)){
                        tryToLoadCSVData(false);
                        loadedPage.add(ALL_SERVERS);
                    }
                    if(currentPagePosition == FAVORITES && !loadedPage.contains(FAVORITES)){
                        tryToLoadDatabaseData(false);
                        loadedPage.add(FAVORITES);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        currentPagePosition = 0;
    }

    @Override
    public List<VPNClass> getVPNClasses(int typeOfVPNs) {
        if(retainFragment != null){
            switch (typeOfVPNs){
                case ALL_SERVERS:
                    if(retainFragment.RetainAllServerVPNs != null)
                        return retainFragment.RetainAllServerVPNs;
                case FAVORITES:
                    if(retainFragment.RetainFavoriteVPNs != null)
                        return retainFragment.RetainFavoriteVPNs;
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public boolean startVPN(VPNClass vpnClass, FORCE_START force) {
        boolean openInThirdPartyApp = Utils.getSettingSharePref("settings_general_open_using_third_party", false, this);
        if(force == FORCE_START.Internal || (force == FORCE_START.None && !openInThirdPartyApp)){
            return startInternal(vpnClass);
        }else if(force == FORCE_START.ThirdParty || (force == FORCE_START.None && openInThirdPartyApp)){
            return startThirdParty(vpnClass);
        }
        return true;
    }

    private boolean startInternal(VPNClass vpnClass){
        if(!isAppReadyToConnect()){
            Toast.makeText(DashBoard.this, R.string.str_app_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        startOrStopVPN(true, vpnClass);
        return true;
    }

    private boolean startThirdParty(VPNClass vpnClass){
        try {
            String data = Utils.ConvertBase64ToString(vpnClass.OpenVPN_ConfigData_Base64);
            String fileDir = getCacheDir().getAbsolutePath() + "/" + vpnClass.HostName + ".ovpn";
            FileWriter cfg = new FileWriter(fileDir);
            cfg.write(data);
            cfg.flush();
            cfg.close();

            File tmpFile = new File(fileDir);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), "tk.usagis2.ez_vpngate", tmpFile);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri,"application/x-openvpn-profile");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open with"));
        }catch (Exception e){
            return false;
        }
        return true;
    }

    private void startFinalStage(){
        moveManagementLayoutUp();
        moveFilterLayoutUp();
    }

    private void startOrStopVPN(boolean isStart, final VPNClass vpnClass) {
        if(isStart){
            if (VpnStatus.isVPNActive()) {
                Toast.makeText(this, R.string.str_vpn_already_started, Toast.LENGTH_SHORT).show();
            } else {
                final boolean enableAll = Utils.getPref(DashBoard.PREF_ENABLED_ALL, true, this);
                String tickedApps = Utils.getPref(DashBoard.TICKED_APP_PACKAGES, "", this);
                boolean excludeThisApp = Utils.getPref(DashBoard.PREF_EXCLUDE_THIS_APP, true, this);
                final HashSet<String> selectedApps = Utils.getPackageNamesFromString(tickedApps);
                if(excludeThisApp){
                    if(enableAll){
                        if(!selectedApps.contains(getString(R.string.package_name))) {
                            selectedApps.add(getString(R.string.package_name));
                        }
                    }else{
                        if(selectedApps.contains(getString(R.string.package_name))) {
                            selectedApps.remove(getString(R.string.package_name));
                        }
                    }
                }
                if(!enableAll && selectedApps.isEmpty()){
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setMessage(R.string.str_ask_mode);
                    builder.setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    builder.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setManagementStatus(R.string.str_vpn_status_preparing);
                            startConvertVPN(vpnClass);
                            startFinalStage();
                        }
                    });
                    android.app.AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
                }else{
                    setManagementStatus(R.string.str_vpn_status_preparing);
                    startConvertVPN(vpnClass);
                    startFinalStage();
                }
            }
        }else{
            if (VpnStatus.isVPNActive()) {
                Intent disconnectVPN = new Intent(this, DisconnectVPN.class);
                disconnectVPN.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                disconnectVPN.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(disconnectVPN);
            } else {
                Toast.makeText(this, R.string.str_no_vpn_running, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startConvertVPN(VPNClass vpnClass) {
        VPNClassToVPNProfileConverter converter = new VPNClassToVPNProfileConverter(this, this);
        converter.ConvertToProfile(vpnClass);
    }

    @Override
    public void finishConvert(VpnProfile vpnProfile) {
        getPM().addProfile(vpnProfile);
        Intent intent = new Intent(this, LaunchVPN.class);
        intent.putExtra(LaunchVPN.EXTRA_KEY, vpnProfile.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        startActivity(intent);
    }

    private ProfileManager getPM() {
        return ProfileManager.getInstance(this);
    }

    @Override
    public boolean stopVPN(VPNClass vpnClass) {
        startOrStopVPN(false, null);
        return true;
    }

    @Override
    public void saveVPN(final VPNClass vpnClass) {
        boolean showSaveDialog = Utils.getSettingSharePref("settings_general_show_change_name_dialog", true, this);
        if(showSaveDialog){
            showSavingDialog(vpnClass);
        }else{
            showOverWriteData(vpnClass);
        }
    }

    @Override
    public void editVPN(final VPNClass vpnClass) {
        if(DatabaseManager.getInstance().checkExistVPN(vpnClass)){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(R.string.str_add_vpn_name);

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
            input.setText(vpnClass.HostName);
            float dpi = this.getResources().getDisplayMetrics().density;
            builder.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    vpnClass.HostName = input.getText().toString();
                    DatabaseManager.getInstance().updateVPN(vpnClass);
                    Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_info_updated, Snackbar.LENGTH_SHORT)
                            .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
                    snackbar.show();
                    tryToLoadDatabaseData(true);
                }
            });
            builder.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            android.app.AlertDialog dialog = builder.create();
            dialog.setView(input, (int)(6 * dpi), (int)(4 * dpi), (int)(6 * dpi), (int)(4 * dpi));
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
        }
    }

    private void showSavingDialog(final VPNClass addVPNClass){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.str_add_vpn_name);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        input.setText(addVPNClass.HostName);
        float dpi = this.getResources().getDisplayMetrics().density;
        builder.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addVPNClass.HostName = input.getText().toString();
                showOverWriteData(addVPNClass);
            }
        });
        builder.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.setView(input, (int)(6 * dpi), (int)(4 * dpi), (int)(6 * dpi), (int)(4 * dpi));
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
    }

    private void showOverWriteData(final VPNClass addVPN){
        if(DatabaseManager.getInstance().checkExistVPN(addVPN)){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(R.string.str_update_vpn_title);
            builder.setMessage(R.string.str_update_vpn_msg);
            builder.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.setNeutralButton(R.string.str_add_as_new, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DatabaseManager.getInstance().insertVPN(addVPN);
                    Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_info_added, Snackbar.LENGTH_SHORT)
                            .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
                    snackbar.show();
                    tryToLoadDatabaseData(true);
                }
            });
            builder.setPositiveButton(R.string.str_update, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DatabaseManager.getInstance().updateVPN(addVPN);
                    Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_info_updated, Snackbar.LENGTH_SHORT)
                            .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
                    snackbar.show();
                    tryToLoadDatabaseData(true);
                }
            });
            android.app.AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
        }else{
            DatabaseManager.getInstance().insertVPN(addVPN);
            Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_info_added, Snackbar.LENGTH_SHORT)
                    .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
            snackbar.show();
            tryToLoadDatabaseData(true);
        }
    }

    @Override
    public void deleteVPN(final VPNClass vpnClass) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.str_ask_delete);
        builder.setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseManager.getInstance().deleteVPN(vpnClass);
                Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_info_deleted, Snackbar.LENGTH_SHORT)
                        .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
                snackbar.show();
                tryToLoadDatabaseData(true);
            }
        });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this,R.color.colorDialogBtn));
    }

    @Override
    public void shareVPN(VPNClass vpnClass) {
        try {
            String data = Utils.ConvertBase64ToString(vpnClass.OpenVPN_ConfigData_Base64);
            String fileDir = getCacheDir().getAbsolutePath() + "/" + vpnClass.HostName + ".ovpn";
            FileWriter cfg = new FileWriter(fileDir);
            cfg.write(data);
            cfg.flush();
            cfg.close();

            File tmpFile = new File(fileDir);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), "tk.usagis2.ez_vpngate", tmpFile);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, getString(R.string.str_share_via)));
        }catch (Exception e){
        }
    }

    String saveName = "";
    String saveData = "";
    @Override
    public void saveToSd(VPNClass vpnClass) {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_error_no_ext, Snackbar.LENGTH_SHORT)
                    .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
            snackbar.show();
            return;
        }

        saveName = vpnClass.HostName + ".ovpn";
        saveData = Utils.ConvertBase64ToString(vpnClass.OpenVPN_ConfigData_Base64);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    private static final int REQUEST_CODE = 0x11;
    String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    if(!saveName.isEmpty() && !saveData.isEmpty()){
                        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + saveName;
                        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                        File folder = new File(dirPath);
                        if (!folder.exists()) {
                            boolean success = folder.mkdir();
                            if (!success) {
                                Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_error_folder_creation_failed, Snackbar.LENGTH_SHORT)
                                        .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
                                snackbar.show();
                                return;
                            }
                        }

                        FileOutputStream outputStream = new FileOutputStream(filePath);
                        outputStream.write(saveData.getBytes());
                        outputStream.close();
                        saveData = "";
                        saveName = "";

                        Uri uri_path = Uri.parse(dirPath);
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri_path, "resource/folder");
                        if (getPackageManager().resolveActivity(intent, 0) != null) {
                            Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_save_sd_done, Snackbar.LENGTH_LONG)
                                    .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent))
                                    .setAction(R.string.str_open_dir, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(Intent.createChooser(intent, "Choose Apps"));
                                        }
                                    });
                            snackbar.show();
                        }else{
                            Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_save_sd_done, Snackbar.LENGTH_LONG)
                                    .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
                            snackbar.show();
                        }

                    }
                }catch (Exception e){
                    Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_error_rnd, Snackbar.LENGTH_SHORT)
                            .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
                    snackbar.show();
                }

            } else {
                Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_error_per, Snackbar.LENGTH_SHORT)
                        .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent));
                snackbar.show();
            }
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    @Override
    public void onListScroll(int dy) {
        //close fillerBar
        if(isFilterShow && (dy > 1) || (dy < -1)){
            moveFilterLayoutUp();
        }

        if(isFilterAppShow && (dy > 1) || (dy < -1)){
            moveFilterAppLayoutUp();
            closeKeyboard();
        }

        //close temp snackBar
        if(mainSnackBar == null) return;
        if(mainSnackBar.isShownOrQueued()){
            mainSnackBar.dismiss();
            mainSnackBar = null;
        }
    }

    @Override
    public void refreshLoadCSV() {
        IVPNFragmentCallbacksForActivity callbacks = ((IVPNFragmentCallbacksForActivity)adapter.getPage(ALL_SERVERS));
        if(callbacks != null)
            callbacks.finishRefreshData();
        tryToLoadCSVData(true);
    }

    @Override
    public void refreshLoadDatabase() {
        IVPNFragmentCallbacksForActivity callbacks = ((IVPNFragmentCallbacksForActivity)adapter.getPage(FAVORITES));
        if(callbacks != null)
            callbacks.finishRefreshData();
        tryToLoadDatabaseData(true);
    }

    //endregion

    //region Init RetainFragment

    private void initRetainFragment(){
        FragmentManager fm = getSupportFragmentManager();
        retainFragment = (RetainFragment) fm.findFragmentByTag(TAG_RETAIN_FRAGMENT);

        if (retainFragment == null) {
            retainFragment = new RetainFragment();
            fm.beginTransaction().add(retainFragment, TAG_RETAIN_FRAGMENT).commit();
        }
    }

    private void tryToLoadCSVData(boolean forceLoad){
        if(!forceLoad){
            if(retainFragment.doReload()){
                retainFragment.loadVPNGateCsvFile(this);
            }else{
                List<VPNClass> oldData = getVPNClasses(ALL_SERVERS);
                if(oldData == null || oldData.size() == 0){
                    retainFragment.loadVPNGateViaSaveFile(this);
                }else if(adapter != null){
                    ((AllServersFragment)adapter.getPage(ALL_SERVERS)).refreshData();
                }
            }
        }else{
            if(adapter != null){
                IVPNFragmentCallbacksForActivity callbacks = ((IVPNFragmentCallbacksForActivity)adapter.getPage(ALL_SERVERS));
                if(callbacks != null) callbacks.clearData();
            }
            retainFragment.loadVPNGateCsvFile(this);
        }
    }

    private void tryToLoadDatabaseData(boolean forceLoad){
        if(!forceLoad){
            if(retainFragment.RetainFavoriteVPNs == null || retainFragment.RetainFavoriteVPNs.size() == 0){
                retainFragment.loadVPNGateSQLite();
            }else{
                if(adapter != null){
                    ((FavoritesFragment)adapter.getPage(FAVORITES)).refreshData();
                }
            }
        }else{
            retainFragment.loadVPNGateSQLite();
        }

    }

    //endregion

    //region CSVLoad callback methods

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onProgressUpdate(int percent) {

    }

    @Override
    public void onCancelled() {

    }

    private String getErrorCodeString(int errorCode){
        switch (errorCode) {
            case RetainFragment.ERROR_PARSING_FAILED:
                return "ERROR_PARSING_FAILED";
            case RetainFragment.ERROR_CONVERSION_FAILED:
                return "ERROR_CONVERSION_FAILED";
            case RetainFragment.ERROR_NO_ERROR_BUT_NO_SERVER:
                return "ERROR_NO_ERROR_BUT_NO_SERVER";
            case RetainFragment.ERROR_READING_SQL_FAILED:
                return "ERROR_READING_SQL_FAILED";
        }
        return "NOT DETECTED ERROR";
    }

    @Override
    public void onPostExecute(List<VPNClass> rs, int errorCode) {
        //errorCode handler
        switch (errorCode){
            case RetainFragment.ERROR_PARSING_FAILED:
                showTwoButtonDialog(R.string.str_err_title, R.string.str_err_parsing_contact_us, R.string.str_button_contact, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.str_author_email), null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.str_mail_subject));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "ErrorCode: " + getErrorCodeString(RetainFragment.ERROR_PARSING_FAILED) + "\n" + getString(R.string.str_mail_comment));
                        startActivity(Intent.createChooser(emailIntent, getString(R.string.str_chooser_tittle)));
                    }
                });
                retry(R.string.str_err_parsing_error);
                break;
            case RetainFragment.ERROR_CONVERSION_FAILED:
                showTwoButtonDialog(R.string.str_err_title, R.string.str_err_parsing_contact_us, R.string.str_button_contact, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.str_author_email), null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.str_mail_subject));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "ErrorCode: " + getErrorCodeString(RetainFragment.ERROR_CONVERSION_FAILED) + "\n" + getString(R.string.str_mail_comment));
                        startActivity(Intent.createChooser(emailIntent, getString(R.string.str_chooser_tittle)));
                    }
                });
                break;
        }
        // not found but no error
        if(rs == null || rs.size() == 0){
            Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, R.string.str_err_no_server_found, Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent))
                    .setAction(R.string.str_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (retainFragment == null) initRetainFragment();
                            retainFragment.loadVPNGateCsvFile(DashBoard.this);
                        }
                    });
            snackbar.show();
        }
        //show
        if(adapter != null){
            ((AllServersFragment)adapter.getPage(ALL_SERVERS)).refreshData();
        }
    }

    private void showTwoButtonDialog(int titleId, int messageId, int positiveButtonId,  DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(positiveButtonId, listener);
        builder.show();
    }

    @Override
    public void retry(int id) {
        Snackbar snackbar = Snackbar.make(DashBoard.this.viewPager, id, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(DashBoard.this,R.color.colorAccent))
                .setAction(R.string.str_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (retainFragment == null) initRetainFragment();
                        retainFragment.loadVPNGateCsvFile(DashBoard.this);
                    }
                });
        snackbar.show();
    }

    @Override
    public void startLoading() {
        showClickPreventingLayout(R.color.colorTransparent, SHOW_TIME_SHORT, true);
    }

    @Override
    public void stopLoading() {
        hideClickPreventingLayout(R.color.colorTransparent, HIDE_TIME_SHORT);
    }

    //endregion

    //region Database callback methods
    @Override
    public void onDbPreExecute() {

    }

    @Override
    public void onDbProgressUpdate(int percent) {

    }

    @Override
    public void onDbCancelled() {

    }

    @Override
    public void onDbPostExecute(List<VPNClass> result, int errorValue) {
        if(adapter != null && adapter.getCount() >=PAGE_NUM){
            ((FavoritesFragment)adapter.getPage(FAVORITES)).refreshData();
        }
    }

    @Override
    public void startDbLoading() {
        //showClickPreventingLayout(R.color.colorTransparent, SHOW_TIME_SHORT, true);
    }

    @Override
    public void stopDbLoading() {
        if(retainFragment != null && retainFragment.IsLoadingCSV()) return;
        hideClickPreventingLayout(R.color.colorTransparent, SHOW_TIME_SHORT);
    }

    //endregion

    //region Init ManagementLayout
    private static final int MANAGEMENT_POS_HIDE = -1;
    private static final int MANAGEMENT_POS_SHOW_ADS = 0;
    private static final int MANAGEMENT_POS_SHOW_TOOLS = 1;
    private boolean canManagementStart = false;
    private int currentManagementPosition = MANAGEMENT_POS_HIDE;
    public int appHeight;
    public int adsHeight;
    public int startPositionInPx;
    private void initConnectManagementLayout(){
        //layout Management
        connectManagementLayout = (RelativeLayout) findViewById(R.id.connect_management_layout);
        managementIndicator = (ImageView)findViewById(R.id.connect_management_status_indicator);
        managementStatus = (TextView)findViewById(R.id.connect_management_status);
        managementUp = (TextView) findViewById(R.id.connect_management_vol_up);
        managementUpSpeed = (TextView)findViewById(R.id.connect_management_speed_up);
        managementDown = (TextView) findViewById(R.id.connect_management_vol_down);
        managementDownSpeed = (TextView)findViewById(R.id.connect_management_speed_down);

        //button Management
        CircleButton btnManagement = (CircleButton) findViewById(R.id.connect_management_button);
        if(btnManagement != null){
            btnManagement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopVPN(null);

                }
            });
        }
    }

    private void moveManagementLayoutToStartPosition(){
        if(currentManagementPosition != MANAGEMENT_POS_HIDE || !canManagementStart) return;
        if(connectManagementLayout.getVisibility() == View.INVISIBLE) connectManagementLayout.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(connectManagementLayout, "translationY", appHeight, startPositionInPx);
        animator.setDuration(SHOW_ADS_TIME);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(VpnStatus.isVPNActive()){
                    moveManagementLayoutUp();
                }
                refreshPaddingForListViews(adView.getHeight());
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
        currentManagementPosition = MANAGEMENT_POS_SHOW_ADS;
    }

    public boolean isAppReadyToConnect(){
        if(connectManagementLayout.getY() == startPositionInPx)
            return true;
        else return false;
    }

    private boolean moveManagementLayoutUp(){
        if(currentManagementPosition != MANAGEMENT_POS_SHOW_ADS || !canManagementStart) return false;
        int distanceInPx = appHeight - connectManagementLayout.getHeight();
        if(connectManagementLayout.getVisibility() == View.INVISIBLE)
            connectManagementLayout.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(connectManagementLayout, "translationY", connectManagementLayout.getY(), distanceInPx);
        animator.setDuration(SHOW_TIME_LONG);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();
        currentManagementPosition = MANAGEMENT_POS_SHOW_TOOLS;

        showClickPreventingLayout(R.color.colorTransparentDark, SHOW_TIME_LONG, false);
        return true;
    }

    private boolean moveManagementLayoutDown(){
        if(currentManagementPosition != MANAGEMENT_POS_SHOW_TOOLS || !canManagementStart) return false;

        ObjectAnimator animator = ObjectAnimator.ofFloat(connectManagementLayout, "translationY", connectManagementLayout.getY(), startPositionInPx);
        animator.setDuration(HIDE_TIME_LONG);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();
        currentManagementPosition = MANAGEMENT_POS_SHOW_ADS;
        hideClickPreventingLayout(R.color.colorTransparentDark, HIDE_TIME_LONG);
        return true;
    }

    public static float pxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPx(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    //endregion

    //region Init FilterLayout
    private boolean isFilterShow = false;
    private int filterStartHeight = 0;
    private AppCompatSpinner filterSpinner;
    private AutoCompleteTextView filterInput;
    private ArrayAdapter<String> filterInputAdapter;
    private AppCompatSpinner sortSpinner;
    private RadioGroup radioGroup;
    private AppCompatRadioButton desCheckBox;
    private AppCompatRadioButton ascCheckBox;
    private int oldPage = -1;

    private void initFilterLayout(){
        //filter Layout
        filterLayout = (ScrollView) findViewById(R.id.filter_sort_layout);

        //filter spinner
        filterSpinner = (AppCompatSpinner)findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(this, R.array.str_filter_array, R.layout.layout_spinner);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if(filterSpinner != null){
            filterSpinner.setAdapter(filterAdapter);
            filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //load filter string to edit text
                    getFilterInputValue(position, currentPagePosition);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        //text
        filterInput = (AutoCompleteTextView) findViewById(R.id.filter_input);
        if(filterInput != null){
            filterInputAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
            filterInput.setAdapter(filterInputAdapter);
            filterInput.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveFilter();
                        if(startFilter(currentPagePosition))
                            moveFilterLayoutUp();
                    }
                    return false;
                }
            });
        }

        filterInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    IForcedHeightToListView callbacks = ((IForcedHeightToListView)adapter.getPage(currentPagePosition));
                    if(callbacks != null)
                        callbacks.addAdsHeightToListView(0);
                }else{
                    IForcedHeightToListView callbacks = ((IForcedHeightToListView)adapter.getPage(currentPagePosition));
                    if(callbacks != null)
                        callbacks.addAdsHeightToListView(adsHeight);
                }
            }
        });

        //sort spinner
        sortSpinner = (AppCompatSpinner)findViewById(R.id.sort_spinner);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this, R.array.str_sort_array, R.layout.layout_spinner);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if(sortSpinner != null){
            sortSpinner.setAdapter(sortAdapter);
            sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int oldPos = getSortSpinnerValueAsInt(currentPagePosition);
                    if(oldPos != position)
                        setSortUI(position);
                    else
                        getRadioGroup(currentPagePosition);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        //radio
        radioGroup = (RadioGroup) findViewById(R.id.sort_radio_group);
        desCheckBox = (AppCompatRadioButton) findViewById(R.id.sort_radio_descending);
        ascCheckBox = (AppCompatRadioButton) findViewById(R.id.sort_radio_ascending);

        //filter button
        Button filterButton = (Button) findViewById(R.id.filter_button);
        if(filterButton != null){
            filterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFilter();
                    if(startFilter(currentPagePosition))
                        moveFilterLayoutUp();
                }
            });
        }

        //reset button
        Button resetButton = (Button) findViewById(R.id.reset_button);
        if(resetButton != null){
            resetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetFilter(currentPagePosition);
                }
            });
        }
    }

    private void loadSavedFilterData(){
        if(oldPage != currentPagePosition){
            getFilterSpinnerValue(currentPagePosition);
            getFilterInputValue(filterSpinner.getSelectedItemPosition(), currentPagePosition);
            getSortSpinnerValue(currentPagePosition);
            getRadioGroup(currentPagePosition);
            oldPage = currentPagePosition;
        }
    }

    private void setSortUI(int position){
        if(position == 0){
            radioGroup.clearCheck();
            for(int i = 0; i < radioGroup.getChildCount(); i++){
                radioGroup.getChildAt(i).setEnabled(false);
                radioGroup.clearCheck();
            }
        }else{
            for(int i = 0; i < radioGroup.getChildCount(); i++){
                radioGroup.getChildAt(i).setEnabled(true);
                radioGroup.check(R.id.sort_radio_descending);
            }
        }
    }

    private void getFilterSpinnerValue(int currentPage){
        switch (currentPage){
            case ALL_SERVERS:
                filterSpinner.setSelection(Utils.getPref(PREF_FILTER_LAST_SELECTED, 0, this));
                break;
            case FAVORITES:
                filterSpinner.setSelection(Utils.getPref(PREF_P2_FILTER_LAST_SELECTED, 0, this));
                break;
        }

    }

    private void setFilterSpinnerValue(int value, int currentPage){
        switch (currentPage){
            case ALL_SERVERS:
                Utils.putPref(PREF_FILTER_LAST_SELECTED, value, this);
                break;
            case FAVORITES:
                Utils.putPref(PREF_P2_FILTER_LAST_SELECTED, value, this);
                break;
        }
    }

    private void getFilterInputValue(int selectedPos, int currentPage){
        String key;
        switch (selectedPos){
            case 0:
                if(currentPage == ALL_SERVERS)
                    key = PREF_FILTER_COUNTRY;
                else if(currentPage == FAVORITES)
                    key = PREF_P2_FILTER_COUNTRY;
                else return;
                break;
            case 1:
                if(currentPage == ALL_SERVERS)
                    key = PREF_FILTER_HOSTNAME;
                else if(currentPage == FAVORITES)
                    key = PREF_P2_FILTER_HOSTNAME;
                else return;
                break;
            case 2:
                if(currentPage == ALL_SERVERS)
                    key = PREF_FILTER_IP;
                else if(currentPage == FAVORITES)
                    key = PREF_P2_FILTER_IP;
                else return;
                break;
            default:
                filterInput.setText("");
                return;
        }
        filterInput.setText(Utils.getPref(key, "", this));
    }

    private void setFilterInputValue(int selectedPos, String value, int currentPage){
        String key;
        switch (selectedPos){
            case 0:
                if(currentPage == ALL_SERVERS)
                    key = PREF_FILTER_COUNTRY;
                else if(currentPage == FAVORITES)
                    key = PREF_P2_FILTER_COUNTRY;
                else return;
                break;
            case 1:
                if(currentPage == ALL_SERVERS)
                    key = PREF_FILTER_HOSTNAME;
                else if(currentPage == FAVORITES)
                    key = PREF_P2_FILTER_HOSTNAME;
                else return;
                break;
            case 2:
                if(currentPage == ALL_SERVERS)
                    key = PREF_FILTER_IP;
                else if(currentPage == FAVORITES)
                    key = PREF_P2_FILTER_IP;
                else return;
                break;
            default:
                filterInput.setText("");
                return;
        }
        Utils.putPref(key, value, this);
    }

    private void getSortSpinnerValue(int currentPage){
        switch (currentPage){
            case ALL_SERVERS:
                sortSpinner.setSelection(Utils.getPref(PREF_SORT_BY, 0, this));
                break;
            case FAVORITES:
                sortSpinner.setSelection(Utils.getPref(PREF_P2_SORT_BY, 0, this));
                break;
        }
    }

    private int getSortSpinnerValueAsInt(int currentPage){
        switch (currentPage){
            case ALL_SERVERS:
                return Utils.getPref(PREF_SORT_BY, 0, this);
            case FAVORITES:
                return Utils.getPref(PREF_P2_SORT_BY, 0, this);
        }
        return 0;
    }

    private void setSortSpinnerValue(int value, int currentPage){
        switch (currentPage){
            case ALL_SERVERS:
                Utils.putPref(PREF_SORT_BY, value, this);
                break;
            case FAVORITES:
                Utils.putPref(PREF_P2_SORT_BY, value, this);
                break;
        }
    }

    private void getRadioGroup(int currentPage){
        int value = 0;
        switch (currentPage){
            case ALL_SERVERS:
                value = Utils.getPref(PREF_SORT_ORDER, 0, this);
                break;
            case FAVORITES:
                value = Utils.getPref(PREF_P2_SORT_ORDER, 0, this);
                break;
        }

        setSortUI(sortSpinner.getSelectedItemPosition());
        if(value == 1){
            desCheckBox.setChecked(true);
        }else if (value == 2){
            ascCheckBox.setChecked(true);
        }else{
            radioGroup.clearCheck();
        }
    }

    private void setRadioGroup(int value, int currentPage){
        switch (currentPage){
            case ALL_SERVERS:
                Utils.putPref(PREF_SORT_ORDER, value, this);
                break;
            case FAVORITES:
                Utils.putPref(PREF_P2_SORT_ORDER, value, this);
                break;
        }
    }

    private void showHideFilterLayout(){
        if(filterLayout.getVisibility() == View.VISIBLE){
            moveFilterLayoutUp();

        }else{
            loadSavedFilterData();
            loadAutoCompleteCountryNames();
            moveFilterLayoutDown();
        }
    }

    private void loadAutoCompleteCountryNames(){

        if(retainFragment.RetainAllServerVPNs != null && retainFragment.RetainAllServerVPNs.size() >= 0){
            List<String> countryNames = new ArrayList<>();
            for (VPNClass vpn: retainFragment.RetainAllServerVPNs) {
                if(!countryNames.contains(vpn.CountryLong))
                    countryNames.add(vpn.CountryLong);
            }
            filterInputAdapter.clear();
            filterInputAdapter.addAll(countryNames);
            filterInputAdapter.notifyDataSetChanged();
        }
    }

    private void moveFilterLayoutDown(){
        if(filterLayout == null || isFilterShow || !canManagementStart) return;

        btFilters.setImageResource(R.drawable.ic_clear_white_24dp);
        filterLayout.setVisibility(View.VISIBLE);
        int height = filterStartHeight + filterLayout.getHeight();
        ObjectAnimator animator = ObjectAnimator.ofFloat(filterLayout, "translationY", filterStartHeight, height);
        animator.setDuration(HIDE_TIME_LONG);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();
        isFilterShow = true;
    }

    private void moveFilterLayoutUp(){
        if(filterLayout == null || !isFilterShow || !canManagementStart) return;
        btFilters.setImageResource(R.drawable.ic_search_white_24dp);

        ObjectAnimator animator = ObjectAnimator.ofFloat(filterLayout, "translationY", filterLayout.getY(), filterStartHeight);
        animator.setDuration(SHOW_TIME_LONG);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                filterLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();

        isFilterShow = false;
        closeKeyboard();
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void saveFilter(){
        //save Filter Spinner
        int selectedPos = filterSpinner.getSelectedItemPosition();

        setFilterSpinnerValue(selectedPos, currentPagePosition);

        //save InputText
        setFilterInputValue(selectedPos, filterInput.getText().toString(), currentPagePosition);

        //save Sort Spinner
        selectedPos = sortSpinner.getSelectedItemPosition();
        setSortSpinnerValue(selectedPos, currentPagePosition);

        //save Radio
        if(ascCheckBox.isChecked()){
            setRadioGroup(2, currentPagePosition);
        }else if (desCheckBox.isChecked())
            setRadioGroup(1, currentPagePosition);
        else setRadioGroup(0, currentPagePosition);
    }

    private boolean startFilter(int currentPage){
        Fragment fragment = adapter.getPage(currentPage);
        if(fragment != null && IVPNFragmentCallbacksForActivity.class.isInstance(fragment)){
            return ((IVPNFragmentCallbacksForActivity)fragment).onClickFilterButton();
        }else{
            return false;
        }
    }

    private void resetFilter(int currentPage){
        switch (currentPage){
            case ALL_SERVERS:
                Utils.putPref(PREF_FILTER_COUNTRY, "", this);
                Utils.putPref(PREF_FILTER_HOSTNAME, "", this);
                Utils.putPref(PREF_FILTER_IP, "", this);
                getFilterInputValue(filterSpinner.getSelectedItemPosition(), currentPagePosition);
                sortSpinner.setSelection(0);
                Utils.putPref(PREF_SORT_BY, 0, this);
                setSortUI(0);
                Utils.putPref(PREF_SORT_ORDER, 0, this);
                break;
            case FAVORITES:
                Utils.putPref(PREF_P2_FILTER_COUNTRY, "", this);
                Utils.putPref(PREF_P2_FILTER_HOSTNAME, "", this);
                Utils.putPref(PREF_P2_FILTER_IP, "", this);
                getFilterInputValue(filterSpinner.getSelectedItemPosition(), currentPagePosition);
                sortSpinner.setSelection(0);
                Utils.putPref(PREF_P2_SORT_BY, 0, this);
                setSortUI(0);
                Utils.putPref(PREF_P2_SORT_ORDER, 0, this);
                break;
        }
        startFilter(currentPage);
    }

    //endregion

    //region Init FilterAppLayout

    private boolean isFilterAppShow = false;
    private int filterAppStartHeight = 0;
    private AutoCompleteTextView filterAppSearchInput;
    private ArrayAdapter<String> filterAppSearchInputAdapter;

    private void initFilterAppLayout(){
        //filter Layout
        filterAppLayout = (ScrollView) findViewById(R.id.filter_sort_app_layout);

        //search text
        filterAppSearchInput = (AutoCompleteTextView) findViewById(R.id.filter_app_search);
        if(filterAppSearchInput != null){
            filterAppSearchInputAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
            filterAppSearchInput.setAdapter(filterAppSearchInputAdapter);
            filterAppSearchInput.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        startFilterApp();
                    }
                    return false;
                }
            });
        }

        filterAppSearchInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    IForcedHeightToListView callbacks = ((IForcedHeightToListView)adapter.getPage(ALLOW_APP));
                    if(callbacks != null)
                        callbacks.addAdsHeightToListView(0);
                }else{
                    IForcedHeightToListView callbacks = ((IForcedHeightToListView)adapter.getPage(ALLOW_APP));
                    if(callbacks != null)
                        callbacks.addAdsHeightToListView(adsHeight);
                }
            }
        });

        //filter button
        Button filterButton = (Button) findViewById(R.id.filter_app_button);
        if(filterButton != null){
            filterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startFilterApp();
                }
            });
        }
    }

    private void showHideFilterAppLayout(){
        if(filterAppLayout.getVisibility() == View.VISIBLE){
            moveFilterAppLayoutUp();
        }else{
            moveFilterAppLayoutDown();
        }
    }

    private void moveFilterAppLayoutDown(){
        if(filterAppLayout == null || isFilterAppShow || !canManagementStart) return;

        btFilters.setImageResource(R.drawable.ic_clear_white_24dp);
        filterAppLayout.setVisibility(View.VISIBLE);
        int height = filterAppStartHeight + filterAppLayout.getHeight();
        ObjectAnimator animator = ObjectAnimator.ofFloat(filterAppLayout, "translationY", filterAppStartHeight, height);
        animator.setDuration(HIDE_TIME_LONG);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                filterAppSearchInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
        isFilterAppShow = true;
    }

    private void moveFilterAppLayoutUp(){
        if(filterAppLayout == null || !isFilterAppShow || !canManagementStart) return;
        btFilters.setImageResource(R.drawable.ic_search_white_24dp);

        ObjectAnimator animator = ObjectAnimator.ofFloat(filterAppLayout, "translationY", filterAppLayout.getY(), filterAppStartHeight);
        animator.setDuration(SHOW_TIME_LONG);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                filterAppLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();

        isFilterAppShow = false;
    }

    private boolean startFilterApp(){
        Fragment fragment = adapter.getPage(ALLOW_APP);
        if(fragment != null && AllowedAppsFragment.class.isInstance(fragment)){
            return ((AllowedAppsFragment)fragment).onClickFilterButton(filterAppSearchInput.getText().toString());
        }else{
            return false;
        }
    }

    //endregion

    //region Init ClickPreventingLayout
    private void initClickPreventingLayout(){
        clickPreventingLayout = (RelativeLayout) findViewById(R.id.prevent_click);
        loadingBar = findViewById(R.id.loading_bar);
        loadingBar.setVisibility(View.VISIBLE);
        if(clickPreventingLayout != null){
            clickPreventingLayout.setVisibility(View.GONE);
            clickPreventingLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }
    ObjectAnimator animator;
    private void showClickPreventingLayout(int color, int time, boolean loading){
        clickPreventingLayout.setVisibility(View.VISIBLE);

        if(loading){
            loadingBar.setVisibility(View.VISIBLE);
        }else{
            loadingBar.setVisibility(View.GONE);
        }

        if(VpnStatus.isVPNActive()) {
            return;
        }
        setEnabledAll(viewPagerTab, false);
        btFilters.setEnabled(false);
        clickPreventingLayout.setBackgroundColor(ContextCompat.getColor(this,color));
        if(animator != null && animator.isRunning()){
            animator.end();
        }
        animator = ObjectAnimator.ofFloat(clickPreventingLayout, "alpha", 0, 1);
        animator.setDuration(time);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.start();
    }

    public static void setEnabledAll(View v, boolean enabled) {
        v.setEnabled(enabled);
        v.setFocusable(enabled);

        if(v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++)
                setEnabledAll(vg.getChildAt(i), enabled);
        }
    }

    private void hideClickPreventingLayout(int color, int time){
        if(VpnStatus.isVPNActive()) return;
        setEnabledAll(viewPagerTab, true);
        btFilters.setEnabled(true);
        clickPreventingLayout.setBackgroundColor(ContextCompat.getColor(this,color));
        if(animator != null && animator.isRunning()){
            animator.end();
        }
        animator = ObjectAnimator.ofFloat(clickPreventingLayout, "alpha", 1, 0);
        animator.setDuration(time);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(retainFragment != null && retainFragment.IsLoadingCSV()) return;
                clickPreventingLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level) {
        updateStateToUIThread(state, logmessage);
    }

    @Override
    public void setConnectedVPN(String uuid) {

    }

    private void updateStateToUIThread(final String state, String logmessage ){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (state){
                    case "NOPROCESS":
                        setManagementStatus(R.string.str_vpn_status_disconnected);
                        moveManagementLayoutDown();
                        break;
                    case "USER_VPN_PERMISSION_CANCELLED":
                        setManagementStatus(R.string.str_vpn_status_disconnected);
                        moveManagementLayoutDown();
                        Toast.makeText(DashBoard.this, R.string.str_no_per, Toast.LENGTH_SHORT).show();
                        break;
                    case "USERPAUSE":
                        setManagementStatus(R.string.str_vpn_status_paused);
                        break;
                    case "EXITING":
                        if(!VpnStatus.isVPNActive()) {
                            setManagementStatus(R.string.str_vpn_status_disconnected);
                            moveManagementLayoutDown();
                        }
                        break;

                }
                setManagementStatus(VpnStatus.getLocalizedState(state));
            }
        });
    }

    private void setManagementIndicator(int colorRes){
        if(colorRes == -1) return;
        int color = ContextCompat.getColor(this,colorRes);
        GradientDrawable drawable = (GradientDrawable)managementIndicator.getBackground();
        drawable.setColor(color);
        managementStatus.setTextColor(color);
    }

    private int getManagementIndicatorColorByStatus(int statusId){

        switch (statusId) {
            case R.string.state_connecting:
                return R.color.colorConnecting;
            case R.string.state_wait:
                return R.color.colorConnecting;
            case R.string.state_auth:
                return R.color.colorConnecting;
            case R.string.state_tcp_connect:
                return R.color.colorConnecting;
            case R.string.state_reconnecting:
                return R.color.colorConnecting;
            case R.string.str_vpn_status_paused:
                return R.color.colorConnecting;
            case R.string.state_connected:
                return R.color.colorOnline;
            case R.string.str_vpn_status_disconnecting:
                return R.color.colorConnecting;
            case R.string.str_vpn_status_disconnected:
                return R.color.colorOffline;
            case R.string.str_vpn_status_preparing:
                return R.color.colorOffline;
            default:
                return -1;
        }
    }

    private void setManagementStatus(int stateId){
        if(stateId == R.string.unknown_state) return;

        if(managementStatus.getText().equals(getString(R.string.str_vpn_status_disconnecting)) && (stateId == R.string.state_connected)) return;
        if(managementStatus.getText().equals(getString(R.string.str_vpn_status_preparing)) && (stateId == R.string.str_vpn_status_disconnected)) return;
        managementStatus.setText(getString(stateId));

        int colorId = getManagementIndicatorColorByStatus(stateId);
        setManagementIndicator(colorId);
    }

    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {
        updateByteCountToUIThread(in, out, diffIn, diffOut);
    }

    private void updateByteCountToUIThread(final long in, final long out, final long diffIn, final long diffOut){
        if(!VpnStatus.isVPNActive()) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                managementUp.setText( OpenVPNService.humanReadableByteCount(out, false, getResources()));
                managementUpSpeed.setText(OpenVPNService.humanReadableByteCount(diffOut / OpenVPNManagement.mBytecountInterval, true, getResources()));

                managementDown.setText( OpenVPNService.humanReadableByteCount(in, false, getResources()));
                managementDownSpeed.setText(OpenVPNService.humanReadableByteCount(diffIn / OpenVPNManagement.mBytecountInterval, true, getResources()));
            }
        });
    }

    //endregion

}
