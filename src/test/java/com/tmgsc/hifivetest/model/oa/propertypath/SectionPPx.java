// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import java.io.Serializable;

import com.tmgsc.hifivetest.model.oa.*;
 
public class SectionPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private AwardTypePPx awardTypes;
    private CatalogPPx catalog;
    private ItemPPx items;
    private SectionPPx parentSection;
    private SectionPPx sections;
     
    public SectionPPx(String name) {
        this(null, name);
    }

    public SectionPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public AwardTypePPx awardTypes() {
        if (awardTypes == null) awardTypes = new AwardTypePPx(this, Section.P_AwardTypes);
        return awardTypes;
    }

    public CatalogPPx catalog() {
        if (catalog == null) catalog = new CatalogPPx(this, Section.P_Catalog);
        return catalog;
    }

    public ItemPPx items() {
        if (items == null) items = new ItemPPx(this, Section.P_Items);
        return items;
    }

    public SectionPPx parentSection() {
        if (parentSection == null) parentSection = new SectionPPx(this, Section.P_ParentSection);
        return parentSection;
    }

    public SectionPPx sections() {
        if (sections == null) sections = new SectionPPx(this, Section.P_Sections);
        return sections;
    }

    public String id() {
        return pp + "." + Section.P_Id;
    }

    public String seq() {
        return pp + "." + Section.P_Seq;
    }

    public String name() {
        return pp + "." + Section.P_Name;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
