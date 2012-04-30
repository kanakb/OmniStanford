package mobisocial.omnistanford.db;

/**
 * A class to track various properties of a user's authorized accounts
 */
public class MAccount {
    public static final String TABLE = "accounts";
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
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
    public String name;
    public String identifier;
    public String type;
}
