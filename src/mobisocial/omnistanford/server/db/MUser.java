package mobisocial.omnistanford.server.db;

/**
 * A class to track various properties of a user's authorized accounts
 */
public class MUser {
    public static final String TABLE = "users";
    
    public MUser() {};
    
    public MUser(long localId, String name, String type, String hash) {
    	this.localId = localId;
    	this.name = name;
    	this.type = type;
    	this.identifier = hash;
    }
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * Local musubi identifier
     */
    public static final String COL_LOCAL_ID = "local_id";
    
    /**
     * Friendly name
     */
    public static final String COL_NAME = "name";
    
    /**
     * Hashed identifier
     */
    public static final String COL_IDENTIFIER = "identifier";
    
    /**
     * Account type
     */
    public static final String COL_ACCOUNT_TYPE = "account_type";
    
    public long id;
    public long localId;
    public String name;
    public String identifier;
    public String type;
}
