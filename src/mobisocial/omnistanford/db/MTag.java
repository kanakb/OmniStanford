package mobisocial.omnistanford.db;


public class MTag {
	public static final String TABLE = "tags";
	
	public static final String COL_ID = "_id";
	
	public static final String COL_NAME = "name";
	
	public static final String COL_LOCATION_ID = "location_id";
	
	public static final String COL_CHECKIN_ID = "checkin_id";
	
	public static final String COL_START_TIME = "start_time";
	
	public static final String COL_END_TIME = "end_time";
	
	public Long id;
	public String name;
	public Long locationId;
	public Long checkinId;
	public Long startTime;
	public Long endTime;
}
