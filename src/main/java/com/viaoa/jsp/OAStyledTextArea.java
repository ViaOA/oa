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


import java.util.ArrayList;
import java.util.Arrays;

import com.viaoa.html.Util;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OATypeAhead;

/**
 * Styled TextArea component that can bind html textarea to OA using summernote js component.
 * 
 * http://summernote.org/deep-dive/
 * 
 * @author vvia
 *
 */
public class OAStyledTextArea extends OATextField {
    private static final long serialVersionUID = 1L;

    private int rows;
    
    public OAStyledTextArea(String id, Hub hub, String propertyPath, int rows) {
        super(id, hub, propertyPath);
        this.rows = rows;
    }
    public OAStyledTextArea(String id, Hub hub, String propertyPath, int columns, int rows) {
        super(id, hub, propertyPath);
        this.width = columns;
        this.rows = rows;
    }
    public OAStyledTextArea(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath, 0, 0);
    }
    public OAStyledTextArea(String id, int columns, int rows) {
        super(id, null, null);
        this.width = columns;
        this.rows = rows;
    }
    public OAStyledTextArea(String id, int rows) {
        super(id, null, null);
        this.rows = rows;
    }
    public OAStyledTextArea(String id) {
        super(id, null, null, 0, 0);
    }
    
    
    @Override
    public String getScript() {
        StringBuilder sb = new StringBuilder(512);

        /*
        if (width > 0) sb.append("$('#"+id+"').css('width', '"+width+"em');\n");
        if (getRows() > 0) sb.append("$('#"+id+"').css('height', '"+getRows()+"em');\n");
        */
        sb.append("$('#"+getId()+"').summernote({\n");
        
        if (rows > 0) sb.append("height: "+(rows*12)+",\n");
        // sb.append("minHeight: null,\n");
        //sb.append("maxHeight: null,\n");
        if (width > 0) sb.append("width: "+(width*10)+",\n");
        
        sb.append("toolbar: [\n");
        sb.append("   // [groupName, [list of button]]\n");
        sb.append("   ['style', ['bold', 'italic', 'underline', 'clear']],\n");
        sb.append("   ['font', ['strikethrough', 'superscript', 'subscript']],\n");
        sb.append("   ['fontsize', ['fontsize']],\n");
        sb.append("   ['color', ['color']],\n");
        sb.append("   ['para', ['ul', 'ol', 'paragraph']],\n");
        sb.append("   ['insert', ['table', 'hr']],\n");
        sb.append("   ['view', ['fullscreen', 'codeview', 'help']],\n");
        sb.append("   ['height', ['height']]\n");
        sb.append(" ]\n");
        sb.append("});\n");
        
        String js = super.getScript();
        sb.append(js);
        
        /*
        int max = getMaxWidth();
        if (max > 30) {
            sb.append("$('#"+getId()+"').wrap('<div id=\""+getId()+"Wrap\" class=\"oaTextAreaWrap\">');\n");
            
            sb.append("$('#"+getId()+"').after('<div><span id=\""+getId()+"Message\" class=\"oaTextAreaMessage\">Xxxx</span></div>');\n");

            sb.append("$('#"+getId()+"').on('summernote.keyup', function(we, e) {\n");
            sb.append("    var text = $(this).summernote('code');\n");
            sb.append("    $('#"+getId()+"Message').html(\" \"+text.length+\" out of "+max+"\");\n");
            sb.append("});\n");

            sb.append("$('#"+getId()+"').on('summernote.paste', function(we, e) {\n");
            sb.append("    var text = $(this).summernote('code');\n");
            sb.append("    $('#"+getId()+"Message').html(\" \"+text.length+\" out of "+max+"\");\n");
            sb.append("});\n");
            
        }
        */
        js = sb.toString();
        return js;
    }


    @Override
    protected String getTextJavaScript() {
        String js = super.getTextJavaScript();
        if (js == null) js = "";

        String val = null;
        if (hub != null) {
            OAObject obj = (OAObject) hub.getAO();
            if (obj != null) {
                val = obj.getPropertyAsString(propertyPath);
                val = convertValue(val);
            }
        }        
        else val = getValue();
        if (val == null) val = "";
        
        js += "$('#"+getId()+"').summernote('code', '"+val+"');\n";
        return js;
    }

    protected String convertValue(String value) {
        value = Util.convert(value, "\r\n", "\\n");
        value = Util.convert(value, "\n", "\\n");
        value = Util.convert(value, "\r", "\\n");
        value = Util.convert(value, "'", "\\'");
        return value;
    }

    public int getColumns() {
        return width;
    }
    public void setColumns(int cols) {
        this.width = cols;
    }
    
    public int getRows() {
        return rows;
    }
    /** number of rows.  */
    public void setRows(int x) {
        rows = x;
    }

    @Override
    public void setTypeAhead(OATypeAhead ta) {
        throw new RuntimeException("TypeAhead is not supported for styled textarea");
    }
    
    protected String getEnabledScript(boolean b) {
        if (b) return ("$('#" + id + "').summernote('enable');\n");
        return ("$('#" + id + "').summernote('disable');\n");
    }
    protected String getVisibleScript(boolean b) {
        return null;
    }
    
    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList(Arrays.asList(super.getRequiredCssNames()));
        al.add(OAJspDelegate.CSS_summernote);
        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }
    
    @Override
    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList(Arrays.asList(super.getRequiredJsNames()));
        al.add(OAJspDelegate.JS_summernote);
        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }    
    
    
}
