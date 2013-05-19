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
import java.util.Vector;
import com.viaoa.hub.*;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;


/** creates a multiList using two hubs: one with all the options, and another with selected options.

<pre>
    [Java Code]
    Hub hub = employee.getDepartments();
    Hub masterHub = oasession.getHub("dept");
    OAList lst = new OAList(hub, masterHub, "name");
    lst.setNullDescription(null); // Default: "None"
    lst.setAllDescription(null);  // Detault: "All"
    form.add("lstDept",lst);

    ....
    [HTML Code]
    &lt;select name="lstDept" multiple size="4"&gt; &lt;%=form.getOptions("lstDept")%&gt; &lt;/select&gt;

</pre>


*/
public class OAList extends OAHtmlComponent {
    private static final long serialVersionUID = 1L;
    protected Hub hubMaster;
    protected String propertyPath;
    protected String recursiveProperty;
    protected Method[] methodGet;
    protected Method recursiveMethod;
    protected Object[] objects = null; // values selected on form
    protected String nullDescription = "None";
    protected String allDescription = "All";
    protected Object currentObject; // the object that this is working with
    protected int columns, rows=-1;


    public OAList(Hub hub, Hub masterHub, String propertyPath) {
        this(hub,masterHub,propertyPath,0);
    }

    public OAList(Hub hub, Hub masterHub, String propertyPath, int columns) {
        this(hub, masterHub, propertyPath, -1, columns);
    }
    public OAList(Hub hub, Hub masterHub, String propertyPath, int rows, int columns) {
        setHub(hub);
        setMasterHub(masterHub);
        setPropertyPath(propertyPath);
        setRows(rows);
        setColumns(columns);
    }
    public OAList(Hub masterHub, String propertyPath) {
        this(null, masterHub, propertyPath,0);
    }

    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
        methodGet = null;
    }

    public Hub getMasterHub() {
        return hubMaster;
    }
    public void setMasterHub(Hub h) {
        this.hubMaster = h;
    }

    public void setColumns(int cols) {
        this.columns = cols;
    }
    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }
    /** number of rows.  */
    public void setRows(int x) {
        rows = x;
    }

    /** if this is a recursive hub: has parent and children of same class.  
        @param prop property name of method to get Hub of children.  ex: categories
    */
    public void setRecursivePropertyName(String prop) {
        recursiveProperty = prop;
    }
    public String getRecursivePropertyName() {
        return recursiveProperty;
    }

    
    /** the "word(s)" to use for the empty slot (null value).  Set to null if not needed.
        Default: "None"
        set to null if none should be used
    */
    public String getNullDescription() {
        return nullDescription;
    }
    public void setNullDescription(String s) {
        nullDescription = s;
    }
    /** the "word(s)" to use for the Select ALL slot.  
        Default: "All". Set to null if not needed.
    */
    public String getAllDescription() {
        return allDescription;
    }
    public void setAllDescription(String s) {
        allDescription = s;
    }

    /** selected objects  */
    public Object[] getValues() {
        initialize();
        return objects;
    }

    protected void initialize() {    
        
        if (hub != null && (objects == null || HubDetailDelegate.getMasterObject(hub) != currentObject)) {  // hub has changed
            currentObject = HubDetailDelegate.getMasterObject(hub);
            hub.loadAllData();
            int x = hub.getSize();
            objects = new Object[x];
            for (int i=0; i<x; i++) {
                objects[i] = hub.elementAt(i);
            }
        }
    }

    public void selectAll() {
        hubMaster.loadAllData();
        if (hub != null) hub.clear();
        int x = hubMaster.getSize();
        objects = new Object[x];
        for (int i=0; i<x; i++) {
            objects[i] = hubMaster.elementAt(i);
            if (hub != null) hub.add(hubMaster.elementAt(i));
        }
    }
    
    public void selectNone() {
        if (hub != null) hub.clear();
        objects = new Object[0];
    }
    
    /************************** OAHtmlComponent ************************/
    protected void beforeSetValuesInternal() {
        objects = new Object[0];
    }
    protected void setValuesInternal(String nameUsed, String[] values) { // called by OAForm
        if (values == null) return;
        for (int i=0; i < values.length; i++) {
            if (values[i].equals("[null]")) return;
            if (values[i].equals("[all]")) {
                hubMaster.loadAllData();
                int x = hubMaster.getSize();
                objects = new Object[x];
                for (int j=0; j<x; j++) {
                    objects[j] = hubMaster.elementAt(j);
                }
                return;
            }
        }
        
        objects = new Object[values.length];
        for (int j=0; j<values.length; j++) {
            if (recursiveMethod != null) {
                Object obj=null;
                String slen = com.viaoa.html.Util.field(values[j],'_',1);
                int x = 0;
                try {
                    x = Integer.parseInt(slen);
                }
                catch (NumberFormatException e) { }
                String value = values[j].substring(slen.length() + 1);  // take off 1st field

                Hub hub = hubMaster; // starting hub
                for (int k=0; k<x; k++) {
                    int len = 0;
                    slen = com.viaoa.html.Util.field(value,'_',1);
                    try {
                        len = Integer.parseInt(slen);
                    }
                    catch (NumberFormatException e) { }

                    int x1 = slen.length()+1;
                    String id = value.substring(x1, x1+len);
                    if (k+1 != x) value = value.substring(slen.length()+1+len+1); // next id
                        
                    obj = Util.getObject(hub, id);
                    if (obj == null) break;
                    hub = (Hub) ClassModifier.getPropertyValue(obj, recursiveMethod);
                }
                objects[j] = obj;
            }
            else objects[j] = Util.getObject(hubMaster, values[j]);
        }                
    }
        
    public boolean isAllSelected() {
        initialize();
        return (objects.length == hubMaster.getSize());
    }
    public boolean isChanged() {
        initialize();
        if (hub.getSize() != objects.length) return true;
        for (int i=0; i<objects.length; i++) {
            if (hub.getObject(objects[i]) == null) return true;
        }
        return false;
    }
    
    public void reset() {
        selectNone();
        currentObject = null;
    }

    public void update() {
        initialize();
        if (hub == null || objects == null) return;
        hub.clear();
        Object[] objs = getValues();
        for (int i=0; i<objs.length; i++) {
            hub.addElement(objs[i]);
        }
    }

    /** returns the string needed for "option" tags.
        ex: 
        <select name="lstDept" multiple size="4"> <%=form.getOptions("lstDept")%> </select>
        NOTE: jsp tag must be last in select tag

    */
    public String getOptions() {
        StringBuffer sb = new StringBuffer(512);
        try {
            getValues(); // load objects
            if (hubMaster == null) return "OAList hubMaster not set";
            if (methodGet == null) {
                methodGet = ClassModifier.getMethods(hubMaster.getObjectClass(), propertyPath);
                if (methodGet == null) return "OAList methods for propertyPath \""+propertyPath+"\" not found";
            }

            if (recursiveProperty != null && recursiveMethod == null) {
                recursiveMethod = ClassModifier.getMethod(hubMaster.getObjectClass(), "get"+recursiveProperty );
                if (recursiveMethod == null) {
                    throw new RuntimeException("method for property \""+recursiveProperty + "\" class="+actualHub.getObjectClass());
                }
            }

            hubMaster.loadAllData();
            int x = hubMaster.getSize();

            if (allDescription != null) {
                String s = format(allDescription);
                sb.append("<option value=\"[all]\""+ (isAllSelected()?" selected":"") + ">"+s+"</option>");
            }
            if (nullDescription != null) {
                String s = format(nullDescription);
                sb.append("<option value=\"[null]\""+ (objects.length==0?" selected":"") + ">"+s+"</option>");
            }
            getOptions(hubMaster, sb, new String[0], isAllSelected());
        }
        catch (Exception e) {
            handleException(e,"getOptions()");
            return "<option value=\"error\" selected>Exception Occured</option>";
        }
        
        lastValue = new String(sb);
        return lastValue;
    }

    /** @param ids the objectIds for all parents if this is using a recursiveHub. */
    void getOptions(Hub hub, StringBuffer sb, String[] ids, boolean bAllFlag) {
        String s;
        hub.loadAllData();
        for (int i=0; ; i++) {
            Object obj = hub.elementAt(i);
            if (obj == null) break;
            
            String id = Util.getObjectIdAsString(hub, obj);
            s = ClassModifier.getPropertyValueAsString(obj, methodGet,getFormat());
            if (obj instanceof OAObject && ((OAObject)obj).isNull(propertyPath)) s = "";
            if (s == null) s = "";
            
            sb.append("<option value=\"");

            if (recursiveMethod != null) {
                // if recursive then id= #1_#2_id1_  where #1=number of ids, #2=length of id
                sb.append((ids.length+1) + "_");  // # of ids
                for (int j=0; j<ids.length; j++) {
                    sb.append(ids[j].length() + "_" + ids[j] + "_");
                }
                sb.append(id.length() + "_");
            }

            sb.append(id+"\"");

            // find out if item should be selected
            if (!bAllFlag) {
                for (int j=0; j<objects.length; j++) {
                    if (obj == objects[j]) {
                        sb.append(" selected");
                        break;
                    }
                }
            }
            
            sb.append(">");
            if (ids.length > 0) {
                sb.append("&nbsp;&nbsp;&nbsp;");
                for (int j=0; j<(ids.length-1); j++) sb.append("&nbsp;&nbsp;&nbsp;");
                sb.append("--&nbsp;");
            }
            s = com.viaoa.html.Util.toEscapeString(s);
            s = format(s);
            
            sb.append(s + "</option>");

            if (recursiveMethod != null) {
                Hub h = (Hub) ClassModifier.getPropertyValue(obj, recursiveMethod);
                String[] ss = new String[ids.length+1];
                System.arraycopy(ids, 0, ss, 0, ids.length);
                ss[ids.length] = id;
                if (h != null) getOptions(h, sb, ss, bAllFlag);
            }

        }
    }
    
    private String format(String s) {
        if (columns > 0) {
            int j = s.length();
            if (j > columns) s = s.substring(0,columns);
            else {
                for ( ;j<columns;j++) s += "&nbsp;";
            }
        }
        return s;
    }        

    private String lastValue;
    public boolean needsRefreshed() {
        String s = lastValue;
        String s2 = getOptions();
        return (s == null || !s.equals(s2));
    }

    public String getHtml(String htmlTags) {
// <select name="lstDept" multiple size="4"> <%=form.getHtmlOptions("lstDept")%> </select>
        String s = "";
        if (htmlBefore != null) s += htmlBefore;
        s += "<SELECT";
        s += " NAME=\""+name+"\"";
        if ( htmlTags != null) s += " "+htmlTags;
        if ( htmlBetween != null) s += " "+htmlBetween;
        
        s += " MULTIPLE";
        if ( (hub != null && !hub.isValid()) || !bEnabled || (form != null && form.getReadOnly()) ) s += " DISABLED";

        if (rows >= 0) s += " size=\""+rows+"\"";
        s += ">"+getOptions()+"</select>" ;
        if (htmlAfter != null) s += htmlAfter;
        return s;
    }
}


