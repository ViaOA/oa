// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import java.io.Serializable;

import com.tmgsc.hifivetest.model.oa.*;
 
public class OrderTrackingStatusPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private OrderTrackingPPx orderTracking;
     
    public OrderTrackingStatusPPx(String name) {
        this(null, name);
    }

    public OrderTrackingStatusPPx(PPxInterface parent, String name) {
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

    public OrderTrackingPPx orderTracking() {
        if (orderTracking == null) orderTracking = new OrderTrackingPPx(this, OrderTrackingStatus.P_OrderTracking);
        return orderTracking;
    }

    public String id() {
        return pp + "." + OrderTrackingStatus.P_Id;
    }

    public String created() {
        return pp + "." + OrderTrackingStatus.P_Created;
    }

    public String description() {
        return pp + "." + OrderTrackingStatus.P_Description;
    }

    public String note() {
        return pp + "." + OrderTrackingStatus.P_Note;
    }

    public String emailAddress() {
        return pp + "." + OrderTrackingStatus.P_EmailAddress;
    }

    public String emailText() {
        return pp + "." + OrderTrackingStatus.P_EmailText;
    }

    public String emailDate() {
        return pp + "." + OrderTrackingStatus.P_EmailDate;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
