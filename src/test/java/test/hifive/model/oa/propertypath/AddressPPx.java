// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class AddressPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private AddressTypePPx addressType;
    private EmployeePPx employee;
    private LocationPPx location;
    private ShipToPPx shipTos;
     
    public AddressPPx(String name) {
        this(null, name);
    }

    public AddressPPx(PPxInterface parent, String name) {
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

    public AddressTypePPx addressType() {
        if (addressType == null) addressType = new AddressTypePPx(this, Address.P_AddressType);
        return addressType;
    }

    public EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(this, Address.P_Employee);
        return employee;
    }

    public LocationPPx location() {
        if (location == null) location = new LocationPPx(this, Address.P_Location);
        return location;
    }

    public ShipToPPx shipTos() {
        if (shipTos == null) shipTos = new ShipToPPx(this, Address.P_ShipTos);
        return shipTos;
    }

    public String id() {
        return pp + "." + Address.P_Id;
    }

    public String created() {
        return pp + "." + Address.P_Created;
    }

    public String address1() {
        return pp + "." + Address.P_Address1;
    }

    public String address2() {
        return pp + "." + Address.P_Address2;
    }

    public String address3() {
        return pp + "." + Address.P_Address3;
    }

    public String address4() {
        return pp + "." + Address.P_Address4;
    }

    public String city() {
        return pp + "." + Address.P_City;
    }

    public String state() {
        return pp + "." + Address.P_State;
    }

    public String zip() {
        return pp + "." + Address.P_Zip;
    }

    public String country() {
        return pp + "." + Address.P_Country;
    }

    public String cityStateZip() {
        return pp + "." + Address.P_CityStateZip;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
