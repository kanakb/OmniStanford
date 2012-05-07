package mobisocial.omnistanford.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class LocationManager extends ManagerBase {
    public static final String TAG = "LocationManager";
    
    private static final int _id = 0;
    private static final int name = 1;
    private static final int principal = 2;
    private static final int accountType = 3;
    private static final int type = 4;
    private static final int minLat = 5;
    private static final int maxLat = 6;
    private static final int minLon = 7;
    private static final int maxLon = 8;
    
    private static final String[] STANDARD_FIELDS = new String[] {
        MLocation.COL_ID,
        MLocation.COL_NAME,
        MLocation.COL_PRINCIPAL,
        MLocation.COL_ACCOUNT_TYPE,
        MLocation.COL_TYPE,
        MLocation.COL_MIN_LAT,
        MLocation.COL_MAX_LAT,
        MLocation.COL_MIN_LON,
        MLocation.COL_MAX_LON
    };

    @SuppressWarnings("unused")
    private SQLiteStatement mUpdateLocation;
    private SQLiteStatement mInsertLocation;
    
    public LocationManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }

    public LocationManager(SQLiteDatabase db) {
        super(db);
    }
    
    public void insertLocation(MLocation location) {
        SQLiteDatabase db = initializeDatabase();
        if (mInsertLocation == null) {
            synchronized(this) {
                StringBuilder sql = new StringBuilder(" INSERT INTO ")
                    .append(MLocation.TABLE).append("(")
                    .append(MLocation.COL_NAME).append(",")
                    .append(MLocation.COL_PRINCIPAL).append(",")
                    .append(MLocation.COL_ACCOUNT_TYPE).append(",")
                    .append(MLocation.COL_TYPE).append(",")
                    .append(MLocation.COL_MIN_LAT).append(",")
                    .append(MLocation.COL_MAX_LAT).append(",")
                    .append(MLocation.COL_MIN_LON).append(",")
                    .append(MLocation.COL_MAX_LON)
                    .append(") VALUES (?,?,?,?,?,?,?,?)");
                mInsertLocation = db.compileStatement(sql.toString());
            }
        }
        
        synchronized(mInsertLocation) {
            bindField(mInsertLocation, name, location.name);
            bindField(mInsertLocation, principal, location.principal);
            bindField(mInsertLocation, accountType, location.accountType);
            bindField(mInsertLocation, type, location.type);
            bindField(mInsertLocation, minLat, location.minLatitude);
            bindField(mInsertLocation, maxLat, location.maxLatitude);
            bindField(mInsertLocation, minLon, location.minLongitude);
            bindField(mInsertLocation, maxLon, location.maxLongitude);
            location.id = mInsertLocation.executeInsert();
        }
    }
    
    public List<MLocation> getLocations(String type) {
        SQLiteDatabase db = initializeDatabase();
        String table = MLocation.TABLE;
        String selection = MLocation.COL_TYPE + "=?";
        String[] selectionArgs = new String[] { type };
        Cursor c = db.query(table, STANDARD_FIELDS, selection, selectionArgs, null, null, null);
        try {
            List<MLocation> result = new ArrayList<MLocation>();
            while (c.moveToNext()) {
                result.add(fillInStandardFields(c));
            }
            return result;
        } finally {
            c.close();
        }
    }
    
    private MLocation fillInStandardFields(Cursor c) {
        MLocation loc = new MLocation();
        loc.id = c.getLong(_id);
        loc.name = c.getString(name);
        loc.principal = c.getString(principal);
        loc.accountType = c.getString(accountType);
        loc.type = c.getString(type);
        try {
            loc.minLatitude = c.getFloat(minLat);
            loc.maxLatitude = c.getFloat(maxLat);
            loc.minLongitude = c.getFloat(minLon);
            loc.maxLongitude = c.getFloat(maxLon);
        } catch (Exception e) {
            loc.minLatitude = null;
            loc.maxLatitude = null;
            loc.minLongitude = null;
            loc.maxLongitude = null;
        }
        return loc;
    }
}
