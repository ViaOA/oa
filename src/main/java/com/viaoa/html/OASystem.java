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

package com.viaoa.html;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.text.*;
import com.viaoa.hub.*;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectInfoDelegate;

/** Central object shared by all users to handle OAApplication objects.
*/
public class OASystem extends OABase {
    private static final long serialVersionUID = 1L;
    transient Hashtable hash = new Hashtable();

    
    public OASystem() {
        System.out.println("OASystem() ..... creating new OASystem object");
    }

    public Vector getInfo() {
        Vector vec = new Vector();
        OAObjectCacheDelegate.getInfo(vec);
        
        System.gc();
        vec.addElement("MEMORY --------------- ");
        DecimalFormat  df = new DecimalFormat("0,000");
        vec.addElement("       max  : "+ df.format(Runtime.getRuntime().maxMemory()) );
        vec.addElement("       total: "+ df.format(Runtime.getRuntime().totalMemory()) );
        vec.addElement("       free : "+ df.format(Runtime.getRuntime().freeMemory()) );
        
        vec.addElement("OASystem ------------- ");
        Enumeration enumx = hash.keys();
        for ( ;enumx.hasMoreElements(); ) {
            String name = (String) enumx.nextElement();
            OAApplication app = (OAApplication) hash.get(name);
            Vector v = app.getInfo();
            int x = v.size();
            for (int i=0; i<x; i++) {
                vec.addElement("  "+((String)v.elementAt(i)));
            }
        }
        return vec;
    }

    /** Used by com.viaoa.jsp. 
        This will make sure that the correct oaapplication is used for applicationName.
        The main purpose for this is for systems that have multiple applications running.
    */
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



    /** removes old OASession object and creates a new one */
    public OASession getNewSession(String applicationName, OAApplication oaapplication, HttpSession session, ServletRequest request, ServletResponse response) {
        session.removeValue(applicationName+".OA");
        return getSession(applicationName, oaapplication, session, request, response);
    }
    
    /** Used by oaheader.jsp. 
        This will make sure that the correct oasession is used for applicationName.
        The main purpose for this is for systems that have multiple applications running.
    */
    public OASession getSession(String applicationName, OAApplication oaapplication, HttpSession session, ServletRequest request, ServletResponse response) {

        //NEW JDK: OASession oasession = (OASession) session.getAttribute(applicationName);
        OASession oasession = (OASession) session.getValue(applicationName+".OA");
        if (oasession == null) {
            oasession = new OASession();
            // System.out.println("OASystem.getSession() new session for "+applicationName); //qqqqqqqqq
            //NEW JDK: session.setAttribute(applicationName, oasession);
            session.putValue(applicationName+".OA", oasession);
        }
        oasession.setApplication(oaapplication);
        oasession.setRequest((HttpServletRequest) request);
        oasession.setResponse((HttpServletResponse)response);
        return oasession;
    }

}

