package mobisocial.omnistanford.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelper";
    
    private static final String DB_NAME = "OmniStanford.db";
    private static final int VERSION = 7;
    
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, MAccount.TABLE,
                MAccount.COL_ID, "INTEGER PRIMARY KEY",
                MAccount.COL_ACCOUNT_TYPE, "TEXT NOT NULL",
                MAccount.COL_IDENTIFIER, "TEXT NOT NULL",
                MAccount.COL_NAME, "TEXT NOT NULL");
        
        createTable(db, MCheckinData.TABLE,
                MCheckinData.COL_ID, "INTEGER PRIMARY KEY",
                MCheckinData.COL_ENTRY_TIME, "INTEGER",
                MCheckinData.COL_EXIT_TIME, "INTEGER",
                MCheckinData.COL_LOCATION_ID, "INTEGER NOT NULL",
                MCheckinData.COL_ACCOUNT_ID, "INTEGER NOT NULL");
        
        createTable(db, MDiscoveredPerson.TABLE,
                MDiscoveredPerson.COL_ID, "INTEGER PRIMARY KEY",
                MDiscoveredPerson.COL_ACCOUNT_TYPE, "TEXT NOT NULL",
                MDiscoveredPerson.COL_IDENTIFIER, "TEXT NOT NULL",
                MDiscoveredPerson.COL_NAME, "TEXT NOT NULL");
        
        createTable(db, MDiscovery.TABLE,
                MDiscovery.COL_ID, "INTEGER PRIMARY KEY",
                MDiscovery.COL_CHECKIN_ID, "INTEGER NOT NULL",
                MDiscovery.COL_PERSON_ID, "INTEGER NOT NULL",
                MDiscovery.COL_CONNECTION_TYPE, "TEXT NOT NULL");
        
        createTable(db, MLocation.TABLE,
                MLocation.COL_ID, "INTEGER PRIMARY KEY",
                MLocation.COL_NAME, "TEXT NOT NULL",
                MLocation.COL_PRINCIPAL, "TEXT NOT NULL",
                MLocation.COL_ACCOUNT_TYPE, "TEXT NOT NULL",
                MLocation.COL_TYPE, "TEXT NOT NULL",
                MLocation.COL_MIN_LAT, "REAL",
                MLocation.COL_MAX_LAT, "REAL",
                MLocation.COL_MIN_LON, "REAL",
                MLocation.COL_MAX_LON, "REAL",
                MLocation.COL_FEED_URI, "TEXT",
                MLocation.COL_IMAGE_URL, "TEXT",
                MLocation.COL_IMAGE, "BLOB");
        
        createTable(db, MUserProperty.TABLE,
                MUserProperty.COL_ID, "INTEGER PRIMARY KEY",
                MUserProperty.COL_NAME, "TEXT NOT NULL",
                MUserProperty.COL_VALUE, "TEXT");
        
        createTable(db, MTag.TABLE,
    			MTag.COL_ID, "INTEGER PRIMARY KEY",
    			MTag.COL_NAME, "TEXT NOT NULL",
    			MTag.COL_LOCATION_ID, "INTEGER NOT NULL",
    			MTag.COL_CHECKIN_ID, "INTEGER NOT NULL",
    			MTag.COL_START_TIME, "INTEGER NOT NULL",
    			MTag.COL_END_TIME, "INTEGER NOT NULL");

        // checkin indexes
        db.execSQL("CREATE INDEX " + MCheckinData.TABLE + "_entry_time_lookup ON " +
                MCheckinData.TABLE + "(" + MCheckinData.COL_ENTRY_TIME + ")");
        db.execSQL("CREATE INDEX " + MCheckinData.TABLE + "_loc_lookup ON " +
                MCheckinData.TABLE + "(" + MCheckinData.COL_LOCATION_ID + ")");
        db.execSQL("CREATE INDEX " + MCheckinData.TABLE + "_loc_entry_lookup ON " +
                MCheckinData.TABLE + "(" + MCheckinData.COL_LOCATION_ID + "," + 
                MCheckinData.COL_ENTRY_TIME + ")");
        db.execSQL("CREATE INDEX " + MCheckinData.TABLE + "_entry_exit_lookup ON " +
                MCheckinData.TABLE + "(" + MCheckinData.COL_ENTRY_TIME + "," + 
                MCheckinData.COL_EXIT_TIME + ")");
        
        // discovery indexes
        db.execSQL("CREATE INDEX " + MDiscovery.TABLE + "_checkin_lookup ON " +
                MDiscovery.TABLE + "(" + MDiscovery.COL_CHECKIN_ID + ")");
        db.execSQL("CREATE INDEX " + MDiscovery.TABLE + "_user_type_lookup ON " +
                MDiscovery.TABLE + "(" + MDiscovery.COL_CHECKIN_ID + "," +
                MDiscovery.COL_CONNECTION_TYPE + ")");
        db.execSQL("CREATE INDEX " + MDiscovery.TABLE + "_user_lookup ON " +
                MDiscovery.TABLE + "(" + MDiscovery.COL_CHECKIN_ID + "," +
                MDiscovery.COL_PERSON_ID + "," + MDiscovery.COL_CONNECTION_TYPE + ")");
        
        // discovered person indexes
        db.execSQL("CREATE INDEX " + MDiscoveredPerson.TABLE + "_user_lookup ON " + 
                MDiscoveredPerson.TABLE + "(" + MDiscoveredPerson.COL_IDENTIFIER + "," +
                MDiscoveredPerson.COL_ACCOUNT_TYPE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion) {
            return;
        }
        if (oldVersion <= 1) {
            db.execSQL("ALTER TABLE " + MLocation.TABLE +
                    " ADD COLUMN " + MLocation.COL_FEED_URI + " TEXT;");
        }
        if (oldVersion <= 2) {
            createTable(db, MUserProperty.TABLE,
                    MUserProperty.COL_ID, "INTEGER PRIMARY KEY",
                    MUserProperty.COL_NAME, "TEXT NOT NULL",
                    MUserProperty.COL_VALUE, "TEXT");
        }
        if (oldVersion <= 3) {
        	createTable(db, MTag.TABLE,
        			MTag.COL_ID, "INTEGER PRIMARY KEY",
        			MTag.COL_NAME, "TEXT NOT NULL",
        			MTag.COL_LOCATION_ID, "INTEGER NOT NULL",
        			MTag.COL_START_TIME, "INTEGER NOT NULL",
        			MTag.COL_END_TIME, "INTEGER NOT NULL");
        }
        if (oldVersion <= 4) {
        	db.execSQL("ALTER TABLE " + MTag.TABLE +
                    " ADD COLUMN " + MTag.COL_CHECKIN_ID + " INTEGER;");
        }
        if (oldVersion <= 5) {
            // checkin indexes
            db.execSQL("CREATE INDEX " + MCheckinData.TABLE + "_entry_time_lookup ON " +
                    MCheckinData.TABLE + "(" + MCheckinData.COL_ENTRY_TIME + ")");
            db.execSQL("CREATE INDEX " + MCheckinData.TABLE + "_loc_lookup ON " +
                    MCheckinData.TABLE + "(" + MCheckinData.COL_LOCATION_ID + ")");
            db.execSQL("CREATE INDEX " + MCheckinData.TABLE + "_loc_entry_lookup ON " +
                    MCheckinData.TABLE + "(" + MCheckinData.COL_LOCATION_ID + "," + 
                    MCheckinData.COL_ENTRY_TIME + ")");
            db.execSQL("CREATE INDEX " + MCheckinData.TABLE + "_entry_exit_lookup ON " +
                    MCheckinData.TABLE + "(" + MCheckinData.COL_ENTRY_TIME + "," + 
                    MCheckinData.COL_EXIT_TIME + ")");
            
            // discovery indexes
            db.execSQL("CREATE INDEX " + MDiscovery.TABLE + "_checkin_lookup ON " +
                    MDiscovery.TABLE + "(" + MDiscovery.COL_CHECKIN_ID + ")");
            db.execSQL("CREATE INDEX " + MDiscovery.TABLE + "_user_type_lookup ON " +
                    MDiscovery.TABLE + "(" + MDiscovery.COL_CHECKIN_ID + "," +
                    MDiscovery.COL_CONNECTION_TYPE + ")");
            db.execSQL("CREATE INDEX " + MDiscovery.TABLE + "_user_lookup ON " +
                    MDiscovery.TABLE + "(" + MDiscovery.COL_CHECKIN_ID + "," +
                    MDiscovery.COL_PERSON_ID + "," + MDiscovery.COL_CONNECTION_TYPE + ")");
            
            // discovered person indexes
            db.execSQL("CREATE INDEX " + MDiscoveredPerson.TABLE + "_user_lookup ON " + 
                    MDiscoveredPerson.TABLE + "(" + MDiscoveredPerson.COL_IDENTIFIER + "," +
                    MDiscoveredPerson.COL_ACCOUNT_TYPE + ")");
        }
        if (oldVersion <= 6) {
            db.execSQL("ALTER TABLE " + MLocation.TABLE +
                    " ADD COLUMN " + MLocation.COL_IMAGE_URL + " TEXT;");
            db.execSQL("ALTER TABLE " + MLocation.TABLE +
                    " ADD COLUMN " + MLocation.COL_IMAGE + " BLOB;");
        }
        db.setVersion(VERSION);
    }
    
    private void createTable(SQLiteDatabase db, String tableName, String... cols){
        assert cols.length % 2 == 0;
        String s = "CREATE TABLE " + tableName + " (";
        for(int i = 0; i < cols.length; i += 2){
            s += cols[i] + " " + cols[i + 1];
            if(i < (cols.length - 2)){
                s += ", ";
            }
            else{
                s += " ";
            }
        }
        s += ")";
        Log.i(TAG, s);
        db.execSQL(s);
    }
}
