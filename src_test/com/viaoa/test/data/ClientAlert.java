package com.viaoa.test.data;
 
import com.viaoa.hub.*;
import com.viaoa.object.*;
 
 
public class ClientAlert extends OAObject {
    private static final long serialVersionUID = 1L;
    protected String id;
    protected String description;
     
    // Links to other objects
    protected transient Client client;
     
    public ClientAlert() {
    }
     
    public ClientAlert(String id) {
        this();
        setId(id);
    }
    public String getId() {
        return id;
    }
    
    public void setId(String newId) {
        String old = id;
        this.id = newId;
        firePropertyChange("id", old, id);
    }
    
     
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newDescription) {
        String old = description;
        this.description = newDescription;
        firePropertyChange("description", old, description);
    }
    
     
    public Client getClient() {
        if (client == null) {
            client = (Client) getObject("client");
        }
        return client;
    }
    
    public void setClient(Client newClient) {
        Client old = getClient();
        this.client = newClient;
        firePropertyChange("client", old, client);
    }
    
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, owner, recursive)
        oaObjectInfo.addLink(new OALinkInfo("client", Client.class, OALinkInfo.ONE, false, false, "clientAlerts", true));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
         
        oaObjectInfo.addRequired("id");
    }
     
}
 
