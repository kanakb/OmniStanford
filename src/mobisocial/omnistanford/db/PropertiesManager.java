package mobisocial.omnistanford.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class PropertiesManager extends ManagerBase {
    public static final String TAG = "PropertiesManager";
    
    private static final int _id = 0;
    private static final int name = 1;
    private static final int value = 2;
    
    private static final String[] STANDARD_FIELDS = new String[] {
        MUserProperty.COL_ID,
        MUserProperty.COL_NAME,
        MUserProperty.COL_VALUE
    };
    
    private SQLiteStatement mInsertProperty;
    private SQLiteStatement mUpdateProperty;
    
    public PropertiesManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }

    public PropertiesManager(SQLiteDatabase db) {
        super(db);
    }

    public void insertProperty(MUserProperty prop) {
        SQLiteDatabase db = initializeDatabase();
        if (mInsertProperty == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder(" INSERT OR REPLACE INTO ")
                    .append(MUserProperty.TABLE).append("(")
                    .append(MUserProperty.COL_NAME).append(",")
                    .append(MUserProperty.COL_VALUE)
                    .append(") VALUES (?,?)");
                mInsertProperty = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertProperty) {
            bindField(mInsertProperty, name, prop.name);
            bindField(mInsertProperty, value, prop.value);
            prop.id = mInsertProperty.executeInsert();
        }
    }
    
    public void updateProperty(MUserProperty prop) {
        SQLiteDatabase db = initializeDatabase();
        if (mUpdateProperty == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder("UPDATE ")
                    .append(MUserProperty.TABLE).append(" SET ")
                    .append(MUserProperty.COL_NAME).append("=?,")
                    .append(MUserProperty.COL_VALUE).append("=?")
                    .append(" WHERE ").append(MUserProperty.COL_ID).append("=?");
                mUpdateProperty = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mUpdateProperty) {
            bindField(mUpdateProperty, name, prop.name);
            bindField(mUpdateProperty, value, prop.value);
            bindField(mUpdateProperty, 3, prop.id);
            mUpdateProperty.execute();
        }
    }
    
    public void ensureProperty(MUserProperty prop) {
        MUserProperty existing = getProperty(prop.name);
        if (existing != null) {
            prop.id = existing.id;
            updateProperty(prop);
        } else {
            insertProperty(prop);
        }
    }
    
    public MUserProperty getProperty(String name) {
        SQLiteDatabase db = initializeDatabase();
        String table = MUserProperty.TABLE;
        String selection = MUserProperty.COL_NAME + "=?";
        String[] selectionArgs = new String[] { name };
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
    
    private MUserProperty fillInStandardFields(Cursor c) {
        MUserProperty prop = new MUserProperty();
        prop.id = c.getLong(_id);
        prop.name = c.getString(name);
        prop.value = c.getString(value);
        return prop;
    }
}
