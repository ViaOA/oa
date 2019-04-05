// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class ProductAuditPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private ProductPPx product;
     
    public ProductAuditPPx(String name) {
        this(null, name);
    }

    public ProductAuditPPx(PPxInterface parent, String name) {
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

    public ProductPPx product() {
        if (product == null) product = new ProductPPx(this, ProductAudit.P_Product);
        return product;
    }

    public String id() {
        return pp + "." + ProductAudit.P_Id;
    }

    public String created() {
        return pp + "." + ProductAudit.P_Created;
    }

    public String cost() {
        return pp + "." + ProductAudit.P_Cost;
    }

    public String handlingCost() {
        return pp + "." + ProductAudit.P_HandlingCost;
    }

    public String note() {
        return pp + "." + ProductAudit.P_Note;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
