package com.viaoa.jsp;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;

public class OALabel extends OAHtmlElement {

    public OALabel(String id, Hub hub, String propertyPath, int width) {
        super(id, hub, propertyPath, width);
    }
    public OALabel(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath);
    }
    public OALabel(String id, Hub hub, String propertyPath, int width, int minWidth, int maxRows) {
        super(id, hub, propertyPath, width, minWidth, maxRows);
    }

    @Override
    public void setMaxWidth(String val) {
        super.setMaxWidth(val);
        setOverflow("hidden");  // oahtmlelement will then add al.add("'text-overflow':'ellipsis'");
    }

    @Override
    public String getEditorHtml(OAObject obj) {
        return getRenderHtml(obj);  // no real editor
    }
    
}
