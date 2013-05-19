package com.viaoa.model;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
 
 
public class AppModel extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Name = "Name";
     
     
    public static final String PROPERTY_AppHubs = "AppHubs";
    public static final String PROPERTY_AppModels = "AppModels";
    public static final String PROPERTY_ParentAppModel = "ParentAppModel";
    public static final String PROPERTY_Model = "Model";
     
    protected String name;
     
    // Links to other objects.
    protected transient Hub hubAppHubs;
    protected transient Hub hubAppModels;
    protected transient AppModel parentAppModel;
    protected transient Model model;
    
     
    public AppModel() {
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
    
     
    public Hub getAppHubs() {
        if (hubAppHubs == null) {
            hubAppHubs = getHub(PROPERTY_AppHubs);
        }
        return hubAppHubs;
    }
    
     
    public Hub getAppModels() {
        if (hubAppModels == null) {
            hubAppModels = getHub(PROPERTY_AppModels);
        }
        return hubAppModels;
    }
    
     
    public AppModel getParentAppModel() {
        if (parentAppModel == null) {
        	parentAppModel = (AppModel) getObject(PROPERTY_ParentAppModel);
        }
        return parentAppModel;
    }
    
    public void setParentAppModel(AppModel newValue) {
        AppModel old = this.parentAppModel;
        this.parentAppModel = newValue;
        firePropertyChange(PROPERTY_ParentAppModel, old, this.parentAppModel);
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
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Model,           Model.class,        OALinkInfo.ONE,  false,false, "appModels"));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_AppHubs, AppHub.class, OALinkInfo.MANY, false, false, AppHub.PROPERTY_AppModel));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_AppModels, AppModel.class, OALinkInfo.MANY, false, false, AppModel.PROPERTY_ParentAppModel));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ParentAppModel, AppModel.class, OALinkInfo.ONE, false, false, AppModel.PROPERTY_AppModels));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        // oaObjectInfo.addRequired("propertyName");
    }
}
 
