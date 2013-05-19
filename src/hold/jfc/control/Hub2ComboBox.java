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
package com.viaoa.jfc.control;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JComboBox.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.jfc.*;

/** 
    Class for binding a JFC JComboBox to a Hub, used by OAComboBox. 
    A property path is used for the display value.  An image property can also be to
    display an image for each row.
    <p>
    Example: <br>
    This will use a JComboBox where the selected object will match the
    Department for the Employee that is the active object in hubEmployee.<br>
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    hubDepartment.select();
    Hub hubDepartment = new Hub(Department.class);
    hubDepartment.select();
    hubDepartment.setLink(hubEmployee); // see Hub.setLink 
    JComboBox cbo = new JComboBox();
    new Hub2Button(hubDepartment, cbo, "name");
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    
    @see OAComboBox
    @see Hub#setLink
*/
public class Hub2ComboBox extends Hub2Gui implements FocusListener {
    JComboBox comboBox;
    MyComboBoxModel myComboBoxModel = new MyComboBoxModel();
    JList list;
    
    public Hub2ComboBox(JComboBox cb) {
        create(cb);
    }

    public Hub2ComboBox(Hub hub, JComboBox cb, String propertyPath) {
        super(hub, propertyPath, cb); // this will add hub listener
        create(cb);
    }

    public Hub2ComboBox(Object object, JComboBox cb, String propertyPath) {
        super(object, propertyPath, cb); // this will add hub listener
        create(cb);
    }

    protected boolean isForThisHub(HubEvent e) {
        Hub eHub = e.getHub();
        Hub thisHub = getHub();
        if (eHub == null || thisHub == null) return false;
        if (eHub == thisHub) return true;
        Hub hubReal = thisHub.getRealHub();
        if (eHub == hubReal) return true;
        Hub eRealHub = eHub.getRealHub(); 
        if (eRealHub == hubReal) return true;
        if (eHub == getActualHub()) return true;
        hubReal = getActualHub().getRealHub();
        if (eHub == hubReal) return true;
        if (eRealHub == hubReal) return true;
        return false;
    }

    private String undoDescription;
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public void setUndoDescription(String s) {
        undoDescription = s;
    }
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public String getUndoDescription() {
        return undoDescription;
    }
    
    protected void create(JComboBox cb) {
        if (comboBox != null) comboBox.removeFocusListener(this);
        if (comboBox != cb && cb != null) cb.setMaximumRowCount(12);
        comboBox = cb;


        Hub h = getHub();
        if (h != null) {
            if (comboBox != null) {
                myComboBoxModel.flag = true;  // this will keep the activeObject from getting changed.  JComboBox sets selectetPos to "0"
               
                comboBox.setModel(myComboBoxModel);
                comboBox.setRenderer(new MyListCellRenderer());
                myComboBoxModel.flag = false;

                comboBox.addFocusListener(this);
                // not needed? might need to be put in the addNotify() method
                // comboBox.getUI().getList().setCellRenderer(new MyListCellRenderer());
                HubEvent e = new HubEvent(getHub(),getHub().getActiveObject());
                this.afterChangeActiveObject(e);  // this will set selectedPos in JComboBox
                myComboBoxModel.fireChange(-1,-1); // hack: must initialize listeners ?!?!?

                // need to know when link hub activeObject is set, in case comboBox was disabled and new value in linkHub is null
                h = h.getLinkHub();
                if (h != null) h.addHubListener(this);
            }
        }
    }


    
    protected void resetHubOrProperty() {  // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (comboBox != null) create(comboBox);
    }

    public void close() {
        if (comboBox != null) comboBox.removeFocusListener(this);
        super.close();  // this will call hub.removeHubListener()
    }

    public void NOTUSED_setColumns(int columns) {
        int width = OATable.getCharWidth(comboBox, comboBox.getFont(), columns);
        Dimension d = comboBox.getPreferredSize();
        d.width = width;
        comboBox.setPreferredSize(d);
        d.width += 8;
        comboBox.setMaximumSize(d);
        d.width -= 12;
        comboBox.setMinimumSize(d);
    }


