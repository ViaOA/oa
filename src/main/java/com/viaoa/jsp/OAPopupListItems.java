package com.viaoa.jsp;

import com.viaoa.hub.Hub;

/**
 * used to popup an OAListItems when an html component is clicked.
 * 
 * creates an outer wrapper div around the command and oalistitems id+"PopupListItemsOuterWrapper"
 * creates a wrapper div around the oalistitems id+"PopupListItemsWrapper"
 * creates oalistitems  id+"PopupListItems"
 *  
 * @author vvia
 *
 */
public class OAPopupListItems extends OAListItems {
    
    // the tag id that is being clicked to popup the listItems
    private String idClick;
    
    public OAPopupListItems(String idPopup, Hub hub, String propertyPath) {
        super(idPopup+"PopupListItems", hub, propertyPath);
        idClick = idPopup;
    }

    
    
    @Override
    protected String getScript2() {
        StringBuilder sb = new StringBuilder(256);

        // need to create an outer div to wrap the "button"
        sb.append("$('#"+idClick+"').wrap(\"<div id='"+idClick+"PopupListItemsOuterWrapper' class='oaPopupListItemsOuterWrapper'></div>\");\n");

        // create another div wrapper for the OAListItems and OListItems
        sb.append("$('#"+idClick+"').after(\"<div id='"+idClick+"PopupListItemsWrapper' class='oaPopupListItemsWrapper'><ul id='"+idClick+"PopupListItems'></ul></div>\");\n");
        
        sb.append("$('#"+idClick+"').click(function(e) {\n");
        sb.append("    $('#"+idClick+"PopupListItemsWrapper').toggle();\n");
        sb.append("    return false;\n");
        sb.append("});\n");
        sb.append("window.onclick = function(event) {\n");
        sb.append("    $('#"+idClick+"PopupListItemsWrapper').hide();\n");
        sb.append("}\n");
        
        String js = sb.toString();
        return js;
    }
    
    public String getMenuId() {
        return idClick+"PopupListItemsWrapper";
    }
    
    
    @Override
    protected String getOnLineClickJs() {
        String s = "$('#"+idClick+" > span').html(liValue);\n";
        return s;
    }
    
    public String getClickId() {
        return idClick;
    }
    
}
