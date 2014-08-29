package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "sta",
    displayName = "State",
    isLookup = true,
    isPreSelect = true
)
@OATable(
)
public class State extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_Abbrev = "Abbrev";
    public static final String P_Name = "Name";
     
     
    protected int id;
    protected String abbrev;
    protected String name;
     
     
    public State() {
    }
     
    public State(int id) {
        this();
        setId(id);
    }
    @OAProperty(displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        int old = id;
        this.id = newValue;
        firePropertyChange(P_Id, old, this.id);
    }
    
     
    @OAProperty(maxLength = 15, displayLength = 6)
    @OAColumn(maxLength = 15)
    public String getAbbrev() {
        return abbrev;
    }
    
    public void setAbbrev(String newValue) {
        String old = abbrev;
        this.abbrev = newValue;
        firePropertyChange(P_Abbrev, old, this.abbrev);
    }
    
     
    @OAProperty(maxLength = 70, displayLength = 4)
    @OAColumn(maxLength = 70)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    
     
}
 
