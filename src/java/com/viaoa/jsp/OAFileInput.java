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

import java.io.OutputStream;

/**
 * Used to upload a file from a browser form, input file element.
 * 
 * 
 * <input type="file" id="fiFile" name="fiFile" size="40">
 * 
 * @author vvia
 */
public class OAFileInput extends OAHtmlElement implements OAJspMultipartInterface {
    private static final long serialVersionUID = 1L;

    public OAFileInput(String id) {
        super(id);
    }

    
    @Override
    public OutputStream getOutputStream(int length, String originalFileName) {
        return null;
    }

    @Override
    public String getAjaxScript() {
        StringBuilder sb = new StringBuilder(1024);
        String s = super.getAjaxScript();
        if (s != null) sb.append(s);
        
        
        sb.append("$('#"+id+"').attr('name', '"+id+"');\n");
        
        s = sb.toString();
        return s;
    }
}
