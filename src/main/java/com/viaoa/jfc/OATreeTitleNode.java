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
                    OATree t = getTree();
                    if (t != null) t.repaint();
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
