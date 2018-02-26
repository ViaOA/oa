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
public class ImageServlet extends HttpServlet {
    private static Logger LOG = Logger.getLogger(JsonServlet.class.getName());
    private String packageName;
    private String defaultPropertyName;
    private Class defaultClass;

    public ImageServlet() {
    }

    public ImageServlet(String packageName, Class defaultClass, String defaultPropertyName) {
        if (!OAString.isEmpty(packageName)) this.packageName = packageName + ".";
        else this.packageName = "";
        this.defaultClass = defaultClass;
        this.defaultPropertyName = defaultPropertyName;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        String s = getValue("packageName", config);
        if (!OAString.isEmpty(s)) {
            this.packageName = s + ".";
        }
        else this.packageName = "";

        defaultPropertyName = getValue("defaultPropertyName", config);
    }
    private String getValue(String name, ServletConfig config) {
        Enumeration<String> enumx = config.getInitParameterNames();
        for ( ; enumx.hasMoreElements(); ) {
            String s = enumx.nextElement();
            if (name.equals(s)) {
                name = s;
                break;
            }
        }
        return config.getInitParameter(name);
    }
    
    
    // keep track of the size of image, so that it wont have to resend to browser
    private ConcurrentHashMap<String, Integer> hmLastSize = new ConcurrentHashMap<String, Integer>();
    
    // keep track of images that have been checked if they include alpha. 
    private ConcurrentHashMap<String, Boolean> hmImageAlphaFlag = new ConcurrentHashMap<String, Boolean>();

    
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get the absolute path of the image
        ServletContext sc = getServletContext();
        
        String className = req.getParameter("c");
        if (className == null) className = req.getParameter("class");

        String id = req.getParameter("i");
        if (id == null) id = req.getParameter("id");

        String propName = req.getParameter("p");
        if (propName == null) {
            propName = req.getParameter("prop");
            if (propName == null) propName = req.getParameter("property");
        }
        if (OAString.isEmpty(propName)) propName = defaultPropertyName;

        String maxW = req.getParameter("mw");
        if (maxW == null) maxW = req.getParameter("maxw");
        String maxH = req.getParameter("mh");
        if (maxH == null) maxH = req.getParameter("maxh");

        final String sizeOfImageInRequest = req.getParameter("len");  // sent by oaservletImage so that this can work with browser cache
        
        LOG.finer(String.format("class=%s, id=%s, property=%s, maxw=%s, maxh=%s", className, id, propName, maxW, maxH));
        final String etag = String.format("%s.%s.%s.%s.%s.%s", className, id, propName, maxW, maxH, sizeOfImageInRequest);

        /*
        Enumeration enumx = req.getHeaderNames();
        for ( ;enumx.hasMoreElements(); ) {
            String sx = (String) enumx.nextElement();
            System.out.println("==> "+sx+", val="+req.getHeader(sx));
        }
        
         example:
            ==> Cookie, val=JSESSIONID=151za46tedr1jmz6mj2l522cr
            ==> Host, val=localhost:8081
            ==> Accept, val=* /*
            ==> Accept-Charset, val=ISO-8859-1,utf-8;q=0.7,*;q=0.3
            ==> Accept-Language, val=en-US,en;q=0.8
            ==> Referer, val=http://localhost:8081/service-award-dvd.jsp
            ==> User-Agent, val=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31
            ==> Connection, val=keep-alive
            ==> Accept-Encoding, val=gzip,deflate,sdch
        */
    
