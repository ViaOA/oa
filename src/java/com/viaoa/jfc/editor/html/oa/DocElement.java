package com.viaoa.jfc.editor.html.oa;

import com.viaoa.annotation.OAClass;
import com.viaoa.hub.Hub;
import com.viaoa.object.*;

/**
 * OAObject used for capturing html elements from html document.
 * @author vvia
 *
 */
@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class DocElement extends OAObject {
    private static final long serialVersionUID = 1L;
    protected String id;
	protected String name;
	
	protected Hub hubDocElement;
	protected DocElement parentDocElement;
	protected Hub hubDocAttribute;

	
    public String getId() {
        return id;
    }
    
    public void setId(String newId) {
        String old = id;
        this.id = newId;
        firePropertyChange("id", old, id);
    }
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange("name", old, name);
	}

	public Hub getDocElements() {
		if (hubDocElement == null) {
			hubDocElement = getHub("DocElements");
		}
		return hubDocElement;
	}
	
	public DocElement getParentDocElement() {
		if (parentDocElement == null) parentDocElement = (DocElement) getObject("parentDocElement");
		return parentDocElement;
	}

    public void setParentDocElement(DocElement newValue) {
        DocElement old = this.parentDocElement;
        this.parentDocElement = newValue;
        firePropertyChange("ParentDocElement", old, this.parentDocElement);
    }
    
	
	
	public Hub getDocAttributes() {
		if (hubDocAttribute == null) hubDocAttribute = getHub("DocAttributes");
		return hubDocAttribute;
	}
	
	
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
        
        // OALinkInfo(property, toClass, ONE/MANY, cascadeSave, cascadeDelete, reverseProperty, allowDelete, owner)
        oaObjectInfo.addLink(new OALinkInfo("parentDocElement", DocElement.class, OALinkInfo.ONE, false, false, "docElements"));
        OALinkInfo li = new OALinkInfo("docElements", DocElement.class, OALinkInfo.MANY, true, true, "parentDocElement", true);
        li.setRecursive(true);
        oaObjectInfo.addLink(li);
        oaObjectInfo.addLink(new OALinkInfo("docAttributes", DocAttribute.class, OALinkInfo.MANY, true, true, "DocElement", true));
        
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        // ex: oaObjectInfo.addCalc(new OACalcInfo("calc", new String[] {"name","manager.fullName"} ));
        
        oaObjectInfo.addRequired("id");
        oaObjectInfo.setLocalOnly(true);
        oaObjectInfo.setUseDataSource(false);
    }

}


