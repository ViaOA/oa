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
 * Component for managing html links.
 * @author vvia
 *
 */
public class OALink extends OAHtmlElement {
    private static final long serialVersionUID = 1L;

    public OALink(String id) {
        super(id);
    }
    public OALink(String id, String forwardUrl) {
        super(id);
        setForwardUrl(forwardUrl);
    }
    public OALink(String id, Hub hub) {
        super(id, hub);
    }

    public OALink(String id, Hub hub, String forwardUrl) {
        super(id, hub);
        setForwardUrl(forwardUrl);
    }

    @Override
    public String getScript() {
        lastAjaxSent = null;

        StringBuilder sb = new StringBuilder(1024);
        
        String furl = getForwardUrl();
        if (!OAString.isEmpty(furl)) {
            sb.append("$('#"+id+"').attr('href', '"+furl+"');\n");
        }
        
        if (bSubmit || bAjaxSubmit) {
            if (bAjaxSubmit) {
                sb.append("$('#"+id+"').click(function() {$('#oacommand').val('"+id+"');ajaxSubmit();return false;});\n");
            }
            else {
                sb.append("$('#"+id+"').click(function() { $('#oacommand').val('"+id+"'); $('form').submit(); return false;});\n");
            }
            sb.append("$('#"+id+"').addClass('oaSubmit');\n");
        }
        
        String s = getAjaxScript();
        if (s != null) sb.append(s);
        String js = sb.toString();
        
        return js;
    }

}
