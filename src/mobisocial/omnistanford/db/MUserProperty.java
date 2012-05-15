package mobisocial.omnistanford.db;

public class MUserProperty {
    public static final String TABLE = "properties";
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * Property name
     */
    public static final String COL_NAME = "name";
    
    /**
     * Property value
     */
    public static final String COL_VALUE = "value";
    
    public long id;
    public String name;
    public String value;
    
    public MUserProperty() { }
    
    public MUserProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public MUserProperty(MUserProperty other) {
        this.id = other.id;
        this.name = other.name;
        this.value = other.value;
    }
}
