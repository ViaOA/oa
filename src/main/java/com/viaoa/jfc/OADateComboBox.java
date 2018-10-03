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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.EmptyBorder;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.sun.java.swing.plaf.motif.MotifComboBoxUI;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

import com.viaoa.hub.*;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;

/**
    ComboBox that has a calendar component display for the dropdown component.
    <p>
    Example:<br>
    Create a ComboBox with popup calendar that is bound to the hire date for a Hub of Employee objects,
    with a width the size of 14 characters.
    <pre>
        OADateComboBox dcbo = new OADateChooser(hub, "dateProperty", 14);

        // unbound to an object property
        OADateComboBox dcbo = new OADateComboBox() {
            public void setSelectedItem(Object obj) {
                OADate date = (OADate) obj;
                super.setSelectedItem(date);
                // ...
            }
        };

        // using a TextField as an editor
        OADateComboBox dcbo = new OADateComboBox(hub, "dateProperty", 16);
        txt = new OATextField(hub, "dateProperty", 15);
        dcbo.setEditor(txt);

    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OADateComboBox
*/
public class OADateComboBox extends OACustomComboBox {

    private MyDateComboBoxModel model;
    
    /**was
        Create an unbound DateComboBox.

    public OADateComboBox() {
        model = new MyDateComboBoxModel(this);
        setModel(model);
        setRenderer(new MyDateListCellRenderer(this));
        initialize();
    }
    */

    protected boolean bAllowClear=true;
    public void setAllowClear(boolean b) {
        this.bAllowClear = b;
    }

    /**
        Create a DateComboBox that is bound to a property for the active object in a Hub.
        @param hub is Hub that used to display and edit date property in active object
        @param propertyPath is date property to display/edit
        @param columns is width to use, using average character width
    */
    public OADateComboBox(Hub hub, String propertyPath, int columns) {
        super(hub,propertyPath, columns);
        initialize();
    }

    /**
        Create a DateComboBox that is bound to a property for the active object in a Hub.
        @param hub is Hub that used to display and edit date property in active object
        @param propertyPath is date property to display/edit
    */
    public OADateComboBox(Hub hub, String propertyPath) {
        super(hub, propertyPath);
        initialize();
    }

    /**
        Create a DateComboBox that is bound to a property for an object.
        @param obj is object to be bound to
        @param propertyPath is date property to display/edit
        @param columns is width to use, using average character width
    */
    public OADateComboBox(Object obj, String propertyPath, int columns) {
        super(obj,propertyPath, columns);
        initialize();
    }

    /**
        Create a DateComboBox that is bound to a property for an object.
        @param obj is object to be bound to
        @param propertyPath is date property to display/edit
    */
    public OADateComboBox(Object obj, String propertyPath) {
        super(obj, propertyPath);
        initialize();
    }

    @Override
    public void initialize() {
    }


    /**
        Returns the date that is currently selected.
    */
    public OADate getDate() {
    	Hub h = getHub();
        if (h != null) {
            OAObject obj = (OAObject) h.getAO();
            Object value = control.getValue(obj);
            //was: Object value = OAReflect.getPropertyValue(obj, control.getGetMethods());
            if (value instanceof OADate) return (OADate) value;
            return null;
	    }
        if (getSelectedItem() instanceof OADate) return (OADate) getSelectedItem();
        return null;
    }
    /**
        Set the date that is currently selected.
    */
    public void setDate(OADate date) {
        setSelectedItem(date);
    }

    @Override
    public void setSelectedItem(Object item) {
        if (bSetting) return; // from super
        if (getHub() == null) {
            if (item instanceof OADate && model != null) {
                model.currentDate = (OADate) item;
                model.setSelectedItem(item);
            }
        }
        super.setSelectedItem(item);
    }

    /**
        override to create popup calendar
    */
    public void updateUI() {
	    ComboBoxUI cui = (ComboBoxUI) UIManager.getUI(this);
	    if (cui instanceof MotifComboBoxUI) {
	        cui = new MotifComboBoxUI() {
	            protected ComboPopup createPopup() {
	                return new DatePopup( comboBox );
	            }
	        };
	    }
	    else if (cui instanceof WindowsComboBoxUI) {
	        cui = new WindowsComboBoxUI() {
	            protected ComboPopup createPopup() {
	                return new DatePopup( comboBox );
	            }
	        };
	    }
	    else cui = new MetalComboBoxUI() {
	        protected ComboPopup createPopup() {
	            return new DatePopup( comboBox );
	        }
	    };
        setUI(cui);
    }
}


class DatePopup implements ComboPopup, MouseMotionListener, MouseListener, KeyListener, PopupMenuListener {

	protected JComboBox comboBox;
	protected Calendar calendar;
	protected JPopupMenu popup;
	protected JLabel monthLabel;
	protected JPanel days = null;
	protected SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy");
	protected SimpleDateFormat monthFormat2 = new SimpleDateFormat("MMMyy");
    JButton cmdToday, cmdClear, cmdCurrent;

