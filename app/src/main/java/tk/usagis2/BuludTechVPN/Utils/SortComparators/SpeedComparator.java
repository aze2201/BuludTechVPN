package tk.usagis2.BuludTechVPN.Utils.SortComparators;

import java.util.Comparator;

import tk.usagis2.BuludTechVPN.CSV.VPNClass;

/**
 * Created by UsagiS2 on 03/05/2016.
 */
public class SpeedComparator implements Comparator<VPNClass> {

    private boolean sortOrder;
    public SpeedComparator(boolean sortOrder){
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(VPNClass lhs, VPNClass rhs) {
        if(sortOrder){
            if(rhs.Speed > lhs.Speed)
                return 1;
            if(rhs.Speed < lhs.Speed)
                return -1;
            return 0;
        }else{
            if(rhs.Speed < lhs.Speed)
                return 1;
            if(rhs.Speed > lhs.Speed)
                return -1;
            return 0;
        }
    }
}