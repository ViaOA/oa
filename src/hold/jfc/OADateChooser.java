/*
This software and documentation is the confidential and proprietary
information of Vince Via ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with Vince Via.

VINCE VIA MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. VINCE VIA SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2000-2001 Vince Via
All rights reserved.
*/
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.beans.*;
import java.text.*;
import com.viaoa.util.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;


/** 
    Popup calendar component. You can set the preferredSize directly or by changing the font,
    which will change the preferredSize automatically.  This component will automatically
    size the font to take up all avail space.  It uses Font.BOLD as a style.
    <p>
    Example:<br>
    Create a popup calendar that is bound to the hire date for a Hub of Employee objects.
    <pre>
        OADateChooser dc = new OADateChooser(HubEmployee, "hireDate");
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OADateComboBox
*/
public class OADateChooser extends JPanel {
    OADate date, displayDate;

    int month;
    int year;
    int daysInMonth;
    int firstDayInWeek;

    int rowsForWeeks;  // number of rows representing weeks
    Vector vecListener;
    private Hub2DateChooser h2dc;

    
    public OADateChooser() {
        setBorder(new LineBorder(Color.black, 1));
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        setDisplayDate(new OADate());
    }

    /**
        Create DataChooser that is bound to a property in the active object of a Hub.
    */
    public OADateChooser(Hub hub, String propertyPath) {
        this();
        h2dc = new Hub2DateChooser(hub, this, propertyPath);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (vecListener == null) vecListener = new Vector(3,2);
        if (!vecListener.contains(l)) vecListener.addElement(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (vecListener != null) vecListener.remove(l);
    }

    protected void firePropertyChange(String propertyName,Object oldValue,Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);

        if (vecListener == null) return;
        if (propertyName != null && propertyName.equalsIgnoreCase("date")) {
            PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
            int x = vecListener.size();
            for (int i=0; i<x; i++) {
                PropertyChangeListener l = (PropertyChangeListener) vecListener.elementAt(i);
                l.propertyChange(e);
            }
        }
    }


    /**
        Set the date to display.
    */
    public void setDisplayDate(OADate d) {
        if (d == null) d = new OADate();
        displayDate = d;
        displayDate.setDay(1);

        firstDayInWeek = displayDate.getDayOfWeek();
        daysInMonth = displayDate.getDaysInMonth();

        rowsForWeeks = 1;  // first week
        int i = 8 - firstDayInWeek;  // days in first week
        rowsForWeeks += (daysInMonth - i) / 7;   // full weeks in month
        if ( ((daysInMonth - i) % 7) > 0 ) rowsForWeeks++;

        repaint();
    }

    /**
        Set the date to display.
    */
    public void setDate(OADate date) {
        OADate old = this.date;
        this.date = date;
        firePropertyChange("date",old, date);
        setDisplayDate(new OADate(date));
    }

    /**
        Returns the date that is displayed.
    */
    public OADate getDate() {
        return date;
    }

    Dimension dimPreferred;
    public void setPreferredSize(Dimension d) {
        super.setPreferredSize(d);
        dimPreferred = d;
    }

    private Dimension dimAdd = new Dimension(15,15);
    public void setAddAmount(Dimension d) {
        if (d == null) d = new Dimension(0,0);
        dimAdd = d;
    }

    public Dimension getMinimumSize() {
    	// return super.getMinimumSize();
    	return getPreferredSize();
    }
    public Dimension getPreferredSize() {
        if (dimPreferred == null) {
            Font f = getFont();
            f = new Font(f.getFamily(), f.getStyle() | Font.BOLD, f.getSize());
            FontMetrics fm = getFontMetrics(f);

            int charWidth = fm.charWidth('7') * 3;
            int charHeight = fm.getAscent();// fm.getHeight();  none of the chars used have a descent

            Border border = getBorder();
            Insets inset;
            if (border != null) inset = border.getBorderInsets(this);
            else inset = new Insets(0,0,0,0);

            if (rowsForWeeks == 0) setDisplayDate(null);
            int w = inset.left + inset.right + (charWidth * 7) + dimAdd.width;
            int h = inset.top + inset.bottom + (charHeight * (rowsForWeeks+2)) + dimAdd.height;
            dimPreferred = new Dimension(w,h);
        }
        return dimPreferred;
    }
    public void setFont(Font f) {
        super.setFont(f);
        dimPreferred = null;
    }

