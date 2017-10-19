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

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

/**
 * 20170528 copied from OACombo ... currently, a work in progress to handle a recursive hub
 *      will be making it like jfc OATree, with nodes, etc qqqqqq
 *
 *      
 *  see: https://github.com/jonmiles/bootstrap-treeview
 *       https://github.com/jonmiles/bootstrap-treeview 
 *       
 *  <script type="text/javascript" language="javascript" src="vendor/bootstrap-treeview.js"></script>
 *  
 * @author vvia
 */
public class OATree implements OAJspComponent, OATableEditor, OAJspRequirementsInterface {
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
    private String sortBy;
    private OAFilter filter;
    private HashMap<Integer, OAObject> hashMap;
    private HashSet<OAObject> hsExpanded;
    private String treeViewParams;
   
    private Object selectedObject;
    
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
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    public String getSortBy() {
        return this.sortBy;
    }
    public void setFilter(OAFilter filter) {
        this.filter = filter;
    }
    public OAFilter getFilter() {
        return filter;
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
    public boolean _beforeFormSubmitted() {
        return true;
    }

    
    private boolean bRefresh;
    // resend the page on the next getAjaxScript
    public void refresh() {
        bRefresh = true;
    }
    
    public void collapseAll() {
        hsExpanded = null;
        refresh();
    }
    
    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
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
        
        boolean bWasSelected = false;
        if (hashMap != null && values[0].length() > 1) {
            if (values[0].startsWith("select.g")) {
                int guid = OAConv.toInt(values[0].substring(8));
                selectedObject = hashMap.get(guid);
                bWasSelected = true;
            }
            else if (values[0].startsWith("unselect.g")) {
                int guid = OAConv.toInt(values[0].substring(10));
                if (selectedObject == hashMap.get(guid)) selectedObject = null;
                bWasSelected = true;
            }
            else if (values[0].startsWith("expand.g")) {
                int guid = OAConv.toInt(values[0].substring(8));
                OAObject obj = hashMap.get(guid);
                if (obj != null) {
                    if (hsExpanded == null) hsExpanded = new HashSet<>();
                    hsExpanded.add(obj);
                }
            }
            else if (values[0].startsWith("collapse.g")) {
                int guid = OAConv.toInt(values[0].substring(10));
                OAObject obj = hashMap.get(guid);
                if (obj != null) {
                    if (hsExpanded != null) hsExpanded.remove(obj);
                }
            }
        }
        else if (OAString.isNumber(values[0]) && hub != null) {
            int x = OAConv.toInt(values[0]);
            selectedObject = hub.getAt(x);
            bWasSelected = true;
        }
        
        /*
        Hub h = hub;
        for (int i=0; ;i++) {
            s = OAString.field(values[0], ".", i+1);
            if (s == null) break;
            int x = OAConv.toInt(s);
            selectedObject = (OAObject) h.getAt(x);

            if (recursiveLinkInfo == null) break;
            h = (Hub) recursiveLinkInfo.getValue(selectedObject);
        }
        */
        return bWasSelected;
    }

    
    @Override
    public String _afterFormSubmitted(String forwardUrl) {
        return afterFormSubmitted(forwardUrl);
    }
    @Override
    public String afterFormSubmitted(String forwardUrl) {
        return forwardUrl;
    }
    
    @Override
    public String _onSubmit(String forwardUrl) {
        return onSubmit(forwardUrl);
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

    /** 
     * 
     * see: https://github.com/jonmiles/bootstrap-treeview
     * 
     * ex:  "showBorder: false, selectedColor: 'black', selectedBackColor: 'white', showTags: true ", 

     */
    public void setTreeViewParams(String s) {
        treeViewParams = s;
    }
    
    
    @Override
    public String getScript() {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("$('form').prepend(\"<input id='oatree"+id+"' type='hidden' name='oatree"+id+"' value=''>\");\n");
        // sb.append("$('#oatree"+id+"').val('');");
        
        sb.append(getScript2());
        String js = sb.toString();
        return js;
    }
        
    public String getScript2() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("$('#"+id+"').treeview({ levels: 1");
        if (OAString.isNotEmpty(treeViewParams)) {
            sb.append(", "+treeViewParams);
        }
        sb.append(", data: [\n");
        sb.append(getData(hub)+"\n");
        sb.append("]\n");

        sb.append(",onNodeSelected : function(event, node) {\n");
        sb.append("$('#oacommand').val('"+id+"');\n");
        sb.append("$('#oatree"+id+"').val('select.'+node.oaid);\n");
        if (bAjaxSubmit) {
            sb.append("ajaxSubmit();return false;\n");
        }
        else if (getSubmit()) {
            sb.append("$('form').submit();return false;\n");
        }
        sb.append("}\n");  //end of onNodeSelected

/* qqqqqqqqqqqqqqqqq causing issues when another is selected qqqqqqqqqqqqq
fork treeview.js and add changes so that unselected event can know if there is a new new selected node or not         
        sb.append(",onNodeUnselected : function(event, node) {\n");
       
        sb.append("$('#oacommand').val('"+id+"');\n");
        sb.append("$('#oatree"+id+"').val('unselect.'+node.oaid);\n");
        if (bAjaxSubmit) {
            sb.append("ajaxSubmit();return false;\n");
        }
        else if (getSubmit()) {
            sb.append("$('form').submit();return false;\n");
        }
        sb.append("}\n");  //end of onNodeUnselected
*/        
        
        
        /* 20171019 removed, event not needed since nothing is selected and the nodes are already loaded on the browser
        sb.append(",\nonNodeExpanded : function(event, node) {\n");
        sb.append("$('#oacommand').val('"+id+"');\n");
        sb.append("$('#oatree"+id+"').val('expand.'+node.oaid);\n");
        sb.append("ajaxSubmit2();return false;\n");
        sb.append("}\n");

        sb.append(",\nonNodeCollapsed : function(event, node) {\n");
        sb.append("$('#oacommand').val('"+id+"');\n");
        sb.append("$('#oatree"+id+"').val('collapse.'+node.oaid);\n");
        sb.append("ajaxSubmit2();return false;\n");
        sb.append("}\n");
       */
        
        sb.append("});\n");  // end of treeview
        
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
        // sb.append("$('#"+id+"').treeview('unSelectAll');");
        
  //      sb.append("$('#"+id+"').remove();");

        sb.append(getScript2());
        String js = sb.toString();
        return js;
    }

    
    /** 
     * this is called to render each node's text.
    */
    protected String getText(int pos, Object object, String text) {
        return text;
    }
    
