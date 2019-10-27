package tk.usagis2.BuludTechVPN.Databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import tk.usagis2.BuludTechVPN.CSV.VPNClass;
import tk.usagis2.BuludTechVPN.Utils.Utils;

/**
 * Created by UsagiS2 on 26/03/2016.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static DatabaseHandler mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(DatabaseHandler helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            mDatabase.close();
        }
    }

    public boolean checkExistVPN(VPNClass vpnClass){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        String selectQuery = "SELECT COUNT(*) FROM " + DatabaseHandler.TABLE_VPN + " WHERE " + DatabaseHandler.KEY_IP + " = '" + vpnClass.IP.trim() +"'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int countValue = 0;
        if (cursor.moveToFirst()) {
            countValue = cursor.getInt(0);
        }
        cursor.close();
        return countValue > 0;
    }

    public void insertVPN(VPNClass vpnClass){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.KEY_HOSTNAME, vpnClass.HostName);
        values.put(DatabaseHandler.KEY_IP, vpnClass.IP);
        values.put(DatabaseHandler.KEY_SCORE, vpnClass.Score);
        values.put(DatabaseHandler.KEY_PING, vpnClass.Ping);
        values.put(DatabaseHandler.KEY_SPEED, vpnClass.Speed);
        values.put(DatabaseHandler.KEY_COUNTRY_LONG, vpnClass.CountryLong);
        values.put(DatabaseHandler.KEY_COUNTRY_SHORT, vpnClass.CountryShort);
        values.put(DatabaseHandler.KEY_NUM_OF_SESSIONS, vpnClass.NumberOfSessions);
        values.put(DatabaseHandler.KEY_UPTIME, vpnClass.UpTime);
        values.put(DatabaseHandler.KEY_TOTAL_USERS, vpnClass.TotalUsers);
        values.put(DatabaseHandler.KEY_TOTAL_TRAFFIC, vpnClass.TotalTraffic);
        values.put(DatabaseHandler.KEY_LOG_TYPE, vpnClass.LogType);
        values.put(DatabaseHandler.KEY_OPERATOR, vpnClass.Operator);
        values.put(DatabaseHandler.KEY_MESSAGE, vpnClass.Message);
        values.put(DatabaseHandler.KEY_BASE64, vpnClass.OpenVPN_ConfigData_Base64);

        db.insert(DatabaseHandler.TABLE_VPN, null, values);
    }

    public void updateVPN(VPNClass vpnClass){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.KEY_HOSTNAME, vpnClass.HostName);
        values.put(DatabaseHandler.KEY_IP, vpnClass.IP);
        values.put(DatabaseHandler.KEY_SCORE, vpnClass.Score);
        values.put(DatabaseHandler.KEY_PING, vpnClass.Ping);
        values.put(DatabaseHandler.KEY_SPEED, vpnClass.Speed);
        values.put(DatabaseHandler.KEY_COUNTRY_LONG, vpnClass.CountryLong);
        values.put(DatabaseHandler.KEY_COUNTRY_SHORT, vpnClass.CountryShort);
        values.put(DatabaseHandler.KEY_NUM_OF_SESSIONS, vpnClass.NumberOfSessions);
        values.put(DatabaseHandler.KEY_UPTIME, vpnClass.UpTime);
        values.put(DatabaseHandler.KEY_TOTAL_USERS, vpnClass.TotalUsers);
        values.put(DatabaseHandler.KEY_TOTAL_TRAFFIC, vpnClass.TotalTraffic);
        values.put(DatabaseHandler.KEY_LOG_TYPE, vpnClass.LogType);
        values.put(DatabaseHandler.KEY_OPERATOR, vpnClass.Operator);
        values.put(DatabaseHandler.KEY_MESSAGE, vpnClass.Message);
        values.put(DatabaseHandler.KEY_BASE64, vpnClass.OpenVPN_ConfigData_Base64);

        db.update(DatabaseHandler.TABLE_VPN,values,DatabaseHandler.KEY_IP + " = '" + vpnClass.IP +"'", null);
    }

    public void deleteVPN(VPNClass vpnClass){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(DatabaseHandler.TABLE_VPN, DatabaseHandler.KEY_ID + " = ?", new String[] { String.valueOf(vpnClass.Id) });
    }

    public List<VPNClass> getAllVPNs(){
        List<VPNClass> list = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseHandler.TABLE_VPN;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                VPNClass vpnClass = new VPNClass();
                vpnClass.Id = Utils.tryParseInt(cursor.getString(0), Integer.MIN_VALUE);
                vpnClass.HostName = cursor.getString(1);
                vpnClass.IP = cursor.getString(2);
                vpnClass.Score =  cursor.getInt(3);
                vpnClass.Ping =  cursor.getInt(4);
                vpnClass.Speed = cursor.getLong(5);
                vpnClass.CountryLong = cursor.getString(6);
                vpnClass.CountryShort = cursor.getString(7);
                vpnClass.NumberOfSessions =cursor.getInt(8);
                vpnClass.UpTime = cursor.getLong(9);
                vpnClass.TotalUsers =cursor.getInt(10);
                vpnClass.TotalTraffic =  cursor.getFloat(11);
                vpnClass.LogType = cursor.getString(12);
                vpnClass.Operator = cursor.getString(13);
                vpnClass.Message = cursor.getString(14);
                vpnClass.OpenVPN_ConfigData_Base64 = cursor.getString(15);

                // Adding contact to list
                list.add(vpnClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public VPNClass getVPNById(int id){
        List<VPNClass> list = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DatabaseHandler.TABLE_VPN + " WHERE " + DatabaseHandler.KEY_ID + " = " + id;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                VPNClass vpnClass = new VPNClass();
                vpnClass.Id = Utils.tryParseInt(cursor.getString(0), Integer.MIN_VALUE);
                vpnClass.HostName = cursor.getString(1);
                vpnClass.IP = cursor.getString(2);
                vpnClass.Score =  cursor.getInt(3);
                vpnClass.Ping =  cursor.getInt(4);
                vpnClass.Speed = cursor.getLong(5);
                vpnClass.CountryLong = cursor.getString(6);
                vpnClass.CountryShort = cursor.getString(7);
                vpnClass.NumberOfSessions =cursor.getInt(8);
                vpnClass.UpTime = cursor.getLong(9);
                vpnClass.TotalUsers =cursor.getInt(10);
                vpnClass.TotalTraffic =  cursor.getFloat(11);
                vpnClass.LogType = cursor.getString(12);
                vpnClass.Operator = cursor.getString(13);
                vpnClass.Message = cursor.getString(14);
                vpnClass.OpenVPN_ConfigData_Base64 = cursor.getString(15);

                // Adding contact to list
                list.add(vpnClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if(list.size() > 0)
            return list.get(0);
        else
            return null;
    }


}
