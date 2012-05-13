package mobisocial.omnistanford.server.db;

import mobisocial.omnistanford.db.ManagerBase;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class CheckinManager extends ManagerBase {

    public static final String TAG = "CheckinManager";
    
    @SuppressWarnings("unused")
    private static final int _id = 0;

    private static final int user_id = 1;
    private static final int location_id = 2;
    private static final int entry_time = 3;
    private static final int exit_time = 4;
    
    @SuppressWarnings("unused")
    private static final String[] STANDARD_FIELDS = new String[] {
    	MCheckinData.COL_ID,
    	MCheckinData.COL_USER_ID,
    	MCheckinData.COL_LOCATION_ID,
    	MCheckinData.COL_ENTRY_TIME,
    	MCheckinData.COL_EXIT_TIME
    };

    @SuppressWarnings("unused")
    private SQLiteStatement mUpdateCheckin;
    private SQLiteStatement mInsertCheckin;
    
    public CheckinManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }

    public CheckinManager(SQLiteDatabase db) {
        super(db);
    }
    
    public void insertCheckin(MCheckinData checkin) {
        SQLiteDatabase db = initializeDatabase();
        if (mInsertCheckin == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder(" INSERT INTO ")
                    .append(MCheckinData.TABLE).append("(")
                    .append(MCheckinData.COL_USER_ID).append(",")
                    .append(MCheckinData.COL_LOCATION_ID).append(",")
                    .append(MCheckinData.COL_ENTRY_TIME).append(",")
                    .append(MCheckinData.COL_EXIT_TIME)
                    .append(") VALUES (?,?,?,?)");
                mInsertCheckin = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertCheckin) {
            bindField(mInsertCheckin, user_id, checkin.userId);
            bindField(mInsertCheckin, location_id, checkin.locationId);
            bindField(mInsertCheckin, entry_time, checkin.entryTime);
            bindField(mInsertCheckin, exit_time, checkin.exitTime);
            checkin.id = mInsertCheckin.executeInsert();
        }
    }
}
