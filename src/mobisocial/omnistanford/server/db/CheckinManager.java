package mobisocial.omnistanford.server.db;

import java.util.ArrayList;
import java.util.List;

import mobisocial.omnistanford.db.ManagerBase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class CheckinManager extends ManagerBase {

    public static final String TAG = "CheckinManager";
    
    private static final int _id = 0;

    private static final int location_id = 1;
    private static final int entry_time = 2;
    private static final int exit_time = 3;
    private static final int user_name = 4;
    private static final int user_type = 5;
    private static final int user_hash = 6;
    private static final int user_dorm = 7;
    private static final int user_department = 8;
    
    private static final String[] STANDARD_FIELDS = new String[] {
    	MCheckinData.COL_ID,
    	MCheckinData.COL_LOCATION_ID,
    	MCheckinData.COL_ENTRY_TIME,
    	MCheckinData.COL_EXIT_TIME,
    	MCheckinData.COL_USER_NAME,
    	MCheckinData.COL_USER_TYPE,
    	MCheckinData.COL_USER_HASH,
    	MCheckinData.COL_USER_DORM,
    	MCheckinData.COL_USER_DEPARTMENT
    };

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
                    .append(MCheckinData.COL_LOCATION_ID).append(",")
                    .append(MCheckinData.COL_ENTRY_TIME).append(",")
                    .append(MCheckinData.COL_EXIT_TIME).append(",")
                    .append(MCheckinData.COL_USER_NAME).append(",")
                    .append(MCheckinData.COL_USER_TYPE).append(",")
                    .append(MCheckinData.COL_USER_HASH).append(",")
                    .append(MCheckinData.COL_USER_DORM).append(",")
                    .append(MCheckinData.COL_USER_DEPARTMENT)
                    .append(") VALUES (?,?,?,?,?,?,?,?)");
                mInsertCheckin = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertCheckin) {
            bindField(mInsertCheckin, location_id, checkin.locationId);
            bindField(mInsertCheckin, entry_time, checkin.entryTime);
            bindField(mInsertCheckin, exit_time, checkin.exitTime);
            bindField(mInsertCheckin, user_name, checkin.userName);
            bindField(mInsertCheckin, user_type, checkin.userType);
            bindField(mInsertCheckin, user_hash, checkin.userHash);
            bindField(mInsertCheckin, user_dorm, checkin.userDorm);
            bindField(mInsertCheckin, user_department, checkin.userDepartment);
            checkin.id = mInsertCheckin.executeInsert();
        }
    }
    
    public void updateCheckin(MCheckinData checkin) {
    	SQLiteDatabase db = initializeDatabase();
        if (mUpdateCheckin == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder("UPDATE ")
                    .append(MCheckinData.TABLE)
                    .append(" SET ")
                    .append(MCheckinData.COL_LOCATION_ID).append("=?,")
                    .append(MCheckinData.COL_ENTRY_TIME).append("=?,")
                    .append(MCheckinData.COL_EXIT_TIME).append("=?,")
                    .append(MCheckinData.COL_USER_NAME).append("=?,")
                    .append(MCheckinData.COL_USER_TYPE).append("=?,")
                    .append(MCheckinData.COL_USER_HASH).append("=?,")
                    .append(MCheckinData.COL_USER_DORM).append("=?,")
                    .append(MCheckinData.COL_USER_DEPARTMENT).append("=?")
                    .append(" WHERE ").append(MCheckinData.COL_ID).append("=?");
                mUpdateCheckin = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mUpdateCheckin) {
            bindField(mUpdateCheckin, location_id, checkin.locationId);
            bindField(mUpdateCheckin, entry_time, checkin.entryTime);
            bindField(mUpdateCheckin, exit_time, checkin.exitTime);
            bindField(mUpdateCheckin, user_name, checkin.userName);
            bindField(mUpdateCheckin, user_type, checkin.userType);
            bindField(mUpdateCheckin, user_hash, checkin.userHash);
            bindField(mUpdateCheckin, user_dorm, checkin.userDorm);
            bindField(mUpdateCheckin, user_department, checkin.userDepartment);
            bindField(mUpdateCheckin, 9, checkin.id);
            mUpdateCheckin.execute();
        }
    }
    
    public void checkin(MCheckinData newCheckin) {
		MCheckinData oldCheckin = findOpenCheckinForUser(newCheckin.locationId,
		        newCheckin.userType, newCheckin.userHash);
		if(oldCheckin != null) {
			newCheckin.id = oldCheckin.id;
			updateCheckin(newCheckin);
		} else {
			insertCheckin(newCheckin);
		}
    }
    
    public MCheckinData findOpenCheckinForUser(Long locationId, 
    		String usrType, String usrHash) {
    	SQLiteDatabase db = initializeDatabase();
    	String table = MCheckinData.TABLE;
    	StringBuilder selection = new StringBuilder()
    		.append(MCheckinData.COL_LOCATION_ID).append( "=? AND ")
    		.append(MCheckinData.COL_USER_TYPE).append("=? AND ")
    		.append(MCheckinData.COL_USER_HASH).append("=? AND ")
    		.append(MCheckinData.COL_EXIT_TIME).append(" IS NULL");
    	String[] selectionArgs = {
    			Long.toString(locationId),
    			usrType,
    			usrHash };
    	Cursor c = db.query(table, STANDARD_FIELDS, selection.toString(), selectionArgs, null, null, null);
    	try {
    		if(c.moveToNext()) {
    			return fillInStandardFields(c);
    		} else {
    			return null;
    		}
    	} finally {
    		c.close();
    	}
    }
    
    public List<MCheckinData> findOpenCheckinForProfile(long locationId,
    		String dorm, String department) {
    	SQLiteDatabase db = initializeDatabase();
        String table = MCheckinData.TABLE;
        StringBuilder selection = new StringBuilder()
        			.append(MCheckinData.COL_LOCATION_ID).append("=? AND (")
        			.append(MCheckinData.COL_USER_DORM).append("=? OR ")
        			.append(MCheckinData.COL_USER_DEPARTMENT).append("=?) AND ")
        			.append(MCheckinData.COL_EXIT_TIME).append(" IS NULL");
        String[] selectionArgs = new String[] { 
        		String.valueOf(locationId),
        		dorm,
        		department
        };
        Cursor c = db.query(table, STANDARD_FIELDS, selection.toString(), selectionArgs, null, null, null);
    	List<MCheckinData> checkins = new ArrayList<MCheckinData>(c.getCount());
        try {
            while (c.moveToNext()) {
            	checkins.add(fillInStandardFields(c));
            } 
        } finally {
            c.close();
        }
        
        return checkins;
    }
    
    private MCheckinData fillInStandardFields(Cursor c) {
    	MCheckinData checkin = new MCheckinData(
    			c.getLong(location_id),
    			c.getLong(entry_time), 
    			c.getLong(exit_time),
    			c.getString(user_name),
    			c.getString(user_type),
    			c.getString(user_hash),
    			c.getString(user_dorm),
    			c.getString(user_department));
    	checkin.id = c.getLong(_id);
    	return checkin;
    }
}
