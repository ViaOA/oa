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
package com.viaoa.jfc.control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

/**
 * Controller for binding OA to JList.
 * @author vvia
 *
 */
public class ListController extends OAJfcController implements ListSelectionListener {
    JList list;
    MyListModel myListModel = new MyListModel();
    ListCellRenderer listCellRenderer;
    protected boolean bAllowDelete, bAllowInsert, bAllowRemove;
    protected HubListener hubMultiSelectListener;
    
    
    /**
        Create list that is bound to a property for the active object in a Hub.
    */
    public ListController(Hub hub, JList list, String propertyPath) {
        super(hub, null, propertyPath, false, list, HubChangeListener.Type.HubValid, true, true); 
        nullDescription = null;
        create(list, 7);
    }

    public ListController(Object object, JList list, String propertyPath) {
        super(null, object, propertyPath, false, list, HubChangeListener.Type.HubValid, true, true); // this will add hub listener
        nullDescription = null;
        create(list, 7);
    }

    /**
        Create list that is bound to a property for the active object in a Hub.
        @param visibleRowCount number of rows to visually display.
    */
    public ListController(Hub hub, JList list, String propertyPath, int visibleRowCount) {
        super(hub, null, propertyPath, false, list, HubChangeListener.Type.HubValid, true, true); // this will add hub listener
        nullDescription = null;
        create(list, visibleRowCount);
    }


