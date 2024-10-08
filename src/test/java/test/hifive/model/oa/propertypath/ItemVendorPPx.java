// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class ItemVendorPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private ItemPPx items;
     
    public ItemVendorPPx(String name) {
        this(null, name);
    }

    public ItemVendorPPx(PPxInterface parent, String name) {
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

    public ItemPPx items() {
        if (items == null) items = new ItemPPx(this, ItemVendor.P_Items);
        return items;
    }

    public String id() {
        return pp + "." + ItemVendor.P_Id;
    }

    public String created() {
        return pp + "." + ItemVendor.P_Created;
    }

    public String name() {
        return pp + "." + ItemVendor.P_Name;
    }

    public String notes() {
        return pp + "." + ItemVendor.P_Notes;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
