package com.viaoa.jsp;

import java.awt.image.BufferedImage;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.hub.*;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.util.Base64;

/**
 * Allows for uploading and saving (as java jpg) using the Croppie + File input.
 * 
 * http://foliotek.github.io/Croppie/#documentation
 *
 * This uses an Html Label, or other component that supports onClick, to then
 * popup a file input and then croppie, and then uses ajax submit to save the 
 * image.
 * 
 *   <label id="cfiUpload" style="cursor: pointer;">Add a new photo</label>
 
    protected OACroppieFileInput cfi;
    public OACroppieFileInput getCroppieFileInput() {
        if (cfi != null) return cfi;
        cfi = new OACroppieFileInput("cfiUpload", modelEmployee.getHub(), EmployeePP.avatar().bytes()) {
            @Override
            protected void onSetValue(byte[] bs) {
                if (bs == null) return;
                Employee emp = modelEmployee.getEmployee();
                if (emp != null) {
                    // make sure that an Avatar exists
                    if (emp.getAvatar() == null) {
                        emp.setAvatar(new ImageStore());
                    }
                }
                super.onSetValue(bs);
                if (emp != null) emp.save();
            }
        };
        return cfi;
    }
 
 
 * @author vvia
 *
 */
public class OACroppieFileInput implements OAJspComponent, OAJspRequirementsInterface {
    private static final long serialVersionUID = 1L;

    protected Hub hub;
    protected String id;
    protected String property;
    
    protected OAForm form;
    protected String lastAjaxSent;
    
    public OACroppieFileInput(String id, Hub hub, String property) {
        this.id = id;
        this.hub = hub;
        this.property = property;
    }

    public Hub getHub() {
        return hub;
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
    public boolean _onFormSubmitted(HttpServletRequest req, HttpServletResponse resp, HashMap<String,String[]> hmNameValue) {
        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        boolean bWasSubmitted = (id != null && id.equals(s));

        if (bWasSubmitted && hmNameValue != null) {
            String[] ss = hmNameValue.get("oahidden"+id);
            if (ss != null && ss.length > 0 && ss[0] != null && ss[0].length() != 0) {
                s = ss[0];
                if (s.startsWith("data:image")) {
                    String s2 = ";base64,";
                    int pos = s.indexOf(s2);
                    if (pos > 0) s = s.substring(pos+s2.length());                            
                }

                char[] c = new char[s.length()];
                s.getChars(0, s.length(), c, 0);
                byte[] bs = Base64.decode(c);
                
                try {
                    BufferedImage bi = OAImageUtil.convertToBufferedImage(bs);
                    byte[] bs2 = OAImageUtil.convertToJavaJPG(bi);  // this is the format for storing images internally (ex: database)
                    bs = bs2;
                }
                catch (Exception e) {
                    throw new RuntimeException("exception while storing image", e);
                }
                onSetValue(bs);
            }
        }
        
        return bWasSubmitted;
    }

    /**
     * only called when the image for croppie is submitted (ajax)
     */
    protected void onSetValue(byte[] bs) {
        if (bs != null && hub != null && property != null) {
            Object objx = hub.getAO();
            if (objx instanceof OAObject) {
                ((OAObject) objx).setProperty(property, bs);
            }
        }
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
        return afterFormSubmitted(forwardUrl);
    }
    @Override
    public String afterFormSubmitted(String forwardUrl) {
        return forwardUrl;
    }

    
    @Override
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(1024);

        sb.append("\n");

        // Bind OACroppieFileInput to id='cfiUpload'
        
        // create hidden input for storing base64 img
        sb.append("$('form').prepend(\"<input id='oahiddencfiUpload' name='oahiddencfiUpload' type='hidden' value=''>\");\n");

        // create hidden fileInput to invoke have user select an image file
        sb.append("$('#cfiUpload').after(\"<input type='file' id='cfiUploadFileInput' name='cfiUploadFileInput' accept='image/*' style='visibility: hidden;'>\");\n");

        // listen for user selecting an image to then use croppie
        sb.append("$('#cfiUploadFileInput').on('change', function() {\n");
        sb.append("    if (!this.files || !this.files[0]) return;\n");
        sb.append("    var reader = new FileReader();\n");
        sb.append("    reader.onload = function(e) {\n");
        sb.append("        cfiUploadCroppie.croppie('bind', {\n");
        sb.append("            url : e.target.result\n");
        sb.append("        });\n");
        sb.append("    };\n");
        sb.append("    reader.readAsDataURL(this.files[0]);\n");
        sb.append("    this.value = null;\n");
        sb.append("    $('#cfiUploadDlg').dialog('open');\n");
        sb.append("});\n");
        
        // have mouse click invoke the hidden file input to have user select a file 
        sb.append("$('#cfiUpload').attr('for', 'cfiUploadFileInput');\n");

        // add modal dialog, with croppie component
        sb.append("$('#cfiUpload').after(\"<div id='cfiUploadDlg'><div id='cfiUploadCroppie'></div></div>\");\n");

        sb.append("$('#cfiUploadDlg').dialog({\n");
        sb.append("    autoOpen : false,\n");
        sb.append("    modal : true,\n");
        sb.append("    resizable : false,\n");
        sb.append("    draggable : false,\n");
        sb.append("    width : 500,\n");
        sb.append("    buttons : {\n");
        sb.append("        'Cancel' : function() {\n");
        sb.append("            $(this).dialog('close');\n");
        sb.append("        },\n");
        sb.append("        'Ok' : function() {\n");
        sb.append("            cfiUploadSubmit(this);\n");
        sb.append("            $(this).dialog('close');\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("});\n");
        // croppie
        sb.append("var cfiUploadCroppie = $('#cfiUploadCroppie').croppie({\n");
        sb.append("    viewport : {\n");
        sb.append("        width : 250,\n");
        sb.append("        height : 250,\n");
        sb.append("        type : 'circle'\n");
        sb.append("    },\n");
        sb.append("    boundary : {\n");
        sb.append("        width : 300,\n");
        sb.append("        height : 300\n");
        sb.append("    }\n");
        sb.append("});\n");

