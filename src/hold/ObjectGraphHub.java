package com.viaoa.model;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ObjectGraphHub extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Type = "Type";
     
     
    public static final String PROPERTY_ObjectGraph = "ObjectGraph";
    public static final String PROPERTY_ObjectDef = "ObjectDef";
     
    protected String name;
    protected int type;
    public static final int TYPE_SELECT = 0;
    public static final int TYPE_SEARCH = 1;
    public static final int TYPE_FILTER = 2;
    public static final int TYPE_DETAIL = 3;
    public static final int TYPE_SHARE = 4;
    public static final int TYPE_SHAREAO = 5;
//qqqqqqqq need to add MERGE_AO, MERGE_ALL    
    public static final Hub hubType;
    static {
        hubType = new Hub(String.class);
        hubType.addElement("Select");
        hubType.addElement("Search");
        hubType.addElement("Filter");
        hubType.addElement("Detail");
        hubType.addElement("Share");
        hubType.addElement("Shareao");
    }
     
    // Links to other objects.
    protected transient ObjectGraph objectGraph;
    protected transient ObjectDef objectDef;
     
     
    public ObjectGraphHub() {
    }
     
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    public int getType() {
        return type;
    }
    
    public void setType(int newValue) {
        int old = type;
        this.type = newValue;
        firePropertyChange(PROPERTY_Type, old, this.type);
    }
    
     
    public ObjectGraph getObjectGraph() {
        if (objectGraph == null) {
        	objectGraph = (ObjectGraph) getObject(PROPERTY_ObjectGraph);
        }
        return objectGraph;
    }
    
    public void setObjectGraph(ObjectGraph newValue) {
    	ObjectGraph old = this.objectGraph;
        this.objectGraph = newValue;
        firePropertyChange(PROPERTY_ObjectGraph, old, this.objectGraph);
    }
    
     
    public ObjectDef getObjectDef() {
        if (objectDef == null) {
            objectDef = (ObjectDef) getObject(PROPERTY_ObjectDef);
        }
        return objectDef;
    }
    
    public void setObjectDef(ObjectDef newValue) {
        ObjectDef old = this.objectDef;
        this.objectDef = newValue;
        firePropertyChange(PROPERTY_ObjectDef, old, this.objectDef);
    }
    
    public String toString() {
    	return super.toString() + " " + getName();
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, thisOwner)
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ObjectGraph, ObjectGraph.class, OALinkInfo.ONE, false, false, ObjectGraph.PROPERTY_ObjectGraphHubs));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ObjectDef, ObjectDef.class, OALinkInfo.ONE, false, false, ""));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        // oaObjectInfo.addRequired("propertyName");
    }
}
 
