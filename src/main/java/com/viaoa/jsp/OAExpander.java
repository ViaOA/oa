/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
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
import javax.servlet.http.*;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;


/**
 * Expand/Collapse (show/hide) an element by clicking on another element.
 * Also allows changing the displayed text for expanding/collapsing. 
 * @author vvia
 */
public class OAExpander implements OAJspComponent, OAJspRequirementsInterface {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String clickId;
    protected OAForm form;
    protected boolean bVisible;
    protected String expandText, collapseText;
    protected String toolTip;
    protected OATemplate templateToolTip;
    private boolean bHadToolTip;
    protected Hub hub;

    /**
     * @param id
     * @param clickId component that will toggle expand/collaps
     * @param expandText name to display when expanded
     * @param collapseText name to display when collapsed
     */
    public OAExpander(String id, String clickId, String expandText, String collapseText) {
        this.id = id;
        this.clickId = clickId;
        this.expandText = expandText;
        this.collapseText = collapseText;
    }

    public void setHub(Hub hub) {
        this.hub = hub;
    }
    
    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void reset() {
        setVisible(false);
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


    @Override
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        return false;
    }

    @Override
    public String _onSubmit(String forwardUrl) {
        return onSubmit(forwardUrl);
    }
    
    @Override
    public String onSubmit(String forwardUrl) {
        return forwardUrl;
    }
    
    @Override
    public String _afterFormSubmitted(String forwardUrl) {
        return forwardUrl;
    }

    @Override
    public String getScript() {
        lastAjaxSent = null;
        bHadToolTip = false;

        StringBuilder sb = new StringBuilder(2048);
        
        String css = "";
        
        if (OAString.isNotEmpty(clickId)) {
            sb.append("$('#"+clickId+"').click(function() {\n");
            sb.append("  if ($('#"+id+"').is(':visible')) {\n");
            sb.append("    $('#"+id+"').hide('slide', {direction: 'up'}, 325);\n");
            if (OAString.isNotEmpty(expandText)) {
                sb.append("    $('#"+clickId+"').html('"+expandText+"');\n");
            }
            sb.append("  }\n");
            sb.append("  else {\n");
            sb.append("    $('#"+id+"').show('slide', {direction: 'up'}, 325);\n");
            if (OAString.isNotEmpty(collapseText)) {
                sb.append("    $('#"+clickId+"').html('"+collapseText+"');\n");
            }
            sb.append("  }\n");
            
            sb.append("  return false;\n");
            
            sb.append("});\n");
        }
        
        String js = sb.toString();
        
        String s = getAjaxScript();
        if (s != null) js += s;
        
        return js;
    }
    
    @Override
    public String getVerifyScript() {
        return null;
    }

    private String lastAjaxSent;
    
    @Override
    public String getAjaxScript() {
        
        StringBuilder sb = new StringBuilder(256);
        
        if (getVisible()) {
            sb.append("if (!$('#"+id+"').is(':visible')) {");
            sb.append("   $('#"+id+"').show('slide', {direction: 'top'}, 325);\n");
            if (OAString.isNotEmpty(clickId)) {
                if (OAString.isNotEmpty(collapseText)) {
                    sb.append("   $('#"+clickId+"').html('"+collapseText+");\n");
                }
            }
            sb.append("}\n");
        }
        else {
            sb.append("if ($('#"+id+"').is(':visible')) {");
            sb.append("  $('#"+id+"').hide('slide', {direction: 'top'}, 325);\n");
            if (OAString.isNotEmpty(clickId)) {
                if (OAString.isNotEmpty(expandText)) {
                    sb.append("   $('#"+clickId+"').html('"+expandText+");\n");
                }
            }
            sb.append("}\n");
        }

        // tooltip
        String prefix = null;
        String tt = getProcessedToolTip();
        if (OAString.isNotEmpty(tt)) {
            tt = OAString.convertForSingleQuotes(tt);
            if (!bHadToolTip) {
                bHadToolTip = true;
                prefix = "$('#"+id+"').tooltip();\n";
            }
            
            sb.append("$('#"+id+"').data('bs.tooltip').options.title = '"+tt+"';\n");
            sb.append("$('#"+id+"').data('bs.tooltip').options.placement = 'top';\n");
        }
        else {
            if (bHadToolTip) {
                sb.append("$('#"+id+"').tooltip('destroy');\n");
                bHadToolTip = false;
            }
        }
        
        String js = sb.toString();
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        if (prefix != null) {
            js = prefix + OAString.notNull(js);
        }
        
        return null;
    }

    @Override
    public void setEnabled(boolean b) {
    }
    @Override
    public boolean getEnabled() {
        return true;
    }


    @Override
    public void setVisible(boolean b) {
        lastAjaxSent = null;  
        this.bVisible = b;
    }
    @Override
    public boolean getVisible() {
        return this.bVisible;
    }


    @Override
    public String getForwardUrl() {
        return null;
    }

    public void setToolTip(String tooltip) {
        this.toolTip = tooltip;
        templateToolTip = null;
    }
    public String getToolTip() {
        return this.toolTip;
    }
    public String getProcessedToolTip() {
        if (OAString.isEmpty(toolTip)) return toolTip;
        if (templateToolTip == null) {
            templateToolTip = new OATemplate();
            templateToolTip.setTemplate(getToolTip());
        }
        OAObject obj = null;
        if (hub != null) {
            Object objx = hub.getAO();
            if (objx instanceof OAObject) obj = (OAObject) objx;
        }
        String s = templateToolTip.process(obj, hub, null);
        return s;
    }


    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();
        al.add(OAJspDelegate.JS_jquery);

        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.JS_bootstrap);
        }

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

        if (OAString.isNotEmpty(getToolTip())) {
            al.add(OAJspDelegate.CSS_bootstrap);
        }

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
