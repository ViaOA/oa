// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class HifiveOrderCardPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private CardPPx card;
    private HifiveOrderPPx hifiveOrder;
     
    public HifiveOrderCardPPx(String name) {
        this(null, name);
    }

    public HifiveOrderCardPPx(PPxInterface parent, String name) {
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

    public CardPPx card() {
        if (card == null) card = new CardPPx(this, HifiveOrderCard.P_Card);
        return card;
    }

    public HifiveOrderPPx hifiveOrder() {
        if (hifiveOrder == null) hifiveOrder = new HifiveOrderPPx(this, HifiveOrderCard.P_HifiveOrder);
        return hifiveOrder;
    }

    public String id() {
        return pp + "." + HifiveOrderCard.P_Id;
    }

    public String created() {
        return pp + "." + HifiveOrderCard.P_Created;
    }

    public String seq() {
        return pp + "." + HifiveOrderCard.P_Seq;
    }

    public String pointsUsed() {
        return pp + "." + HifiveOrderCard.P_PointsUsed;
    }

    public String completedDate() {
        return pp + "." + HifiveOrderCard.P_CompletedDate;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
