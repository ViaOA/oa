/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jsp;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viaoa.util.OAString;

/**
 * Create a dialog out of Html element.
 * @author vvia
 */
public class OADialog extends OAHtmlElement {
    protected boolean bModal;
    protected Dimension dim, dimMin, dimMax;
    protected String title;
    
    // list of button names. If selected, then the name/text will be set when onSubmit is called.
    private ArrayList<String> alButtons = new ArrayList<String>();  
    
    public OADialog(String id) {
        bVisible = false;
        this.id = id;
    }
    public void setModal(boolean b) {
        bModal = b;
    }
    public boolean getModal() {
        return bModal;
    }

    public void hide() {
        bVisible = false;
    }
    public void show() {
        bVisible = true;
    }

    private String submitButtonText;
    @Override
    public boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hmNameValue) {
        submitButtonText = null;
        bWasSubmitted = false;

        String s = req.getParameter("oacommand");
        if (s == null && hmNameValue != null) {
            String[] ss = hmNameValue.get("oacommand");
            if (ss != null && ss.length > 0) s = ss[0];
        }
        
        bWasSubmitted  = (s != null && s.startsWith(id+" "));
        if (bWasSubmitted) {
            submitButtonText = s.substring(id.length()+1);
        }
        return bWasSubmitted; // true if this caused the form submit
    }

    @Override
    public String _afterSubmit(String forwardUrl) {
        if (bWasSubmitted) {
            String furl = getForwardUrl();
            if (furl != null) forwardUrl = furl;
            return onSubmit(forwardUrl, submitButtonText); 
        }
        return null;
    }

    public String onSubmit(String forwardUrl, String submitButtonText) {
        return forwardUrl;
    }
    
    public Dimension getDimension() {
        return dim;
    }
    public void setDimension(Dimension d) {
        this.dim = d;
    }
    public Dimension getMinDimension() {
        return dimMin;
    }
    public void setMinDimension(Dimension d) {
        this.dimMax = d;
    }
    public Dimension getMaxDimension() {
        return dimMax;
    }
    public void setMaxDimension(Dimension d) {
        this.dimMax = d;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    protected String closeButtonText;
    public void setCloseButtonText(String text) {
        this.closeButtonText = text;
    }
    public String getCloseButtonText() {
        return this.closeButtonText;
    }
    
    
    @Override
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(1024);

        
        sb.append("$('#"+id+"').dialog({autoOpen: false, modal: "+bModal);
        sb.append(", closeOnEscape: true, resizable: true");

        if (dim != null) {
            sb.append(", width: "+dim.width);
            sb.append(", height: "+dim.height);
        }
        if (dimMin != null) {
            sb.append(", minWidth: "+dimMin.width);
            sb.append(", minHeight: "+dimMin.height);
        }
        if (dimMax != null) {
            sb.append(", maxWidth: "+dimMax.width);
            sb.append(", maxHeight: "+dimMax.height);
        }

        String s = getCloseButtonText();
        if (!OAString.isEmpty(s)) {
            sb.append(", closeText: '"+s+"'");
        }

        if (alButtons.size() > 0) {
            sb.append(", buttons: [");
            int i=0;
            for (String text : alButtons) {
                if (i++ > 0) sb.append(", ");
                sb.append("{text: '"+text+"', click: function() { $(this).dialog('close'); ");
                
                if (bAjaxSubmit) {
                    sb.append("$('#oacommand').val('"+id+" "+text+"');ajaxSubmit();");
                }
                else if (bSubmit) {
                    sb.append("$('#oacommand').val('"+id+" "+text+"'); $('form').submit();");
                }
                sb.append("}}");
            }
            sb.append("]");
        }
        
        // end of constructor
        sb.append("});\n");
        
        
        s = getAjaxScript();
        if (s != null) sb.append(s);
        String js = sb.toString();
        
        return js;
    }

    public void addButton(String text) {
        alButtons.add(text);
    }
    
    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);
        
        if (bVisible) {
            sb.append("$('#"+id+"').dialog('open');\n");
            bVisible = false;
            lastAjaxSent = null;
        }
        else sb.append("$('#"+id+"').dialog('close');\n");

        String s = getTitle();
        if (s == null) s = "";
        sb.append("$('#"+id+"').dialog('option', 'title', '"+s+"');\n");
        

        String js = sb.toString();
        if (lastAjaxSent != null && lastAjaxSent.equals(js)) js = null;
        else lastAjaxSent = js;

        return js;
    }
}
