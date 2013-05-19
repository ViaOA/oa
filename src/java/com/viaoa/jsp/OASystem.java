/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.jsp;

import java.io.*;
import javax.servlet.ServletContext;

/**
 * Top component used for managing web server, others include application, user sesssion, page/form.
 * @author vvia
 *
 */
public class OASystem extends OABase implements Serializable {
    private static final long serialVersionUID = 1L;

    public OASystem() {
    }
    
    public OAApplication getApplication(String applicationName, ServletContext application) {
        OAApplication oaapplication = (OAApplication) application.getAttribute(applicationName+".OA");
        if (oaapplication == null) {
            synchronized(application) {
                oaapplication = (OAApplication) application.getAttribute(applicationName+".OA");
                if (oaapplication == null) {
                    System.out.println("OASystem.getApplication() ... creating new OAApplication "+applicationName);
                    oaapplication = new OAApplication();
                    oaapplication.setName(applicationName);
                    application.setAttribute(applicationName+".OA", oaapplication);
                }
            }
        }
        return oaapplication;
    }

}

