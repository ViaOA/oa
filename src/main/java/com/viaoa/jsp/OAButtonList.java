package com.viaoa.jsp;

import com.viaoa.hub.Hub;

public class OAButtonList extends OAPopupList {

    public OAButtonList(String idPopup, Hub hub, String propertyPath) {
        super(idPopup, hub, propertyPath, true);
    }
    public OAButtonList(String idPopup, Hub hub, String propertyPath, int cols, int rows) {
        super(idPopup, hub, propertyPath, true, cols, rows);
    }

}
