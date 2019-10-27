package tk.usagis2.BuludTechVPN.Activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import tk.usagis2.BuludTechVPN.BuildConfig;
import tk.usagis2.BuludTechVPN.CSV.VPNClass;
import tk.usagis2.BuludTechVPN.Databases.DatabaseManager;
import tk.usagis2.BuludTechVPN.R;
import tk.usagis2.BuludTechVPN.Utils.Utils;

import java.io.File;
import java.util.List;

public class Settings extends AppCompatPreference {
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else if (preference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary("Silent");
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                    if (ringtone == null) {
                        preference.setSummary(null);
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    static Toolbar actionBar;
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        actionBar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.setting_action_bar, root, false);
        root.addView(actionBar, 0);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null)
//            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || VPNConnectionPreferenceFragment.class.getName().equals(fragmentName)
                 || VPNBootPreferenceFragment.class.getName().equals(fragmentName)
                || AboutFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("settings_general_list_start_page"));
            bindPreferenceSummaryToValue(findPreference("settings_general_list_force_reload_time"));
            bindPreferenceSummaryToValue(findPreference("settings_general_list_item_direction"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), Settings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onResume() {
            super.onResume();
            actionBar.setTitle(getString(R.string.settings_general));
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class VPNBootPreferenceFragment extends PreferenceFragment implements  Preference.OnPreferenceChangeListener {
        private ListPreference mAlwaysOnVPN;
        private CharSequence[] entries;
        private CharSequence[] entryValues;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_boot_settings);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            // exp: bindPreferenceSummaryToValue(findPreference("settings_general_list_start_page"));

            //bindPreferenceSummaryToValue(findPreference("alwaysOnVpn"));
            mAlwaysOnVPN = (ListPreference) findPreference("alwaysOnVpn");
            mAlwaysOnVPN.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), Settings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mAlwaysOnVPN) {
                int index = -1;
                int i=0;
                for (CharSequence value : entryValues) {
                    if(value.equals(newValue)){
                        index = i;
                        break;
                    }
                    i++;
                }
                if(index == -1){
                    mAlwaysOnVPN.setSummary(R.string.novpn_selected);
                }else
                    mAlwaysOnVPN.setSummary(entries[index]);
            }
            return true;
        }

        @Override
        public void onResume() {
            super.onResume();

            actionBar.setTitle(getString(R.string.settings_boot));

            List<VPNClass> vpnList = DatabaseManager.getInstance().getAllVPNs();
            entries = new CharSequence[vpnList.size()];
            entryValues = new CharSequence[vpnList.size()];

            int i=0;
            String savedId = Utils.getPref("alwaysOnVpn","",getActivity());
            int indexOfId = -1;

            for (VPNClass p: vpnList)
            {
                entries[i]=p.HostName;
                entryValues[i]=String.valueOf(p.Id);
                if(savedId.equals(entryValues[i])){
                    indexOfId = i;
                }
                i++;
            }

            mAlwaysOnVPN.setEntries(entries);
            mAlwaysOnVPN.setEntryValues(entryValues);

            if(indexOfId == -1){
                mAlwaysOnVPN.setSummary(R.string.novpn_selected);
            }else
                mAlwaysOnVPN.setSummary(entries[indexOfId]);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class VPNConnectionPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_connect_settings);
            setHasOptionsMenu(true);

            PreferenceCategory devHacks = (PreferenceCategory) findPreference("device_hacks");
            Preference loadtun = findPreference("loadTunModule");
            if(!isTunModuleAvailable()) {
                loadtun.setEnabled(false);
                devHacks.removePreference(loadtun);
            }

            CheckBoxPreference cm9hack = (CheckBoxPreference) findPreference("useCM9Fix");
            if (!cm9hack.isChecked() && (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1)) {
                devHacks.removePreference(cm9hack);
            }

            if(devHacks.getPreferenceCount()==0)
                getPreferenceScreen().removePreference(devHacks);

            if (!"ovpn3".equals(BuildConfig.FLAVOR)) {
                PreferenceCategory appBehaviour = (PreferenceCategory) findPreference("vpn_connection");
                appBehaviour.removePreference(findPreference("ovpn3"));
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), Settings.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private boolean isTunModuleAvailable() {
            // Check if the tun module exists on the file system
            return new File("/system/lib/modules/tun.ko").length() > 10;
        }

        @Override
        public void onResume() {
            super.onResume();
            actionBar.setTitle(getString(R.string.settings_connection));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_about, container, false);
        }

        @Override
        public void onResume() {
            super.onResume();
            actionBar.setTitle(getString(R.string.str_about_title));
        }
    }
}
