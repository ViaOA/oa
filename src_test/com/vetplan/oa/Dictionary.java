package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
 
 
public class Dictionary extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Word = "Word";
    public static final String PROPERTY_Soundex = "Soundex";
    public static final String PROPERTY_DateCreated = "DateCreated";
    public static final String PROPERTY_Valid = "Valid";
     
     
    public static final String PROPERTY_Dictionaries = "Dictionaries";
    public static final String PROPERTY_BaseDictionary = "BaseDictionary";
    public static final String PROPERTY_InvalidDictionaries = "InvalidDictionaries";
    public static final String PROPERTY_ValidDictionary = "ValidDictionary";
    public static final String PROPERTY_Language = "Language";
     
    protected String id;
    protected String word;
    protected String soundex;
    protected OADate dateCreated;
    protected boolean valid;
     
    // Links to other objects.
    protected transient Hub hubDictionaries;
    protected transient Dictionary baseDictionary;
    protected transient Hub hubInvalidDictionaries;
    protected transient Dictionary validDictionary;
    protected transient Language language;
     
     
    public Dictionary() {
    }
     
    public Dictionary(String id) {
        this();
        setId(id);
    }
    public String getId() {
        return id;
    }
    public void setId(String newValue) {
        String old = this.id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    public String getWord() {
        return word;
    }
    public void setWord(String newValue) {
        String old = this.word;
        this.word = newValue;
        firePropertyChange(PROPERTY_Word, old, this.word);
    }
    
     
    public String getSoundex() {
        return soundex;
    }
    public void setSoundex(String newValue) {
        String old = this.soundex;
        this.soundex = newValue;
        firePropertyChange(PROPERTY_Soundex, old, this.soundex);
    }
    
     
    public OADate getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(OADate newValue) {
        OADate old = this.dateCreated;
        this.dateCreated = newValue;
        firePropertyChange(PROPERTY_DateCreated, old, this.dateCreated);
    }
    
     
    public boolean getValid() {
        return valid;
    }
    public void setValid(boolean newValue) {
        boolean old = this.valid;
        this.valid = newValue;
        firePropertyChange(PROPERTY_Valid, old, this.valid);
    }
    
     
    public Hub getDictionaries() {
        if (hubDictionaries == null) {
            hubDictionaries = getHub(PROPERTY_Dictionaries);
        }
        return hubDictionaries;
    }
    
     
    public Dictionary getBaseDictionary() {
        if (baseDictionary == null) {
            baseDictionary = (Dictionary) getObject(PROPERTY_BaseDictionary);
        }
        return baseDictionary;
    }
    
    public void setBaseDictionary(Dictionary newValue) {
        Dictionary old = this.baseDictionary;
        this.baseDictionary = newValue;
        firePropertyChange(PROPERTY_BaseDictionary, old, this.baseDictionary);
    }
     
    public Hub getInvalidDictionaries() {
        if (hubInvalidDictionaries == null) {
            hubInvalidDictionaries = getHub(PROPERTY_InvalidDictionaries);
        }
        return hubInvalidDictionaries;
    }
    
     
    public Dictionary getValidDictionary() {
        if (validDictionary == null) {
            validDictionary = (Dictionary) getObject(PROPERTY_ValidDictionary);
        }
        return validDictionary;
    }
    
    public void setValidDictionary(Dictionary newValue) {
        Dictionary old = this.validDictionary;
        this.validDictionary = newValue;
        firePropertyChange(PROPERTY_ValidDictionary, old, this.validDictionary);
    }
     
    public Language getLanguage() {
        if (language == null) {
            language = (Language) getObject(PROPERTY_Language);
        }
        return language;
    }
    
    public void setLanguage(Language newValue) {
        Language old = this.language;
        this.language = newValue;
        firePropertyChange(PROPERTY_Language, old, this.language);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Dictionaries, Dictionary.class, OALinkInfo.MANY, false, false, Dictionary.PROPERTY_BaseDictionary));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_BaseDictionary, Dictionary.class, OALinkInfo.ONE, false, false, Dictionary.PROPERTY_Dictionaries));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_InvalidDictionaries, Dictionary.class, OALinkInfo.MANY, false, false, Dictionary.PROPERTY_ValidDictionary));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ValidDictionary, Dictionary.class, OALinkInfo.ONE, false, false, Dictionary.PROPERTY_InvalidDictionaries));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Language, Language.class, OALinkInfo.ONE, false, false, Language.PROPERTY_Dictionaries));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 
