package tk.usagis2.BuludTechVPN.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import tk.usagis2.BuludTechVPN.CSV.VPNClass;
import tk.usagis2.BuludTechVPN.R;

/**
 * Created by Usagi on 7/27/2016.
 */
public class VPNClassToVPNProfileConverter {

    public interface IFinishConvertCallback{
        void finishConvert(VpnProfile profile);
    }
    private Context context;
    private AsyncTask<Void, Void, Integer> importTask;
    private VPNClass vpnClass;
    private VpnProfile mResult;
    private String mEmbeddedPwFile;
    private IFinishConvertCallback iFinishConvertCallback;
    public VPNClassToVPNProfileConverter(Context context, IFinishConvertCallback iFinishConvertCallback){
        this.context = context;
        this.iFinishConvertCallback = iFinishConvertCallback;
    }

    public VpnProfile ConvertToProfile(VPNClass vpnClass){
        this.vpnClass = vpnClass;
        startImportTask(vpnClass.OpenVPN_ConfigData_Base64);
        return mResult;
    }

    private void startImportTask(final String data) {
        importTask = new AsyncTask<Void, Void, Integer>() {

            @Override
            protected void onPreExecute() {
                //mProgress = new ProgressBar(ConfigConverter.this);
                //addViewToLog(mProgress);
            }

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    mResult = GetVpnProfile(data);
                    if (mResult ==null) return -3;
                }
                catch (SecurityException se)
                {
                    return -2;
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer errorCode) {
                if (errorCode == 0 && iFinishConvertCallback != null) {
                    mResult.mName = vpnClass.HostName;
                    iFinishConvertCallback.finishConvert(mResult);
                }
            }
        };
        importTask.execute();
    }

    public VpnProfile GetVpnProfile(String base64Config){
        String decodedConfig = Utils.ConvertBase64ToString(base64Config);
        InputStream is;
        try {
            is = new ByteArrayInputStream(decodedConfig.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return doImport(is);
    }

    private VpnProfile doImport(InputStream is) {
        ConfigParser cp = new ConfigParser();
        try {
            InputStreamReader isr = new InputStreamReader(is);

            cp.parseConfig(isr);
            VpnProfile result = cp.convertProfile();
            embedFiles(result, cp);
            return result;

        } catch (IOException | ConfigParser.ConfigParseError e) {
            Toast.makeText(context, R.string.error_reading_config_file, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void embedFiles(VpnProfile vpnProfile, ConfigParser cp) {
        // This where I would like to have a c++ style
        // void embedFile(std::string & option)

        vpnProfile.mCaFilename = embedFile(vpnProfile.mCaFilename);
        vpnProfile.mClientCertFilename = embedFile(vpnProfile.mClientCertFilename);
        vpnProfile.mClientKeyFilename = embedFile(vpnProfile.mClientKeyFilename);
        vpnProfile.mTLSAuthFilename = embedFile(vpnProfile.mTLSAuthFilename);
        vpnProfile.mPKCS12Filename = embedFile(vpnProfile.mPKCS12Filename);
        vpnProfile.mCrlFilename = embedFile(vpnProfile.mCrlFilename);
        if (cp != null) {
            mEmbeddedPwFile = cp.getAuthUserPassFile();
            mEmbeddedPwFile = embedFile(cp.getAuthUserPassFile());
        }
    }

    private String embedFile(String filename) {
        if (filename == null)
            return null;

        // Already embedded, nothing to do
        if (VpnProfile.isEmbedded(filename))
            return filename;else return null;
    }
}
