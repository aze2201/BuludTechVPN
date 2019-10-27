package tk.usagis2.BuludTechVPN.Utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.Base64;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import tk.usagis2.BuludTechVPN.R;

/**
 * Created by UsagiS2 on 11/04/2016.
 */
public class Utils {

    public static int tryParseInt(String tryParseValue, int defValue){
        if(tryParseValue.matches("^(?:[1-9]\\d*|0)$")){
            return Integer.parseInt(tryParseValue);
        }
        return defValue;
    }

    public static long tryParseLong(String tryParseValue, long defValue){
        if(tryParseValue.matches("^(?:[1-9]\\d*|0)$")){
            return Long.parseLong(tryParseValue);
        }
        return defValue;
    }

    public static float tryParseFloat(String tryParseValue, int defValue){
        try {
            return Float.parseFloat(tryParseValue);
        }
        catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static String getDisplayInt(int value){
        if(value != Integer.MIN_VALUE){
            return String.valueOf(value);
        }
        return "-";
    }

    public static String getDisplayFloat(int value){
        if(value != Integer.MIN_VALUE){
            return String.valueOf(value);
        }
        return "-";
    }

    public static String getHumanReadableByteCount(long bytes, boolean si) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes/Math.pow(unit, exp), pre);
    }

    public static String ConvertBase64ToString(String data){
        byte[] baseInBytes = Base64.decode(data, Base64.DEFAULT);
        try {
            return new String(baseInBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String ConvertStringFromBase64(String data){
        try {
            byte[] baseInBytes = data.getBytes("UTF-8");
            return Base64.encodeToString(baseInBytes, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String getDisplayTime(long seconds){
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        if(day > 0){
            if(day == 1) return day + " day";
            return day + " days";
        }else if (hours > 0){
            if(hours == 1) return hours + " hour";
            return hours + " hours";
        }else{
            return minute + "'" + second + "s";
        }
    }

    public static HashSet<String> getPackageNamesFromString(String tickedAppStr){
        HashSet<String> result = new HashSet<>(Arrays.asList(tickedAppStr.split(",")));
        result.remove("");
        return result;
    }

    public static void putPref(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs != null){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public static void putPref(String key, int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs != null){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."), fileName.length());
    }

    public static void putPref(String key, long value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs != null){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(key, value);
            editor.apply();
        }
    }

    public static void putPref(String key, boolean value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs != null){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

    public static String getPref(String key, String defValue, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(preferences != null)
            return preferences.getString(key, defValue);
        else
            return defValue;
    }

    public static int getPref(String key, int defValue, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(preferences != null)
            return preferences.getInt(key, defValue);
        else
            return defValue;
    }

    public static long getPref(String key, long defValue, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(preferences != null)
            return preferences.getLong(key, defValue);
        else
            return defValue;
    }

    public static boolean getPref(String key, boolean defValue, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(preferences != null)
            return preferences.getBoolean(key, defValue);
        else
            return defValue;
    }

    public static int getSettingSharePref(String key, int defValue, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs != null){
            String wantedPageStr = prefs.getString(key, String.valueOf(defValue));
            return Utils.tryParseInt(wantedPageStr, defValue);
        }else
            return defValue;
    }

    public static String getSettingSharePref(String key, String defValue, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs != null)
            return prefs.getString(key, defValue);
        else
            return defValue;
    }

    public static boolean getSettingSharePref(String key, boolean defValue, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs != null)
            return prefs.getBoolean(key, defValue);
        else
            return defValue;
    }

    private static Map<String,Integer> mapFrags = new HashMap<>();
    private static void initMap(Context context){
        mapFrags.clear();
        String[] strings = context.getResources().getStringArray(R.array.str_array_frags);
        TypedArray integers = context.getResources().obtainTypedArray(R.array.int_array_frags);
        for (int i = 0; i < strings.length; i++){
            mapFrags.put(strings[i], integers.getResourceId(i, R.mipmap.unknown));
        }
        integers.recycle();
    }

    public static int getFlagDrawable(Context context, String countryShort){
        if(mapFrags == null || mapFrags.size() == 0){
            initMap(context);
        }
        Integer result = mapFrags.get(countryShort);
        if(result == null){
            return R.mipmap.unknown;
        }else{
            return result;
        }
    }
}
