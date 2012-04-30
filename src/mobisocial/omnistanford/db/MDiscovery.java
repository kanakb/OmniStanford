package mobisocial.omnistanford.db;

/**
 * Details about when and why a person was discovered
 * This table may need to be pruned over time; it might get too large
 */

// TODO: aggregations?
public class MDiscovery {
    public static final String TABLE = "discoveries";
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * ID of the check in
     */
    public static final String COL_CHECKIN_ID = "checkin_id";
    
    /**
     * ID of the discovered person
     */
    public static final String COL_PERSON_ID = "person_id";
    
    /**
     * How the person is connected to the user
     */
    public static final String COL_CONNECTION_TYPE = "connection_type";
    
    public long id;
    public Long checkinId;
    public Long personId;
    public String connectionType;
}
