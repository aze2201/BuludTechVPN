package tk.usagis2.BuludTechVPN.Utils.SortComparators;

import java.util.Comparator;

import tk.usagis2.BuludTechVPN.CSV.VPNClass;

/**
 * Created by UsagiS2 on 03/05/2016.
 */
public class CountryComparator implements Comparator<VPNClass> {

    private boolean sortOrder;
    public CountryComparator(boolean sortOrder){
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(VPNClass lhs, VPNClass rhs) {
        if(sortOrder){
            return rhs.CountryLong.compareToIgnoreCase(lhs.CountryLong);
        }else{
            return lhs.CountryLong.compareToIgnoreCase(rhs.CountryLong);
        }
    }
}
