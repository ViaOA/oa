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

import com.viaoa.hub.Hub;
import com.viaoa.util.OAString;


/**
 * Component for managing html password textfield.
 * @author vvia
 *
 */
public class OAPassword extends OATextField {
    private static final long serialVersionUID = 1L;

    public OAPassword(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath, 0, 0);
//qqqqqqqqq passwords are not encrypted yet        
//        setConversion('P');
    }
    public OAPassword(String id, Hub hub, String propertyPath, int width, int maxWidth) {
        super(id, hub, propertyPath, width, maxWidth);
//qqqqqqqqq passwords are not encrypted yet        
//        setConversion('P');
    }

    public OAPassword(String id) {
        super(id);
    }

    @Override
    protected String convertValue(String value) {
        // if (OAString.isEmpty(value)) return "";
        return "";
    }

    @Override
    public String getTableEditorHtml() {
        // let cell take up all space
        width = 0;  // so that the "size" attribute wont be set        
        String s = "<input id='"+id+"' type='password' style='top:4px; left:2px; width:90%; position:absolute;'>";
        return s;
    }
}
