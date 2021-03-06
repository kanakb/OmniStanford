package mobisocial.omnistanford.db;

/**
 * A class describing details of a check in
 */
public class MCheckinData {
    public static final String TABLE = "checkins";
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * The account that checked in
     */
    public static final String COL_ACCOUNT_ID = "account_id";
    
    /**
     * The location
     */
    public static final String COL_LOCATION_ID = "location_id";
    
    /**
     * The entry time as a UNIX timestamp in milliseconds
     */
    public static final String COL_ENTRY_TIME = "entry_time";
    
    /**
     * The exit time (optional)
     */
    public static final String COL_EXIT_TIME = "exit_time";
    
    public long id;
    public Long accountId;
    public Long locationId;
    public Long entryTime;
    public Long exitTime;
}
