package mobisocial.omnistanford.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DiscoveryManager extends ManagerBase {
    public static final String TAG = "DiscoveryManager";
    
    private static final int _id = 0;
    private static final int checkinId = 1;
    private static final int personId = 2;
    private static final int connectionType = 3;
    
    private static final String[] STANDARD_FIELDS = new String[] {
        MDiscovery.COL_ID,
        MDiscovery.COL_CHECKIN_ID,
        MDiscovery.COL_PERSON_ID,
        MDiscovery.COL_CONNECTION_TYPE
    };
    
    private SQLiteStatement mInsertDiscovery;
    private SQLiteStatement mUpdateDiscovery;
    
    public DiscoveryManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }
    
    public DiscoveryManager(SQLiteDatabase db){
        super(db);
    }
    
    public void insertDiscovery(MDiscovery discovery) {
        SQLiteDatabase db = initializeDatabase();
        if (mInsertDiscovery == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder(" INSERT OR REPLACE INTO ")
                    .append(MDiscovery.TABLE).append("(")
                    .append(MDiscovery.COL_CHECKIN_ID).append(",")
                    .append(MDiscovery.COL_PERSON_ID).append(",")
                    .append(MDiscovery.COL_CONNECTION_TYPE)
                    .append(") VALUES (?,?,?)");
                mInsertDiscovery = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertDiscovery) {
            bindField(mInsertDiscovery, checkinId, discovery.checkinId);
            bindField(mInsertDiscovery, personId, discovery.personId);
            bindField(mInsertDiscovery, connectionType, discovery.connectionType);
            discovery.id = mInsertDiscovery.executeInsert();
        }
    }
    
    public void updateDiscovery(MDiscovery discovery) {
        SQLiteDatabase db = initializeDatabase();
        if (mUpdateDiscovery == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder("UPDATE ")
                    .append(MDiscovery.TABLE)
                    .append(" SET ")
                    .append(MDiscovery.COL_CHECKIN_ID).append("=?,")
                    .append(MDiscovery.COL_PERSON_ID).append("=?,")
                    .append(MDiscovery.COL_CONNECTION_TYPE).append("=?")
                    .append(" WHERE ").append(MDiscovery.COL_ID).append("=?");
                mUpdateDiscovery = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mUpdateDiscovery) {
            bindField(mUpdateDiscovery, checkinId, discovery.checkinId);
            bindField(mUpdateDiscovery, personId, discovery.personId);
            bindField(mUpdateDiscovery, connectionType, discovery.connectionType);
            bindField(mUpdateDiscovery, 4, discovery.id);
            mUpdateDiscovery.execute();
        }
    }
    
    public void ensureDiscovery(MDiscovery discovery) {
        MDiscovery existing = getDiscovery(
                discovery.checkinId, discovery.personId, discovery.connectionType);
        if (existing != null) {
            discovery.id = existing.id;
            updateDiscovery(discovery);
        } else {
            insertDiscovery(discovery);
        }
    }
    
    public List<MDiscovery> getDiscoveries(Long checkin) {
        Cursor c = getDiscoveriesCursor(checkin);
        try {
            List<MDiscovery> result = new ArrayList<MDiscovery>();
            while (c.moveToNext()) {
                result.add(fillInStandardFields(c));
            }
            return result;
        } finally {
            c.close();
        }
    }
    
    public MDiscovery getDiscovery(Long checkinId, Long personId, String connectionType) {
        SQLiteDatabase db = initializeDatabase();
        String table = MDiscovery.TABLE;
        String selection = MDiscovery.COL_CHECKIN_ID + "=? AND " +
                MDiscovery.COL_PERSON_ID + "=? AND " +
                MDiscovery.COL_CONNECTION_TYPE + "=?";
        String[] selectionArgs = new String[]
                { checkinId.toString(), personId.toString(), connectionType };
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
    
    public Cursor getDiscoveriesCursor(Long checkin) {
        SQLiteDatabase db = initializeDatabase();
        String table = MDiscovery.TABLE;
        String selection = MDiscovery.COL_CHECKIN_ID + "=?";
        String[] selectionArgs = new String[] { checkin.toString() };
        String orderBy = MDiscovery.COL_CONNECTION_TYPE;
        return db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, orderBy);
    }
    
    public MDiscovery fillInStandardFields(Cursor c) {
        MDiscovery discovery = new MDiscovery();
        discovery.id = c.getLong(_id);
        discovery.checkinId = c.getLong(checkinId);
        discovery.personId = c.getLong(personId);
        discovery.connectionType = c.getString(connectionType);
        return discovery;
    }
}
