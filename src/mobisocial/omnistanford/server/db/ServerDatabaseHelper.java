package mobisocial.omnistanford.server.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ServerDatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "ServerDatabaseHelper";
    
    private static final String DB_NAME = "OmniStanford_Server.db";
    private static final int VERSION = 1;
    
    public ServerDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, MUser.TABLE,
        		MUser.COL_ID, "INTEGER PRIMARY KEY",
        		MUser.COL_LOCAL_ID, "INTEGER NOT NULL",
        		MUser.COL_ACCOUNT_TYPE, "TEXT NOT NULL",
        		MUser.COL_IDENTIFIER, "TEXT NOT NULL",
        		MUser.COL_NAME, "TEXT NOT NULL");
        
        createTable(db, MCheckinData.TABLE,
                MCheckinData.COL_ID, "INTEGER PRIMARY KEY",
                MCheckinData.COL_ENTRY_TIME, "INTEGER",
                MCheckinData.COL_EXIT_TIME, "INTEGER",
                MCheckinData.COL_LOCATION_ID, "INTEGER NOT NULL",
                MCheckinData.COL_USER_ID, "INTEGER NOT NULL");
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion) {
            return;
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
