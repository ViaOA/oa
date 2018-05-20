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

import com.viaoa.hub.Hub;

// attr: WRAP={"HARD","SOFT","OFF"}

/**
 * TextArea component that can bind html textarea to OA.
 * @author vvia
 *
 */
public class OATextArea extends OATextField {
    private static final long serialVersionUID = 1L;

    private int rows;
    
    public OATextArea(String id, Hub hub, String propertyPath, int columns, int rows, int maxWidth) {
        super(id, hub, propertyPath, columns, maxWidth);
        this.rows = rows;
    }
    public OATextArea(Hub hub, String propertyPath, int columns, int rows, int maxWidth) {
        super(hub, propertyPath, columns, maxWidth);
        this.rows = rows;
    }

    public OATextArea(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath, 0, 0);
    }
    public OATextArea(Hub hub, String propertyPath) {
        super(hub, propertyPath, 0, 0);
    }

    public OATextArea(String id, int columns, int rows, int maxWidth) {
        super(id, null, null, columns, maxWidth);
        this.rows = rows;
    }
    public OATextArea(int columns, int rows, int maxWidth) {
        super(null, null, columns, maxWidth);
        this.rows = rows;
    }

    public OATextArea(String id) {
        super(id, null, null, 0, 0);
    }
    public OATextArea() {
        super(null, null, null, 0, 0);
    }
    
    @Override
    public String getScript() {
        String js = super.getScript();

        if (width > 0) js += ("$('#"+id+"').attr('cols', '"+width+"');\n");
        if (getRows() > 0) js += ("$('#"+id+"').attr('rows', '"+getRows()+"');\n");
        
        int max = getMaxWidth();
        if (max > 30) {
            StringBuilder sb = new StringBuilder(300);
            sb.append("$('#"+getId()+"').wrap('<div id=\""+getId()+"Wrap\" class=\"oaTextAreaWrap\">');\n");
            
            
            sb.append("$('#"+getId()+"').after('<div><span id=\""+getId()+"Message\" class=\"oaTextAreaMessage\"></span></div>');\n");
            //was:  sb.append("$('#"+getId()+"').after('<div><span id=\""+getId()+"z\" style=\"padding: 2px 15px; font-style: italic;font-size:9px;\"></span></div>');\n");
            
            
            sb.append("$('#"+getId()+"').blur(function() {\n");
            sb.append("    $('#"+getId()+"Message').css('visibility', 'hidden');\n");
            //sb.append("    $('#"+getId()+"Message').hide();\n");
            sb.append("});\n");
            sb.append("$('#"+getId()+"').focus(function() {\n");
            sb.append("    $('#"+getId()+"Message').css('visibility', 'visible');\n");
            // sb.append("    $('#"+getId()+"Message').show();\n");
            sb.append("});\n");
            sb.append("$('#"+getId()+"').bind('propertychange change keyup paste input', function() {\n");
            sb.append("    var text = $(this).val();\n");
            sb.append("    $('#"+getId()+"Message').html(\" \"+text.length+\" out of "+max+"\");\n");
            sb.append("});\n");
            js += sb.toString();
        }
        
        return js;
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

}
