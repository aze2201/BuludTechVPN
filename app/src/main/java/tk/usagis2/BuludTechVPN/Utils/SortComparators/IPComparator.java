package tk.usagis2.BuludTechVPN.Utils.SortComparators;

import java.util.Comparator;

import tk.usagis2.BuludTechVPN.CSV.VPNClass;

/**
 * Created by UsagiS2 on 03/05/2016.
 */
public class IPComparator implements Comparator<VPNClass> {

    private boolean sortOrder;
    public IPComparator(boolean sortOrder){
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(VPNClass lhs, VPNClass rhs) {
        if(sortOrder){
            return rhs.HostName.compareToIgnoreCase(lhs.HostName);
        }else{
            return lhs.HostName.compareToIgnoreCase(rhs.HostName);
        }
    }
}