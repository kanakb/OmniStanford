package mobisocial.omnistanford.server.db;

public class MProfile {
	public static final String TABLE = "profiles";
	
	public MProfile() {}
	
	public MProfile(Long userId, String dorm, String department) {
		this.userId = userId;
		this.dorm = dorm;
		this.department = department;
	}
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * User id
     */
    public static final String COL_USER_ID = "user_id";
    
    /**
     * Dorm
     */
    public static final String COL_DORM = "dorm";
    
    /**
     * Department
     */
    public static final String COL_DEPARTMENT = "department";
    
    public Long id;
    public Long userId;
    public String dorm;
    public String department;
}
