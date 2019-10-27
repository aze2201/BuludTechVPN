package tk.usagis2.BuludTechVPN.Utils.SortComparators;

import java.util.Comparator;

import tk.usagis2.BuludTechVPN.CSV.VPNClass;

/**
 * Created by UsagiS2 on 03/05/2016.
 */
public class SessionComparator implements Comparator<VPNClass> {

    private boolean sortOrder;
    public SessionComparator(boolean sortOrder){
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(VPNClass lhs, VPNClass rhs) {
        if(sortOrder){
            return rhs.NumberOfSessions - lhs.NumberOfSessions;
        }else{
            return lhs.NumberOfSessions - rhs.NumberOfSessions;
        }
    }
}