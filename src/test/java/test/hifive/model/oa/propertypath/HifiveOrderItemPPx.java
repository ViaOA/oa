// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class HifiveOrderItemPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private HifiveOrderPPx hifiveOrder;
    private ProductPPx product;
     
    public HifiveOrderItemPPx(String name) {
        this(null, name);
    }

    public HifiveOrderItemPPx(PPxInterface parent, String name) {
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

    public HifiveOrderPPx hifiveOrder() {
        if (hifiveOrder == null) hifiveOrder = new HifiveOrderPPx(this, HifiveOrderItem.P_HifiveOrder);
        return hifiveOrder;
    }

    public ProductPPx product() {
        if (product == null) product = new ProductPPx(this, HifiveOrderItem.P_Product);
        return product;
    }

    public String id() {
        return pp + "." + HifiveOrderItem.P_Id;
    }

    public String created() {
        return pp + "." + HifiveOrderItem.P_Created;
    }

    public String seq() {
        return pp + "." + HifiveOrderItem.P_Seq;
    }

    public String quantity() {
        return pp + "." + HifiveOrderItem.P_Quantity;
    }

    public String pointsUsed() {
        return pp + "." + HifiveOrderItem.P_PointsUsed;
    }

    public String billDate() {
        return pp + "." + HifiveOrderItem.P_BillDate;
    }

    public String paidDate() {
        return pp + "." + HifiveOrderItem.P_PaidDate;
    }

    public String itemSentDate() {
        return pp + "." + HifiveOrderItem.P_ItemSentDate;
    }

    public String itemShippingInfo() {
        return pp + "." + HifiveOrderItem.P_ItemShippingInfo;
    }

    public String itemLastStatusDate() {
        return pp + "." + HifiveOrderItem.P_ItemLastStatusDate;
    }

    public String itemLastStatus() {
        return pp + "." + HifiveOrderItem.P_ItemLastStatus;
    }

    public String completedDate() {
        return pp + "." + HifiveOrderItem.P_CompletedDate;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
