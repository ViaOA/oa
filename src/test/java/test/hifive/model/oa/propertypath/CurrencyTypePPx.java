// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class CurrencyTypePPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private CountryCodePPx countryCodes;
     
    public CurrencyTypePPx(String name) {
        this(null, name);
    }

    public CurrencyTypePPx(PPxInterface parent, String name) {
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

    public CountryCodePPx countryCodes() {
        if (countryCodes == null) countryCodes = new CountryCodePPx(this, CurrencyType.P_CountryCodes);
        return countryCodes;
    }

    public String id() {
        return pp + "." + CurrencyType.P_Id;
    }

    public String name() {
        return pp + "." + CurrencyType.P_Name;
    }

    public String abbreviation() {
        return pp + "." + CurrencyType.P_Abbreviation;
    }

    public String symbol() {
        return pp + "." + CurrencyType.P_Symbol;
    }

    public String exchangeRate() {
        return pp + "." + CurrencyType.P_ExchangeRate;
    }

    public String created() {
        return pp + "." + CurrencyType.P_Created;
    }

    public String convertTo() {
        return pp + ".convertTo";
    }

    public String convertFrom() {
        return pp + ".convertFrom";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
