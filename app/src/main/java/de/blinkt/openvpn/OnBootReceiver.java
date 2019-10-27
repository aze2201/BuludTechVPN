/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import de.blinkt.openvpn.core.Preferences;
import de.blinkt.openvpn.core.ProfileManager;
import tk.usagis2.BuludTechVPN.CSV.VPNClass;
import tk.usagis2.BuludTechVPN.Databases.DatabaseManager;
import tk.usagis2.BuludTechVPN.Utils.VPNClassToVPNProfileConverter;


public class OnBootReceiver extends BroadcastReceiver {

	// Debug: am broadcast -a android.intent.action.BOOT_COMPLETED
	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();
		SharedPreferences prefs = Preferences.getDefaultSharedPreferences(context);

		boolean useStartOnBoot = prefs.getBoolean("restartvpnonboot", false);
		if (!useStartOnBoot)
			return;

		if(Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
			String idAsString = prefs.getString("alwaysOnVpn", "");
			int id;
			try {
				id = Integer.parseInt(idAsString);
			}catch (Exception e){
				id = -1;
			}
			if(id >= 0){
				VPNClass vpn = DatabaseManager.getInstance().getVPNById(id);
				if(vpn != null){
					startConvertVPN(vpn, context);
				}
			}
		}
	}

	private void startConvertVPN(VPNClass vpnClass, Context context) {
		VPNClassToVPNProfileConverter converter = new VPNClassToVPNProfileConverter(context, null);
		VpnProfile profile = converter.GetVpnProfile(vpnClass.OpenVPN_ConfigData_Base64);
		launchVPN(profile, context);
	}

	void launchVPN(VpnProfile vpnProfile, Context context) {
		ProfileManager.getInstance(context).addProfile(vpnProfile);
		Intent intent = new Intent(context, LaunchVPN.class);
		intent.putExtra(LaunchVPN.EXTRA_KEY, vpnProfile.getUUID().toString());
		intent.setAction(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
