package com.viaoa.jsp;

import com.viaoa.hub.Hub;

/**
 * used to popup an OAList when an html component is clicked.
 * 
 * creates an outer wrapper div around the command and oalist id+"PopupListOuterWrapper"
 * creates a wrapper div around the oalist id+"PopupListWrapper"
 * creates oalist  id+"PopupList"
 *  
 * @author vvia
 *
 */
public class OAPopupList extends OAList {
    
    // the tag id that is being clicked to popup the list
    private String idClick;
    private boolean bUpdateText;
    
    /**
     * @param idPopup html element to listen for click event
     * @param bUpdateText update html text for idPopup to match the selected item
     */
    public OAPopupList(String idPopup, Hub hub, String propertyPath, boolean bUpdateText) {
        this(idPopup, hub, propertyPath, bUpdateText, 0, 0);
    }
    public OAPopupList(String idPopup, Hub hub, String propertyPath, boolean bUpdateText, int cols, int rows) {
        super(idPopup+"PopupList", hub, propertyPath, cols, rows);
        this.idClick = idPopup;
        this.bUpdateText = bUpdateText;
    }
    public OAPopupList(String idPopup, Hub hub, String propertyPath) {
        this(idPopup, hub, propertyPath, false);
    }
    
    
    
    @Override
    protected String getScript2() {
        StringBuilder sb = new StringBuilder(1024);

        // need to create an outer div to wrap the "button"
        sb.append("$('#"+idClick+"').wrap(\"<div id='"+idClick+"PopupListOuterWrapper' class='oaPopupListOuterWrapper'></div>\");\n");

        // create another div wrapper for the OAList and OList
        sb.append("$('#"+idClick+"').after(\"<div id='"+idClick+"PopupListWrapper' class='oaPopupListWrapper'><ul id='"+idClick+"PopupList'></ul></div>\");\n");

        
        sb.append("$('#"+idClick+"').html(\"<span class='oaPopupListText'></span> <span class='oaCaret'></span>\");\n");
        
        if (columns > 0) {
            int x = (int) (columns*.75);
            sb.append("$('#"+idClick+" > span:first-child').addClass('oaTextNoWrap');\n");
            sb.append("$('#"+idClick+" > span:first-child').css(\"width\", \""+x+"em\");");
            sb.append("$('#"+idClick+" > span:first-child').css(\"max-width\", \""+x+"em\");");
        }
        
        
        sb.append("$('#"+idClick+"').click(function(e) {\n");
        sb.append("    $('#"+idClick+"PopupListWrapper').slideToggle(80);\n");
        sb.append("    return false;\n");
        sb.append("});\n");
        sb.append("window.onclick = function(event) {\n");
        
        sb.append("    if ($('#"+idClick+"PopupListWrapper').is(':visible')) ");         
        sb.append("$('#"+idClick+"PopupListWrapper').slideToggle(80);\n");
        // was:  sb.append("    $('#"+idClick+"PopupListWrapper').hide();\n");
        sb.append("}\n");

        String js = sb.toString();
        return js;
    }

    @Override
    protected String getScript3() {
        String s = null;
        if (bUpdateText) {
            if (hub != null && (hub.getPos() >= 0 || getNullDescription() != null)) {
                s = "$('#"+idClick+" > span:first-child').html($('#"+idClick+"PopupList li.oaSelected').html());\n";
            }
        }
        return s;
    }
    
    @Override
    public String getAjaxScript() {
        String s = super.getAjaxScript();
        if (s == null) s = "";
        s += getScript3();
        return s;
    }
    
    public String getMenuId() {
        return idClick+"PopupListWrapper";
    }
    
    
    @Override
    protected String getOnLineClickJs() {
        String s = null;
        if (bUpdateText) {
            if (hub != null && (hub.getPos() >= 0 || getNullDescription() != null)) {
                s = "$('#"+idClick+" > span:first-child').html($(this).html());\n";
            }
        }
        return s;
    }
    
    public String getClickId() {
        return idClick;
    }
    
}
