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

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface used for implementing JSP controls.
 * @author vvia
 *
 */
public interface OAJspComponent extends java.io.Serializable{

    boolean isChanged();
    String getId();
    void reset();
    
    void setForm(OAForm form);
    OAForm getForm();
    
    /** returns true to continue, false to not process the request */
    boolean _beforeSubmit();
    
    /** returns true if this component caused the form submit */
    boolean _onSubmit(HttpServletRequest req, HttpServletResponse resp, HashMap<String, String[]> hashNameValue);

    /** return forward url */
    String _afterSubmit(String forwardUrl);
    
    String getScript();    // to initialize the html page
    String getVerifyScript();  // called on client before submit 
    String getAjaxScript();  // to update the page on an ajax update
    
    void setEnabled(boolean b);
    boolean getEnabled();
    
    void setVisible(boolean b);
    boolean getVisible();

    /** used for anonymous subclasses to customize submit */
    String onSubmit(String forwardUrl);
}
