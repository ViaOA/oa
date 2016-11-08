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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.WeakHashMap;

import javax.swing.JTable;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.hub.HubMerger;
import com.viaoa.jfc.console.Console;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;


/**
 * Acts as a console to display and scroll changes to a property.
 * 
 * Ex:  Message.text, where each change to text will be added to the display. 
 * 
 * @author vvia
 */
public class OAConsole extends OATable implements FocusListener, MouseListener {
    private Hub hubListen;
    private String property;
    private String listenProperty;
    private WeakHashMap<OAObject, Hub<Console>> hmConsole = new WeakHashMap<OAObject, Hub<Console>>();
    private int columns;
    private HubListener hubListener;
    private Hub hubFromMerger;
    private int maxRows = 500;
    
    public OAConsole(Hub hub, String property, int columns) {
        super(new Hub<Console>(Console.class));
        this.hubListen = hub;
        this.property = property;
        this.columns = columns;

        setSelectHub(new Hub(Console.class));

        setup();
    }

    
    @Override
    public void setSelectHub(Hub hub) {
        super.setSelectHub(hub);
    }

    public void setMaxRows(int x) {
        this.maxRows = x;
    }
    public int getMaxRows() {
        return maxRows;
    }
    
    
    public void close() {
        if (hubListener != null && hubListen != null) {
            hubListen.removeHubListener(hubListener);
        }
        if (hmConsole != null) hmConsole.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
    
    public void setup() {
        OALabel lbl;
        // addColumn("xxx", 10, new OALabel(getHub(), Console.P_DateTime, 10));
        addColumn("xxx", columns, new OALabel(getHub(), Console.P_Text, columns));
        setPreferredSize(12, 1);

        setTableHeader(null);
        setShowHorizontalLines(false);
        setAllowDnD(false);
        setAllowSorting(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        
        
        hubListener = new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                Object obj = e.getObject();
                if (!(obj instanceof OAObject)) {
                    OAConsole.this.getHub().setSharedHub(null);
                    return;
                }
                OAObject oaObj = (OAObject) obj;
                Hub<Console> h = hmConsole.get(oaObj);
                if (h == null) {
                    h = new Hub<Console>(Console.class);
                    hmConsole.put(oaObj, h);
                }
                getHub().setSharedHub(h);
            }
            
            @Override
            public void afterPropertyChange(HubEvent e) {
                if (property == null) return;
                
                String prop = e.getPropertyName();
                if (prop == null) return;
                if (!listenProperty.equalsIgnoreCase(prop)) return;
                
                if (getHub() == null) return;
                Object obj = e.getObject();
                if (obj == null) return;
                
                //qqqqqq there needs to be a flag to have it use AO or all objs
                if (obj != hubListen.getAO()) return;
                
                if (!(obj instanceof OAObject)) return;
                OAObject oaObj = (OAObject) obj;
                
                Console console = new Console();
                Object val = e.getNewValue();
                if (val == null) val = "";
                console.setText(""+val);
                if (hub.getSize() > maxRows) {
                    hub.remove(0);
                }
                hub.add(console);
                
                        
                if (!OAConsole.this.bHasFocus && !OAConsole.this.bHasMouse) {
                    boolean b;
                    if (hubFromMerger != null) b = hubFromMerger.contains(oaObj);
                    else b = (OAConsole.this.hubListen.getAO() == oaObj); 
                    if (b) {
                        int pos = OAConsole.this.getHub().getSize();
                        Rectangle rect = OAConsole.this.getCellRect(pos, 0, true);
                        try {
                            OAConsole.this.scrollRectToVisible(rect);
                        }
                        catch (Exception ex) {}
                        OAConsole.this.repaint();
                    }
                }
            }
            
            @Override
            public void afterRemove(HubEvent e) {
                Object obj = e.getObject();
                if (obj == null) return;
                if (!(obj instanceof OAObject)) return;
                OAObject oaObj = (OAObject) obj;
                hmConsole.remove(oaObj);
            }
            @Override
            public void beforeRemoveAll(HubEvent e) {
                hmConsole.clear();
            }
        };
        listenProperty = property;
        if (property != null) {
            if (property.indexOf('.') > 0) {
                hubFromMerger = new Hub();
                int dcnt = OAString.dcount(property, '.');
                String s = OAString.field(property, ".", 1, dcnt-1);
                new HubMerger(hubListen, hubFromMerger, s, false);
                listenProperty = OAString.field(property, ".", dcnt);
                hubFromMerger.addHubListener(hubListener, listenProperty, true);
            }
            else hubListen.addHubListener(hubListener, property, true);
        }
        
        addFocusListener(this);
        
        addMouseListener(this);
    }

    private volatile boolean bHasFocus;
    @Override
    public void focusGained(FocusEvent e) {
        bHasFocus = true;
    }
    @Override
    public void focusLost(FocusEvent e) {
        bHasFocus = false;
    }

    private volatile boolean bHasMouse;
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        bHasMouse = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        bHasMouse = false;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return d;
    }
    
//qqqqqqqqq toDo:    
    public void setVisible(Hub hub) {
    }    
    public void setVisible(Hub hub, String prop) {
    }    
    public void setVisible(Hub hub, String prop, Object compareValue) {
    }    
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }

    public void setEnabled(Hub hub, String prop) {
    }
    public void setEnabled(Hub hub, String prop, Object compareValue) {
    }
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }
    
}