    @Override
    public void setReadOnly(boolean b) {
        super.setReadOnly(b);
        setEnabled(!b);
        afterChangeActiveObject(null);
    }
    
    /**
        Hub event to updated selected value.
    */
    public @Override void afterChangeActiveObject(HubEvent evt) {
        if (getHub() == null) return;
        Object oaObject = getHub().getActiveObject(); // use hub instead of actualHub
        
        if (comboBox != null) {
            if (evt != null) {
                myComboBoxModel.flag = true;
                comboBox.setSelectedItem(oaObject);
                myComboBoxModel.flag = false;
            }
            if (list != null) {
                if (oaObject == null) oaObject = OANullObject.nullObject;
                myComboBoxModel.flag = true;
                try {
                	list.setSelectedValue(oaObject,true);
                }
                catch (Exception e) {}
                myComboBoxModel.flag = false;
            }
            comboBox.repaint();
         
            boolean b = true;
            if (b) {
                Hub h = getHub();
                if (h == null) b = false;
                else {
                    if (!HubDelegate.isValid(h)) b = false;
                    h = h.getLinkHub();
                    if (h != null && h.getPos() < 0) b = false;
                }
            }
            
            this.setInternalEnabled(b && isParentEnabled(comboBox));
        }
    }

    /**
        Hub property change event that causes ComboBox to be repainted.
    */
    public @Override void afterPropertyChange(HubEvent e) {
        if ( isForThisHub(e) && comboBox != null && e.getPropertyName().equalsIgnoreCase(getPropertyName()) ) {
            comboBox.repaint();
        }
    }

    
    /**
	    Hub insert event to update row in ComboBox.
	*/
	public @Override void afterInsert(HubEvent e) {
	    if (!isForThisHub(e)) return; 
	    final int pos = e.getPos();
        if (SwingUtilities.isEventDispatchThread()) {
        	myComboBoxModel.fireChange(pos,pos);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {   
                    myComboBoxModel.fireChange(pos,pos);
                }
            });
        }
    }
    

    /**
	    Hub add event to add a row in ComboBox.
	*/
	public @Override void afterAdd(HubEvent e) {
	    if (!isForThisHub(e) || HubSelectDelegate.isFetching(getHub())) return;
	    final Object obj = e.getObject();

        Hub h = getHub();
        final int pos = h.getPos(obj);
	    
        if (SwingUtilities.isEventDispatchThread()) {
            myComboBoxModel.fireAdd(pos,pos);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {   
                    myComboBoxModel.fireAdd(pos,pos);
                }
            });
        }
    }
    
    
    /**
	    Hub add event to remove a row in ComboBox.
	*/
	public @Override void afterRemove(HubEvent e) {
	    if (!isForThisHub(e)) return;
	    final int pos = e.getPos();
	    if (pos < 0) return;
	    if (SwingUtilities.isEventDispatchThread()) {
        	myComboBoxModel.fireRemove(pos,pos);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {   
                	myComboBoxModel.fireRemove(pos,pos);
                }
            });
        }
    }

    /**
	    Hub event to notify that a new list of objects has been loaded into the Hub.
	    The ComboBox will be updated to show new list.
	*/
	public @Override void onNewList(HubEvent e) {
	    if (!isForThisHub(e)) return;
	    final int size = getHub().getSize();
        if (SwingUtilities.isEventDispatchThread()) {
            myComboBoxModel.fireChange(0,size); // this includes nullObject!!!
            afterChangeActiveObject(null);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {   
                    myComboBoxModel.fireChange(0,size); // this includes nullObject!!!
                    afterChangeActiveObject(null);
                }
            });
        }
    }

    /**
        Hub event to notify that Hub has been sorted.
        The ComboBox will be updated to show new list.
    */
    public @Override void afterSort(HubEvent e) {
        onNewList(e);
    }

    //==============================================================================
    // note: these need to be ran in the AWT Thread, use SwingUtilities.invokeLater() to call these
    class MyComboBoxModel extends DefaultListModel implements ComboBoxModel {
         OANullObject empty = OANullObject.nullObject;
         boolean flag;

         public synchronized void fireChange(int index0, int index1) {
            // this will set selection back to 0, so we need to set a flag so
            // activeObject wont be changed
            flag = true;
            if (index0 >= 0) fireContentsChanged(comboBox, index0, index1);
            flag = false;
         }
         public synchronized void fireAdd(int index0, int index1) {
            // this will set selection back to 0, so we need to set a flag so
            // activeObject wont be changed
            flag = true;
            if (index0 >= 0) {
            	
                try {
                    fireIntervalAdded(comboBox, index0, index1);
                }
                catch (Exception e) {}
            }
            flag = false;
         }
         public synchronized void fireRemove(int index0, int index1) {
            // this will set selection back to 0, so we need to set a flag so
            // activeObject wont be changed
            flag = true;
            if (index0 >= 0) fireIntervalRemoved(comboBox, index0, index1);
            flag = false;
         }
         
         public synchronized void setSelectedItem(Object obj) {
            if (!flag && obj != getHub().getActiveObject()) {
                if (obj == empty) obj = null;
                Hub h = getHub();

                boolean b = getEnableUndo();
                if (b) {
                    OAUndoableEdit ue = OAUndoableEdit.createUndoableChangeAO(undoDescription, h, h.getAO(), obj);
                    String s = undoDescription;
                    if (s == null || s.length() == 0) s = ue.getPresentationName();
                    OAUndoManager.startCompoundEdit(s);
                    OAUndoManager.add(ue);
                }
                getHub().setActiveObject(obj);
                if (b) {
                    OAUndoManager.endCompoundEdit();
                }
            }
         }
         public Object getSelectedItem() {
            Object obj = getHub().getActiveObject();
            if (obj == null) obj = empty;
            return obj;
         }
         public Object getElementAt(int index) {
            if (getHub() == null) return empty;
            Object obj = getHub().elementAt(index);
            if (obj == null) obj = empty;
            return obj;
         }
         public int getSize() {
            Hub h = getHub();
            if (h == null) return (nullDescription==null)?0:1;
            if (h.isMoreData()) h.loadAllData();
            int x = h.getSize();
            if (nullDescription != null) x++;  // extra one for "blank" line
            return x;
         }
    }

    String match = "";  // string to match on incremental search
    public void focusGained(FocusEvent e) {
        match = "";
    }
    public void focusLost(FocusEvent e) {
    }


    /** called by MyListCellRenderer.getListCellRendererComponent */
    public Component getRenderer(Component renderer, JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
        String s;
        if (value == null) s = "   ";
        else if (value instanceof OANullObject) {
        	s = nullDescription;
        }
        else if (value instanceof String) {
        	s = (String) value;
        }
        else {
            Object obj = OAReflect.getPropertyValue(value, getGetMethods());
            s = OAConv.toString(obj, getFormat());
            if (s == null) {
            	s = getFormat();
                s = OAConv.toString(obj, s);
            }
            if (s.length() == 0) s = " ";  // if length == 0 then Jlist wont show any
        }
        if (renderer instanceof JLabel) {
            JLabel lbl = (JLabel) renderer;

            if (!isSelected) {
            	lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
            
            updateComponent(lbl, value, s);

            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } 
        }
        return renderer;
    }

    /**
        Default cell renderer.
    */
    class MyListCellRenderer extends JLabel implements ListCellRenderer {
        public MyListCellRenderer() {
            setOpaque(true);
        }

        /** will either call OAComboBox.getRenderer() or Hub2ComboBox.getRenderer() */
        public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
            if (Hub2ComboBox.this.list == null) Hub2ComboBox.this.list = list;
            
            Component comp;
            if (Hub2ComboBox.this.comboBox instanceof OAComboBox) {
                comp = ((OAComboBox)Hub2ComboBox.this.comboBox).getRenderer(this, list, value, index, isSelected, cellHasFocus);
            }
            else {
            	comp = Hub2ComboBox.this.getRenderer(this, list, value, index, isSelected, cellHasFocus);
            }
            return comp;
        }
    }
}

