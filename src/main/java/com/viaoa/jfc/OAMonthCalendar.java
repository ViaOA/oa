package com.viaoa.jfc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubAODelegate;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.jfc.OAButton;
import com.viaoa.jfc.OAColorIcon;
import com.viaoa.jfc.OADateComboBox;
import com.viaoa.jfc.OAJfcUtil;
import com.viaoa.jfc.OAList;
import com.viaoa.jfc.OATextField;
import com.viaoa.model.oa.VDate;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.util.OADate;
import com.viaoa.util.OAString;

/**
 * UI for displaying items in a Calendar.
 * Given a hub and date properties to use, this will then display them by day.
 * 
 * If the date property is unique (only one) then another property (many) can be used for this display of objects
 * for that day, and an optional create command can be added for days that dont have an object that exists.
 * 
 * If date is not unique, then they will be combined by day.
 * 
 */
public class OAMonthCalendar<F extends OAObject, T extends OAObject> extends JScrollPane {
    
    protected Hub<F> hub;
    protected Hub<T> hubDetail;
    protected String propertyPath;  // list display pp
    protected String[] datePropertyPaths; 
    
    protected VDate vdCalendar;
    protected OADate dateLastBegin;

    protected ArrayList<DayPanel> alDayPanel;
    protected JPanel panDays;
    protected JPanel panHeader;
    protected OADateComboBox dcboCalendar;

    protected String displayTemplate;
    protected String toolTipTextTemplate;
    protected String iconColorPropertyPath;
    
    protected static final Color colorSelected = (ColorUIResource) UIManager.get("TabbedPane.focus");
    protected static final Border borderSelected = new LineBorder(colorSelected, 2);

    protected static final Color colorUnselected = (ColorUIResource) UIManager.get("TabbedPane.background");
    protected static final Border borderUnselected = new LineBorder(colorUnselected, 2);

    protected Icon iconSquare = new OAColorIcon(colorUnselected, 8, 8);

    protected boolean bAllowCreateNew;
    
    
    /**
     * Used to display 0+ objetcts by date.
     */
    public OAMonthCalendar(Hub<F> hub, String propertyPath, String[] datePropertyPaths) {
        this.hub = hub;
        this.propertyPath = propertyPath;
        this.datePropertyPaths = datePropertyPaths;
        setup();
        setSelectedDate(new OADate());
    }

    /**
     * Used when there is only one object with date (unique), and want to display another detail hub.
     */
    public OAMonthCalendar(Hub<F> hub, String datePropertyPath, Hub<T> hubDetail) {
        this.hub = hub;
        this.hubDetail = hubDetail;
        this.datePropertyPaths = new String[] { datePropertyPath };
        setup();
        setSelectedDate(new OADate());
    }
    public OAMonthCalendar(Hub<F> hub, String datePropertyPath, Hub<T> hubDetail, String propertyPath) {
        this.hub = hub;
        this.hubDetail = hubDetail;
        this.datePropertyPaths = new String[] { datePropertyPath };
        this.propertyPath = propertyPath;
        setup();
        setSelectedDate(new OADate());
    }
    

    public void setAllowCreateNew(boolean b) {
        this.bAllowCreateNew = b;
    }
    public boolean getAllowCreateNew() {
        return this.bAllowCreateNew;
    }
    
    public void setSelectedDate(OADate dx) {
        setSelectedDate(dx, false);
    }
    
    public Hub<F> getHub() {
        return hub;
    }
    public Hub<T> getDetailHub() {
        return hubDetail;
    }

    /**
     * First day that is displayed in the UI.
     */
    public OADate getBeginDate() {
        return new OADate(alDayPanel.get(0).date);
    }
    /**
     * Last day that is displayed in the UI.
     */
    public OADate getEndDate() {
        return new OADate(alDayPanel.get(alDayPanel.size()-1).date);
    }
    

    /**
     * called when a date is selected.
     * @param date 
     * @param hub used to find the AO that was selected
     */
    protected void onDaySelected(OADate date, Hub<F> hub, Hub<T> hubDetail) {
        getHub().setAO(hub.getAO());
        if (hubDetail != null && getDetailHub() != null) {
            getDetailHub().setAO(hubDetail.getAO());
        }
    }
    
    
    /**
     * Called when a new month is being displayed.
     * This is used when working with objectCache, so that objects can be loaded from datasource that match the selected date range for the new month.
     * @see #getBeginDate()
     * @see #getEndDate()
     */
    protected void onNewMonth() {
    }

