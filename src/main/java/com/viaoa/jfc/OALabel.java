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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.table.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

public class OALabel extends JLabel implements OATableComponent, OAJfcComponent {
    private OALabelController control;
    private OATable table;
    private String heading = "";

    /**
        Create label that is bound to a property for the active object in a Hub.
    */
    public OALabel(Hub hub, String propertyPath) {
        control = new OALabelController(hub, propertyPath);
        initialize();
    }
    /**
        Create label that is bound to a property for the active object in a Hub.
        @param cols width of label.
    */
    public OALabel(Hub hub, String propertyPath, int cols) {
        control = new OALabelController(hub, propertyPath);
        setColumns(cols);
        initialize();
    }

    /**
        Create label that is bound to a property for an object.
    */
    public OALabel(OAObject hubObject, String propertyPath) {
        control = new OALabelController(hubObject, propertyPath);
        initialize();
    }

    /**
        Create label that is bound to a property for an object.
        @param cols width of label.
    */
    public OALabel(OAObject hubObject, String propertyPath, int cols) {
        control = new OALabelController(hubObject, propertyPath);
        setColumns(cols);
        // setText(" ");  //<-- this screws preferredSize if before setColumns()
        initialize();
    }

    /** Used with imageProperty, imagePath to display icon */
    public OALabel(Hub hub) {
        control = new OALabelController(hub);
        initialize();
    }

    @Override
    public void initialize() {
    }
    
    public OALabelController getController() {
    	return control;
    }

    public void setPassword(boolean b) {
        getController().setPassword(b);
    }
    public boolean isPassword() {
        return getController().isPassword();
    }
    
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
    */
    public void setFormat(String fmt) {
        control.setFormat(fmt);
    }
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
    */
    public String getFormat() {
        return control.getFormat();
    }

    public void addNotify() {
        if (getColumns() > 0) setColumns(getColumns());
        super.addNotify();
    }
    
    
    /**
        Get the property name used for displaying an image with component.
    */
    public void setImageProperty(String prop) {
        control.setImagePropertyPath(prop);
    }
    /**
        Get the property name used for displaying an image with component.
    */
    public String getImageProperty() {
        return control.getImagePropertyPath();
    }

    /**
        Root directory path where images are stored.
    */
    public void setImagePath(String path) {
        control.setImagePropertyPath(path);
    }
    /**
        Root directory path where images are stored.
    */
    public String getImagePath() {
        return control.getImagePropertyPath();
    }

    
    public int getMaxImageHeight() {
    	return control.getMaxImageHeight();
	}
	public void setMaxImageHeight(int maxImageHeight) {
		control.setMaxImageHeight(maxImageHeight);
	}

	public int getMaxImageWidth() {
		return control.getMaxImageWidth();
	}
	public void setMaxImageWidth(int maxImageWidth) {
		control.setMaxImageWidth(maxImageWidth);
	}
    
    
    /**
        Hub this this component is bound to.
    */
    public Hub getHub() {
        if (control == null) return null;
        return control.getHub();
    }

    /**
        Returns the single object that is bound to this component.
    */
    public Object getObject() {
        return control.getObject();
    }

    /**
        Set by OATable when this component is used as a column.
    */
    public void setTable(OATable table) {
        this.table = table;
        if (table != null) table.resetColumn(this);
    }


    /**
        Set by OATable when this component is used as a column.
    */
    public OATable getTable() {
        return table;
    }

    /**
        Width of label, based on average width of the font's character 'w'.
    */
    public int getColumns() {
        return control.getColumns();            
    }
    /**
        Width of label, based on average width of the font's character.
    */
    public void setColumns(int x) {
        control.setColumns(x);
        invalidate();
        if (table != null) {
            int w = OATable.getCharWidth(this,getFont(),x+1);
            table.setColumnWidth(table.getColumnIndex(this),w);
        }
    }
    public void setMaximumColumns(int x) {
        control.setMaximumColumns(x);
        invalidate();
    }
    public int getMaxColumns() {
        return control.getMaximumColumns();
    }
    public void setMiniColumns(int x) {
        control.setMinimumColumns(x);
        invalidate();
    }
    public int getMiniColumns() {
        return control.getMinimumColumns();
    }

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (isPreferredSizeSet()) return d;
        int cols = getColumns();
        if (cols <= 0) {
            cols = control.getPropertyInfoDisplayColumns();
        }
        
