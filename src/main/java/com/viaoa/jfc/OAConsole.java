package com.viaoa.jfc;

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
import com.viaoa.jfc.console.Console;
import com.viaoa.object.OAObject;


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
    private WeakHashMap<OAObject, Hub<Console>> hmConsole = new WeakHashMap<OAObject, Hub<Console>>();
    private int columns;
    private HubListener hubListener; 
    
    public OAConsole(Hub hub, String property, int columns) {
        super(new Hub<Console>(Console.class));
        this.hubListen = hub;
        this.property = property;
        this.columns = columns;
        setup();
    }
    
    public void close() {
        if (hubListener != null && hubListen != null) {
            hubListen.removeHubListener(hubListener);
        }
        if (hmConsole != null) hmConsole.clear();
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
                int pos = property.lastIndexOf('.');
                if (pos > 0) {
                    if (!prop.equalsIgnoreCase("xxxConsole")) return;
                    //if (!property.substring(pos+1).equalsIgnoreCase(prop)) return;
                }
                else if (!property.equalsIgnoreCase(prop)) return;
                
                if (getHub() == null) return;
                Object obj = e.getObject();
                if (obj == null) return;
                if (!(obj instanceof OAObject)) return;
                OAObject oaObj = (OAObject) obj;
                
                Console console = new Console();
                Object val = e.getNewValue();
                if (val == null) val = "";
                console.setText(""+val);
                if (hub.getSize() > 2500) {
                    hub.remove(0);
                }
                hub.add(console);
                
                Object ao = OAConsole.this.hubListen.getAO();
                        
                if (!OAConsole.this.bHasFocus && !OAConsole.this.bHasMouse && ao == oaObj) {
                    pos = OAConsole.this.getHub().getSize();
                    Rectangle rect = OAConsole.this.getCellRect(pos, 0, true);
                    try {
                        OAConsole.this.scrollRectToVisible(rect);
                    }
                    catch (Exception ex) {}
                    OAConsole.this.repaint();
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
        if (property != null) {
            if (property.indexOf('.') > 0) hubListen.addHubListener(hubListener, "xxxConsole", new String[]{property});
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
    
}
