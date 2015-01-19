package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class Category extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String name;
    
    // Links to other objects
    protected Hub hubCategory;
    protected Category parentCategory;
    protected Hub hubMilitaryJobCode;
    
    public Category() {
    }
     
    public Category(int id) {
        setId(id);
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange("name",old,name);
    }
    
    public Hub getCategories() {
        if (hubCategory == null) hubCategory = getHub("categories");
        return hubCategory;
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
    
    public Hub getMilitaryJobCodes() {
        if (hubMilitaryJobCode == null) hubMilitaryJobCode = getHub("militaryJobCodes");
        return hubMilitaryJobCode;
    }
    
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("batchRows",BatchRow.class,OALinkInfo.MANY, false,"category"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("categories",Category.class,OALinkInfo.MANY, false,"parentCategory"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("parentCategory",Category.class,OALinkInfo.ONE, false,"categories"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("militaryJobCodes",MilitaryJobCode.class,OALinkInfo.MANY, false,"categories"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("jobs",Job.class,OALinkInfo.MANY, false,"categories"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetUsers",VetUser.class,OALinkInfo.MANY, false,"categories"));
    }
}

