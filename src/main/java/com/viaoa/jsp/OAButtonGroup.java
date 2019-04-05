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
package com.viaoa.jsp;
import java.util.*;

/** used with OAToggleButtons to make sure that only one button is selected at a time. 
    @see OAToggleButton
*/
public class OAButtonGroup {
    private static final long serialVersionUID = 1L;
    protected Vector vec = new Vector(5,5);

    public OAToggleButton[] getButtons() {
        int x = vec.size();
        OAToggleButton[] t = new OAToggleButton[x];
        vec.copyInto(t);
        return t;
    }
    public void add(OAToggleButton tog) {
        if (tog != null && !vec.contains(tog)) {
            vec.addElement(tog);
            if (tog.getSelected() || vec.size() == 1) setSelected(tog);
            tog.setButtonGroup(this);
        }
    }

    public void remove(OAToggleButton tog) {
        if (tog != null && vec.contains(tog)) {
            vec.removeElement(tog);
        }
    }
    
    protected int getSelectedIndex() {
        int x = vec.size();
        for (int i=0; i<x; i++) {
            OAToggleButton tog = (OAToggleButton) vec.elementAt(i);
            if (tog.getSelected()) return i;
        }
        if (x > 0) {
            setSelectedIndex(0);
            return 0;
        }
        return -1;
    }
    protected void setSelectedIndex(int pos) {
        int x = vec.size();
        if (pos < x) {
            for (int i=0; i<x; i++) {
                OAToggleButton tog = (OAToggleButton) vec.elementAt(i);
                tog.setSelected( (i == pos) );
            }
        }
    }

    public void setSelected(OAToggleButton cmd) {
        int x = vec.size();
        for (int i=0; i<x; i++) {
            OAToggleButton tog = (OAToggleButton) vec.elementAt(i);
            boolean b = (tog == cmd);
            if (tog.getSelected() != b) tog.setSelected(b);
        }
    }
    
}
