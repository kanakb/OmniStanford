package mobisocial.omnistanford.server.db;

import java.util.ArrayList;
import java.util.List;

import mobisocial.omnistanford.db.ManagerBase;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class ProfileManager extends ManagerBase {
    public static final String TAG = "AccountManager";
    
    private static final int _id = 0;
    private static final int userId = 1;
    private static final int dorm = 2;
    private static final int department = 3;
    
    private static final String[] STANDARD_FIELDS = new String[] {
        MProfile.COL_ID,
        MProfile.COL_USER_ID,
        MProfile.COL_DORM,
        MProfile.COL_DEPARTMENT
    };

    private SQLiteStatement mInsertProfile;
    private SQLiteStatement mUpdateProfile;
    
    public ProfileManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }

    public ProfileManager(SQLiteDatabase db) {
        super(db);
    }
    
    public void insertProfile(MProfile profile) {
        SQLiteDatabase db = initializeDatabase();
        if (mInsertProfile == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder(" INSERT OR REPLACE INTO ")
                    .append(MProfile.TABLE).append("(")
                    .append(MProfile.COL_USER_ID).append(",")
                    .append(MProfile.COL_DORM).append(",")
                    .append(MProfile.COL_DEPARTMENT)
                    .append(") VALUES (?,?,?)");
                mInsertProfile = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertProfile) {
        	bindField(mInsertProfile, userId, profile.userId);
            bindField(mInsertProfile, dorm, profile.dorm);
            bindField(mInsertProfile, department, profile.department);
            profile.id = mInsertProfile.executeInsert();
        }
    }
    
    public void updateProfile(MProfile profile) {
        SQLiteDatabase db = initializeDatabase();
        if (mUpdateProfile == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder("UPDATE ")
                    .append(MProfile.TABLE)
                    .append(" SET ")
                    .append(MProfile.COL_DEPARTMENT).append("=?,")
                    .append(MProfile.COL_DORM).append("=?")
                    .append(" WHERE ").append(MProfile.COL_USER_ID).append("=?");
                mUpdateProfile = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mUpdateProfile) {
            bindField(mUpdateProfile, 1, profile.department);
            bindField(mUpdateProfile, 2, profile.dorm);
            bindField(mUpdateProfile, 3, profile.userId);
            mUpdateProfile.execute();
        }
    }
    
    public MProfile getProfile(long userId) {
        SQLiteDatabase db = initializeDatabase();
        String table = MProfile.TABLE;
        String selection = MProfile.COL_USER_ID + "=?";
        String[] selectionArgs = new String[] { Long.toString(userId) };
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
    
    public List<Long> getMatchingUserIds(List<Long> userIds, String dorm, String department) {
    	// TODO: assert userIds.size() >= 1
    	List<Long> matchedUserIds = new ArrayList<Long>();
    	SQLiteDatabase db = initializeDatabase();
    	String table = MProfile.TABLE;
    	String selection = "(" + MProfile.COL_DEPARTMENT + "=? OR "
    					+ MProfile.COL_DORM + "=? ) AND "
    					+ MProfile.COL_USER_ID + " IN (";
    	String[] selectionArgs = new String[2 + userIds.size()];
    	selectionArgs[0] = department;
    	selectionArgs[1] = dorm;
    	for(int i = 0; i < userIds.size(); i++) {
    		if(i == userIds.size() - 1) {
    			selection += "?)";
    		} else {
        		selection += "?,";
    		}
    		selectionArgs[i+2] = userIds.get(i).toString();
    	}
    	Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
    	try {
    		while(c.moveToNext()) {
    			matchedUserIds.add(c.getLong(userId));
    		}
    	} finally {
    		c.close();
    	}
    	
    	return matchedUserIds;
    }
    
    public void ensureProfile(MProfile profile) {
    	MProfile existing = getProfile(profile.userId);
    	if(existing != null) {
    		profile.id = existing.id;
    		updateProfile(profile);
    	} else {
    		insertProfile(profile);
    	}
    }
    
    private MProfile fillInStandardFields(Cursor c) {
    	MProfile profile = new MProfile();
    	profile.id = c.getLong(_id);
    	profile.userId = c.getLong(userId);
    	profile.dorm = c.getString(dorm);
    	profile.department = c.getString(department);
        return profile;
    }

}
