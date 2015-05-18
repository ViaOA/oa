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
package com.viaoa.util;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import com.viaoa.*;
import com.viaoa.hub.*;
import com.viaoa.object.*;


/**
    OAXMLWriter creates an XML file that can then be read using an OAXMLReader.<br>
    If an object has already been stored in the file, then its key will be stored.

    @see OAXMLReader
*/
public class OAXMLWriter {
    protected PrintWriter pw;
    public int indent;
    private String pad="";
    private int indentLast;
    public int indentAmount=2;
    private boolean bInit;
    private String encodeMessage;
    
    /** param used to write an object */
    public static final int WRITE_YES = 0;
    /** param used to only write an object key */
    public static final int WRITE_KEYONLY = 1;
    /** param used to not write an object */
    public static final int WRITE_NO = 2;
    /** param used to not write any new objects in a hub, so that Hub does not write new objects.  Used in OAObject.log() for M2M links */
    public static final int WRITE_NONEW_KEYONLY = 3;
    protected Hashtable hash; // used for storing objects without an Object Key - updated by OAObject

    private boolean bIncludeEmptyHubs;
    private boolean bIncludeNullProperties;
    
    public OAXMLWriter(String fname) {
        setFileName(fname);
    }    

    public OAXMLWriter(PrintWriter pw) {
        setPrintWriter(pw);
    }

    public void setPrintWriter(PrintWriter pw) {
        close();
        this.pw = pw;
    }

    public void setFileName(String fname) {
        try {
            setPrintWriter(new PrintWriter(new FileWriter(fname)));
        }
        catch (Exception e) {
        }
    }


    
    /** saves OAObject as XML */
    public void write(OAObject obj) {
        OACascade cascade = new OACascade();
		OAObjectXMLDelegate.write(obj, this, false, cascade);
    }
    /** saves Hub as XML */
    public void write(Hub hub) {
        OACascade cascade = new OACascade();
        HubXMLDelegate.write(hub, this, false, cascade);
    }

    /**
     * callback used to know if property should be written 
     * @deprecated use {@link #shouldWriteProperty(Object, String, Object)}
     */
    public int writeProperty(Object obj, String propertyName, Object value) {
        return shouldWriteProperty(obj, propertyName, value);
    }
    /** called by OAObject.write to know if object should
        be written for a property that references another OAObject.
        This can be overwritten to control which properties are saved.
        default = WRITE_YES;
    */
    public int shouldWriteProperty(Object obj, String propertyName, Object value) {
        return WRITE_YES;
    }

    /**
        hook used to know when an object is being saved.
    */
    public void writing(Object obj) {
    }

    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void setIndentAmount(int x) {
        this.indentAmount = x;
    }
    
    public void flush() {
        if (pw != null) {
            pw.flush();
        }
    }
    
    public void close() {
        if (pw != null) {
            end();
            pw.flush();
            pw.close();
            pw = null;
        }
        bEnd = false;
    }

    private boolean bEnd;
    public void end() {
        if (!bEnd) {
            bEnd = true;
            indent--;
            println("</OAXML>");
        }        
    }

    /**    
        Used to encrypt all data. 
        <p>
        NOTE: currently only works with printCDATA.  This needs to be changed so that all printXX methods
        encrypt data.
        @see OAXMLReader#setDecodeMessage
    */
    public void setEncodeMessage(String msg) {
        if (msg != null && msg.length() == 0) throw new IllegalArgumentException("EncodeMessage cant be an empty string");
        encodeMessage = msg;
    }
    public String getEncodeMessage() {
        return encodeMessage;
    }

    /** encloses line in XML CDATA tag and internally encodes illegal XML characters */
    public void printCDATA(String line) {
        if (pw == null) throw new NullPointerException("PrintWrite is null");
        if (!bInit) init();
        if (encodeMessage != null) line = encodeMessage+Base64.encode(line);
        pw.print("<![CDATA["+OAString.convertToXML(line,true)+"]]>");
    }

    /** converts XML codes and encodes illegal XML characters */
    public void printXML(String line) {
        if (pw == null) throw new NullPointerException("PrintWrite is null");
        if (!bInit) init();
        pw.print(OAString.convertToXML(line,false));
    }
    
    public void print(String line) {
        if (pw == null) throw new NullPointerException("PrintWriter is null");
        if (!bInit) init();
        pw.print(line);
    }

    public void println(String line) {
        if (pw == null) throw new NullPointerException("PrintWriter is null");
        if (!bInit) init();
        pw.println(line);
    }

    public void indent() {
        if (pw == null) throw new NullPointerException("PrintWriter is null");
        if (!bInit) init();

        if (indent != indentLast) {
            indentLast = indent;
            pad = "";
            int x = indent * indentAmount;
            for (int i=0; i < x; i++) pad += " ";
        }
        pw.print(pad);
    }

    protected void init() {
        bInit = true;
        // println("<?xml version='1.0'?>");
        println("<?xml version='1.0' encoding='utf-8'?>");
        //was:  println("<?xml version='1.0' encoding='windows-1252'?>");
        println("<OAXML VERSION='1.0' DATETIME='"+(new OADateTime())+"'>");
        indent++;
    }

    /** Method that can be overwritten to provide custom conversion to String.
        Called to convert a value to a String when there does not exist an OAConverter 
        for a class.  
        @return null to skip property.
    */
    public String convertToString(String property, Object value) {
        return null;
    }
    
    public String getClassName(Class c) {
        return c.getName();
    }
    
    public void setIncludeEmptyHubs(boolean b) {
        bIncludeEmptyHubs = b;
    }
    public boolean getIncludeEmptyHubs() {
        return bIncludeEmptyHubs;
    }
    public void setIncludeNullProperties(boolean b) {
        bIncludeNullProperties = b;
    }
    public boolean getIncludeNullProperties() {
        return bIncludeNullProperties;
    }
}
