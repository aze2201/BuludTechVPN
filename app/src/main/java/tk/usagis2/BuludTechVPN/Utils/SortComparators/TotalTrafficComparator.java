package tk.usagis2.BuludTechVPN.Utils.SortComparators;

import java.util.Comparator;

import tk.usagis2.BuludTechVPN.CSV.VPNClass;

/**
 * Created by UsagiS2 on 03/05/2016.
 */
public class TotalTrafficComparator implements Comparator<VPNClass> {

    private boolean sortOrder;
    public TotalTrafficComparator(boolean sortOrder){
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(VPNClass lhs, VPNClass rhs) {
        if(sortOrder){
            if(rhs.TotalTraffic > lhs.TotalTraffic)
                return 1;
            if(rhs.TotalTraffic < lhs.TotalTraffic)
                return -1;
            return 0;
        }else{
            if(rhs.TotalTraffic < lhs.TotalTraffic)
                return 1;
            if(rhs.TotalTraffic > lhs.TotalTraffic)
                return -1;
            return 0;
        }
    }
}