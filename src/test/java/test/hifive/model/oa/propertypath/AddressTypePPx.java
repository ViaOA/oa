// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class AddressTypePPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private AddressPPx addresses;
     
    public AddressTypePPx(String name) {
        this(null, name);
    }

    public AddressTypePPx(PPxInterface parent, String name) {
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

    public AddressPPx addresses() {
        if (addresses == null) addresses = new AddressPPx(this, AddressType.P_Addresses);
        return addresses;
    }

    public String id() {
        return pp + "." + AddressType.P_Id;
    }

    public String name() {
        return pp + "." + AddressType.P_Name;
    }

    public String type() {
        return pp + "." + AddressType.P_Type;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