    private OADate lastSelectedDate;
    
    /**
     * called by UI or manually, to display the new day.  If the month is not currently displayed, then onNewMonth will also be called.
     * @param dx date to display.
     * @param bFromDayPanel to know if this was called by user selecting one of the current days.
     */
    protected void setSelectedDate(OADate dx, boolean bFromDayPanel) {
        if (dx == null) return;
        dx = new OADate(dx);

        if (dx.equals(lastSelectedDate)) return;
        lastSelectedDate = new OADate(dx);
        
        vdCalendar.setValue(new OADate(dx));
        
        final int month = bFromDayPanel ? alDayPanel.get(20).date.getMonth() :dx.getMonth(); 
        boolean bNewMonth;
        if (bFromDayPanel) {
            dx = dateLastBegin;
            bNewMonth = false;
        }
        else {
            dx.setDay(1);
            int dow = dx.getDayOfWeek();
            
            int x = (dow-Calendar.SUNDAY);
            dx = (OADate) dx.addDays(-x);
            
            bNewMonth = !dx.equals(dateLastBegin);
            dateLastBegin = dx;
        }
        
        int i = 0;
        boolean bVis = true;
        if (bNewMonth) {
            for (DayPanel dp : alDayPanel) {
                dp.date = dx;
                dp.lbl.setText(dx.toString());
                
                if (i++ == 35) {
                    if (dx.getDay() < 20) bVis = false;
                }
                dp.setVisible(bVis);
                dx = (OADate) dx.addDay();

                dp.hub.clear();
            }
        
            // reload
            for (F obj : hub) {
                for (String pp : datePropertyPaths) {
                    dx = (OADate) obj.getProperty(pp);
                    if (dx == null) continue;
        
                    for (DayPanel dp : alDayPanel) {
                        int x = dp.date.compare(dx);
                        if (x == 0) {
                            dp.hub.add(obj);
                            break;
                        }
                        if (x > 0) break;
                    }
                }
            }
        }        
        
        for (DayPanel dp : alDayPanel) {
            //dp.spLst.setVisible(dp.hub.getSize() > 0);
            dp.panCmd.setVisible(bAllowCreateNew && dp.hub.getSize() == 0);
            if (dp.hub.getSize() == 0) dp.lbl.setText(dp.date.toString());
            
            final boolean b = dp.date.equals(vdCalendar.getValue());
            
            if (b) {
                // if (dp.bLabelSetText || dp.hub.getSize() == 0) dp.lbl.setIcon(null);
                dp.lst.setBackground(Color.WHITE);
                dp.scrollRectToVisible(new Rectangle(0, 0, 10, dp.getHeight()));
            }
            else {
                if (dp.date.getMonth() != month) {
                    dp.lbl.setForeground(Color.GRAY);
                    if (dp.bLabelSetText || dp.hub.getSize() == 0) {
                        // dp.lbl.setIcon(null);
                    }
                    dp.lst.setBackground(new Color(245,245,245));
                }
                else {
                    if (dp.bLabelSetText || dp.hub.getSize() == 0 || dp.lbl.getIcon() == null) {
                        dp.lbl.setIcon(iconSquare);
                    }
                    dp.lst.setBackground(Color.WHITE);
                }
            }
            dp.setSelected(b);
        }
        if (bNewMonth) onNewMonth();
    }
    

    protected void setup() {
        if (vdCalendar != null) return;
        // used to manage selected date 
        vdCalendar = new VDate();
        
        // create daily lists
        alDayPanel = new ArrayList<>();
        for (int i=0; i<42; i++) {  // 6 rows of 7 cols
            final DayPanel dp = new DayPanel() {
                @Override
                protected void onMouseClick() {
                    setSelectedDate(this.date, true);
                    onDaySelected(this.date, this.hub, hubDetail);
                }
            };
            alDayPanel.add(dp);
            
            if (OAMonthCalendar.this.hubDetail != null) {
                dp.hubDetail.addHubListener(new HubListenerAdapter() {
                    public void afterChangeActiveObject(HubEvent e) {
                        if (dp.hubDetail.getAO() != null) {
                            setSelectedDate(dp.date, true);
                            onDaySelected(dp.date, dp.hub, dp.hubDetail);
                        }
                    }
                });
            }
            else {
                dp.hub.addHubListener(new HubListenerAdapter() {
                    public void afterChangeActiveObject(HubEvent e) {
                        if (dp.hub.getAO() != null) {
                            setSelectedDate(dp.date, true);
                            onDaySelected(dp.date, dp.hub, dp.hubDetail);
                        }
                    }
                });
            }
        }
        
        setupHubs();
        setViewportView(getDaysPanel());
        setColumnHeaderView(getHeaderPanel());
    }        

