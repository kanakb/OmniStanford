package mobisocial.omnistanford.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Manage owned accounts in this database
 */

public class AccountManager extends ManagerBase {
    public static final String TAG = "AccountManager";
    
    private static final int _id = 0;
    private static final int accountType = 1;
    private static final int identifier = 2;
    private static final int name = 3;
    
    private static final String[] STANDARD_FIELDS = new String[] {
        MAccount.COL_ID,
        MAccount.COL_ACCOUNT_TYPE,
        MAccount.COL_IDENTIFIER,
        MAccount.COL_NAME
    };

    private SQLiteStatement mUpdateAccount;
    private SQLiteStatement mInsertAccount;
    
    public AccountManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }

    public AccountManager(SQLiteDatabase db) {
        super(db);
    }
    
    public void insertAccount(MAccount account) {
        SQLiteDatabase db = initializeDatabase();
        if (mInsertAccount == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder(" INSERT OR REPLACE INTO ")
                    .append(MAccount.TABLE).append("(")
                    .append(MAccount.COL_ACCOUNT_TYPE).append(",")
                    .append(MAccount.COL_IDENTIFIER).append(",")
                    .append(MAccount.COL_NAME)
                    .append(") VALUES (?,?,?)");
                mInsertAccount = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertAccount) {
            bindField(mInsertAccount, accountType, account.type);
            bindField(mInsertAccount, identifier, account.identifier);
            bindField(mInsertAccount, name, account.name);
            account.id = mInsertAccount.executeInsert();
        }
    }
    
    public void updateAccount(MAccount account) {
        SQLiteDatabase db = initializeDatabase();
        if (mUpdateAccount == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder("UPDATE ")
                    .append(MAccount.TABLE)
                    .append(" SET ")
                    .append(MAccount.COL_ACCOUNT_TYPE).append("=?,")
                    .append(MAccount.COL_IDENTIFIER).append("=?,")
                    .append(MAccount.COL_NAME).append("=?")
                    .append(" WHERE ").append(MAccount.COL_ID).append("=?");
                mUpdateAccount = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mUpdateAccount) {
            bindField(mUpdateAccount, accountType, account.type);
            bindField(mUpdateAccount, identifier, account.identifier);
            bindField(mUpdateAccount, name, account.name);
            bindField(mUpdateAccount, 4, account.id);
            mUpdateAccount.execute();
        }
    }
    
    public void ensureAccount(MAccount account) {
        MAccount existing = getAccount(account.name, account.type, account.identifier);
        if (existing != null) {
            account.id = existing.id;
            updateAccount(account);
        } else {
            insertAccount(account);
        }
    }
    
    public List<MAccount> getAccounts(String accountType) {
        SQLiteDatabase db = initializeDatabase();
        String table = MAccount.TABLE;
        String selection = null;
        String[] selectionArgs = null;
        if(accountType != null) {
        	selection = MAccount.COL_ACCOUNT_TYPE + "=?";
        	selectionArgs = new String[] { accountType };
        }
       
        Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
        try {
            List<MAccount> result = new ArrayList<MAccount>();
            while (c.moveToNext()) {
                result.add(fillInStandardFields(c));
            }
            return result;
        } finally {
            c.close();
        }
    }
    
    public MAccount getAccount(Long id) {
        SQLiteDatabase db = initializeDatabase();
        String table = MAccount.TABLE;
        String selection = MAccount.COL_ID + "=?";
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
    
    public MAccount getAccount(String accountName, String accountType, String hashed) {
        SQLiteDatabase db = initializeDatabase();
        String table = MAccount.TABLE;
        String selection = MAccount.COL_NAME + "=? AND " +
                MAccount.COL_ACCOUNT_TYPE + "=? AND " +
                MAccount.COL_IDENTIFIER + "=?";
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
    
    public MAccount getAccount(String accountType, String hashed) {
        SQLiteDatabase db = initializeDatabase();
        String table = MAccount.TABLE;
        String selection = MAccount.COL_ACCOUNT_TYPE + "=? AND " +
                MAccount.COL_IDENTIFIER + "=?";
        String[] selectionArgs = new String[] { accountType, hashed };
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
    
    private MAccount fillInStandardFields(Cursor c) {
        MAccount acc = new MAccount();
        acc.id = c.getLong(_id);
        acc.type = c.getString(accountType);
        acc.identifier = c.getString(identifier);
        acc.name = c.getString(name);
        return acc;
    }
}