        if (className == null || className.length() == 0) {
            if (defaultClass == null) {
                LOG.fine("className is required");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        if (propName == null || propName.length() == 0) {
            LOG.fine("propertyName is required");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        propName = OAString.convert(propName, "/", null);
        if (id == null || id.length() == 0) {
            LOG.fine("id is required");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Class c;
        String cname = "";
        try {
            if (OAString.isEmpty(className) && defaultClass != null) {
                c = defaultClass;
                cname = c.getName();
            }
            else {
                if (className.indexOf('.') >= 0) cname = className;
                else cname = (packageName + className);

                c = Class.forName(cname);
            }
        }
        catch (ClassNotFoundException e) {
            LOG.fine("class not found, class=" + cname);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        OAObject obj;
        obj = OAObjectCacheDelegate.get(c, id);
        if (obj == null) {
            OASelect sel = new OASelect(c);
            sel.select("ID = ?", new Object[] { id });
            obj = (OAObject) sel.next();
            sel.cancel();
        }
        if (obj == null) {
            LOG.fine("objet not found, class=" + (packageName + className) + ", id=" + id);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        byte[] bs;
        try {
            bs = (byte[]) obj.getProperty(propName);
            if (bs == null) {
                LOG.fine("could not read image from property" + (packageName + className) + ", id=" + id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (bs.length == 0) {
                LOG.fine("image is empty, from property" + (packageName + className) + ", id=" + id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        catch (Exception e) {
            LOG.fine("could not read image from property" + (packageName + className) + ", id=" + id + ", Exception=" + e);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // see if browser has image cached, and that it has not been changed
        boolean bImageHasChanged = true;
        Object objx = hmLastSize.get(etag);
        if (objx instanceof Integer) {
            int len = ((Integer) objx).intValue();
            if (len == bs.length) {
                bImageHasChanged = false;
                String etagRequest = req.getHeader("If-None-Match");
                if (etagRequest != null) etagRequest = OAString.convert(etagRequest, "\"", "");
                 
                if (etagRequest != null && etag.equals(etagRequest)) {
                    long ts = req.getDateHeader("If-Modified-Since");
                    if (ts > 0) {
                        OADateTime dt = new OADateTime(ts);
                        if (dt.addHours(24).after(new OADateTime())) {
                            resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                            resp.setHeader("Last-Modified", req.getHeader("If-Modified-Since"));
                            return;  // browser will use the image in it's cache
                        }
                    }
                }
            }
            else {
                hmImageAlphaFlag.remove(etag);
            }
        }
        hmLastSize.put(etag, bs.length);
        
        int maxw = maxW == null ? 0 : OAConv.toInt(maxW);
        int maxh = maxH == null ? 0 : OAConv.toInt(maxH);

        if (maxw > 0 || maxh > 0) {
            BufferedImage bi = OAImageUtil.convertToBufferedImage(bs);
            bi = OAImageUtil.scaleDownToSize(bi, maxw, maxh);
            bs = OAImageUtil.convertToBytes(bi);  // also removes alpha
        }
        
        // Open the file and output streams
        OutputStream out = resp.getOutputStream();

        if (bImageHasChanged || (hmImageAlphaFlag.get(etag) == null)) {
            BufferedImage bi = OAImageUtil.convertToBufferedImage(bs);
            if (OAImageUtil.hasAlpha(bi)) {
                hmImageAlphaFlag.put(etag, Boolean.TRUE);
            }
            else {
                hmImageAlphaFlag.put(etag, Boolean.FALSE);
            }
        }

        String imageType;
        if (hmImageAlphaFlag.get(etag)) {
            imageType = "png";
            bs = OAImageUtil.convertToPNG(bs);  // this is for cases where image bs[] is jpg+alpha
        }
        else {
            imageType = "jpeg";
        }
        
        
        resp.setContentLength(bs.length);
        
        resp.setHeader("ETag", "\""+etag+"\""); 
        
        // Set content type
        resp.setContentType("image/"+imageType); 
        
        resp.setDateHeader("Date", System.currentTimeMillis());
        resp.setDateHeader("Last-Modified", System.currentTimeMillis());
        
        int maxAgeSeconds;
        if (OAString.isNotEmpty(sizeOfImageInRequest)) maxAgeSeconds = 60 * 60 * 24;  // 24hrs.  The request will include the size, making it a new value if the img is changed.
        else maxAgeSeconds = 0;  // this will have the browser req everytime.  But it will send the etag, and the browser can send back 304 is img size has not changed.
        resp.setHeader("Cache-Control", "private, max-age="+maxAgeSeconds+", must-revalidate");
        
        out.write(bs);
        out.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    
    private final long msLastModified = (new Date()).getTime();
    private final long msOneDay = (1000 * 60 * 60 * 24);

    // NOTE: this does not seem to be called.  If needed, needs to match logic in doGet(..)
    // @Override
    protected void XXdoHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // response.sendError(HttpServletResponse.SC_NOT_FOUND);
        
        // If-Modified-Since header should be greater than LastModified. If so, then return 304.
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        if (ifModifiedSince != -1 && ifModifiedSince + 1000 > msLastModified) {
            String eTag = "fileName" + "_" + "1234" + "_" + msLastModified;
            response.setHeader("ETag", eTag); // Required in 304.
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        response.setDateHeader("Last-Modified", msLastModified);
        response.setDateHeader("Expires", System.currentTimeMillis() + msOneDay);
        
        
        super.doHead(request, response);
    }

    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // check to see if a session has already been created for this user
        // don't create a new session yet.
        HttpSession session = request.getSession(false);
        super.service(request, response);
/*        
        String requestedPage = request.getParameter(Constants.REQUEST);
        if (session != null) {
            // retrieve authentication parameter from the session
            Boolean isAuthenticated = (Boolean) session.getValue(Constants.AUTHENTICATION);
            // if the user is not authenticated
            if (!isAuthenticated.booleanValue()) {
                // process the unauthenticated request
                unauthenticatedUser(response, requestedPage);
            }
        }
        else // the session does not exist
        {
            // therefore the user is not authenticated
            // process the unauthenticated request
            unauthenticatedUser(response, requestedPage);
        }
*/        
    }

}
