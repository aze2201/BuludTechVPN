package tk.usagis2.BuludTechVPN.Databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by UsagiS2 on 26/03/2016.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "VPNManager";

    //Table
    static final String TABLE_VPN = "vpns";

    //Table Columns names
    static final String KEY_ID = "id";
    static final String KEY_HOSTNAME = "hostname";
    static final String KEY_IP = "ip";
    static final String KEY_SCORE = "score";
    static final String KEY_PING = "ping";
    static final String KEY_SPEED = "speed";
    static final String KEY_COUNTRY_LONG = "country_long";
    static final String KEY_COUNTRY_SHORT = "country_short";
    static final String KEY_NUM_OF_SESSIONS = "num_of_sessions";
    static final String KEY_UPTIME = "uptime";
    static final String KEY_TOTAL_USERS = "total_users";
    static final String KEY_TOTAL_TRAFFIC = "total_traffic";
    static final String KEY_LOG_TYPE = "log_type";
    static final String KEY_OPERATOR = "operator";
    static final String KEY_MESSAGE = "message";
    static final String KEY_BASE64 = "base64";

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " +
                TABLE_VPN + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_HOSTNAME + " TEXT,"
                + KEY_IP + " TEXT,"
                + KEY_SCORE + " INTEGER,"
                + KEY_PING + " INTEGER,"
                + KEY_SPEED + " INTEGER,"
                + KEY_COUNTRY_LONG + " TEXT,"
                + KEY_COUNTRY_SHORT + " TEXT,"
                + KEY_NUM_OF_SESSIONS + " INTEGER,"
                + KEY_UPTIME + " INTEGER,"
                + KEY_TOTAL_USERS + " INTEGER,"
                + KEY_TOTAL_TRAFFIC + " REAL,"
                + KEY_LOG_TYPE + " TEXT,"
                + KEY_OPERATOR + " TEXT,"
                + KEY_MESSAGE + " TEXT,"
                + KEY_BASE64 + " TEXT"
                + ")";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion > 1){
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VPN);
            onCreate(db);
        }
    }
}
