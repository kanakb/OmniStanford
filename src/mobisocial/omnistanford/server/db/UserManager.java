package mobisocial.omnistanford.server.db;

import java.util.ArrayList;
import java.util.List;

import mobisocial.omnistanford.db.ManagerBase;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Manage users in this database
 */

public class UserManager extends ManagerBase {
    public static final String TAG = "AccountManager";
    
    private static final int _id = 0;
    private static final int localId = 1;
    private static final int accountType = 2;
    private static final int identifier = 3;
    private static final int name = 4;
    
    private static final String[] STANDARD_FIELDS = new String[] {
        MUser.COL_ID,
        MUser.COL_LOCAL_ID,
        MUser.COL_ACCOUNT_TYPE,
        MUser.COL_IDENTIFIER,
        MUser.COL_NAME
    };

    private SQLiteStatement mInsertUser;
    
    public UserManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }

    public UserManager(SQLiteDatabase db) {
        super(db);
    }
    
    public void insertUser(MUser user) {
        SQLiteDatabase db = initializeDatabase();
        if (mInsertUser == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder(" INSERT OR REPLACE INTO ")
                    .append(MUser.TABLE).append("(")
                    .append(MUser.COL_LOCAL_ID).append(",")
                    .append(MUser.COL_ACCOUNT_TYPE).append(",")
                    .append(MUser.COL_IDENTIFIER).append(",")
                    .append(MUser.COL_NAME)
                    .append(") VALUES (?,?,?,?)");
                mInsertUser = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertUser) {
        	bindField(mInsertUser, localId, user.localId);
            bindField(mInsertUser, accountType, user.type);
            bindField(mInsertUser, identifier, user.identifier);
            bindField(mInsertUser, name, user.name);
            user.id = mInsertUser.executeInsert();
        }
    }
    
    public MUser getUser(String accountName, String accountType, String hashed) {
        SQLiteDatabase db = initializeDatabase();
        String table = MUser.TABLE;
        String selection = MUser.COL_NAME + "=? AND " +
        			MUser.COL_ACCOUNT_TYPE + "=? AND " +
        			MUser.COL_IDENTIFIER + "=?";
        String[] selectionArgs = new String[] { accountName, accountType, hashed };
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
    
    public List<MUser> getUsers(List<Long> userIds) {
    	List<MUser> users = new ArrayList<MUser>();
    	SQLiteDatabase db = initializeDatabase();
    	String table = MUser.TABLE;
    	String selection = MUser.COL_ID + " IN (";
    	String[] selectionArgs = new String[userIds.size()];
    	for(int i = 0; i < userIds.size(); i++) {
    		if(i == userIds.size() - 1) {
    			selection += "?)";
    		} else {
        		selection += "?,";
    		}
    		selectionArgs[i] = userIds.get(i).toString();
    	}
    	Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
    	try {
    		while(c.moveToNext()) {
    			users.add(fillInStandardFields(c));
    		}
    	} finally {
    		c.close();
    	}
    	
    	return users;
    }
    
    private MUser fillInStandardFields(Cursor c) {
    	MUser user = new MUser();
    	user.id = c.getLong(_id);
    	user.localId = c.getLong(localId);
    	user.type = c.getString(accountType);
    	user.identifier = c.getString(identifier);
    	user.name = c.getString(name);
        return user;
    }
}