        sb.append("function cfiUploadSubmit() {\n");
        sb.append("    cfiUploadCroppie.croppie('result', {\n");
        sb.append("        type : 'base64',\n");
        sb.append("        format : 'jpg',\n");
        sb.append("        size : {\n");
        sb.append("            width : 240,\n");
        sb.append("            height : 240\n");
        sb.append("        }\n");
        sb.append("    }).then(function(resp) {\n");
        sb.append("        $('#oahiddencfiUpload').val(resp);\n");
        
        sb.append("        var args = $('form:eq(0)').serialize();\n");
        sb.append("        args = 'oacommand=cfiUpload&' + args;\n");
        sb.append("        $.ajax({\n");
        sb.append("            type : 'POST',\n");
        sb.append("            data : args,\n");
        sb.append("            url : 'oaajax.jsp',\n");
        sb.append("            success : function(data) {\n");
        sb.append("                if (data) eval(data);\n");
        sb.append("            },\n");
        sb.append("            dataType : 'text'\n");
        sb.append("        });\n");

        sb.append("    });\n");
        sb.append("}\n");
        
        
        String s = getAjaxScript();
        if (s != null) sb.append(s);
        
        String js = sb.toString();

        js = OAString.convert(js, "cfiUpload", getId());
        
        return js;
    }

    @Override
    public String getVerifyScript() {
        return null;
    }

    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);

        sb.append("$('#oahidden"+id+"').val('');");
        
        String js = sb.toString();
        return js;
    }

    
    @Override
    public void setEnabled(boolean b) {
    }

    @Override
    public boolean getEnabled() {
        return hub == null || hub.isValid();
    }

    @Override
    public void setVisible(boolean b) {
    }

    @Override
    public boolean getVisible() {
        return true;
    }

    @Override
    public String getForwardUrl() {
        return null;
    }


    public String[] getRequiredJsNames() {
        ArrayList<String> al = new ArrayList<>();

        al.add(OAJspDelegate.JS_jquery);
        al.add(OAJspDelegate.JS_croppie);

        String[] ss = new String[al.size()];
        return al.toArray(ss);
    }

    @Override
    public String[] getRequiredCssNames() {
        ArrayList<String> al = new ArrayList<>();

        al.add(OAJspDelegate.CSS_bootstrap);
        al.add(OAJspDelegate.CSS_croppie);

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
