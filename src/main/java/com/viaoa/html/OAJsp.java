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


/* This is the class used for the JSP <%@page extends="oa.html.OAJsp" %> directive.  
    The purpose is to create class level properties that can be accessed from the pages inner classes.
    The properties are OAApplication and OASession.  oabeans.jsp will init these.
    
    
THIS WONT WORK SINCE ALL SESSIONS WILL BE USING THE SAME PAGE **********    
    
*/
public abstract class OAJsp {//implements HttpJspPage {
    OAApplication oaapplication;
    OASession oasession;
/*    
extends attribute must implement the interface HttpJspPage. 
See section 3.2.4 of the JSP 1.0 specification for more details 
*/    

}

