package com.viaoa.jsp;

import com.viaoa.hub.Hub;

public class OAButtonList extends OAPopupList {

    public OAButtonList(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath, true);
    }
    public OAButtonList(String id, Hub hub, String propertyPath, int cols, int rows) {
        super(id, hub, propertyPath, true, cols, rows);
    }

}
