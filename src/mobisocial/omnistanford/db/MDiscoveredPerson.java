package mobisocial.omnistanford.db;

/**
 * A class to describe a person found through a checkin
 */
public class MDiscoveredPerson {
    public static final String TABLE = "discovered";
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * The person's display name
     */
    public static final String COL_NAME = "name";
    
    /**
     * The person's hashed Musubi identifier
     */
    public static final String COL_IDENTIFIER = "identifier";
    
    /**
     * The account type associated with this identifier
     */
    public static final String COL_ACCOUNT_TYPE = "account_type";
    
    public long id;
    public String name;
    public String identifier;
    public String accountType;
}
