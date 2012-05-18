package mobisocial.omnistanford.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DiscoveredPersonManager extends ManagerBase {
    public static final String TAG = "DiscoveredPersonManager";
    
    private static final int _id = 0;
    private static final int name = 1;
    private static final int identifier = 2;
    private static final int accountType = 3;
    
    private static final String[] STANDARD_FIELDS = new String[] {
        MDiscoveredPerson.COL_ID,
        MDiscoveredPerson.COL_NAME,
        MDiscoveredPerson.COL_IDENTIFIER,
        MDiscoveredPerson.COL_ACCOUNT_TYPE
    };
    
    private SQLiteStatement mInsertPerson;
    private SQLiteStatement mUpdatePerson;
    
    public DiscoveredPersonManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }
    public DiscoveredPersonManager(SQLiteDatabase db) {
        super(db);
    }
    
    public void insertPerson(MDiscoveredPerson person) {
        SQLiteDatabase db = initializeDatabase();
        if (mInsertPerson == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder(" INSERT OR REPLACE INTO ")
                    .append(MDiscoveredPerson.TABLE).append("(")
                    .append(MDiscoveredPerson.COL_NAME).append(",")
                    .append(MDiscoveredPerson.COL_IDENTIFIER).append(",")
                    .append(MDiscoveredPerson.COL_ACCOUNT_TYPE)
                    .append(") VALUES (?,?,?)");
                mInsertPerson = db.compileStatement(sql.toString());
            }
        }
        synchronized(mInsertPerson) {
            bindField(mInsertPerson, name, person.name);
            bindField(mInsertPerson, identifier, person.identifier);
            bindField(mInsertPerson, accountType, person.accountType);
            person.id = mInsertPerson.executeInsert();
        }
    }
    
    public void updatePerson(MDiscoveredPerson person) {
        SQLiteDatabase db = initializeDatabase();
        if (mUpdatePerson == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder("UPDATE ")
                    .append(MDiscoveredPerson.TABLE)
                    .append(" SET ")
                    .append(MDiscoveredPerson.COL_NAME).append("=?,")
                    .append(MDiscoveredPerson.COL_IDENTIFIER).append("=?,")
                    .append(MDiscoveredPerson.COL_ACCOUNT_TYPE).append("=?")
                    .append(" WHERE ").append(MDiscoveredPerson.COL_ID).append("=?");
                mUpdatePerson = db.compileStatement(sql.toString());
            }
        }
        synchronized(mUpdatePerson) {
            bindField(mUpdatePerson, name, person.name);
            bindField(mUpdatePerson, identifier, person.identifier);
            bindField(mUpdatePerson, accountType, person.accountType);
            bindField(mUpdatePerson, 4, person.id);
            mUpdatePerson.execute();
        }
    }
    
    public void ensurePerson(MDiscoveredPerson person) {
        MDiscoveredPerson existing = getPerson(person.identifier, person.accountType);
        if (existing != null) {
            person.id = existing.id;
            updatePerson(person);
        } else {
            insertPerson(person);
        }
    }
    
    public MDiscoveredPerson getPerson(Long id) {
        SQLiteDatabase db = initializeDatabase();
        String table = MDiscoveredPerson.TABLE;
        String selection = MDiscoveredPerson.COL_ID + "=?";
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
    
    public MDiscoveredPerson getPerson(String identifier, String accountType) {
        SQLiteDatabase db = initializeDatabase();
        String table = MDiscoveredPerson.TABLE;
        String selection = MDiscoveredPerson.COL_IDENTIFIER + "=? AND "
                + MDiscoveredPerson.COL_ACCOUNT_TYPE + "=?";
        String[] selectionArgs = new String[] { identifier, accountType };
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
    
    private MDiscoveredPerson fillInStandardFields(Cursor c) {
        MDiscoveredPerson person = new MDiscoveredPerson();
        person.id = c.getLong(_id);
        person.name = c.getString(name);
        person.identifier = c.getString(identifier);
        person.accountType = c.getString(accountType);
        return person;
    }
}
