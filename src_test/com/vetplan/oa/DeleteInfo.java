package com.vetplan.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class DeleteInfo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_ClassName = "ClassName";
    public static final String PROPERTY_Id = "Id";
     
     
    protected String className;
    protected String id;
     
     
    public DeleteInfo() {
    }
     
    public String getClassName() {
        return className;
    }
    public void setClassName(String newValue) {
        String old = this.className;
        this.className = newValue;
        firePropertyChange(PROPERTY_ClassName, old, this.className);
    }
    
     
    public String getId() {
        return id;
    }
    public void setId(String newValue) {
        String old = this.id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        // ex: oaObjectInfo.addLink(new OALinkInfo("employees", Employee.class, OALinkInfo.MANY/ONE, true, true, "department", false);
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        // oaObjectInfo.addRequired("propertyName");
    }
}
 
