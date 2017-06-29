package com.viaoa.jsp;

import com.viaoa.hub.Hub;


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