    protected void setupHubs() {

        if (hubDetail != null) {
            hubDetail.addHubListener(new HubListenerAdapter<T>() {
                @Override
                public void afterChangeActiveObject(HubEvent<T> e) {
                    T obj = e.getObject();
                    if (obj == null) return;
                    OADate d = vdCalendar.getValue();
                    if (d == null) return;
                    
                    for (DayPanel dp : alDayPanel) {
                        if (!d.equals(dp.date)) continue;
                        dp.hubDetail.setAO(obj);
                        break;
                    }
                    
                }            
            });
        }
        
        // hub for storing all workorders with deliveryDate for the selected month
        final String propNamex = "calcCalendarProp";
        hub.addHubListener(new HubListenerAdapter<F>() {
            @Override
            public void afterChangeActiveObject(HubEvent<F> e) {
                F obj = e.getObject();
                if (obj == null) return;
                OADate dx = (OADate) obj.getProperty(datePropertyPaths[0]);
                if (dx == null) return;
                
                if (!dx.equals(OAMonthCalendar.this.vdCalendar.getValue())) {
                    setSelectedDate(dx, false);
                }
                if (OAMonthCalendar.this.hubDetail == null) {
                    for (DayPanel dp : alDayPanel) {
                        int x = dp.date.compare(dx);
                        if (x == 0) {
                            dp.hub.setAO(obj);
                            break;
                        }
                        if (x > 0) break;
                    }
                }
            }
            
            @Override
            public void afterPropertyChange(HubEvent<F> e) {
                if (!propNamex.equalsIgnoreCase(e.getPropertyName())) return;
                remove(e.getObject());
                add(e.getObject());
            }
            public void afterRemove(HubEvent<F> e) {
                remove(e.getObject());
            }
            void remove(F obj) {
                if (obj == null) return;
                for (DayPanel dp : alDayPanel) {
                    if (dp.hub.remove(obj)) {
                        if (OAMonthCalendar.this.hubDetail != null) {
                            //dp.spLst.setVisible(false);
                            if (bAllowCreateNew) dp.panCmd.setVisible(true);
                        }
                        break;
                    }
                }
            }
            public void afterNewList(HubEvent<F> e) {
                for (DayPanel dp : alDayPanel) {
                    dp.hub.clear();
                    if (OAMonthCalendar.this.hubDetail != null) {
                        //dp.spLst.setVisible(false);
                        if (bAllowCreateNew) dp.panCmd.setVisible(true);
                    }
                }
                for (F obj : hub) {
                    add(obj);
                }
            }
            public void afterAdd(HubEvent<F> e) {
                add(e.getObject());
            }
            public void afterInsert(HubEvent<F> e) {
                add(e.getObject());
            }
            void add(F obj) {
                // put in the correct day list
                for (String pp : datePropertyPaths) {
                    OADate dx = (OADate) obj.getProperty(pp);
                    if (dx == null) continue;
    
                    for (DayPanel dp : alDayPanel) {
                        int x = dp.date.compare(dx);
                        if (x == 0) {
                            dp.hub.add(obj);
                            if (OAMonthCalendar.this.hubDetail != null) {
                                //dp.spLst.setVisible(true);
                                if (bAllowCreateNew) dp.panCmd.setVisible(false);
                            }
                            break;
                        }
                        if (x > 0) break;
                    }
                }
            }
        }, propNamex, datePropertyPaths);
        
    }

