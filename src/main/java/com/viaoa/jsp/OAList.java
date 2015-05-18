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
