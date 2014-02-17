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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;

import com.viaoa.hub.*;

public class OATreeTitleNode extends OATreeNode {

    public OATreeTitleNode(String path) {
        super(path);
        this.titleFlag = true;
    }
    public String getTitle() {
        return fullPath;
    }
    public void setTitle(String title) {
        this.fullPath = title;
    }

    private Hub<?> hubCount;
    private HubListener hlCount;

    /**
     * Used to have a count added to the node label
     * 
     */
    public void setCountHub(Hub hub) {
        if (this.hubCount != null && hlCount != null) {
            this.hubCount.removeHubListener(hlCount);
            hlCount = null;
        }
        this.hubCount = hub;
        
        if (hubCount != null) {
            hlCount = new HubListenerAdapter() {
                @Override
                public void afterAdd(HubEvent e) {
                    refresh();
                }
                @Override
                public void afterRemove(HubEvent e) {
                    refresh();
                }
                @Override
                public void afterInsert(HubEvent e) {
                    refresh();
                }
                @Override
                public void onNewList(HubEvent e) {
                    refresh();
                }
                void refresh() {
                    getTree().repaint();
                }
            };
            this.hubCount.addHubListener(hlCount);
        }
    }
    
    @Override
    public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        comp = super.getTreeCellRendererComponent(comp, tree, value, selected, expanded, leaf, row, hasFocus);
        if (hubCount != null) {
            String text = ((JLabel)comp).getText();
            if (text == null) text = "";
            if (text.toLowerCase().indexOf("<html") < 0) text = "<html>"+text + "<span style='color:silver'>";
            text += " ("+hubCount.getSize()+")";
            ((JLabel)comp).setText(text);
        }
        return comp;
    }

}
