package com.viaoa.jsp;

import java.util.ArrayList;

import com.viaoa.hub.Hub;
import com.viaoa.util.OAString;


/**
 * creates a bootstrap badge
 * @author vvia
 */
public class OABadge extends OAHtmlElement {

    public OABadge(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath);
        addClass("badge");
    }
}
