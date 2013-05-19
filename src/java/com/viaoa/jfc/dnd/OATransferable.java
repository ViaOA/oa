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
package com.viaoa.jfc.dnd;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.Vector;
import java.lang.reflect.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import com.viaoa.hub.*;

/** 
     Used to allow any OAObject or Hub Collection the ability to support Drag and Drop.
     OA gui components OATree, OAList, OATable use this to <i>wrap</i> objects that are 
     Dragged or Dropped.
*/
public class OATransferable implements Transferable {
    protected Hub hub;
    protected Object object;
    protected Class clazz;


    /** 
        Create new DND Data Flavor that supports Hub.
    */
    public static DataFlavor HUB_FLAVOR = new DataFlavor(Hub.class, "TransferableHub") {
        public boolean isFlavorSerializedObjectType() {
            return false; //was true - if true then a DND will serialize the Hub, which will create a new Hub     
        }
    };

    /** 
        Create new DND Data Flavor that supports OAObject.
    */
    public static DataFlavor OAOBJECT_FLAVOR = new DataFlavor(Object.class, "TransferableOAObject") {
        public boolean isFlavorSerializedObjectType() {
            return false; //was: true;   
        }
    };

    
    static final DataFlavor[] flavors = { HUB_FLAVOR, OAOBJECT_FLAVOR };

    /**
        Creates a new transferable object to <i>wrap</i> a Hub or OAObject.
    */
    public OATransferable(Hub hub, Object obj) {
        this.hub = hub;
        this.object = obj;
    }
    
    /**
        Returns DataFlavors for the Hub and OAObject
    */
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    /** 
        Returns the object that is transferable, Hub or Object (OAObject).
    */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(HUB_FLAVOR)) return hub;
        return object;
    }

    /**
        Returns true if flave if for the Hub or OAObject Flavor.
    */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor.equals(HUB_FLAVOR)) return true;
        if (flavor.equals(OAOBJECT_FLAVOR)) return true;
        return false;
    }
    
}


