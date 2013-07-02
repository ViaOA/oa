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
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;
import com.viaoa.jfc.tree.OATreeNodeData;


/**
    Popup OATree that is controlled by mouse click on another component.
    <p>
    Example:<br>
    OAPopupTree tree = new OAPopupTree();
    
    JButton cmd = new JButton("See Tree");
    dc.setButton(cmd);
    -- or --
    OACommand cmd = new OACommand(hubEmployee);
    dc.setButton(cmd);
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OAPopup
    @see OATree
*/
public class OAPopupTree extends OATree {

    protected OAPopup popup;
    protected boolean bHideOnSelect=true;
    private JComponent controller;  // component that triggers popup

    /**
        Create a new Popup Tree.
    */
    public OAPopupTree() {
    }
    
    /**
        Create a new Popup Tree.
    */
    public OAPopupTree(JComponent comp) {
        setController(comp);
    }

    // 2006/12/13
    protected OAPopup getPopup() {
    	Component comp = null;
    	if (popup == null) {
    		if (getAllowClearButton()) {
    			JPanel pan = new JPanel(new BorderLayout());
                pan.add(BorderLayout.NORTH, new JScrollPane(this));
                pan.add(getButtonCommands());
                comp = pan;
    		}
    		else {
    			comp = this;
    		}
    		popup = new OAPopup(new JScrollPane(comp));
            if (controller != null) popup.setupListener(controller);
    	}
    	return popup;
    }
    
    
    /**
        @deprecated does not support align anymore.
    */
    public OAPopupTree(JComponent comp, int align) {
        setController(comp);
    }


    /** 
        Component used to set the popup to be visible.
    */
    public void setController(JComponent comp) {
    	this.controller = comp;
    	popup = null;
    	getPopup();
    }

    /**
        Flag to have the popup displayed only when the right mouse button is clicked.
    */
    public void setRightClickOnly(boolean b) {
        getPopup().setRightClickOnly(b);
    }

    /** if true (default), popup is hidden when tree item is selected. */
    public void setHideOnSelect(boolean b) {
        bHideOnSelect = b;
    }
    public boolean getHideOnSelect() {
        return bHideOnSelect;
    }

    /** 
        Overwritten to hide the popup when a node is selected. 
    */
    public @Override void nodeSelected(TreeSelectionEvent e) {
        super.nodeSelected(e);
        if (bHideOnSelect) {
            TreePath tp = e.getNewLeadSelectionPath();
            if (tp != null) {
                Object[] objs = tp.getPath();
                OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
                if (!(tnd.node instanceof OATreeTitleNode)) {
                    if (tnd.node.hub != null || tnd.node.def.updateHub != null) {
                        if (popup != null) popup.setVisible(false);
                    }
                }
            }
        }
    }
    
    /*
     *  2006/12/14
     *  Can be overwritten to clear selected value.  Called when clicking the clear button
     *  on the bottom of the combo popup.
     */
    public void onClear() {
    	// this needs to be overwritten.
    }
    private boolean bAllowClear;
    public void allowClearButton(boolean b) {
    	if (b != bAllowClear) {
	    	popup = null;
	    	this.bAllowClear = b;
	    	getPopup();
    	}
    }
    public boolean getAllowClearButton() {
    	return bAllowClear;
    }
   
    private JPanel getButtonCommands() {
    	JPanel pan = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    	JButton cmd = new JButton("close");
    	cmd.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
                if (popup != null) popup.setVisible(false);
    		}
    	});
    	OACommand.setup(cmd);
    	pan.add(cmd);

    	if (getAllowClearButton()) {
	    	cmd = new JButton("clear");
	    	cmd.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			onClear();
                    if (popup != null) popup.setVisible(false);
	    		}
	    	});
	    	OACommand.setup(cmd);
	    	// cmd.setToolTipText("remove current selected value and close.");
	    	pan.add(cmd);
    	}
    	return pan;
    }
    

}


