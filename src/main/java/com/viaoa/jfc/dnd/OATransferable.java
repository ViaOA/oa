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
package com.viaoa.jfc.dnd;

import java.awt.datatransfer.*;
import java.io.IOException;
import com.viaoa.hub.*;

/** 
     Used to allow any OAObject or Hub Collection the ability to support Drag and Drop.
     OA gui components OATree, OAList, OATable use this to <i>wrap</i> objects that are 
     Dragged or Dropped.
*/
public class OATransferable implements Transferable {
    protected final Hub hub;
    protected final Object object;  // the original (not a copy).  Use bFromCut=false to know know if paste needs to then make a new copy of the object. 
    protected final boolean bFromCut; // if the object is from a clipbord "cut"

    /** 
        Create new DND Data Flavor that supports Hub.
    */
    public static DataFlavor HUB_FLAVOR = new DataFlavor(Hub.class, "TransferableHub") {
        public boolean isFlavorSerializedObjectType() {
            return false; //was true - if true then a DND will serialize the Hub, which will create a new Hub     
        }
    };
    public static DataFlavor HUB_CUT_FLAVOR = new DataFlavor(Hub.class, "TransferableCutHub") {
        public boolean isFlavorSerializedObjectType() {
            return false;     
        }
    };
    public static DataFlavor HUB_COPY_FLAVOR = new DataFlavor(Hub.class, "TransferableCopyHub") {
        public boolean isFlavorSerializedObjectType() {
            return false;     
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

    /** 
        Create new DND Data Flavor where object is from a "cut" command
    */
    public static DataFlavor OAOBJECT_CUT_FLAVOR = new DataFlavor(Object.class, "TransferableCutOAObject") {
        public boolean isFlavorSerializedObjectType() {
            return false; //was: true;   
        }
    };

    /** 
        Create new DND Data Flavor where object is from a "copy" command
    */
    public static DataFlavor OAOBJECT_COPY_FLAVOR = new DataFlavor(Object.class, "TransferableCopyOAObject") {
        public boolean isFlavorSerializedObjectType() {
            return false; //was: true;   
        }
    };
    
    static final DataFlavor[] flavors = { HUB_FLAVOR, HUB_CUT_FLAVOR, HUB_COPY_FLAVOR, OAOBJECT_FLAVOR, OAOBJECT_CUT_FLAVOR, OAOBJECT_COPY_FLAVOR };

    /**
        Creates a new transferable object to <i>wrap</i> a Hub and/or OAObject.
        @param bReferenceOnly if the object is from a clipbord "cut"
    */
    public OATransferable(Hub hub, Object obj, boolean bFromCut) {
        this.hub = hub;
        this.object = obj;
        this.bFromCut = bFromCut;
    }
    public OATransferable(Hub hub, Object obj) {
        this(hub, obj, true); // true, since the default is used by DND (moving object=cutting)
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
        if (flavor == null) return null;
        if (flavor.equals(HUB_FLAVOR)) {
            if (object instanceof Hub) return (Hub) object;
            return hub;
        }
        if (flavor.equals(HUB_COPY_FLAVOR)) {
            if (bFromCut) return null;
            if (object instanceof Hub) {
                return (Hub) object;
            }
            return hub;
        }
        if (flavor.equals(HUB_CUT_FLAVOR)) {
            if (!bFromCut) return null;
            if (object instanceof Hub) {
                return (Hub) object;
            }
            return hub;
        }
        
        
        if (flavor.equals(OAOBJECT_CUT_FLAVOR) && flavor.getHumanPresentableName().equals(OAOBJECT_CUT_FLAVOR.getHumanPresentableName())) {
            if (bFromCut) return object;
            else return null;
        }
        if (flavor.equals(OAOBJECT_COPY_FLAVOR) && flavor.getHumanPresentableName().equals(OAOBJECT_COPY_FLAVOR.getHumanPresentableName())) {
            if (!bFromCut) return object;
            else return null;
        }
        return object;
    }

    /**
        Returns true if Hub or OAObject Flavor.
    */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor.equals(HUB_FLAVOR)) return true;
        if (flavor.equals(OAOBJECT_FLAVOR)) return true;
        if (flavor.equals(OAOBJECT_CUT_FLAVOR)) return true;
        if (flavor.equals(OAOBJECT_COPY_FLAVOR)) return true;
        return false;
    }
    
}


