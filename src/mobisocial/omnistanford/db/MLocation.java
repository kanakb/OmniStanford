package mobisocial.omnistanford.db;

/**
 * A class to track properties of a location
 */
public class MLocation {
    public static final String TABLE = "locations";
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * Name of location
     */
    public static final String COL_NAME = "name";
    
    /**
     * Type of the location (e.g. "Dining Hall")
     */
    public static final String COL_TYPE = "type";
    
    /**
     * Identifier that Musubi can contact
     */
    public static final String COL_PRINCIPAL = "principal";
    
    /**
     * Bounding box for location (optional)
     */
    public static final String COL_MIN_LAT = "min_latitude";
    public static final String COL_MAX_LAT = "max_latitude";
    public static final String COL_MIN_LON = "min_longitude";
    public static final String COL_MAX_LON = "max_longitude";
    
    public long id;
    public String name;
    public String type;
    public String principal;
    public Float minLatitude;
    public Float maxLatitude;
    public Float minLongitude;
    public Float maxLongitude;
}