	protected Color selectedBackground;
	protected Color selectedForeground;
	protected Color background;
	protected Color foreground;

	public DatePopup(JComboBox comboBox) {
	    this.comboBox = comboBox;
	    calendar = Calendar.getInstance();
	    // check Look and Feel
	    background = UIManager.getColor("ComboBox.background");
	    foreground = UIManager.getColor("ComboBox.foreground");
	    selectedBackground = UIManager.getColor("ComboBox.selectionBackground");
	    selectedForeground = UIManager.getColor("ComboBox.selectionForeground");

	    initializePopup();
	}

	//========================================
	// begin ComboPopup method implementations
	//
    public void show() {
        if (cmdClear != null) {
            cmdClear.setVisible( ((OADateComboBox)comboBox).bAllowClear);
        }
	    try {
		// if setSelectedItem() was called with a valid date, adjust the calendar
	    	Object obj;
	    	if (comboBox instanceof OADateComboBox) {
	    		obj = ((OADateComboBox) comboBox).getDate();
	    	}
	    	else obj = comboBox.getSelectedItem();
            if (obj instanceof OADateTime) {
            	OADateTime od = (OADateTime) obj;
            	calendar.setTime(od.getDate());
            }
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("OADateComboBox.show() exception "+e);
	    }
	    updatePopup();
	    popup.show(comboBox, 0, comboBox.getHeight());
    }

	public void hide() {
	    popup.setVisible(false);
	}

	protected JList list = new JList();
	public JList getList() {
	    return list;
	}

	public MouseListener getMouseListener() {
	    return this;
	}

	public MouseMotionListener getMouseMotionListener() {
	    return this;
	}

	public KeyListener getKeyListener() {
	    return this;
	}

	public boolean isVisible() {
	    return popup.isVisible();
	}

	public void uninstallingUI() {
	    popup.removePopupMenuListener(this);
	}

	//
	// end ComboPopup method implementations
	//======================================



	//===================================================================
	// begin Event Listeners
	//

	// MouseListener
	public void mousePressed( MouseEvent e ) {
		doPopup(e); // 20080515
	}
    public void mouseReleased( MouseEvent e ) {}
    

	// something else registered for MousePressed
	public void mouseClicked(MouseEvent e) {
		// 20080515 was: doPopup(e);
	}
	protected void doPopup(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        if (!comboBox.isEnabled()) return;

	    if (comboBox.isEditable() ) { 
	    	comboBox.getEditor().getEditorComponent().requestFocus();
	    } 
	    else {
	    	comboBox.requestFocus();
	    }
	    togglePopup();
	}

	protected boolean mouseInside = false;
	public void mouseEntered(MouseEvent e) {
	    mouseInside = true;
	}
	public void mouseExited(MouseEvent e) {
	    mouseInside = false;
	}

