package mobisocial.omnistanford.server.db;

/**
 * A class describing details of a check in
 */
public class MCheckinData {
    public static final String TABLE = "checkins";
    
    public MCheckinData(Long locationid, Long entrytime, Long exittime,
    		String name, String type, String hash, String dorm, String department) {
    	locationId = locationid;
    	entryTime = entrytime;
    	exitTime = exittime;
    	userName = name;
    	userType = type;
    	userHash = hash;
    	userDorm = dorm;
    	userDepartment = department;
    }
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
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
    
    /**
     *  Checkin user's account name
     */
    public static final String COL_USER_NAME = "user_name";
    
    /**
     *  Checkin user's account type
     */
    public static final String COL_USER_TYPE = "user_type";
    
    /**
     *  Checkin user's account principal hash
     */
    public static final String COL_USER_HASH = "user_hash";
    
    /**
     *  Checkin user's dorm
     */
    public static final String COL_USER_DORM = "user_dorm";
    
    /**
     *  Checkin user's department
     */
    public static final String COL_USER_DEPARTMENT = "user_department";
    
    public long id;
    public Long locationId;
    public Long entryTime;
    public Long exitTime;
    public String userName;
    public String userType;
    public String userHash;
    public String userDorm;
    public String userDepartment;
}
