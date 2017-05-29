/*  Copyright 1999-2017 Vince Via vince@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.jsp;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.html.Util;
import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

/**
 * 20170528 copied from OACombo ... currently, a work in progress to handle a recursive hub
 *      will be making it like jfc OATree, with nodes, etc qqqqqq
 *      
 *  see: https://github.com/jonmiles/bootstrap-treeview 
 *       
 *       
 * @author vvia
 */
public class OATree implements OAJspComponent, OATableEditor {
    private static final long serialVersionUID = 1L;

    private Hub hub;
    protected OALinkInfo recursiveLinkInfo;
    
    protected String id;
    protected String propertyPath;
    private OAForm form;
    private boolean bEnabled = true;
    private boolean bVisible = true;
    private boolean bAjaxSubmit, bSubmit;
    private String name;
    private String forwardUrl;

    private OAObject selectedObject;
    
    public OATree(String id, Hub hub, String propertyPath) {
        this.id = id;
        this.hub = hub;
        this.propertyPath = propertyPath;
        recursiveLinkInfo = OAObjectInfoDelegate.getRecursiveLinkInfo(OAObjectInfoDelegate.getObjectInfo(hub.getObjectClass()), OALinkInfo.MANY);
    }

    public void setPropertyPath(String pp) {
        this.propertyPath = pp;
    }
    public String getPropertyPath() {
        return this.propertyPath;
    }
    
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    public Hub getHub() {
        return hub;
    }
    public void setHub(Hub hub) {
        this.hub = hub;
    }
    
    @Override
    public void reset() {
    }

    @Override
    public void setForm(OAForm form) {
        this.form = form;
    }
    @Override
    public OAForm getForm() {
        return this.form;
    }

    @Override
    public boolean _beforeSubmit() {
        return true;
    }

    
    private boolean bRefresh;
    // resend the page on the next getAjaxScript
    public void refresh() {
        bRefresh = true;
    }
    
    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }

        boolean b = (id != null && id.equals(s));
        if (!b) return false;

        if (hmNameValue == null) return false;
        
        String[] values = hmNameValue.get("oatree"+id);
        if (values == null || values.length == 0) return false;

        selectedObject = null;
        
        Hub h = hub;
        for (int i=0; ;i++) {
            s = OAString.field(values[0], ".", i+1);
            if (s == null) break;
            int x = OAConv.toInt(s);
            selectedObject = (OAObject) h.getAt(x);

            if (recursiveLinkInfo == null) break;
            h = (Hub) recursiveLinkInfo.getValue(selectedObject);
        }
        return true;
    }

    
    @Override
    public String _afterSubmit(String forwardUrl) {
        return forwardUrl;
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }
    
    public void setAjaxSubmit(boolean b) {
        bAjaxSubmit = b;
    }
    public boolean getAjaxSubmit() {
        return bAjaxSubmit;
    }
    public void setSubmit(boolean b) {
        bSubmit = b;
    }
    public boolean getSubmit() {
        return bSubmit;
    }

    
    
    @Override
    public String getScript() {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("$('form').prepend(\"<input id='oatree"+id+"' type='hidden' name='oatree"+id+"' value=''>\");\n");
        // sb.append("$('#oatree"+id+"').val('');");

        sb.append("$('#"+id+"').treeview({ levels: 1, showBorder: false, data: [\n");
        sb.append(getData(hub, null)+"\n");
        sb.append("],\nonNodeSelected : function(event, node) {\n");

        
        sb.append("$('#oacommand').val('"+id+"');\n");
        sb.append("$('#oatree"+id+"').val(node.oaid);\n");
        
        if (bAjaxSubmit) {
            sb.append("ajaxSubmit();return false;\n");
        }
        else if (getSubmit()) {
            sb.append("$('form').submit();return false;\n");
        }
        
        sb.append("}\n");
        sb.append("});\n");
        
        String js = sb.toString();
        return js;
    }

    @Override
    public String getVerifyScript() {
        return null;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    
    @Override
    public String getAjaxScript() {
        if (!bRefresh) return null;
        
        bRefresh = false;
        StringBuilder sb = new StringBuilder(1024);
        // sb.append("$('#"+id+"').treeview('collapseAll');");
        sb.append("$('#"+id+"').treeview('unSelectAll');");
        
        return sb.toString();
    }

    
    /** 
     * this is called to render each node's text.
    */
    protected String getText(int pos, Object object, String text) {
        return text;
    }
    
    protected String format;
    private boolean bDefaultFormat=true;
    
    public void setFormat(String fmt) {
        this.format = fmt;
        bDefaultFormat = false;
    }
    public String getFormat() {
        if (format == null && bDefaultFormat && hub != null) {
            bDefaultFormat = false;
            OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), propertyPath);
            if (pp != null) format = pp.getFormat();
        }
        return format;
    }
    
    protected String getData(Hub hubx, final String objId) {
        if (hubx == null) {
            return "";
        }
        String options = "";
        for (int i=0; ;i++) {
            Object obj = hubx.getAt(i);
            if (obj == null) break;

            if (i > 0) options += ",";
            options += "{";
            
            String value = null;
            if (obj instanceof OAObject) {
                value = ((OAObject) obj).getPropertyAsString(propertyPath, getFormat());
            }
            else {
                value = OAConv.toString(obj, getFormat());
            }
            if (value == null) value = "";
            
            value = getText(i, obj, value);
            value = Util.convert(value, "\'", "\\' ");

            String sid;
            if (objId != null) sid = objId+"."+i;
            else sid = ""+i;
            
            options += "text: '"+value+"', oaid: '"+sid+"'";
            
            if (recursiveLinkInfo != null) {
                Hub h = (Hub) recursiveLinkInfo.getValue(obj);
                if (h != null && h.size() > 0) {
                    options += ", nodes: [";
                    options += getData(h, sid); 
                    options += "]";
                }
            }
            options += "}";
        }
        return options;
    }
    
    @Override
    public void setEnabled(boolean b) {
        this.bEnabled = b;
    }
    @Override
    public boolean getEnabled() {
        return this.bEnabled;
    }

    @Override
    public void setVisible(boolean b) {
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        return bVisible;
    }

    @Override
    public String getTableEditorHtml() {
        return null;
    }

    public OAObject getSelectedObject() {
        return this.selectedObject;
    }

    @Override
    public String getForwardUrl() {
        return null;
    }
    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
}
