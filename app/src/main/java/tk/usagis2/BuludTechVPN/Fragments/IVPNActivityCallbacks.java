package tk.usagis2.BuludTechVPN.Fragments;

import java.util.List;

import tk.usagis2.BuludTechVPN.Activities.DashBoard;
import tk.usagis2.BuludTechVPN.CSV.VPNClass;

/**
 * Created by UsagiS2 on 24/03/2016.
 */

public interface IVPNActivityCallbacks extends IListScrollCallback {
    List<VPNClass> getVPNClasses (int typeOfVPNs);
    boolean startVPN(VPNClass vpnClass, DashBoard.FORCE_START force);
    boolean stopVPN(VPNClass vpnClass);
    void saveVPN(VPNClass vpnClass);
    void editVPN(VPNClass vpnClass);
    void deleteVPN(VPNClass vpnClass);
    void shareVPN(VPNClass vpnClass);
    void saveToSd(VPNClass vpnClass);
    void refreshLoadCSV();
    void refreshLoadDatabase();
}