        if (cols > 0) {
            Insets ins = getInsets();
            int inx = ins == null ? 0 : ins.left + ins.right;
            d.width = OATable.getCharWidth(this,getFont(),cols)+inx+2;
        }
        return d;
    }
    
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (isMaximumSizeSet()) return d;
        int cols = getMaxColumns();
        if (cols < 1)  {
            //maxCols = control.getDataSourceMaxColumns();
            //if (maxCols < 1) {
            cols = control.getPropertyInfoMaxColumns();
            
            if (cols < 1) {
                cols = getColumns() * 2; 
            }
        }

        Insets ins = getInsets();
        int inx = ins == null ? 0 : ins.left + ins.right;
        
        if (cols > 0) d.width = OATable.getCharWidth(this, getFont(), cols) + inx;
        else {
            // also check size of text, so that label is not bigger then the text it needs to display
            String s = getText();
            if (s == null) s = " ";
            
            FontMetrics fm = getFontMetrics(getFont());
            d.width = Math.min(d.width, fm.stringWidth(s)+inx+2);
        }
        
        // dont size under pref size
        Dimension dx = getPreferredSize();
        d.width = Math.max(d.width, dx.width);

        return d;
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (isMinimumSizeSet()) return d;
        int cols = getMiniColumns();
        if (cols < 1) return d;
        d.width = OATable.getCharWidth(this, getFont(), cols+1);
        return d;
    }
        
    /**
        Property path used to retrieve/set value for this component.
    */
    @Override
    public String getPropertyPath() {
        if (control == null) return null;
        return control.getPropertyPath();
    }
    public String getEndPropertyName() {
        if (control == null) return null;
        return control.getEndPropertyName();
    }
    
    /**
        Column heading when this component is used as a column in an OATable.
    */
    public String getTableHeading() { 
        return heading;   
    }
    /**
        Column heading when this component is used as a column in an OATable.
    */
    public void setTableHeading(String heading) {
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    /**
        Editor used when this component is used as a column in an OATable.
    */
    public TableCellEditor getTableCellEditor() {
        return null;
    }

    // OATableComponent Interface method
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (control != null) {
            control.getTableRenderer(renderer, table, value, isSelected, hasFocus, row, column);
        }
        return renderer;
    }

    
    public void setIconColorProperty(String s) {
    	control.setIconColorPropertyPath(s);
    }
    public String getIconColorProperty() {
    	return control.getIconColorPropertyPath();
    }
    
    public void setBackgroundColorProperty(String s) {
        control.setBackgroundColorPropertyPath(s);
    }
    public String getBackgroundColorProperty() {
    	return control.getBackgroundColorPropertyPath();
    }

    
    public void addEnabledCheck(Hub hub) {
        control.getEnabledChangeListener().add(hub);
    }
    public void addEnabledCheck(Hub hub, String propPath) {
        control.getEnabledChangeListener().add(hub, propPath);
    }
    public void addEnabledCheck(Hub hub, String propPath, Object compareValue) {
        control.getEnabledChangeListener().add(hub, propPath, compareValue);
    }
    protected boolean isEnabled(boolean defaultValue) {
        return defaultValue;
    }
    public void addVisibleCheck(Hub hub) {
        control.getVisibleChangeListener().add(hub);
    }
    public void addVisibleCheck(Hub hub, String propPath) {
        control.getVisibleChangeListener().add(hub, propPath);
    }
    public void addVisibleCheck(Hub hub, String propPath, Object compareValue) {
        control.getVisibleChangeListener().add(hub, propPath, compareValue);
    }
    protected boolean isVisible(boolean defaultValue) {
        return defaultValue;
    }

    

    public class OALabelController extends LabelController {
        public OALabelController(Hub hub) {
            super(hub, OALabel.this);
        }
        public OALabelController(Hub hub, String propertyPath) {
            super(hub, OALabel.this, propertyPath);
        }
        public OALabelController(OAObject hubObject, String propertyPath) {
            super(hubObject, OALabel.this, propertyPath);
        }        
        
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OALabel.this.isVisible(bIsCurrentlyVisible);
        }
        
        // 20160516
        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            return bIsCurrentlyEnabled;
        }
    }

    @Override
    public String getTableToolTipText(JTable table, int row, int col, String defaultValue) {
        Object obj = ((OATable) table).getObjectAt(row, col);
        defaultValue = getToolTipText(obj, row, defaultValue);
        return defaultValue;
    }
    
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
        Object obj = ((OATable) table).getObjectAt(row, column);
        customizeRenderer(lbl, obj, value, isSelected, hasFocus, row, wasChanged, wasMouseOver);
    }


    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    public JLabel getLabel() {
        if (getController() == null) return null;
        return getController().getLabel();
    }

    private Color fgColor, bgColor;
    private final AtomicInteger aiBlink = new AtomicInteger();
    
    public void blink(final Color fcolor, final Color bcolor, final int numberOfTimes) {
        final int cntBlink = aiBlink.getAndIncrement();
        if (fgColor == null) fgColor = this.getForeground(); 
        if (bgColor == null) bgColor = this.getBackground(); 
        final Timer timer = new Timer(150, null);

        ActionListener al = new ActionListener() {
            int cnt;
            public void actionPerformed(ActionEvent e) {
                if (cntBlink != aiBlink.get()) {
                    cnt = numberOfTimes-1;
                }

                boolean b = (cnt++ % 2 == 0);
                
                Color c;
                if (fcolor != null) {
                    c = (b ? fcolor : fgColor);
                    setForeground(c);
                }
                if (bcolor != null) {
                    c = (b ? bcolor : bgColor);
                    setBackground(c);
                }

                if (!b && ((cnt / 2) >= numberOfTimes) ) {
                    timer.stop();
                }
            }
        };                 
        timer.addActionListener(al);
        timer.setRepeats(true);
        timer.setInitialDelay(250);
        timer.start();
    }
}
