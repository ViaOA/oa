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
package com.viaoa.util;

import java.util.*;
import java.io.*;
import com.viaoa.hub.*;
import com.viaoa.object.*;

/**
    OAJsonWriter creates an JSON file that can then be read using an OAJsonReader.<br>
    If an object has already been stored in the file, then its key will be stored.

    @see OAJsonReader
*/
public class OAJsonWriter {
    protected PrintWriter pw;
    protected StringWriter sw;
    public int indent;
    private String pad="";
    private int indentLast;
    public int indentAmount=2;
    private boolean bInit;
    private String encodeMessage;
    private boolean bCompress=true;
    private ArrayList<String> alInclude;
    private ArrayList<String> alExclude;
    
    protected Hashtable hash; // used for storing objects without an Object Key - updated by OAObject
    private Stack<Object> stack;
    private boolean includeOneRef;
    private boolean includeManyRef;

    public OAJsonWriter() {
    }    
    public void setCompress(boolean b) {
        this.bCompress = b;
    }
    public boolean getCompress() {
        return this.bCompress;
    }

    /** saves OAObject as JSON */
    public String write(OAObject obj) {
        reset();
        OACascade cascade = new OACascade();
		OAObjectJsonDelegate.write(obj, this, false, cascade);
		String s = new String(sw.getBuffer());
		return s;
    }
    /** saves Hub as JSON */
    public String write(Hub hub) {
        reset();
        OACascade cascade = new OACascade();
        HubJsonDelegate.write(hub, this, cascade);
        String s = new String(sw.getBuffer());
        return s;
    }
   
    protected void reset(){
        this.sw = new StringWriter();
        this.pw = new PrintWriter(sw);
        stack = new Stack<Object>();
    }

//qqq  NEW CODE 20120610 qqqqqqqqqqqqq THis needs to be tested **********qqqqqqqqq
    
//qqqqqqqqqqqq todo: build a tree ?    
    
    /** called by OAObject.write to know if object should
        be written for a property that references another OAObject.
        This can be overwritten to control which properties are saved.
        default is true;
    */
    public boolean shouldIncludeProperty(Object obj, String propertyName, Object value, OALinkInfo li) {
        if (li != null) {
            if (li.getType() == li.ONE) {
                if (!includeOneRef) return false;
            }
            else {
                if (!includeManyRef) return false;
            }
        }
        
        if (propertyName == null) return false;
        
        String current = getCurrentPath();
        if (current == null) current = "";
        else if (current.length() > 0) current += ".";
        current += propertyName;
        
        if (alInclude != null) {
            boolean bMaybe = false;
            for (String s : alInclude) {
                if (s.equalsIgnoreCase(current)) return true; 

                if (s.toUpperCase().startsWith(current.toUpperCase())) {
                    if (s.charAt(s.length()) == '.') {
                        return true;
                    }
                }
                
                if (current.toUpperCase().startsWith(s.toUpperCase())) {
                    if (current.charAt(s.length()) == '.') {
                        bMaybe = true;
                    }
                }
            }
            if (!bMaybe) return false;
        }

        if (alExclude != null) {
            for (String s : alExclude) {
                if (s.equalsIgnoreCase(current)) return false; 
                if (s.toUpperCase().startsWith(current.toUpperCase())) {
                    if (s.charAt(current.length()) == '.') return false;
                }
            }
        }
        
        return true;
    }

    /*
qqqqqq example:      
      save Employee object
          dept
          dept.manager
          dept.manager.employees
          firstName
          lastName
          calcProperty
     */
    
    /**
     * Current propertyPath from the root OAObject.
     * Only includes reference/link names, no regular properties 
     */
    protected String getCurrentPath() {
        int x = stack.size();
        String s = "";
        for (int i=0; i<x; i++) {
            if (i > 0) s = "." + s;
            s = stack.elementAt(i) + s;
        }
        return s;
    }
    
    // called by OAObjectJsonDelegate to track the serialized path of references
    public void pushReference(String name) {
        if (!OAString.isEmpty(name)) {
            stack.push(name);
        }
    }
    public void popReference() {
        if (stack.size() > 0) {
            stack.pop();
        }
    }
    
    public void includeOneReferences(boolean b) {
        this.includeOneRef = b;
    }
    public void includeManyReferences(boolean b) {
        this.includeManyRef = b;
    }
    
    
    public void includePropertyPath(String propertyPath) {
        if (alInclude == null) {
            alInclude = new ArrayList<String>();
        }
        if (!OAString.isEmpty(propertyPath)) alInclude.add(propertyPath);
    }
    public void excludePropertyPath(String propertyPath) {
        if (alExclude == null) {
            alExclude = new ArrayList<String>();
        }
        if (!OAString.isEmpty(propertyPath)) alExclude.add(propertyPath);
    }
    
    
    /**
        hook used to know when an object is being saved.
    */
    public void writing(Object obj) {
    }

    /** encloses line in XML CDATA tag and internally encodes illegal XML characters */
    public void printCDATA(String line) {
        if (pw == null) throw new NullPointerException("PrintWrite is null");
//qqqqq Json does not have CDATA        
//qqqqqqq check to see what conversions JSON needs, qqqqqqqqqqqq        
        if (encodeMessage != null) line = encodeMessage+Base64.encode(line);
        pw.print("<![CDATA["+OAString.convertToXML(line,true)+"]]>");
    }

    /** converts XML codes and encodes illegal XML characters */
    public void printJson(String line) {
        if (pw == null) throw new NullPointerException("PrintWrite is null");
//qqqqqqq check to see what conversions JSON needs, qqqqqqqqqqqq        
        pw.print(OAString.convertToXML(line, false));
    }
    
    public void print(String line) {
        if (pw == null) throw new NullPointerException("PrintWriter is null");
        pw.print(line);
    }

    public void println(String line) {
        if (pw == null) throw new NullPointerException("PrintWriter is null");
        if (bCompress) pw.print(line);
        else pw.println(line);
    }

    public void indent() {
        if (bCompress) return;
        if (pw == null) throw new NullPointerException("PrintWriter is null");

        if (indent != indentLast) {
            indentLast = indent;
            pad = "";
            int x = indent * indentAmount;
            for (int i=0; i < x; i++) pad += " ";
        }
        pw.print(pad);
    }

    /** Method that can be overwritten to provide custom conversion to String.
        Called to convert a value to a String when there does not exist an OAConverter 
        for a class.  
        @return null to skip property.
    */
    public String convertToString(String property, Object value) {
        return null;
    }
}
