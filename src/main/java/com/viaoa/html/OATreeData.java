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

import java.util.Vector;
import java.lang.reflect.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import java.util.*;

/** TreeNodeData for OATree. */
public class OATreeData implements Cloneable, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    OATreeNode node;
    Object object;
    OATreeData parent;
    boolean bExpanded;
    boolean bFormExpanded;
    Hub hub;

    public OATreeData(OATreeData parent, OATreeNode node, Object obj, boolean expanded, boolean formExpanded) {
        this.parent = parent;
        this.node = node;
        this.object = obj;
        this.bExpanded = expanded;
        this.bFormExpanded = formExpanded;
    }
}



