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
import java.io.*;
import com.viaoa.hub.*;

/** Central object shared by all users within an application.  This has methods for storing
    Hubs and misc objects, saving and loading a serializable file, adding message, and
    works with OASession objects for tracking users.
    @see OASession
*/
public class OAApplication extends OABase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected transient boolean bInit;
    protected static int userCount, maxUserCount, totalCount;
    protected String name;


    public Vector getInfo() {
        Vector vec = new Vector(10);
        vec.addElement("OAApplication "+name);
        vec.addElement("    Hubs   : "+hashHub.size());
        vec.addElement("    Users    ");
        vec.addElement("    Current: "+userCount);
        vec.addElement("    Max    : "+maxUserCount);
        vec.addElement("    Total  : "+totalCount);

        return vec;
    }
    
    
    /** save hubs and misc objects to file */
    public void save(String fname) throws IOException {
        FileOutputStream fos = new FileOutputStream(fname);
        ObjectOutputStream os = new ObjectOutputStream(fos);

        os.writeObject(this);
        os.close();
    }

    /** load hubs and misc objects from file and uses them to replace the ones in this object. */
    public void load(String fname) {
        OAApplication app = null;
        try {
            FileInputStream fis = new FileInputStream(fname);
            ObjectInputStream ois = new ObjectInputStream(fis);
            app = (OAApplication) ois.readObject();
        }
        catch (Exception e){}
        if (app != null) {
            if (app.hashtable != null) this.hashtable = app.hashtable;
            if (app.hashHub != null) this.hashHub = app.hashHub;
        }
    }

    /** flag that can be used to know if OAApplication has been setup. */
    public boolean isInitialized() {
        return bInit;
    }
    /** @see isInitialized */
    public void setInitialized(boolean b) {
        bInit = b;
    }
   
    /** name of Application */
    public String getName() {
        return name;
    }
    public void setName(String s) {
        name = s;
    }
     
    
    /** called by OASession when adding a new user to site */
    protected static void addUser() {
        userCount++;
        totalCount++;
        if (userCount > maxUserCount) maxUserCount = userCount;
    }
    /** called by OASession to add a new user to site */
    protected static void removeUser() {
        userCount--;
    }
    /** number of OASessions that are being used. */
    public static int getUserCount() {
        return userCount;
    }
    /** maximum number of OASessions that have been used at one time. */
    public static int getMaxUserCount() {
        return maxUserCount;
    }
    /** total number of OASessions that have been used. */
    public static int getTotalCount() {
        return totalCount;
    }
}

