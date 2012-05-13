package mobisocial.omnistanford.server.db;

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
    
    private MProfile fillInStandardFields(Cursor c) {
    	MProfile profile = new MProfile();
    	profile.id = c.getLong(_id);
    	profile.userId = c.getLong(userId);
    	profile.dorm = c.getString(dorm);
    	profile.department = c.getString(department);
        return profile;
    }

}
