package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class CategoryFix extends OAObject {
    private static final long serialVersionUID = 1L;
//    protected long id;
    protected String name;
    
    // Links to other objects
    protected Category parentCategory;

    public CategoryFix() {
    }
/*
    public CategoryFix(long id) {
        setId(id);
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
*/
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange("name",old,name);
    }
    
    public Category getParentCategory() {
        if (parentCategory == null) parentCategory = (Category) getObject("parentCategory");
        return parentCategory;
    }
    
    public void setParentCategory(Category parentCategory) {
        Category old = this.parentCategory;
        this.parentCategory = parentCategory;
        firePropertyChange("parentCategory",old,parentCategory);
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("name");
        oaObjectInfo.addLink(new OALinkInfo("parentCategory",Category.class,OALinkInfo.ONE, false,"categories"));
        
    }
}
     


