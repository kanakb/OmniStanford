package mobisocial.omnistanford.db;

import android.net.Uri;

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
     * Account type of the location
     */
    public static final String COL_ACCOUNT_TYPE = "account_type";
    
    /**
     * Feed associated with this user and the location
     */
    public static final String COL_FEED_URI = "feed_uri";
    
    /**
     * Bounding box for the location (optional)
     */
    public static final String COL_MIN_LAT = "min_latitude";
    public static final String COL_MAX_LAT = "max_latitude";
    public static final String COL_MIN_LON = "min_longitude";
    public static final String COL_MAX_LON = "max_longitude";
    
    /**
     * Image data for the location (optional)
     */
    public static final String COL_IMAGE_URL = "image_url";
    public static final String COL_IMAGE = "image";
    
    public long id;
    public String name;
    public String type;
    public String principal;
    public String accountType;
    public Double minLatitude;
    public Double maxLatitude;
    public Double minLongitude;
    public Double maxLongitude;
    public Uri feedUri;
    public String imageUrl;
    public byte[] image;
}
