// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class MerchantPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private CardPPx cards;
    private ImageStorePPx imageStore;
    private MerchantCategoryPPx merchantCategories;
     
    public MerchantPPx(String name) {
        this(null, name);
    }

    public MerchantPPx(PPxInterface parent, String name) {
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

    public CardPPx cards() {
        if (cards == null) cards = new CardPPx(this, Merchant.P_Cards);
        return cards;
    }

    public ImageStorePPx imageStore() {
        if (imageStore == null) imageStore = new ImageStorePPx(this, Merchant.P_ImageStore);
        return imageStore;
    }

    public MerchantCategoryPPx merchantCategories() {
        if (merchantCategories == null) merchantCategories = new MerchantCategoryPPx(this, Merchant.P_MerchantCategories);
        return merchantCategories;
    }

    public String id() {
        return pp + "." + Merchant.P_Id;
    }

    public String created() {
        return pp + "." + Merchant.P_Created;
    }

    public String name() {
        return pp + "." + Merchant.P_Name;
    }

    public String description() {
        return pp + "." + Merchant.P_Description;
    }

    public String text() {
        return pp + "." + Merchant.P_Text;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
