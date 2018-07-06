/* Copyright 1999-2015 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
package com.viaoa.jfc.control;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.object.OAObject;

/**
 * Functionality for binding JComboBox to OA.
 * 
 * @author vvia
 */
public class ComboBoxController extends JFCController implements FocusListener {
    JComboBox comboBox;
    MyComboBoxModel myComboBoxModel = new MyComboBoxModel();
    JList list;

    public ComboBoxController(JComboBox cb) {
        super(cb);
        create(cb);
    }

    public ComboBoxController(Hub hub, JComboBox cb, String propertyPath) {
        super(hub, propertyPath, cb); // this will add hub listener
        create(cb);
    }

    public ComboBoxController(Object object, JComboBox cb, String propertyPath) {
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
     * Description to use for Undo and Redo presentation names.
     * 
     * @see OAUndoableEdit#setPresentationName
     */
    public void setUndoDescription(String s) {
        undoDescription = s;
    }

    /**
     * Description to use for Undo and Redo presentation names.
     * 
     * @see OAUndoableEdit#setPresentationName
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
                myComboBoxModel.flag = true; // this will keep the activeObject
                                             // from getting changed. JComboBox
                                             // sets selectetPos to "0"

                comboBox.setModel(myComboBoxModel);
                comboBox.setRenderer(new MyListCellRenderer());
                myComboBoxModel.flag = false;

                comboBox.addFocusListener(this);
                // not needed? might need to be put in the addNotify() method
                // comboBox.getUI().getList().setCellRenderer(new
                // MyListCellRenderer());
                HubEvent e = new HubEvent(getHub(), getHub().getActiveObject());
                this.afterChangeActiveObject(e); // this will set selectedPos in
                                                 // JComboBox
                myComboBoxModel.fireChange(-1, -1); // hack: must initialize
                                                    // listeners ?!?!?

                if (getHub() != null) {
                    getEnabledController().add(getHub(), null, OAAnyValueObject.instance); // so that it will verify that hub is valid
                }
            }
        }
    }

    protected void resetHubOrProperty() { // called when Hub or PropertyName is
                                          // changed
        super.resetHubOrProperty();
        if (comboBox != null) create(comboBox);
    }

    public void close() {
        if (comboBox != null) comboBox.removeFocusListener(this);
        super.close(); // this will call hub.removeHubListener()
    }

    /**
     * Hub event to updated selected value.
     */
    public @Override void afterChangeActiveObject(HubEvent evt) {
        if (getHub() == null) return;
        Object oaObject = getHub().getActiveObject(); // use hub instead of
                                                      // actualHub

        if (comboBox != null) {
            if (evt != null) {
                myComboBoxModel.flag = true;
                comboBox.setSelectedItem(oaObject);
                myComboBoxModel.flag = false;
            }
            if (list != null) {
                if (oaObject == null) oaObject = OANullObject.instance;
                myComboBoxModel.flag = true;
                try {
                    list.setSelectedValue(oaObject, true);
                }
                catch (Exception e) {
                }
                myComboBoxModel.flag = false;
            }
            update();
            comboBox.repaint();
        }
    }

    /**
     * Hub property change event that causes ComboBox to be repainted.
     */
    public @Override void afterPropertyChange(HubEvent e) {
        if (isForThisHub(e) && comboBox != null && e.getPropertyName().equalsIgnoreCase(getHubListenerPropertyName())) {
            comboBox.repaint();
        }
    }

    /**
     * Hub insert event to update row in ComboBox.
     */
    public @Override void afterInsert(HubEvent e) {
        if (!isForThisHub(e)) return;
        final int pos = e.getPos();
        if (SwingUtilities.isEventDispatchThread()) {
            myComboBoxModel.fireChange(pos, pos);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    myComboBoxModel.fireChange(pos, pos);
                }
            });
        }
    }

    /**
     * Hub add event to add a row in ComboBox.
     */
    @Override
    public void afterAdd(HubEvent e) {
        if (!isForThisHub(e)) return;
        final Object obj = e.getObject();

        Hub h = getHub();
        final int pos = h.getPos(obj);

        if (SwingUtilities.isEventDispatchThread()) {
            myComboBoxModel.fireAdd(pos, pos);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    myComboBoxModel.fireAdd(pos, pos);
                }
            });
        }
    }

    /**
     * Hub add event to remove a row in ComboBox.
     */
    @Override
    public void afterRemove(HubEvent e) {
        if (!isForThisHub(e)) return;
        final int pos = e.getPos();
        if (pos < 0) return;
        if (SwingUtilities.isEventDispatchThread()) {
            myComboBoxModel.fireRemove(pos, pos);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    myComboBoxModel.fireRemove(pos, pos);
                }
            });
        }
    }

    /**
     * Hub event to notify that a new list of objects has been loaded into the Hub. The ComboBox will be
     * updated to show new list.
     */
    public @Override void onNewList(HubEvent e) {
        if (!isForThisHub(e)) return;
        if (myComboBoxModel == null) return;
        final int size = getHub().getSize();
        if (SwingUtilities.isEventDispatchThread()) {
            myComboBoxModel.fireChange(0, size); // this includes nullObject!!!
            afterChangeActiveObject(null);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    myComboBoxModel.fireChange(0, size); // this includes
                                                         // nullObject!!!
                    afterChangeActiveObject(null);
                }
            });
        }
    }

    /**
     * Hub event to notify that Hub has been sorted. The ComboBox will be updated to show new list.
     */
    public @Override void afterSort(HubEvent e) {
        onNewList(e);
    }

    // ==============================================================================
    // note: these need to be ran in the AWT Thread, use
    // SwingUtilities.invokeLater() to call these
    class MyComboBoxModel extends DefaultListModel implements ComboBoxModel {
        OANullObject empty = OANullObject.instance;
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
                catch (Exception e) {
                }
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
            if (obj instanceof OANullObject) obj = null;
            if (!flag && obj != getHub().getActiveObject()) {
                Hub h = getHub();

                boolean b = getEnableUndo();
                if (b) {
                    OAUndoableEdit ue = OAUndoableEdit.createUndoableChangeAO(undoDescription, h, h.getAO(), obj);
                    String s = undoDescription;
                    if (s == null || s.length() == 0) s = ue.getPresentationName();
                    OAUndoManager.startCompoundEdit(s);
                    OAUndoManager.add(ue);
                }

                String msg = validateNewValue(obj, obj);
                if (msg == null) {
                    msg = isValid(obj, obj);
                }
                if (msg != null) {
                    JOptionPane.showMessageDialog(SwingUtilities.getRoot(comboBox), "Invalid selection\n" + msg, "Invalid selection",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                getHub().setActiveObject(obj);
                if (b) {
                    OAUndoManager.endCompoundEdit();
                }
                onItemSelected(getHub().getPos());
            }
        }

        public Object getSelectedItem() {
            Hub h = getHub();
            Object obj = h.getActiveObject();

            if (obj == null) {
                obj = getLinkedToValue();
            }

            if (obj == null) {
                obj = empty;
            }
            return obj;
        }

        public Object getElementAt(int index) {
            Hub h = getHub();
            if (h == null) return empty;
            Object obj = h.elementAt(index);
            if (obj == null && index == h.getSize()) {
                // 201207 might have a place holder for the linked to object that is not in this hub
                obj = getLinkedToValue();
                if (h.contains(obj)) obj = null;
            }

            if (obj == null) obj = empty;
            return obj;
        }

        public int getSize() {
            Hub h = getHub();
            if (h == null) return (nullDescription == null) ? 0 : 1;
            if (h.isMoreData()) h.loadAllData();
            int x = h.getSize();
            if (nullDescription != null) x++; // extra one for "blank" line

            if (h.getAO() == null) {
                if (getLinkedToValue() != null) x++;
            }

            return x;
        }
    }

    String match = ""; // string to match on incremental search

    public void focusGained(FocusEvent e) {
        match = "";
    }

    public void focusLost(FocusEvent e) {
    }

    public void onItemSelected(int row) {
    }

    // 20120928 replaces method from 7/11, which was not getting the linked value correctly
    protected Object getLinkedToValue() {
        Hub h = getHub();
        if (h == null) return null;

        /*Object obj = h.getAO(); if (obj != null) return obj; */

        Hub hx = h.getLinkHub();
        if (hx == null) return null;

        Object objx = HubLinkDelegate.getPropertyValueInLinkedToHub(h, hx.getAO());
        if (!(objx instanceof OAObject)) return null;

        return objx;
    }

    /* was: / ** 20120711 This is in case the value is not in the Hub. Need to get it from the linked to
     * Hub / protected Object getRealValue() { Hub h = getHub(); if (h == null) return null; Object obj
     * = h.getAO(); if (obj != null) return obj;
     * 
     * Hub hx = h.getLinkHub(); if (hx == null) return null;
     * 
     * Object objx = h.getMasterObject(); if (!(objx instanceof OAObject)) return null;
     * 
     * objx = OAObjectReflectDelegate.getProperty((OAObject) objx,
     * HubDetailDelegate.getPropertyFromDetailToMaster(h));
     * 
     * return objx; } */

    /** called by MyListCellRenderer.getListCellRendererComponent */
    protected Component getRenderer(Component renderer, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String s;

        if (index < 0 && value instanceof OANullObject) {
            // 20120711 see if it needs to show the selected value (if it is not
            // in the Hub)
            Object objx = getLinkedToValue();
            if (objx != null) value = objx;
        }

        if (value == null) s = null;
        else if (value instanceof OANullObject) {
            s = nullDescription;
        }
        else if (value instanceof String) {
            s = (String) value;
        }
        else {
            Object obj = getPropertyPathValue(value);
            // Object obj = OAReflect.getPropertyValue(value, getGetMethods());
            s = OAConv.toString(obj, getFormat());
            if (s == null) {
                s = getFormat();
                s = OAConv.toString(obj, s);
            }
        }

        if (s == null || s.length() == 0) s = " "; // if length == 0 then Jlist
                                                   // wont show any

        if (renderer instanceof JLabel) {
            JLabel lbl = (JLabel) renderer;

            if (!isSelected) {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }

            lbl.setText(s);
            
            if (index >= 0) update(lbl, value);

            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            }
        }
        return renderer;
    }

    /**
     * Default cell renderer.
     */
    class MyListCellRenderer extends JLabel implements ListCellRenderer {
        public MyListCellRenderer() {
            setOpaque(true);
        }

        /**
         * will either call OAComboBox.getRenderer() or Hub2ComboBox.getRenderer()
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (ComboBoxController.this.list == null) ComboBoxController.this.list = list;

            Component comp = ComboBoxController.this.getRenderer(this, list, value, index, isSelected, cellHasFocus);
            return comp;
        }
    }

}
