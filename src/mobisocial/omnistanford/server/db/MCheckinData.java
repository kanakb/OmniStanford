package mobisocial.omnistanford.server.db;

/**
 * A class describing details of a check in
 */
public class MCheckinData {
    public static final String TABLE = "checkins";
    
    public MCheckinData(Long userid, Long locationid, Long entrytime, Long exittime) {
    	userId = userid;
    	locationId = locationid;
    	entryTime = entrytime;
    	exitTime = exittime;
    }
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * The user that checked in
     */
    public static final String COL_USER_ID = "user_id";
    
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
    public Long userId;
    public Long locationId;
    public Long entryTime;
    public Long exitTime;
}
