package com.viaoa.jsp;

import com.viaoa.hub.Hub;
import com.viaoa.util.OAString;

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

    // need to have separate vars, so that oalist only gets popup values
    protected int columns;
    protected String width; 
    
    
    public int getColumns() {
        return this.columns;
    }
    public void setColumns(int x) {
        this.columns = x;
    }
    public int getPopupColumns() {
        return super.getColumns();
    }
    public void setPopupColumns(int x) {
        super.setColumns(x);
    }

    public String getWidth() {
        return this.width;
    }
    public void setWidth(String width) {
        this.width = width;
    }
    public String getPopupWidth() {
        return super.getWidth();
    }
    public void setPopupWidth(String width) {
        super.setWidth(width);
    }
    
    /*
     * @param idPopup html element to listen for click event
     * @param bUpdateText update html text for idPopup to match the selected item. Example: set the text/name for a button.
     */
    public OAPopupList(String id, Hub hub, String propertyPath, boolean bUpdateText) {
        this(id, hub, propertyPath, bUpdateText, 0, 0);
    }
    public OAPopupList(String id, Hub hub, String propertyPath, boolean bUpdateText, int cols, int rows) {
        super(id+"PopupList", hub, propertyPath);
        setColumns(cols);
        setRows(rows);
        this.idThis = id;
        this.bUpdateText = bUpdateText;
    }
    public OAPopupList(String idPopup, Hub hub, String propertyPath) {
        this(idPopup, hub, propertyPath, false);
    }
    public OAPopupList(String id, Hub hub, String propertyPath, boolean bUpdateText, String width, String height) {
        super(id+"PopupList", hub, propertyPath);
        setWidth(width);
        setHeight(height);
        this.idThis = id;
        this.bUpdateText = bUpdateText;
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
        sb.append("$('#"+idThis+"').wrap(\"<div id='"+idThis+"PopupListOuterWrapper' class='oaPopupListOuterWrapper' style='");

        if (OAString.isNotEmpty(width)) {
            sb.append("width: "+width+";");
            sb.append("max-width: "+width+";");
        }
        else if (columns > 0) {
            int x = (int) (columns*.75);
            sb.append("width: "+x+"em;");
            sb.append("max-width: "+x+"em;");
        }
        sb.append("'></div>\");\n");


        // button uses all of wrapper width
        if (getColumns() > 0 || OAString.isNotEmpty(getWidth())) {
            sb.append("$('#"+idThis+"').css(\"width\", \"100%\");\n");
        }
        
        // add area for text and caret icon
        sb.append("$('#"+idThis+"').html(\"<span class='oaPopupListText");
        if (getColumns() > 0 || OAString.isNotEmpty(getWidth())) {
            sb.append(" oaPopupListTextSized");
        }
        sb.append("'></span><span class='oaCaret'></span>\");\n");

        
        // create another div wrapper for the popup OAList
        sb.append("$('#"+idThis+"').after(\"<div id='"+idThis+"PopupListWrapper' class='oaPopupListWrapper'><ul id='"+idThis+"PopupList'></ul></div>\");\n");
        
        sb.append("$('#"+idThis+"').click(function(e) {\n");
        
        if (OAString.isEmpty(getPopupWidth()) && getPopupColumns() < 1) {  // make same size as the button
            sb.append("    $('#"+idThis+"PopupListWrapper').css(\"min-width\", $('#"+idThis+"').width()+26);\n");
            //was: sb.append("    $('#"+idThis+"PopupListWrapper').width($('#"+idThis+"').width()+26);\n");
        }
        else {
            if (OAString.isNotEmpty(getPopupWidth())) {
                sb.append("    $('#"+idThis+"PopupListWrapper').width($('#"+idThis+"').width()+26);\n");
            }
        }
        
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
                s = "$('#"+idThis+" .oaPopupListText').html($('#"+idThis+"PopupList li.oaSelected').html());\n";
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
                s = "$('#"+idThis+" .oaPopupListText').html($(this).html());\n";
            }
        }
        return s;
    }
    
}