    protected JPanel getDaysPanel() {
        if (panDays != null) return panDays;
        panDays = new MyPanel();
        panDays.setLayout(new GridBagLayout());
        
        GridBagConstraints gc = new GridBagConstraints();
        Insets ins = new Insets(1, 1, 1, 1);
        gc.insets = ins;
        gc.gridwidth = 1;

        gc.anchor = gc.NORTHWEST;
        gc.fill = gc.BOTH;
        gc.weightx = gc.weighty = 1.0f;
        
        int pos = 0;
        for (int r=0; r<6 ;r++) {
            for (int c=0; c<7 && pos < alDayPanel.size(); c++) {
                DayPanel dp = alDayPanel.get(pos++);
                if (c == 6) gc.gridwidth = gc.REMAINDER;
                panDays.add(dp, gc);
                gc.gridwidth = 1;
            }
        }
        return panDays;
    }


    
    /**
     * Header pane tobe usedin the JScrollPane that contains this.
     * @see JScrollPane#setColumnHeaderView(java.awt.Component)
     */
    protected JPanel getHeaderPanel() {
        if (panHeader != null) return panHeader;
        panHeader = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        Insets ins = new Insets(1, 1, 1, 1);
        gc.insets = ins;
        gc.fill = gc.NONE;

        gc.weightx = gc.weighty = 1.0f;
        gc.fill = gc.HORIZONTAL;
        gc.gridwidth = gc.REMAINDER;
        gc.anchor = gc.CENTER;
        panHeader.add(getSelectDatePanel(), gc);
        gc.gridwidth = 1;

        
        gc.fill = gc.HORIZONTAL;
        gc.anchor = gc.NORTH;
        String[] ss = new String[] {"Sun","Mon","Tue","Wed","Thr","Fri","Sat"};
        Dimension dim = null;
        for (int i=0; i<7; i++) {
            JLabel lbl = new JLabel(ss[i]);
            lbl.setOpaque(true);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);

            lbl.setBackground(colorUnselected);
            lbl.setForeground(Color.black);
            lbl.setBorder(borderSelected);
            if (dim == null) {
                dim = new Dimension( OAJfcUtil.getCharWidth(lbl, lbl.getFont(), 4), lbl.getPreferredSize().height);
            }
            lbl.setPreferredSize(dim);

            if (i == 6) gc.gridwidth = gc.REMAINDER;
            panHeader.add(lbl, gc);
            gc.gridwidth = 1;
        }
        
