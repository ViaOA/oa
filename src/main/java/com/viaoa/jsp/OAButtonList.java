package com.viaoa.jsp;

import com.viaoa.hub.Hub;


/* *
 * Adds a popup list to a button, and sets the button text to the selected value.
 * 
 * Example:
 * <button id="blTest">ButtonList Test here</button>
 * @author vvia
 */
public class OAButtonList extends OAPopupList {

    public OAButtonList(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath, true);
    }
    public OAButtonList(String id, Hub hub, String propertyPath, int cols, int rows) {
        super(id, hub, propertyPath, true, cols, rows);
    }
    public OAButtonList(String id, Hub hub, String propertyPath, String width, String height) {
        super(id, hub, propertyPath, true, width, height);
    }
}

