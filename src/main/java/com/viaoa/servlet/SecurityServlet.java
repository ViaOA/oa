/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.viaoa.ds.OASelect;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.util.Base64;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

/**
 * Get an image from an Object Property
 * 
 * similar to protocol handler:
 * com.viaoa.jfc.editor.html.protocol.classpath.Handler
 * 
 * Note: images were stored in byte[] property using
 * OAImageUtil.convertToBytes(), which uses *.jpg format
 * 
 * get params: c|class, i|id, p|prop|property, mw|maxw (max width), mh|maxh (max height)
 * 
 * 
 * @author vincevia
 */
public class SecurityServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(JsonServlet.class.getName());

    private String userId;
    private String password;
    
    public SecurityServlet() {
    }
    public SecurityServlet(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        if (OAString.isEmpty(userId)) {
            userId = getValue("userId", config);
        }
        if (OAString.isEmpty(password)) {
            password = getValue("password", config);
        }
    }
    private String getValue(String name, ServletConfig config) {
        if (name == null) return null;
        Enumeration<String> enumx = config.getInitParameterNames();
        for ( ; enumx.hasMoreElements(); ) {
            String s = enumx.nextElement();
            if (name.equalsIgnoreCase(s)) {
                name = s;
                break;
            }
        }
        return config.getInitParameter(name);
    }
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext sc = getServletContext();

        PrintWriter out = resp.getWriter();
        
        out.println("Secure pw protected information");
        
       // super.doGet(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(req, resp);
    }
    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String auth = request.getHeader("Authorization");

        boolean bValid = false;
        String userId = null;
        String pw = null;
        if (auth != null && auth.toUpperCase().startsWith("BASIC ")) {
            String userpassEncoded = auth.substring(6);
            String s = Base64.decode(userpassEncoded);
            int pos = s.indexOf(':');
            userId = s.substring(0, pos); 
            pw = s.substring(pos+1);
            
            bValid = (this.userId.equals(userId) && this.password.equals(pw));
        }
        if (!bValid) {
            response.setHeader("WWW-Authenticate", "BASIC realm=\"security servlet\"");
            response.sendError(response.SC_UNAUTHORIZED);
        }
        super.service(request, response);
    }

}