    /**
     * 
     * shows a badge to on the right of the node.
     * requires setTreeViewParams("showTags: true")
     * 
     * Return text tag for a node.
     */
    public String getTag(int pos, Object obj) {
        return null;        
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

    protected String getData(Hub hub) {
        if (hub == null) {
            return "";
        }
        hashMap = null;
        if (hub.isOAObject()) {
            hashMap = new HashMap<>();
        }
        return _getData(hub);
    }
    
    protected String _getData(Hub hubx) {
        if (hubx == null) {
            return "";
        }
        
        Object[] objs = hubx.toArray();

        if (sortBy != null && hubx.isOAObject()) {
            final OAPropertyPath pp = new OAPropertyPath(hubx.getObjectClass(), sortBy);
            Arrays.sort(objs, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 == o2) return 0;
                    if (o1 == null) return -1;
                    if (o2 == null) return 1;
                    
                    Object x1 = pp.getValue(o1);
                    Object x2 = pp.getValue(o2);
                    
                    int x = OACompare.compare(x1, x2);
                    return x;
                }
            });
        }
        
        String options = "";
        int cnt = 0;
        for (int i=0; i < objs.length; i++) {
            Object obj = objs[i];
            /*
            Object obj = hubx.getAt(i);
            if (obj == null) break;
            */
            if (filter != null) {
                if (!filter.isUsed(obj)) {
                    continue;
                }
            }

            if (cnt++ > 0) options += ",";
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
            value = OAJspUtil.createJsString(value, '\"');
            value = OAString.convert(value, "\'", "\\\'");

            if (hashMap != null) {
                options += "text: '"+value+"', oaid: 'g"+OAObjectDelegate.getGuid((OAObject)obj)+"'";
                String s = getTag(i, obj);
                if (OAString.isNotEmpty(s)) {
                    s = OAJspUtil.createJsString(s, '\"');            
                    s = OAString.convert(s, "\'", "\\\'");
                    // must be in format "['a', 'b', ..]"
                    options += ", tags: ['"+s+"']";
                }
                hashMap.put(OAObjectDelegate.getGuid((OAObject)obj), (OAObject) obj);
            }
            else {
                options += "text: '"+value+"', oaid: '"+i+"'";
            }
            
            
            String s = "";
            if (hsExpanded !=null && hsExpanded.contains((OAObject) obj)) {
                s = "expanded: true";
            }
            
            if (obj == selectedObject) {
                if (s.length() > 0) s += ", ";
                s += "selected: true";
            }
            if (s.length() > 0) {
                options += ", state: {"+s+"}";
            }
            
            if (recursiveLinkInfo != null) {
                Hub h = (Hub) recursiveLinkInfo.getValue(obj);
                if (h != null && h.size() > 0) {
                    options += ", nodes: [";
                    options += _getData(h); 
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

    public Object getSelectedObject() {
        return this.selectedObject;
    }

    @Override
    public String getForwardUrl() {
        return null;
    }
    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }

    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();
        al.add(OAJspDelegate.JS_jquery);

        al.add(OAJspDelegate.JS_bootstrap);
        al.add(OAJspDelegate.JS_bootstrap_treeview);
        
        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

        al.add(OAJspDelegate.CSS_bootstrap);
        al.add(OAJspDelegate.CSS_bootstrap_treeview);
        
        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String getEditorHtml(OAObject obj) {
        return null;
    }

    @Override
    public String getRenderHtml(OAObject obj) {
        return null;
    }

    @Override
    public void _beforeOnSubmit() {
    }
}