    @Override
    public void setSelectHub(Hub newHub) {
        super.setSelectHub(newHub);

        ListSelectionModel model = list.getSelectionModel();
        if (hubSelect == null) {
            model.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        else {
            changeFlag = true;
            model.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            model.clearSelection();
            changeFlag = false;

            hubMultiSelectListener = new HubListenerAdapter() {
                public @Override void afterAdd(HubEvent e) {
                    if (selectionFlag) return;
                    if (e.getObject() != null) {
                        int x = getHub().getPos(e.getObject());
                        if (x >= 0) {
                            changeFlag = true;
                            ListController.this.list.getSelectionModel().addSelectionInterval(x, x);
                            changeFlag = false;
                        }
                    }
                }
                public @Override void afterInsert(HubEvent e) {
                    afterAdd(e);
                }
                public @Override void afterRemove(HubEvent e) {
                    if (selectionFlag) return;
                    if (e.getObject() != null) {
                        int x = getHub().getPos(e.getObject());
                        if (x >= 0) {
                            changeFlag = true;
                            ListController.this.list.getSelectionModel().removeSelectionInterval(x, x);
                            changeFlag = false;
                        }
                    }
                }
                @Override
                public void onNewList(HubEvent e) {
                	onNewSelectionHubList();
                }
            };
            hubSelect.addHubListener(hubMultiSelectListener);
        	onNewSelectionHubList();
        }
    }
    
    protected void onNewSelectionHubList() {
        if (hubSelect != null) {
            ListSelectionModel model = list.getSelectionModel();
            changeFlag = true;
            model.clearSelection();
            for (int i=0; ;i++) {
                Object obj = hubSelect.elementAt(i);
                if (obj == null) break;
                int pos = getHub().getPos(obj);
                if (pos >= 0) model.addSelectionInterval(pos, pos);
                else {
                    /* 20131224 dont remove, other hubs might not be updated yet
                    hubMultiSelect.remove(i);
                    i--;
                    */
                }
            }
            changeFlag = false;
        } 
    }

    @Override
    protected void reset() {
        super.reset();
        if (list != null) create(list, 7);
    }
    
    
    protected void create(JList list, int rows) {
        // called once by constructor
        if (this.list != null) this.list.getSelectionModel().removeListSelectionListener(this);
        this.list = list;

        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // list.setPrototypeCellValue("1234567890ABCDEF"); // this keeps JList from reading all rows, DONT REMOVE
        list.setVisibleRowCount(rows);
        
        list.getSelectionModel().addListSelectionListener(this);
        if (listCellRenderer == null) {
            list.setModel(myListModel);
        
            listCellRenderer = list.getCellRenderer();
            list.setCellRenderer(new MyListCellRenderer());
        }
        if (hubSelect == null) {
            // not needed? might need to be put in the addNotify() method
            // list.getUI().getList().setCellRenderer(new MyListCellRenderer());
            HubEvent e = new HubEvent(getHub(), getHub().getActiveObject());
            afterChangeActiveObject(e);
        }
        
        list.registerKeyboardAction( new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
                Hub h = getHub();
                Object ho = h.getActiveObject();
                int pos = h.getPos();
                if (ho != null && pos > 0) {
                    h.move(pos,pos-1);
                    HubAODelegate.setActiveObjectForce(h, ho);
                }
            }
        },  KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        
        list.registerKeyboardAction( new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
                Hub h = getHub();
                Object ho = h.getActiveObject();
                int pos = h.getPos();
                if (ho != null && pos+1 != h.getSize()) {
                    h.move(pos,pos+1);
                    HubAODelegate.setActiveObjectForce(h, ho);
                }
            }
        },  KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK, false), JComponent.WHEN_FOCUSED);

        list.registerKeyboardAction( new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
                if (!bAllowDelete || !bAllowRemove) return;
                Hub h = getHub();
                Object ho = h.getActiveObject();
                if (ho != null && (ho instanceof OAObject)) {
                    if (bAllowDelete) ((OAObject)ho).delete();
                    else if (bAllowRemove) h.remove(ho);
                }
            }
        },  KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true), JComponent.WHEN_FOCUSED);
    
        list.registerKeyboardAction( new ActionListener() {
            public void actionPerformed(ActionEvent e) {  
                if (!bAllowInsert) return;
                Hub hub = getHub();
                if (hub == null) return;
                Class c = hub.getObjectClass();
                if (c == null) return;
                Object obj = OAObjectReflectDelegate.createNewObject(c);

                int pos = hub.getPos();
                if (pos < 0) pos = 0;
                hub.insert(obj, pos);
                
                if (getEnableUndo()) OAUndoManager.add(OAUndoableEdit.createUndoableInsert(undoDescription, hub, obj, pos));
                hub.setActiveObject(obj);
            }
        },  KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0, true), JComponent.WHEN_FOCUSED);
    }

    
    
    /**
        Flag to know if a row/object can be removed from the Hub by using the [Delete] key.
    */
    public void setAllowRemove(boolean b) {
        bAllowRemove = b;
    }
    /**
        Flag to know if a row/object can be removed from the Hub by using the [Delete] key.
    */
    public boolean getAllowRemove() {
        return bAllowRemove;
    }

    /**
        Flag to know if a row/object can be deleted by using the [Delete] key.
    */
    public void setAllowDelete(boolean b) {
        bAllowDelete = b;
    }
    /**
        Flag to know if a row/object can be deleted by using the [Delete] key.
    */
    public boolean getAllowDelete() {
        return bAllowDelete;
    }

    /**
        Flag to know if a new row/object can be inserted by using the [Insert] key.
    */
    public void setAllowInsert(boolean b) {
        bAllowInsert = b;
    }
    /**
        Flag to know if a new row/object can be inserted by using the [Insert] key.
    */
    public boolean getAllowInsert() {
        return bAllowInsert;
    }
    
    public void close() {
        if (this.list != null) this.list.getSelectionModel().removeListSelectionListener(this);
        super.close();  // this will call hub.removeHubListener()
    }


    // ListSelectionModel Events
    boolean changeFlag, selectionFlag;

    /**
        JList Event used to know that a new row was selected.  This will change the 
        active object in the Hub, and/or update the multi-select Hub with value
        selected/deselected.
    */
    public synchronized void valueChanged(ListSelectionEvent e) {
        if (changeFlag) return;
        try {
            selectionFlag = true;
            if (hubSelect != null) {
                if (e.getValueIsAdjusting()) return;
                int pos1 = e.getFirstIndex();
                int pos2 = e.getLastIndex();
                ListSelectionModel model = list.getSelectionModel();
                for (int i=pos1; i<=pos2; i++) {
                    Object obj = getHub().elementAt(i);
                    if (model.isSelectedIndex(i)) {
                        if (!hubSelect.contains(obj)) hubSelect.add(obj);
                    }
                    else {
                        hubSelect.remove(obj);
                    }
                }
            }
            else {
                int row = list.getSelectionModel().getMinSelectionIndex(); // dont use e.first or e.last
                
                // 20181018
                String s = isValidHubChangeAO(hub.getAt(row));
                boolean b = true;
                if (OAString.isNotEmpty(s)) {
                    b = false;
                    JOptionPane.showMessageDialog(ListController.this.list, s, "Warning", JOptionPane.WARNING_MESSAGE);
                }
                else if (!confirmHubChangeAO(hub.getAt(row))) {
                    b = false;
                }
                
                if (b) {
                    // 20181006
                    //was: if (row != -1) {
                        if (getHub().getLinkHub() != null) { // 20140501 dont need undoable if not linked
                            OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(undoDescription, getHub(), getHub().getAO(), getHub().elementAt(row)));
                        }
                        getHub().setActiveObject(row);
                    //was: }
                }
            }            
        }
        finally {
            selectionFlag = false;
        }
    }
    
    /**
        Hub Event that will insert the object into the List.
    */
    public @Override void afterInsert(HubEvent e) {
        invoker(e.getPos(), true);
    }

    /**
        Hub Event that will add the object to the List.
    */
    public @Override void afterAdd(HubEvent e) {
        invoker(e.getPos(), true);
    }

    /**
        Hub Event that will remove the object to the List.
    */
    public @Override void afterRemove(HubEvent e) {
        invoker(e.getPos(), false);
    }

    public @Override void afterMove(HubEvent e) {
        final int fromPos = e.getPos();
        final int toPos = e.getToPos();
        if (SwingUtilities.isEventDispatchThread()) {
            myListModel.fireMove(fromPos, toPos);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {   
                    myListModel.fireMove(fromPos, toPos);
                }
            });
        }
    }

    /**
        Hub Event that will refresh the List.
    */
    public @Override void onNewList(HubEvent e) {
        if (SwingUtilities.isEventDispatchThread()) {
            myListModel.fireNewList();
            onNewSelectionHubList(); // added 20100326
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {   
                    myListModel.fireNewList();
                    onNewSelectionHubList(); // added 20100326
                }
            });
        }
    }

    /**
        Hub Event that will refresh the List.
    */
    public @Override void afterSort(HubEvent e) {
        onNewList(e);
    }
    
    /**
        Hub Event that will change the rows in the List that are selected.
    */
    public @Override synchronized void afterChangeActiveObject() {
        if (!selectionFlag && hubSelect == null && list != null) {
            changeFlag = true;
            Object oaObject = getHub().getActiveObject();
            if (oaObject == null) {
                list.clearSelection();
                list.scrollRectToVisible(new Rectangle(0,0,1,1));
            }
            else list.setSelectedValue(oaObject,true);
            changeFlag = false;
        }
    }

    /**
        Hub Event that a property has changed, causing a repaint on the List.
    */
    public @Override void afterPropertyChange() {
        list.repaint();
    }

    protected void invoker(final int pos, final boolean bAdd) {
    	if (pos < 0) return;
    	if (SwingUtilities.isEventDispatchThread()) {
            if (bAdd) myListModel.fireAdd(pos);
            else myListModel.fireRemove(pos);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {   
                    if (bAdd) myListModel.fireAdd(pos);
                    else myListModel.fireRemove(pos);
                }
            });
        }
    }

    

    //==============================================================================
    // all methods must be called by the AWT Thread
    class MyListModel extends AbstractListModel {
         boolean loadingFlag;
         public void fireAdd(int pos) {
            if (!loadingFlag) {
                changeFlag = true;
                fireIntervalAdded(this, pos, pos);
                changeFlag = false;
            }
         }
         public void fireAdd(int pos, int pos2) {
            if (!loadingFlag) {
                changeFlag = true;
                fireIntervalAdded(this, pos, pos2);
                changeFlag = false;
            }
         }
         public void fireRemove(int pos) {
            if (!loadingFlag) {
                changeFlag = true;
                fireIntervalRemoved(this, pos, pos);
                changeFlag = false;
            }
         }
         public void fireMove(int fromPos, int toPos) {
             changeFlag = true;
             fireContentsChanged(this, fromPos, toPos);
             changeFlag = false;
      }
         public void fireNewList() {
                fireContentsChanged(this, 0, getHub().getSize());
         }
         public Object getElementAt(int row) {
            if (getHub() == null) return new String("");
            Object obj = getHub().getAt(row);
            Hub h = getHub();
            if (h != null && !loadingFlag && h.isMoreData() && row+5 > h.getSize()) {
                loadingFlag = true;
                h.elementAt(row + 5);
                loadingFlag = false;
                fireAdd(row, h.getSize()-1);
            }
            if (obj == null) {
                if (nullDescription != null && getHub().getAt(row-1) != null) return nullDescription;
            	return "";
            }
            return obj;
         }

         public int getSize() {
            if (getHub() == null) return nullDescription==null?0:1;
            int i = getHub().getSize() + ((nullDescription==null)?0:1);
            
            return i;
         }
         
    }



    /**
        Custom renderer component used to display each row in List.
    */
    public Component getRenderer(Component renderer, JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
        boolean bDone = false;
        
        String s = null;
        if (value instanceof String) s = (String) value; // JList will send "setPrototypeCellValue" for measuring
        else if (value == null) s = "";
        else {
            // 20181004
            if (renderer instanceof JLabel) {
                update((JComponent) renderer, value, true);
                bDone = true;
            }
            //was: s = getValueAsString(value, getFormat());
        }
        if (!bDone) s = getDisplayText(value, s);
        
        if (renderer instanceof JLabel) {
            JLabel lbl = (JLabel) renderer;
            if (!bDone) lbl.setText(s);

            if (!isSelected) {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }

            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            }
        }
        else if (renderer instanceof JRadioButton) {
        	JRadioButton rad = (JRadioButton) renderer;
            if (!isSelected) {
                rad.setBackground(list.getBackground());
                rad.setForeground(list.getForeground());
                rad.setSelected(false);
            }
            rad.setText(s);
        	update(rad, value, false);
            if (isSelected) {
                rad.setBackground(list.getSelectionBackground());
                rad.setForeground(list.getSelectionForeground());
                rad.setSelected(true);
            }
        }
        return renderer;
    }
    
    //==============================================================================
    class MyListCellRenderer extends JLabel implements ListCellRenderer {
        public MyListCellRenderer() {
            setOpaque(true);
        }
        public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
            return ListController.this.getRenderer(this, list, value, index, isSelected, cellHasFocus);
        }
    }

}

