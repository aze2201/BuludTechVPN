package tk.usagis2.BuludTechVPN.Fragments;

import android.view.View;

/**
 * Created by UsagiS2 on 01/04/2016.
 */
public interface IVPNFragmentCallbacksForAdapter extends IForcedHeightToListView {
    void onClickEvent(View view, int position, boolean doOpen);
    void onClickToButton(int position, int keyCode);
}
