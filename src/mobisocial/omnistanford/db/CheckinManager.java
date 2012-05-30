package mobisocial.omnistanford.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class CheckinManager extends ManagerBase {

    public static final String TAG = "CheckinManager";
    
    private static final int _id = 0;
    private static final int accountId = 1;
    private static final int locationId = 2;
    private static final int entryTime = 3;
    private static final int exitTime = 4;
    
    private static final long DAY = 1000L * 60L * 60L * 24L;
    private static final long SHORT_INTERVAL = 1000L * 60L * 60L * 3L;
    
    private static final String[] STANDARD_FIELDS = new String[] {
    	MCheckinData.COL_ID,
    	MCheckinData.COL_ACCOUNT_ID,
    	MCheckinData.COL_LOCATION_ID,
    	MCheckinData.COL_ENTRY_TIME,
    	MCheckinData.COL_EXIT_TIME
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
                    .append(MCheckinData.COL_ACCOUNT_ID).append(",")
                    .append(MCheckinData.COL_LOCATION_ID).append(",")
                    .append(MCheckinData.COL_ENTRY_TIME).append(",")
                    .append(MCheckinData.COL_EXIT_TIME)
                    .append(") VALUES (?,?,?,?)");
                mInsertCheckin = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertCheckin) {
            bindField(mInsertCheckin, accountId, checkin.accountId);
            bindField(mInsertCheckin, locationId, checkin.locationId);
            bindField(mInsertCheckin, entryTime, checkin.entryTime);
            bindField(mInsertCheckin, exitTime, checkin.exitTime);
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
                    .append(MCheckinData.COL_ACCOUNT_ID).append("=?,")
                    .append(MCheckinData.COL_LOCATION_ID).append("=?,")
                    .append(MCheckinData.COL_ENTRY_TIME).append("=?,")
                    .append(MCheckinData.COL_EXIT_TIME).append("=?")
                    .append(" WHERE ").append(MCheckinData.COL_ID).append("=?");
                mUpdateCheckin = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mUpdateCheckin) {
            bindField(mUpdateCheckin, accountId, checkin.accountId);
            bindField(mUpdateCheckin, locationId, checkin.locationId);
            bindField(mUpdateCheckin, entryTime, checkin.entryTime);
            bindField(mUpdateCheckin, exitTime, checkin.exitTime);
            bindField(mUpdateCheckin, 5, checkin.id);
            mUpdateCheckin.execute();
        }
    }
    
    public MCheckinData getCheckin(Long id) {
        SQLiteDatabase db = initializeDatabase();
        String table = MCheckinData.TABLE;
        String selection = MCheckinData.COL_ID + "=?";
        String[] selectionArgs = new String[] { id.toString() };
        Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
        try {
            if (c.moveToFirst()) {
                return fillInStandardFields(c);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }
    
    public MCheckinData getRecentCheckin(Long locationId) {
        Long cutoff = System.currentTimeMillis() - SHORT_INTERVAL;
        SQLiteDatabase db = initializeDatabase();
        String table = MCheckinData.TABLE;
        String selection = MCheckinData.COL_LOCATION_ID + "=? AND " +
                MCheckinData.COL_ENTRY_TIME + ">?";
        String[] selectionArgs = new String[] { locationId.toString(), cutoff.toString() };
        Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
        try {
            if (c.moveToFirst()) {
                return fillInStandardFields(c);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }
    
    public List<MCheckinData> getDailyCheckins(long date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(date);
    	Long start, end;
    	calendar.set(Calendar.HOUR_OF_DAY, 0);
    	start = calendar.getTimeInMillis();
    	calendar.set(Calendar.HOUR_OF_DAY, 24);
    	end = calendar.getTimeInMillis();
    	
    	SQLiteDatabase db = initializeDatabase();
        String table = MCheckinData.TABLE;
        String selection = MCheckinData.COL_ENTRY_TIME + ">? AND " + 
        	MCheckinData.COL_ENTRY_TIME + "<? AND (" + 
        	MCheckinData.COL_EXIT_TIME + " IS NOT NULL OR " +
        	MCheckinData.COL_EXIT_TIME + " <?)";
        String[] selectionArgs = new String[] { start.toString(), end.toString(), end.toString() };
        String orderBy = MCheckinData.COL_ENTRY_TIME + " DESC";
        Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, orderBy);
        try {
            List<MCheckinData> checkins = new ArrayList<MCheckinData>();
            while (c.moveToNext()) {
                checkins.add(fillInStandardFields(c));
            }
            return checkins;
        } finally {
            c.close();
        }
    }
    
    public List<MCheckinData> getRecentCheckins() {
        return getRecentCheckins(DAY);
    }
    
    public List<MCheckinData> getRecentCheckins(long duration) {
        Long cutoff = System.currentTimeMillis() - duration;
        Log.d(TAG, "time: " + System.currentTimeMillis());
        Log.d(TAG, "duration: " + duration);
        Log.d(TAG, "cutoff: " + cutoff);
        SQLiteDatabase db = initializeDatabase();
        String table = MCheckinData.TABLE;
        String selection = MCheckinData.COL_ENTRY_TIME + ">?";
        String[] selectionArgs = new String[] { cutoff.toString() };
        String orderBy = MCheckinData.COL_ENTRY_TIME + " DESC";
        Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, orderBy);
        try {
            List<MCheckinData> checkins = new ArrayList<MCheckinData>();
            while (c.moveToNext()) {
                checkins.add(fillInStandardFields(c));
            }
            return checkins;
        } finally {
            c.close();
        }
    }
    
    public List<MCheckinData> getRecentOpenCheckins(long duration) {
        Long cutoff = System.currentTimeMillis() - duration;
        Log.d(TAG, "time: " + System.currentTimeMillis());
        Log.d(TAG, "duration: " + duration);
        Log.d(TAG, "cutoff: " + cutoff);
        SQLiteDatabase db = initializeDatabase();
        String table = MCheckinData.TABLE;
        String selection = MCheckinData.COL_ENTRY_TIME + ">? AND "
                + MCheckinData.COL_EXIT_TIME + " IS NULL";
        String[] selectionArgs = new String[] { cutoff.toString() };
        String orderBy = MCheckinData.COL_ENTRY_TIME + " DESC";
        Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, orderBy);
        try {
            List<MCheckinData> checkins = new ArrayList<MCheckinData>();
            while (c.moveToNext()) {
                checkins.add(fillInStandardFields(c));
            }
            return checkins;
        } finally {
            c.close();
        }
    }
    
    private MCheckinData fillInStandardFields(Cursor c) {
        MCheckinData data = new MCheckinData();
        data.id = c.getLong(_id);
        data.accountId = c.getLong(accountId);
        data.locationId = c.getLong(locationId);
        data.entryTime = c.getLong(entryTime);
        data.exitTime = c.getLong(exitTime);
        return data;
    }
}
