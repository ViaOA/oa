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

package com.viaoa.jfc.tree;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.*;

/** 
    @depreciated See OATree for description on setting Listeners
    @see OATree
*/
public interface OATreeNodeListener {
    public Component getTreeCellRendererComponent(Component comp,JTree tree,Object value,boolean selected,boolean expanded,boolean leaf,int row,boolean hasFocus);
}
