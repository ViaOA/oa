
package com.viaoa.jfc;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

public class OAFormattedTextField_ extends BaseFormattedTextField implements OAJFCComponent, OATableComponent {
    Hub2FormattedTextField htf;
    OATable table;
    String heading = "";

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
        @param cols is the width
    */
    public OAFormattedTextField(Hub hub, String propertyPath, int cols, String mask, String validChars, boolean bRightJustified, boolean bAllowSpaces) {
        super(mask, validChars, bRightJustified, bAllowSpaces);
        setColumns(cols);
        htf = new Hub2FormattedTextField(hub,this,propertyPath);
    }

    public void addNotify() {
        super.addNotify();
        htf.afterChangeActiveObject(null);
    }

    // ----- OATableComponent Interface methods -----------------------
    public void setHub(Hub hub) {
        htf.setHub(hub);
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),getPropertyPath());
    }
    public Hub getHub() {
        return htf.getHub();
    }
    public void setTable(OATable table) {
        this.table = table;
    }
    public OATable getTable() {
        return table;
    }
    public void setColumns(int x) {
        super.setColumns(x);
        if (table != null) table.setColumnWidth(table.getColumnIndex(this),super.getPreferredSize().width);
    }
    
    @Override
    public String getFormat() {
        return null;
    }

    public String getPropertyPath() {
        return htf.getPropertyPath();
    }
    public void setPropertyPath(String path) {
        htf.setPropertyPath(path);
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),path);
    }
    public String getTableHeading() { //zzzzz
        return heading;
    }
    public void setTableHeading(String heading) { //zzzzz
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getPreferredSize();
        //09/15/99   Dimension d = super.getMinimumSize();
        return d;
    }

    /**
        Flag to set TextField to be read only.
    */
    public void setReadOnly(boolean b) {
        htf.setReadOnly(b);
    }
    /**
        Flag to set TextField to be read only.
    */
    public boolean getReadOnly() {
        return htf.getReadOnly();
    }


    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (hasFocus) {
            // renderer.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
            renderer.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder") );
        }
        else renderer.setBorder(null);

        if (hasFocus) {
            renderer.setForeground( UIManager.getColor("Table.focusCellForeground") );
            renderer.setBackground( UIManager.getColor("Table.focusCellBackground") );
        }
        else if (isSelected) {
            renderer.setForeground( UIManager.getColor("Table.selectionForeground") );
            renderer.setBackground( UIManager.getColor("Table.selectionBackground") );
        }
        else {
            renderer.setForeground( UIManager.getColor(table.getForeground()) );
            renderer.setBackground( UIManager.getColor(table.getBackground()) );
        }

        return renderer;
    }


    OAFormattedTextFieldTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OAFormattedTextFieldTableCellEditor(this);
        }
        return tableCellEditor;
    }

    // 2004/08/04
    /**
        Used to manually enable/disable.
    */
    public void setEnabled(boolean b) {
        setEnabled(b, false);
    }
    // @param bOnce if this is to resume normal enable afterwards
    public void setEnabled(boolean b, boolean bOnce) {
        // overwritten to find out if button is being manually enabled
        super.setEnabled(b);
        if (htf != null && !bOnce) {
            htf.setEnabled(b);
        }
    }

    // 20101108
    private EnableController controlEnable;
    public void setEnabled(Hub hub, String prop) {
        if (controlEnable != null) {
            controlEnable.close();
        }
        controlEnable = new EnableController(hub, this, prop);
    }
    
    
    @Override
    protected void onValueChange(String value) {
        if (htf != null) {
            htf.saveChanges(value);
        }
    }

}