        return panHeader;
    }
    
    /**
     * Included in headerPanel
     */
    protected JPanel getSelectDatePanel() {
        Hub<VDate> hubCalendar = new Hub<>(VDate.class);
        hubCalendar.add(vdCalendar);
        hubCalendar.setPos(0);
        
        dcboCalendar = new OADateComboBox(hubCalendar, "value");
        dcboCalendar.setAllowClear(false);
        hubCalendar.addHubListener(new HubListenerAdapter() {
            public void afterPropertyChange(HubEvent e) {
                if ("value".equalsIgnoreCase(e.getPropertyName())) {
                    setSelectedDate(vdCalendar.getValue());
                }
            }
        });
        OATextField txt = new OATextField(dcboCalendar.getHub(), "value", 10);
        dcboCalendar.setEditor(txt);
        dcboCalendar.setColumns(10);

        JPanel panx = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        JLabel lbl = new JLabel("Selected Date: ");
        panx.add(lbl);
        URL url = OAButton.class.getResource("icons/calendar.png");
        lbl.setIcon(new ImageIcon(url));
        
        panx.add(dcboCalendar);
        
        return panx;
    }

    public void setDisplayTemplate(String s) {
        displayTemplate = s;
        for (DayPanel dp : alDayPanel) {
            dp.lst.setDisplayTemplate(displayTemplate);
        }
    }
    public String getDisplayTemplate() {
        return this.displayTemplate;
    }

    public void setToolTipTextTemplate(String s) {
        toolTipTextTemplate = s;
        for (DayPanel dp : alDayPanel) {
            dp.lst.setToolTipTextTemplate(toolTipTextTemplate);
        }
    }
    public String getToolTipTextTemplate() {
        return this.toolTipTextTemplate;
    }

    public void setIconColorProperty(String s) {
        this.iconColorPropertyPath = s;
        for (DayPanel dp : alDayPanel) {
            dp.lst.setIconColorProperty(s);
        }
    }
    public String getIconColorProperty() {
        return this.iconColorPropertyPath;
    }
    
    
    
    public void customizeRenderer(JLabel label, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    }
    
    public OAList createList(Hub<T> hub) {
        OAList lst = new OAList(hub, propertyPath, 4, 12) {
            @Override
            public void customizeRenderer(JLabel label, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                OAMonthCalendar.this.customizeRenderer(label, list, value, index, isSelected, cellHasFocus);
            }
        };
        lst.setDisplayTemplate(getDisplayTemplate());
        lst.setToolTipTextTemplate(getDisplayTemplate());
        lst.setIconColorProperty(getIconColorProperty());
        
        return lst;
    }
    
    public JLabel createLabel(Hub<F> hub) {
        return null;
    }
    
    
    class DayPanel extends JPanel {
        OADate date;
        JLabel lbl;
        OAList lst;
        Hub<F> hub;
        Hub<T> hubDetail;
        boolean bLabelSetText;
        JButton cmd;
        JPanel panCmd;
        JScrollPane spLst;
        
        public DayPanel() {
            setLayout(new BorderLayout());
            
            this.hub = new Hub(OAMonthCalendar.this.getHub().getObjectClass());
            
            cmd = new JButton("create");
            cmd.setFont(cmd.getFont().deriveFont(9.5f));
            cmd.setForeground(Color.gray);
            cmd.setToolTipText("this day is empty, click to create a new day");
            // URL url = OAButton.class.getResource("icons/new.gif");
            // if (url != null) cmd.setIcon(new ImageIcon(url));

            cmd.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Class c = hub.getObjectClass();
                    if (c == null) return;
                    OAObject obj = (OAObject) OAObjectReflectDelegate.createNewObject(c);
                    obj.setProperty(OAMonthCalendar.this.datePropertyPaths[0], date);
                    obj.save();
                    OAMonthCalendar.this.hub.add((F) obj);
                    onDaySelected(date, hub, hubDetail);
                    setSelectedDate(date, true);
                }
            });
            OAButton.setup(cmd);
            panCmd = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panCmd.add(cmd);
            panCmd.setForeground(Color.lightGray);
            add(panCmd, BorderLayout.SOUTH);

            
            if (OAMonthCalendar.this.hubDetail != null) {
                lbl = createLabel(hub);
                if (lbl instanceof OALabel) {
                    ((OALabel) lbl).setAllowSetTextBlink(false);
                }
            }
            if (lbl == null) {
                lbl = new JLabel();
                //lbl.setOpaque(true);
                bLabelSetText = true;
            }
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12.5f));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            add(lbl, BorderLayout.NORTH);
            
            Hub hubx = hub;
            if (OAMonthCalendar.this.getDetailHub() != null) {
                HubAODelegate.keepActiveObject(this.hub);
                
                hubDetail = hub.getDetailHub(OAMonthCalendar.this.getDetailHub().getObjectClass()).createSharedHub();
                hubx = hubDetail;
            }
            lst = createList(hubx);
            lst.setVisibleRows(5);

            // hack to let scrollwheel work for panel, and not list
            //   https://stackoverflow.com/questions/12911506/why-jscrollpane-does-not-react-to-mouse-wheel-events
            spLst = new JScrollPane(lst) {
                @Override
                protected void processMouseWheelEvent(MouseWheelEvent e) {
                    boolean b = getVerticalScrollBar().isVisible();
                    if (!b) {
                        getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, OAMonthCalendar.this.getParent()));
                        return;
                    }
                    super.processMouseWheelEvent(e);
                }
            };
            // sp.setWheelScrollingEnabled(false);
            
            spLst.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(spLst, BorderLayout.CENTER);

            lst.addMouseListener(new MouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    onMouseClick();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    onMouseClick();
                }
            });
            
            addMouseListener(new MouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    onMouseClick();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                }
            });
        }
        
        protected void onMouseClick() {
            
        }
        public void setSelected(boolean b) {
            if (b) {
                lbl.setOpaque(true);
                lbl.setBackground(colorSelected);
                lbl.setForeground(Color.white);
                setBorder(borderSelected);
            }
            else {
                lbl.setOpaque(false);
                lbl.setForeground(Color.black);
                setBorder(borderUnselected);
               if (hubDetail != null) hubDetail.setAO(null);
               else hub.setAO(null);
            }
            if (lbl instanceof OALabel) {
                ((OALabel) lbl).customizeRenderer(lbl, hub.getAO(), date, b, b, 0, false, false);
            }
        }
    }
    
    static class MyPanel extends JPanel implements Scrollable {
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation, int direction) {
            return 20;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation, int direction) {
            return 40;
        }
        
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            // take up fullwidth, no hort scrollbar
            return true;
        }
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            Dimension d = getPreferredSize();
            return d;
        }
    }
}

    
    
    