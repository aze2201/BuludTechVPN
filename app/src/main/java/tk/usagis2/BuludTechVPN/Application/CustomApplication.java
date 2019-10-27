package tk.usagis2.BuludTechVPN.Application;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.PRNGFixes;
import de.blinkt.openvpn.core.StatusListener;
import de.blinkt.openvpn.core.VpnStatus;
import tk.usagis2.BuludTechVPN.Databases.DatabaseHandler;
import tk.usagis2.BuludTechVPN.Databases.DatabaseManager;
import tk.usagis2.BuludTechVPN.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by UsagiS2 on 26/03/2016.
 */
public class CustomApplication extends Application {
    private StatusListener mStatus;

    @Override
    public void onCreate()
    {
        super.onCreate();
        initDatabase();
        PRNGFixes.apply();
        VpnStatus.initLogCache(getApplicationContext().getCacheDir());
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/open_sans_regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannels();
        mStatus = new StatusListener();
        mStatus.init(getApplicationContext());
    }

    protected void initDatabase() {
        DatabaseManager.initializeInstance(new DatabaseHandler(this.getBaseContext()));
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Background message
        CharSequence name = getString(R.string.channel_name_background);
        NotificationChannel mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
                name, NotificationManager.IMPORTANCE_MIN);

        mChannel.setDescription(getString(R.string.channel_description_background));
        mChannel.enableLights(false);

        mChannel.setLightColor(Color.DKGRAY);
        mNotificationManager.createNotificationChannel(mChannel);

        // Connection status change messages
        name = getString(R.string.channel_name_status);
        mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
                name, NotificationManager.IMPORTANCE_DEFAULT);

        mChannel.setDescription(getString(R.string.channel_description_status));
        mChannel.enableLights(true);

        mChannel.setLightColor(Color.BLUE);
        mNotificationManager.createNotificationChannel(mChannel);
    }
}
