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
    
    public OAConsole(Hub hub, String property) {
        super(new Hub<Console>(Console.class));
        this.hubListen = hub;
        this.property = property;
        setup();
    }
    
    public void setup() {
        OALabel lbl;
        // addColumn("xxx", 10, new OALabel(getHub(), Console.P_DateTime, 10));
        addColumn("xxx", 20, new OALabel(getHub(), Console.P_Text, 20));

        setTableHeader(null);
        setShowHorizontalLines(false);
        setAllowDnD(false);
        setAllowSorting(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        
        hubListen.addHubListener(new HubListenerAdapter() {
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
                if (!property.equalsIgnoreCase(e.getPropertyName())) return;
                if (getHub() == null) return;
                Object obj = e.getObject();
                if (obj == null) return;
                if (!(obj instanceof OAObject)) return;
                OAObject oaObj = (OAObject) obj;
                
                Console console = new Console();
                Object val = oaObj.getProperty(property);
                if (val == null) val = "";
                console.setText(""+val);
                hub.add(console);
                if (hub.getSize() > 1000) {
                    hub.remove(0);
                }
                
                Object ao = OAConsole.this.hubListen.getAO();
                        
                if (!OAConsole.this.bHasFocus && !OAConsole.this.bHasMouse && ao == oaObj) {
                    int pos = OAConsole.this.getHub().getSize();
                    Rectangle rect = OAConsole.this.getCellRect(pos, 0, true);
                    OAConsole.this.scrollRectToVisible(rect);
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
        });
    
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