    Point ptMousePressed;
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (isEnabled() && e.getID() == MouseEvent.MOUSE_PRESSED) {
            ptMousePressed = e.getPoint();
            repaint();
        }
    }

    /**
     * Callback method that can be overwritten to draw a solid rectangle on date.
     * @param bDefault internally set to show if date will be highlited or not.
     * @return
     */
    public boolean shouldHighlight(OADate date, boolean bDefault) {
    	return bDefault;
    }
    /**
     * Callback that is used by shouldHightlight() returns true.
     * @param c default color that will be used.
     * @param bToday if this is for today.
     * @return the color to use, by default it will return c.
     */
    public Color getHighlightForeground(Color c, boolean bToday) {
    	return c;
    }
    public Color getHighlightBackground(Color c, boolean bToday) {
    	return c;
    }
    
    Rectangle recLeft, recRight;
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        Dimension dim = getSize();
        Border border = getBorder();
        Insets inset;
        if (border != null) inset = border.getBorderInsets(this);
        else inset = new Insets(0,0,0,0);

        int width = dim.width - inset.left - inset.right;
        int cellWidth = (int) Math.floor(width / 7);
        int startX = inset.left + ((width - (cellWidth * 7)) / 2); // left

        int height = dim.height - inset.top - inset.bottom;
        int cellHeight = (int) Math.floor(height/(rowsForWeeks+2));
        int startY = inset.top + 3;

        // determine font
        Font f = getFont();
        FontMetrics fm;
        int fontSize = 8;
        int charHeight = 0;
        for (boolean b=false;  ; fontSize+=2) {
            f = new Font(f.getFamily(), f.getStyle() | Font.BOLD, fontSize);
            fm = getFontMetrics(f);
            int charWidth = fm.charWidth('7') * 3;
            charHeight = fm.getAscent();// fm.getHeight();  none of the chars used have a descent
            if (b) {
                g.setFont(f);
                break;
            }

            if ( charHeight > cellHeight || charWidth > cellWidth ) {
                fontSize -= 2;
                if (fontSize > 8) fontSize -= 2;  // mini is 8pt
                b = true;
            }
        }
        charHeight -= (fontSize > 16) ? 4 : 2;
        int x = startX;
        int y = startY;
        int extraY = ((cellHeight - charHeight)/2) + charHeight;
        int extraX;

        drawTop(g, fm, width, charHeight, cellHeight, y+extraY, dim.width);

        y += cellHeight;
        String days = "SMTWTFS";
        for (int i=0; i<7; i++) {
            extraX = ((cellWidth - fm.stringWidth(""+days.charAt(i)))/2);
            g.drawString(""+days.charAt(i), x+extraX, y+extraY);
            x += cellWidth;
        }
        g.drawLine(0, y+1+extraY, dim.width, y+1+extraY);
        if (fontSize > 10) g.drawLine(0, y+2+extraY, dim.width, y+2+extraY);

        y += cellHeight;
        //was: x = startX + ((firstDayInWeek-1) * cellWidth);
        x = startX;

        int day = 1;
        day -= (firstDayInWeek - Calendar.SUNDAY);
        
        Color bg = getBackground();
        Color fg = getForeground();
        OADate today = new OADate();
        int newDay = -20;
        for (int i=Calendar.SUNDAY; ; i++, day++) {
            if (i == 8) {
                if (day > daysInMonth) break;
                y += cellHeight;
                x = startX;
                i = 1;
            }
            
            // see if the user used the mouse to select a different day
            if (ptMousePressed != null) {
                if (ptMousePressed.x > x && ptMousePressed.x < x + cellWidth) {
                    if (ptMousePressed.y > y && ptMousePressed.y < y + cellHeight) {
                    	newDay = day;
                        ptMousePressed = null;
                    }
                }
            }

            OADate onDate = displayDate;
            if (day < 1 || day > daysInMonth) {
            	displayDate.setDay(1);
            	onDate = (OADate) displayDate.addDays((day-1));
            }
            else {
            	displayDate.setDay(day);
            }
            
            boolean b = (date != null && date.equals(onDate));
            boolean b2 = shouldHighlight(onDate, b);

            if (b || b2 || onDate.equals(today)) {
                Color fg2 = getHighlightForeground(bg, b);
                Color bg2 = getHighlightBackground(fg, b);

            	if (b || onDate.equals(today)) {
	            	g.setColor(bg2);
	                g.draw3DRect(x,y,cellWidth-1, cellHeight-1,true);
                }
                if (b || b2) {
                    g.setColor(bg2);
                    g.fillRect(x+2,y+2,cellWidth-4, cellHeight-4);
                    g.setColor(fg2);
                }
            }

            extraX = ((cellWidth - fm.stringWidth(""+day))/2);
            g.drawString(""+(onDate.getDay()), x+extraX, y+extraY);
            x += cellWidth;
            g.setColor(fg);
        }
        if (newDay > -10) {
            OADate d = new OADate(displayDate);
            if (newDay < 1 || newDay > daysInMonth) {
            	d.setDay(1);
            	d = (OADate) d.addDays((newDay-1));
            }
            else {
                d.setDay(newDay);
            }
            setDate(d);
        }
    }

    protected void drawTop(Graphics g, FontMetrics fm, int width, int charHeight, int cellHeight, int yBase, int dimWidth) {
        String s = displayDate.toString("MMMM yyyy");
        int strWidth = fm.stringWidth(s);

        int x = (width - strWidth);
        if (x < charHeight) x = charHeight;
        x /= 2;

        g.drawString(s, x, yBase);

        if (ptMousePressed != null) {
            // see if mouse was pressed
            if (recLeft.contains(ptMousePressed)) {
                ptMousePressed = null;
                displayDate = (OADate) displayDate.addMonths(-1);
                setDisplayDate(displayDate);
                return;
            }
        }
        Polygon p = new Polygon();
        p.addPoint(5, yBase-(charHeight/2));
        p.addPoint(5+charHeight, yBase-charHeight);
        p.addPoint(5+charHeight, yBase);
        g.fillPolygon(p);

        recLeft = new Rectangle(5,yBase-charHeight,charHeight,charHeight);


        if (ptMousePressed != null) {
            // see if mouse was pressed
            if (recRight.contains(ptMousePressed)) {
                ptMousePressed = null;
                displayDate = (OADate) displayDate.addMonths(1);
                setDisplayDate(displayDate);
                return;
            }
        }
        p = new Polygon();
        p.addPoint(dimWidth-5, yBase-(charHeight/2));
        p.addPoint(dimWidth-5-charHeight, yBase-charHeight);
        p.addPoint(dimWidth-5-charHeight, yBase);
        g.fillPolygon(p);

        recRight = new Rectangle(dimWidth-5-charHeight,yBase-charHeight,charHeight,charHeight);
    }

    // 20101108
    private EnableController controlEnable;
    public void setEnabled(Hub hub, String prop) {
        if (controlEnable != null) {
            controlEnable.close();
        }
        controlEnable = new EnableController(hub, this, prop);
    }
    
    
    public static void main(String[] argv) {
        JFrame f = new JFrame();
    f.setLocation(50,50);
//        f.setBounds(20,20,180,180);
        OADateChooser dc = new OADateChooser();
        dc.setBackground(Color.white);
        dc.setOpaque(true);

            Font font = dc.getFont();
            font = new Font(font.getFamily(), font.getStyle() | Font.BOLD, 14);
            dc.setFont(font);

//        dc.setPreferredSize(new Dimension(180,180));
        f.getContentPane().add(dc);
        f.setVisible(true);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    f.pack();
    }

    
}
