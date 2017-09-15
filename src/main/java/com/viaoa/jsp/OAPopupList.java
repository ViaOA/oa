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
    private String idThis;
    private boolean bUpdateText;
    
    /**
     * @param idPopup html element to listen for click event
     * @param bUpdateText update html text for idPopup to match the selected item
     */
    public OAPopupList(String id, Hub hub, String propertyPath, boolean bUpdateText) {
        this(id, hub, propertyPath, bUpdateText, 0, 0);
    }
    public OAPopupList(String id, Hub hub, String propertyPath, boolean bUpdateText, int cols, int rows) {
        super(id+"PopupList", hub, propertyPath, cols, rows);
        this.idThis = id;
        this.bUpdateText = bUpdateText;
    }
    public OAPopupList(String idPopup, Hub hub, String propertyPath) {
        this(idPopup, hub, propertyPath, false);
    }
    
    @Override
    public String getId() {
        return idThis;
    }

    
    private String lastAjaxSent;
    
    @Override
    public String getScript() {
        lastAjaxSent = null;
        return super.getScript();
    }
    
    @Override
    protected String getScript2() {
        StringBuilder sb = new StringBuilder(1024);

        // need to create an outer div to wrap the "button"
        sb.append("$('#"+idThis+"').wrap(\"<div id='"+idThis+"PopupListOuterWrapper' class='oaPopupListOuterWrapper'></div>\");\n");

        // create another div wrapper for the OAList and OList
        sb.append("$('#"+idThis+"').after(\"<div id='"+idThis+"PopupListWrapper' class='oaPopupListWrapper'><ul id='"+idThis+"PopupList'></ul></div>\");\n");

        
        sb.append("$('#"+idThis+"').html(\"<span class='oaPopupListText'></span> <span class='oaCaret'></span>\");\n");
        
        if (columns > 0) {
            int x = (int) (columns*.75);
            sb.append("$('#"+idThis+" > span:first-child').addClass('oaTextNoWrap');\n");
            sb.append("$('#"+idThis+" > span:first-child').css(\"width\", \""+x+"em\");");
            sb.append("$('#"+idThis+" > span:first-child').css(\"max-width\", \""+x+"em\");");
        }
        
        
        sb.append("$('#"+idThis+"').click(function(e) {\n");
        sb.append("    $('#"+idThis+"PopupListWrapper').slideToggle(80);\n");
        sb.append("    return false;\n");
        sb.append("});\n");
        

        sb.append("var "+idThis+"WindowClick = window.onclick;\n");
        sb.append("window.onclick = function(event) {\n");
        sb.append("    if ("+idThis+"WindowClick) "+idThis+"WindowClick(event);\n");
        sb.append("    if ($('#"+idThis+"PopupListWrapper').is(':visible')) ");         
        sb.append("$('#"+idThis+"PopupListWrapper').slideToggle(80);\n");
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
                s = "$('#"+idThis+" > span:first-child').html($('#"+idThis+"PopupList li.oaSelected').html());\n";
            }
        }
        return s;
    }

    @Override
    public String getAjaxScript() {
        String js = super.getAjaxScript();
        if (js == null) js = "";
        String s = getScript3();
        if (s != null) js += s;
        
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;
        return js;
    }
    
    public String getPopupId() {
        return idThis+"PopupListWrapper";
    }
    
    
    @Override
    protected String getOnLineClickJs() {
        String s = null;
        if (bUpdateText) {
            if (hub != null && (hub.getPos() >= 0 || getNullDescription() != null)) {
                s = "$('#"+idThis+" > span:first-child').html($(this).html());\n";
            }
        }
        return s;
    }
    
}
