package com.viaoa.jsp;


import com.viaoa.html.Util;
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
    public OATextArea(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath, 0, 0);
    }
    public OATextArea(String id, int columns, int rows, int maxWidth) {
        super(id, null, null, columns, maxWidth);
        this.rows = rows;
    }
    public OATextArea(String id) {
        super(id, null, null, 0, 0);
    }
    
    protected String getTextJavaScript() {
        String js = super.getTextJavaScript();

        if (width > 0) js += ("$('#"+id+"').attr('cols', '"+width+"');\n");
        if (getRows() > 0) js += ("$('#"+id+"').attr('rows', '"+getRows()+"');\n");
        
        int max = getMaxWidth();
        if (max > 30) {
            StringBuilder sb = new StringBuilder(300);
            sb.append("$('#"+getId()+"').wrap('<div style=\"display: inline-block;\">');\n");
            sb.append("$('#"+getId()+"').after('<div><span id=\""+getId()+"z\" style=\"padding: 2px 15px; font-style: italic;font-size:9px;\"></span></div>');\n");
            sb.append("$('#"+getId()+"').blur(function() {\n");
            sb.append("    $('#"+getId()+"z').hide();\n");
            sb.append("});\n");
            sb.append("$('#"+getId()+"').focus(function() {\n");
            sb.append("    $('#"+getId()+"z').show();\n");
            sb.append("});\n");
            sb.append("$('#"+getId()+"').bind('input propertychange', function() {\n");
            sb.append("    var text = $(this).val();\n");
            sb.append("    $('#"+getId()+"z').html(\" \"+text.length+\" out of "+max+"\");\n");
            sb.append("});\n");
            js += sb.toString();
        }
        
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

}
