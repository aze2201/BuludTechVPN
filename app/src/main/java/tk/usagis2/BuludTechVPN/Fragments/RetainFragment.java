package tk.usagis2.BuludTechVPN.Fragments;

/**
 * Created by UsagiS2 on 22/03/2016.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.loopj.android.http.AsyncHttpResponseHandler;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import tk.usagis2.BuludTechVPN.CSV.CSVReader;
import tk.usagis2.BuludTechVPN.CSV.HTTPLoader;
import tk.usagis2.BuludTechVPN.CSV.VPNClass;
import tk.usagis2.BuludTechVPN.Databases.DatabaseManager;
import tk.usagis2.BuludTechVPN.R;
import tk.usagis2.BuludTechVPN.Utils.MCrypt;
import tk.usagis2.BuludTechVPN.Utils.Utils;

public class RetainFragment extends Fragment {

    public static final String PREF_LAST_LOAD_TIME = "PREF_LAST_LOAD_TIME";
    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_PARSING_FAILED = 1;
    public static final int ERROR_CONVERSION_FAILED = 2;
    public static final int ERROR_NO_ERROR_BUT_NO_SERVER = 3;
    public static final int ERROR_READING_SQL_FAILED = 4;
    public static final String API = "https://buludtech.com/BuludTechVpn/list";
    public static final String SUB_API = "https://buludtech.com/BuludTechVpn/list"; //"http://mr3chitk.x10host.com/"
    public static final String SAVE_DATA = "data.backup";

    //Activity callback interface
    public interface CSVLoadCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute(List<VPNClass> result, int errorValue);
        void retry(int id);
        void startLoading();
        void stopLoading();
    }

    public interface DatabaseLoadCallbacks {
        void onDbPreExecute();
        void onDbProgressUpdate(int percent);
        void onDbCancelled();
        void onDbPostExecute(List<VPNClass> result, int errorValue);
        void startDbLoading();
        void stopDbLoading();
    }

    //callbacks
    private CSVLoadCallbacks csvLoadCallbacks;
    private DatabaseLoadCallbacks databaseLoadCallbacks;

    //tasks
    private CSVLoadTask csvLoadTask;
    private DecryptTask decryptTask;
    private DatabaseLoadTask databaseLoadTask;
    private OfflineFileTask offlineFileTask;

    //retain data
    public List<VPNClass> RetainAllServerVPNs;
    public List<VPNClass> RetainFavoriteVPNs;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        csvLoadCallbacks = (CSVLoadCallbacks) activity;
        databaseLoadCallbacks = (DatabaseLoadCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        csvLoadCallbacks = null;
        databaseLoadCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public boolean doReload(){
        long currentTime = Calendar.getInstance().getTimeInMillis();
        int forceReloadTime = Utils.getSettingSharePref("settings_general_list_force_reload_time", 60, this.getActivity());
        boolean doReload;

        if(forceReloadTime == 1){
            //migrate old save data
            Utils.putPref("settings_general_list_force_reload_time", "5", this.getActivity());
        }
        if(forceReloadTime == 10){
            //migrate old save data
            Utils.putPref("settings_general_list_force_reload_time", "15", this.getActivity());
        }

        if(forceReloadTime == 0){
            return false;
        }

        long lastLoadTime = Utils.getPref(PREF_LAST_LOAD_TIME, (long)0, getContext());
        doReload = currentTime - lastLoadTime > forceReloadTime * 60000;
        return doReload;
    }

    private boolean isLoadingCSV = false;
    public boolean IsLoadingCSV(){
        return isLoadingCSV;
    }
    public void loadVPNGateCsvFile(final Context context){
        isLoadingCSV = true;
        if(context != null)
            Utils.putPref(PREF_LAST_LOAD_TIME, Calendar.getInstance().getTimeInMillis(), context);
        HTTPLoader.setMaxRetriesAndTimeout(0, 30);
        HTTPLoader.get(API, null, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                if(csvLoadCallbacks != null) {
                    csvLoadCallbacks.startLoading();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                csvLoadTask = new CSVLoadTask(true);
                csvLoadTask.execute(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                reloadVPNGateCsvFile(context);
            }
        });
    }

    public void reloadVPNGateCsvFile(Context context){
        if(context != null)
            Utils.putPref(PREF_LAST_LOAD_TIME, Calendar.getInstance().getTimeInMillis(), context);
        HTTPLoader.setMaxRetriesAndTimeout(0, 30);
        HTTPLoader.get(SUB_API, null, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                if(csvLoadCallbacks != null) {
                    csvLoadCallbacks.startLoading();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                decryptTask = new DecryptTask();
                decryptTask.execute(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                if(csvLoadCallbacks != null){
                    csvLoadCallbacks.stopLoading();
                    csvLoadCallbacks.retry(R.string.str_err_http_error);
                }
                isLoadingCSV = false;
            }
        });
    }

    public void  loadVPNGateViaSaveFile(final Context context){
        if(csvLoadCallbacks != null) {
            csvLoadCallbacks.startLoading();
        }
        String fileDir = context.getFilesDir().getAbsolutePath() + "/" + SAVE_DATA;
        final File file = new File(fileDir);
        if(!file.exists()) {
            loadVPNGateCsvFile(context);
            return;
        }

        offlineFileTask = new OfflineFileTask(context);
        offlineFileTask.execute(file);
    }

    private void saveByteArrayToFile(Context context, byte[] data){
        try{
            String fileDir = context.getFilesDir().getAbsolutePath() + "/" + SAVE_DATA;
            File file = new File(fileDir);
            if (!file.exists()) {
                file.createNewFile();
            }else{
                file.delete();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        }catch (Exception e){
        }
    }

    public void loadVPNGateSQLite(){
        databaseLoadTask = new DatabaseLoadTask();
        databaseLoadTask.execute();
    }

    public boolean isLoading(){
        return csvLoadTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    //AsyncTasks
    private class DecryptTask extends AsyncTask<byte[], Integer, byte[]>{

        @Override
        protected byte[] doInBackground(byte[]... bytes) {
            try {
                MCrypt mcrypt = new MCrypt();
                String str = new String(bytes[0], "UTF-8");
                String decrypted = new String(mcrypt.decrypt(str));
                return decrypted.getBytes("UTF-8");
            }
            catch (Exception ex){
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            isLoadingCSV = false;
            if(bytes != null){
                csvLoadTask = new CSVLoadTask(true);
                csvLoadTask.execute(bytes);
            }else{
                if(csvLoadCallbacks != null){
                    csvLoadCallbacks.stopLoading();
                    csvLoadCallbacks.retry(R.string.str_err_http_error);
                }
            }
        }
    }
    private class OfflineFileTask extends AsyncTask<File, Integer, byte[]>{
        private Context context;
        public OfflineFileTask(Context context){
            this.context = context;
        }
        @Override
        protected byte[] doInBackground(File... files) {
            final int size = (int) files[0].length();
            byte[] allBytes = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(files[0]));
                buf.read(allBytes, 0, allBytes.length);
                buf.close();
                return allBytes;
            } catch (Exception e) {
                if(getActivity()== null) return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            if(bytes == null){
                loadVPNGateCsvFile(context);
            }else{
                csvLoadTask = new CSVLoadTask(false);
                csvLoadTask.execute(bytes);
            }
        }
    }

    private class CSVLoadTask extends AsyncTask<byte[], Integer, List<VPNClass>> {
        private boolean doSave;
        public CSVLoadTask(boolean doSave){
            this.doSave = doSave;
        }
        int errorCode;
        @Override
        protected List<VPNClass> doInBackground(byte[]... params) {
            List<VPNClass> vpnList;
            try {
                List<String[]> rs = parseToCSV(params[0]);
                if(rs.size()>=1) rs.remove(0);
                else return null;
                vpnList = parseDataToClassObjects(rs);
            }catch (IOException e){
                vpnList = null;
                errorCode = ERROR_PARSING_FAILED;
            }catch (NumberFormatException ex){
                vpnList = null;
                errorCode = ERROR_CONVERSION_FAILED;
            }

            if(doSave)
                saveByteArrayToFile(getContext(), params[0]);
            return vpnList;
        }

        @Override
        protected void onPreExecute() {
            if (csvLoadCallbacks != null) {
                csvLoadCallbacks.onPreExecute();
                errorCode = ERROR_NO_ERROR;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (csvLoadCallbacks != null) {
                csvLoadCallbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            isLoadingCSV = false;
            if (csvLoadCallbacks != null) {
                csvLoadCallbacks.stopLoading();
                csvLoadCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<VPNClass> postData) {
            isLoadingCSV = false;
            if (csvLoadCallbacks != null) {
                csvLoadCallbacks.stopLoading();
                RetainAllServerVPNs = postData;
                csvLoadCallbacks.onPostExecute(postData, errorCode);
            }
        }

        //CSV parser func
        private List<String[]> parseToCSV(byte[] response) throws IOException{
            String next[];
            List<String[]> list = new ArrayList<>();

            InputStreamReader streamReader = new InputStreamReader(new ByteArrayInputStream(response));
            CSVReader reader = new CSVReader(streamReader);

            while(true) {
                next = reader.readNext();
                if(next != null) {
                    if(next.length <= 1) continue;
                    list.add(next);
                } else {
                    break;
                }
            }
            return list;
        }

        //parse data to class objects
        private List<VPNClass> parseDataToClassObjects(List<String[]> data) throws NumberFormatException, UnsupportedEncodingException {
            List<VPNClass> items = new ArrayList<>();
            for (String[] dataFields: data) {
                if(dataFields == null || dataFields.length < 15){
                    Log.e("error", " not enough fields");
                    continue;
                }
                VPNClass obj = new VPNClass();
                obj.HostName = dataFields[0];
                obj.IP = dataFields[1];
                obj.Score = Utils.tryParseInt(dataFields[2], Integer.MIN_VALUE);
                obj.Ping = Utils.tryParseInt(dataFields[3], Integer.MIN_VALUE);
                obj.Speed = Utils.tryParseLong(dataFields[4], Integer.MIN_VALUE);
                obj.CountryLong = dataFields[5];
                obj.CountryShort = dataFields[6];
                obj.NumberOfSessions = Utils.tryParseInt(dataFields[7], Integer.MIN_VALUE);
                obj.UpTime = Utils.tryParseLong(dataFields[8], Integer.MIN_VALUE);
                obj.TotalUsers = Utils.tryParseInt(dataFields[9], Integer.MIN_VALUE);
                obj.TotalTraffic = Utils.tryParseFloat(dataFields[10], Integer.MIN_VALUE);
                obj.LogType = dataFields[11];
                obj.Operator = dataFields[12];
                obj.Message = dataFields[13];
                obj.OpenVPN_ConfigData_Base64 = dataFields[14];
                items.add(obj);
            }

            return items;
        }
    }

    private class DatabaseLoadTask extends AsyncTask<Void, Integer, List<VPNClass>> {
        int errorCode;
        @Override
        protected List<VPNClass> doInBackground(Void... params) {
            List<VPNClass> vpnList = null;
            if (!isCancelled()) {
                try {
                    vpnList = DatabaseManager.getInstance().getAllVPNs();
                } catch (Exception ex) {
                    vpnList = null;
                    errorCode = ERROR_READING_SQL_FAILED;
                }
            }
            return vpnList;
        }

        @Override
        protected void onPreExecute() {
            if (databaseLoadCallbacks != null) {
                databaseLoadCallbacks.startDbLoading();
                databaseLoadCallbacks.onDbPreExecute();
                errorCode = ERROR_NO_ERROR;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (databaseLoadCallbacks != null) {
                databaseLoadCallbacks.onDbProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (databaseLoadCallbacks != null) {
                databaseLoadCallbacks.stopDbLoading();
                databaseLoadCallbacks.onDbCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<VPNClass> postData) {
            if (databaseLoadCallbacks != null) {
                databaseLoadCallbacks.stopDbLoading();
                RetainFavoriteVPNs = postData;
                databaseLoadCallbacks.onDbPostExecute(postData, errorCode);
            }
        }
    }
}