	// MouseMotionListener
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}

	// KeyListener
	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased( KeyEvent e ) {
	    if ( e.getKeyCode() == KeyEvent.VK_SPACE ||
		 e.getKeyCode() == KeyEvent.VK_ENTER ) {
		togglePopup();
	    }
	}

	/**
	 * Variables hideNext and mouseInside are used to
	 * hide the popupMenu by clicking the mouse in the JComboBox
	 */
	public void popupMenuCanceled(PopupMenuEvent e) {}
	protected boolean hideNext = false;
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//System.out.println("popupMenuWillBecomeInvisible");//qqqq
	    hideNext = mouseInside;
	}
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}

	//
	// end Event Listeners
	//=================================================================

	//===================================================================
	// begin Utility methods
	//

	protected void togglePopup() {
//20080515 was:	    if ( isVisible() || hideNext ) {
	    if ( isVisible() ) {

		hide();
	    } else {

		show();
	    }
	    hideNext = false;
	}

	//
	// end Utility methods
	//=================================================================

	// Note *** did not use JButton because Popup closes when pressed
	protected JLabel createUpdateButton(final int field, final int amount) {
	    final JLabel label = new JLabel();
	    final Border selectedBorder = new EtchedBorder();
	    final Border unselectedBorder = new EmptyBorder(selectedBorder.getBorderInsets(new JLabel()));
	    label.setBorder(unselectedBorder);
	    label.setForeground(foreground);
	    label.addMouseListener(new MouseAdapter() {
		    public void mouseReleased(MouseEvent e) {
			    calendar.add(field, amount);
			    updatePopup();
		    }
		    public void mouseEntered(MouseEvent e) {
    			label.setBorder(selectedBorder);
		    }
		    public void mouseExited(MouseEvent e) {
	    		label.setBorder(unselectedBorder);
		    }
		});
	    return label;
	}


	protected void initializePopup() {
	    JPanel header = new JPanel(); // used Box, but it wasn't Opaque
	    header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
	    header.setBackground(background);
	    header.setOpaque(true);

	    JLabel label;
	    label = createUpdateButton(Calendar.YEAR, -1);
	    label.setText("<<");
	    label.setToolTipText("Previous Year");

	    header.add(Box.createHorizontalStrut(12));
	    header.add(label);
	    header.add(Box.createHorizontalStrut(12));

	    label = createUpdateButton(Calendar.MONTH, -1);
	    label.setText("<");
	    label.setToolTipText("Previous Month");
	    header.add(label);

	    monthLabel = new JLabel("", JLabel.CENTER);
	    monthLabel.setForeground(foreground);
	    header.add(Box.createHorizontalGlue());
	    header.add(monthLabel);
	    header.add(Box.createHorizontalGlue());

	    label = createUpdateButton(Calendar.MONTH, 1);
	    label.setText(">");
	    label.setToolTipText("Next Month");
	    header.add(label);

	    label = createUpdateButton(Calendar.YEAR, 1);
	    label.setText(">>");
	    label.setToolTipText("Next Year");

	    header.add(Box.createHorizontalStrut(12));
	    header.add(label);
	    header.add(Box.createHorizontalStrut(12));

	    popup = new JPopupMenu();
	    popup.setBorder(BorderFactory.createLineBorder(Color.black));
	    popup.setLayout(new BorderLayout());
	    popup.setBackground(background);
	    popup.addPopupMenuListener(this);
	    popup.add(BorderLayout.NORTH, header);


        // Commands
        JPanel pan = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        cmdCurrent = new JButton("Current");
        cmdCurrent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	Object obj;
    	    	if (comboBox instanceof OADateComboBox) {
    	    		obj = ((OADateComboBox) comboBox).getDate();
    	    	}
    	    	else obj = comboBox.getSelectedItem();
                if (obj instanceof OADateTime) {
                    OADateTime dt = (OADateTime) obj;
                    calendar.set(dt.getYear(),dt.getMonth(),dt.getDay());
                    updatePopup();
                }
            }
        });
        OACommand.setup(cmdCurrent);
        // cmdCurrent.setToolTipText("set calendar to current date setting");
        pan.add(cmdCurrent);

        cmdToday = new JButton("Today");
        cmdToday.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OADate t = new OADate();
                calendar.set(t.getYear(),t.getMonth(),t.getDay());
                comboBox.setSelectedItem(new OADate(calendar));
                updatePopup();
            }
        });
        OACommand.setup(cmdToday);
        cmdToday.setToolTipText("set calendar to todays date");
        pan.add(cmdToday);

        cmdClear = new JButton("Clear");
        cmdClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                comboBox.setSelectedItem(null);
                updatePopup();
            }
        });
        OACommand.setup(cmdClear);
        cmdClear.setToolTipText("clear current date");
        pan.add(cmdClear);

	    popup.add(BorderLayout.SOUTH, pan);
	}

	// update the Popup when either the month or the year of the calendar has been changed
	protected void updatePopup() {
	    monthLabel.setText( monthFormat.format(calendar.getTime()) );
	    if (days != null) popup.remove(days);

	    days = new JPanel(new GridLayout(0, 7));
	    days.setBackground(background);
	    days.setOpaque(true);

	    Calendar setupCalendar = (Calendar) calendar.clone();
	    int intFirstDayOfWeek = setupCalendar.getFirstDayOfWeek();

    	int dayInt = intFirstDayOfWeek;
	    for (int i = 0; i < 7; i++) {
		    JLabel label = new JLabel();
		    label.setHorizontalAlignment(JLabel.CENTER);
		    label.setForeground(foreground);
            switch (dayInt) {
		        case Calendar.SUNDAY: label.setText("Sun"); break;
		        case Calendar.MONDAY: label.setText("Mon"); break;
		        case Calendar.TUESDAY: label.setText("Tue"); break;
		        case Calendar.WEDNESDAY: label.setText("Wed"); break;
		        case Calendar.THURSDAY: label.setText("Thu"); break;
		        case Calendar.FRIDAY: label.setText("Fri"); break;
		        case Calendar.SATURDAY: label.setText("Sat"); break;
		    }
		    days.add(label);
    		if (dayInt == Calendar.SATURDAY) dayInt = Calendar.SUNDAY;
    		else dayInt++;
	    }

	    setupCalendar = (Calendar) calendar.clone();
	    setupCalendar.set(Calendar.DAY_OF_MONTH, 1);
	    int firstDay = setupCalendar.get(Calendar.DAY_OF_WEEK);

    	// 2006/12/26
	    dayInt = intFirstDayOfWeek;
	    for ( ; ; ) {
		    if (dayInt == firstDay) break;
	    	days.add(new JLabel(""));
    		if (dayInt == Calendar.SATURDAY) dayInt = Calendar.SUNDAY;
    		else dayInt++;
	    }

	    /* was:
	    for (int i = 0; i < (first - 1); i++) {
		    days.add(new JLabel(""));
	    }
	    */

	    OADate today = new OADate();
	    OADateTime currentDate = null;
	    Object obj;
    	if (comboBox instanceof OADateComboBox) {
    		obj = ((OADateComboBox) comboBox).getDate();
    	}
    	else obj = comboBox.getSelectedItem();
	    
	    boolean b = false;
	    if (obj instanceof OADateTime) {
	        currentDate = (OADateTime) obj;
	        b = (currentDate.getYear() != calendar.get(calendar.YEAR) || currentDate.getMonth() != calendar.get(calendar.MONTH));
	    }

	    if (b) {
    	    cmdCurrent.setText( monthFormat2.format(currentDate.getDate()) );
	        cmdCurrent.setEnabled(true);
	        cmdCurrent.setToolTipText("Go back to "+monthFormat.format(currentDate.getDate()));
	    }
	    else {
	        cmdCurrent.setText("");
	        cmdCurrent.setEnabled(false);
	    }

        if (currentDate == null) {
            cmdClear.setText("");
	        cmdClear.setEnabled(false);
        }
        else {
            cmdClear.setText("Clear");
	        cmdClear.setEnabled(true);
        }

        if (currentDate == null || today.compareTo(currentDate) != 0) {
            cmdToday.setText("Today");
	        cmdToday.setEnabled(true);
        }
        else {
            cmdToday.setText("");
	        cmdToday.setEnabled(false);
        }


        int x = setupCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

	    for (int i = 1; i <= x; i++) {
		    final int day = i;
		    final JLabel label = new JLabel(String.valueOf(day));
		    label.setHorizontalAlignment(JLabel.CENTER);
		    label.setForeground(foreground);
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            label.setOpaque(false);
            if (today.compareTo(setupCalendar) == 0) {
                // label.setBorder(new LineBorder(foreground, 1));
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setBorder(new LineBorder(foreground, 1));
                label.setToolTipText("Today "+today.toString());
            }
            b = false;
            if (currentDate != null && currentDate.compareTo(setupCalendar) == 0) {
                b = true;
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setBorder(new LineBorder(selectedForeground, 1));
                label.setOpaque(true);
                label.setBackground(selectedBackground);
                label.setForeground(selectedForeground);
                label.setToolTipText("current selection " + currentDate.toString());
            }

            final boolean bCurrentDate = b;
		    label.addMouseListener(new MouseListener() {
			    public void mousePressed(MouseEvent e) {
			    }
			    public void mouseClicked(MouseEvent e) {
			    }
			    public void mouseReleased(MouseEvent e) {
			        label.setOpaque(false);
			        label.setBackground(background);
			        label.setForeground(foreground);
			        calendar.set(Calendar.DAY_OF_MONTH, day);
			        OADate d = new OADate(calendar);
			        comboBox.setSelectedItem(d);
			        hide();
			        comboBox.requestFocus();
			    }
			    public void mouseEntered(MouseEvent e) {
			        if (bCurrentDate) return;
			        label.setOpaque(true);
			        label.setBackground(selectedBackground);
			        label.setForeground(selectedForeground);
			    }
			    public void mouseExited(MouseEvent e) {
                    if (bCurrentDate) return;
			        label.setOpaque(false);
			        label.setBackground(background);
			        label.setForeground(foreground);
			    }
		    });
        	days.add(label);
            setupCalendar.add(Calendar.DATE, 1);
	    }

	    popup.add(days, BorderLayout.CENTER);
//	    popup.add(BorderLayout.CENTER, days);
	    popup.pack();
	}
}


class MyDateComboBoxModel extends DefaultListModel implements ComboBoxModel {
    public OADate currentDate;
    OADateComboBox cbo;
    public MyDateComboBoxModel(OADateComboBox cb) {
        this.cbo = cb;
    }
    public synchronized void setSelectedItem(Object obj) {
        fireContentsChanged(cbo, 0, 0);
    }
    public Object getSelectedItem() {
        return currentDate;
    }
    public Object getElementAt(int index) {
        if (index == 0) return currentDate;
        return null;
    }
    public int getSize() {
        return 1;
    }
}
class MyDateListCellRenderer extends JLabel implements ListCellRenderer {
    OADateComboBox cbo;
    public MyDateListCellRenderer(OADateComboBox cb) {
        this.cbo = cb;
        setOpaque(true);
    }

    /** will either call OAComboBox.getRenderer() or Hub2ComboBox.getRenderer() */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
        String s = OAConv.toString(value, cbo.getFormat());
        if (s == null) s = "";
        setText(s);
        return this;
    }
}
