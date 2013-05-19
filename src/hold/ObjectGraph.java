package com.viaoa.model;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class ObjectGraph extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Name = "Name";
     
     
    public static final String PROPERTY_ObjectGraphHubs = "ObjectGraphHubs";
    public static final String PROPERTY_ObjectGraphs = "ObjectGraphs";
    public static final String PROPERTY_ParentObjectGraph = "ParentObjectGraph";
    public static final String PROPERTY_Model = "Model";
     
    protected String name;
     
    // Links to other objects.
    protected transient Hub hubObjectGraphHubs;
    protected transient Hub hubObjectGraphs;
    protected transient ObjectGraph parentObjectGraph;
    protected transient Model model;
    
     
    public ObjectGraph() {
    	int x = 44;
    }
     
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    public Hub getObjectGraphHubs() {
        if (hubObjectGraphHubs == null) {
            hubObjectGraphHubs = getHub(PROPERTY_ObjectGraphHubs);
        }
        return hubObjectGraphHubs;
    }
    
     
    public Hub getObjectGraphs() {
        if (hubObjectGraphs == null) {
            hubObjectGraphs = getHub(PROPERTY_ObjectGraphs);
        }
        return hubObjectGraphs;
    }
    
     
    public ObjectGraph getParentObjectGraph() {
        if (parentObjectGraph == null) {
        	parentObjectGraph = (ObjectGraph) getObject(PROPERTY_ParentObjectGraph);
        }
        return parentObjectGraph;
    }
    
    public void setParentObjectGraph(ObjectGraph newValue) {
        ObjectGraph old = this.parentObjectGraph;
        this.parentObjectGraph = newValue;
        firePropertyChange(PROPERTY_ParentObjectGraph, old, this.parentObjectGraph);
    }
    
    public Model getModel() {
        if (model == null) model = (Model) getObject(PROPERTY_Model);
        return model;
    }
    public void setModel(Model model) {
        Model old = this.model;
        this.model = model;
        firePropertyChange(PROPERTY_Model, old, model);
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
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Model,           Model.class,        OALinkInfo.ONE,  false,false, "objectGraphs"));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ObjectGraphHubs, ObjectGraphHub.class, OALinkInfo.MANY, false, false, ObjectGraphHub.PROPERTY_ObjectGraph));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ObjectGraphs, ObjectGraph.class, OALinkInfo.MANY, false, false, ObjectGraph.PROPERTY_ParentObjectGraph));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ParentObjectGraph, ObjectGraph.class, OALinkInfo.ONE, false, false, ObjectGraph.PROPERTY_ObjectGraphs));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        // oaObjectInfo.addRequired("propertyName");
    }
}
 
