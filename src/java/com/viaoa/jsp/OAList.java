package com.viaoa.jsp;

import com.viaoa.hub.Hub;

/**
 * Used for an HTML select, with attribute multiple.
 *
 */
public class OAList extends OACombo {

    protected int rows;
    public OAList(String id, Hub hub, String propertyPath, int rows, int columns) {
        super(id, hub, propertyPath, columns);
        this.rows = rows;
    }
    
    public void setSelectHub(Hub<?> hubSelect) {
        this.hubSelect = hubSelect;
    }
    
    public int getRows() {
        return rows;
    }
    public void setRows(int rows) {
        this.rows = rows;
    }
    
    @Override
    public String getScript() {
        String s = super.getScript();
        if (rows > 0) {
            s += "$('#"+id+"').attr('size', '"+getRows()+"');\n";
            if (hubSelect != null) s += "$('#"+id+"').attr('multiple', 'multiple');\n";
            else s += "$('#"+id+"').removeAttr('multiple');\n";
        }
        return s;
    }
}
