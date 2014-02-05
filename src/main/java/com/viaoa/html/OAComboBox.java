/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.html;


import java.lang.reflect.*;
import java.util.ArrayList;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/** OAComboBox that uses a masterHub and another hub with all of the values to use.
<pre>
    [Java Code]
    Hub hubState = oasession.getHub("stringstate").createSharedHub();
    hubState.setLink(hubJob,"state");
    OAComboBox cbo = new OAComboBox(hubState, "name");
    cbo.setNullDescription(null);
    form.add("cboState",cbo);
    ....
    [HTML Code]
    &lt;select name="cboState"&gt; &lt;%=form.getComboBox("cboState").getOptions()%&gt; &lt;/select&gt;
    output =&gt;
    &lt;select name="cboState" &gt;&lt;option value="1" selected&gt;Texas&lt;/option&gt;&lt;/select&gt;

    -- JavaScript to Automatically cause form to submit:
    &lt;select name="cboState" onChange="forms[0].submit();" &gt;

    
    ... sample for creating a recursive listing that is linked to another hub
    Hub hubProduct = oasession.getHub("Product"); // hub that is going to be linked to
    Hub hubTop = oaapplication.getHub("Category").createSharedHub();  // categories without parentCategory (top Hub)
    Hub hub = new Hub(Category.class);  // hub that comboBox will use
    hub.setRecursiveHub(hubTop);
    hub.setLink(hubProduct); 

    OAComboBox cbo = new OAComboBox(hub, "name");
    cbo.setNullDescription("Top Level");
    form.add("cboCategory", cbo);
<pre>
*/
public class OAComboBox extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected Object value; // value on form
    protected String nullDescription = "";
    private boolean bUsed; // flag to know if it was used on form
    protected int columns;
    protected OALinkInfo recursiveLinkInfo;
    protected Hub topHub;
    protected boolean bNullOptionFirst = true;
    
    public OAComboBox(Hub hub, String propertyPath) {
        this(hub,propertyPath, 0);
    }
    public OAComboBox(Hub hub, String propertyPath, int columns) {
        setHub(hub);
        setPropertyPath(propertyPath);
        setColumns(columns);
    }


    /** 
        This will call hub.setLink(linkHub).
        Note: if hub is not a Shared Hub, then one will be created and it will be used.
        @param masterHub hub that is used for listing
        @param linkHub hub to update
        @parem propertyPath property to display
    */
    public OAComboBox(Hub masterHub, Hub linkHub, String propertyPath, int columns) {
        if (masterHub != null && linkHub != null) {
            if (masterHub.getSharedHub() == null) masterHub = masterHub.createShared();
            if (masterHub.getLinkHub() != linkHub) masterHub.setLink(linkHub);
        }
        setHub(masterHub);
        setPropertyPath(propertyPath);
        setColumns(columns);
    }
    public OAComboBox(Hub masterHub, Hub linkHub, String propertyPath) {
        this(masterHub,linkHub,propertyPath,0);
    }

    public void setHub(Hub hub) {
        super.setHub(hub);
        if (hub.getRootHub() != null) setRecursive(true);
    }
        
    /** Used to create a recursive listing.
        Set to true if Hub has a recursiveHub.
    */
    public void setRecursive(boolean b) {
        this.topHub = null;
        this.recursiveLinkInfo = null;
        if (b) {
            topHub = hub.getRootHub();
            if (topHub == null) throw new RuntimeException("Hub must have a recursive hub. see Hub.setRecursiveHub()");
            
            // find recursive method
            OAObjectInfo oi = Hub.getOAObjectInfo(hub.getObjectClass());
            ArrayList al = oi.getLinkInfos();
            for (int i=0; al != null && i < al.size(); i++) {
                OALinkInfo li = (OALinkInfo) al.get(i); 
                if ( hub.getObjectClass().equals(li.getToClass())) {
                    if (li.getType() == OALinkInfo.MANY) {
                        this.recursiveLinkInfo = li;
                        return;
                    }
                }
            }
            throw new RuntimeException("cant find recursive property");
        }
    }
    public boolean getRecursive() {
        return (recursiveLinkInfo != null);
    }

    /** set the position of the null selection to First in list. Default=true */
    public void setNullOptionFirst(boolean b) {
        bNullOptionFirst = b;
    }
    public boolean getNullOptionFirst() {
        return bNullOptionFirst;
    }
        
    /** get the currently selected object.  Same as calling getHub().getActiveObject() */
    public Object getSelectedObject() {
        if (hub == null) return null;
        return hub.getActiveObject();
    }
        
    /** the "word(s)" to use for the empty slot (null value).  
        ex: "none of the above".  
        Default: "" 
        set to null if none should be used
    */
    public String getNullDescription() {
        return nullDescription;
    }
    public void setNullDescription(String s) {
        nullDescription = s;
    }

    public void setColumns(int cols) {
        this.columns = cols;
    }
    public int getColumns() {
        return columns;
    }


    /** @return the selected object */
    public Object getValue() {
        initialize();
        return value;
    }

    protected void resetHubOrProperty() {
        super.resetHubOrProperty();
        initialize();
    }


    protected void initialize() {
        if (hub != null) {
            value = hub.getActiveObject();
        }
        else value = null;
    }

    /************************** OAHtmlComponent ************************/
    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
        bUsed = true;
        if (values == null) return;
        if (values.length != 1 || values[0].equals("[null]")) {
            value = null;
            return;
        }
        
        String svalue = values[0];

        if (recursiveLinkInfo != null) {
            Hub h = topHub;
            Object obj=null;
            String slen = com.viaoa.html.Util.field(svalue,'_',1);
            int x = 0;
            try {
                x = Integer.parseInt(slen);
            }
            catch (NumberFormatException e) { }
            svalue = svalue.substring(slen.length() + 1);  // take off 1st field

            for (int k=0; k<x; k++) {
                int len = 0;
                slen = com.viaoa.html.Util.field(svalue,'_',1);
                try {
                    len = Integer.parseInt(slen);
                }
                catch (NumberFormatException e) { }

                int x1 = slen.length()+1;
                String id = svalue.substring(x1, x1+len);
                value = Util.getObject(h, id);
                if (value == null) break;
                
                if (k+1 == x) break;
                svalue = svalue.substring(slen.length()+1+len+1); // next id
                h = (Hub) recursiveLinkInfo.getValue(value);
            }
        }
        else value = com.viaoa.html.Util.getObject(hub, svalue);
    }
    
    public void update() {
        if (bUsed) {
            if (hub != null) hub.setActiveObject(value);
        }
        bUsed = false;
    }

    public boolean isChanged() {
        if (hub != null) return (hub.getActiveObject() != value);
        return false;
    }
    public void reset() {
    }



    private String lastValue;
    public boolean needsRefreshed() {
        String s = lastValue;
        String s2 = getOptions();
        return (s == null || !s.equals(s2));
    }

    private String format(String s, boolean bPad) {
        if (columns > 0) {
            int j = s.length();
            if (j > columns) s = s.substring(0,columns);
            else if (bPad) {
                for ( ;j<columns;j++) s += "&nbsp;";
            }
        }
        return s;
    }        

    /** returns the string needed for "option" tags.
    */
    public String getOptions() {
        return getOptions(null);
    }
    /** returns the string needed for "option" tags.
        @param strSelectValue, if null (default), then the hub will be used to set "select" item.
        Else, strSelectValue will be used to compare against items displayed.
        Note: if strSelectValue is not null, then the activeObject will not be set.
        It can be used to set to value of a textField by creating an OATextField and using its name for
        the ComboBox "name" tag.  The textfield will then be updated on submit.
    */
    public String getOptions(String strSelectValue) {
        bUsed = true;
        StringBuffer sb = new StringBuffer(1024);

        
        Object obj = hub.getActiveObject();
        String nullOpt = null;
        if (nullDescription != null) {
            String s = format(nullDescription, true);
            nullOpt = ("<option value=\"[null]\""+ ((obj==null)?" selected":"") + ">"+s+"</option>");
        }

        if (bNullOptionFirst && nullOpt != null) sb.append(nullOpt);
        try {
            getOptions((topHub!=null)?topHub:hub, sb, new String[0], strSelectValue);
        }
        catch (Exception e) {
            handleException(e,"getOptions()");
            return "<option value=\"error\" selected>Exception Occured</option>";
        }
        if (!bNullOptionFirst && nullOpt != null) sb.append(nullOpt);
        
        lastValue = new String(sb);

        return lastValue;
    }

    /** @param ids the objectIds for all parents if this is using a recursiveHub. */
    void getOptions(Hub h, StringBuffer sb, String[] ids, String strSelectValue) {
        String s;
        h.loadAllData();

        Object activeObject = hub.getActiveObject();

        for (int i=0; ; i++) {
            Object obj = h.elementAt(i);
            if (obj == null) break;
            
            String id;
            if (obj instanceof OAObject) id = Util.getObjectIdAsString(h,(OAObject) obj);
            else id = obj.toString();

            s = ClassModifier.getPropertyValueAsString(obj, getGetMethods(), getFormat());
            if (obj instanceof OAObject && ((OAObject)obj).isNull(propertyPath)) s = "";
            if (s == null) s = "";
            s = com.viaoa.html.Util.toEscapeString(s);

            sb.append("<option value=\"");

            if (recursiveLinkInfo != null) {
                // if recursive then id= #1_#2_id1_  where #1=number of ids, #2=length of id
                sb.append((ids.length+1) + "_");  // # of ids
                for (int j=0; j<ids.length; j++) {
                    sb.append(ids[j].length() + "_" + ids[j] + "_");
                }
                sb.append(id.length() + "_");
            }

            if (strSelectValue != null) id = s;
            
            sb.append(id+"\"");
            if (obj.equals(activeObject) || (strSelectValue != null && id.equals(strSelectValue)) ) {
                sb.append(" selected");
            }
            
            sb.append(">");
            if (ids.length > 0) {
                sb.append("&nbsp;&nbsp;&nbsp;");
                for (int j=0; j<(ids.length-1); j++) sb.append("&nbsp;&nbsp;&nbsp;");
                sb.append("--&nbsp;");
            }
            s = format(s, i==0);
            
            sb.append(s + "</option>");

            if (recursiveLinkInfo != null) {
                Hub h2 = (Hub) recursiveLinkInfo.getValue(obj);
                String[] ss = new String[ids.length+1];
                System.arraycopy(ids, 0, ss, 0, ids.length);
                ss[ids.length] = id;
                if (h2 != null) getOptions(h2, sb, ss,strSelectValue);
            }
        }

    }

    public String getHtml(String htmlTags) {
// <select name="cboState"> <%=form.getComboBox("cboState").getOptions()%> </select>
// <select name="cboState" ><option value="1" selected>Texas</option></select>
        String s = "";
        if (htmlBefore != null) s += htmlBefore;
        s += "<select ";
        s += " name=\""+name+"\"";

        if ( (hub != null && !hub.isValid()) || !bEnabled || (form != null && form.getReadOnly()) ) s += " DISABLED";

        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;
        s += ">";
        s += getOptions();
        s += "</select>";
        if (htmlAfter != null) s += htmlAfter;
        return s;
    }
}
