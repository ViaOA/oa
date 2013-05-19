package com.viaoa.project.objectnav.process;


import java.sql.Types;

import com.viaoa.ds.jdbc.db.*;
import com.viaoa.hub.Hub;
import com.viaoa.project.objectnav.model.*;

public class DatabaseLoader {
    
    public DatabaseLoader() {
    }
    
    public void load(Database database, Hub<DataObject> hubDataObject) {

        for (Table table : database.getTables()) {
            if (table.bLink) continue;
            DataObject ic = new DataObject();
            String s = table.clazz.getName();
            int pos = s.lastIndexOf('.');
            if (pos > 0) s.substring(pos+1);
            ic.setName(s);
            hubDataObject.add(ic);
        }
        
        for (int i=0,j=0; i < database.getTables().length; i++) {
            Table table = database.getTables()[i];
            if (table.bLink) continue;
            DataObject ic = hubDataObject.getAt(j++);
            
            for (Column col : table.getColumns()) {
                if (col.foreignKey) continue;
                
                int type = -1;
                switch (col.type) {
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.LONGNVARCHAR:
                    type = DataProperty.TYPE_String;
                    break;
                case Types.BIGINT:
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    type = DataProperty.TYPE_Integer;
                    break;
                case Types.DOUBLE:
                case Types.FLOAT:
                case Types.DECIMAL:
                case Types.REAL:
                case Types.NUMERIC:
                    type = DataProperty.TYPE_Decimal;
                    break;
                case Types.BIT:
                case Types.CHAR:
                case Types.BOOLEAN:
                    type = DataProperty.TYPE_Boolean;
                    break;
                case Types.DATE:
                    type = DataProperty.TYPE_Date;
                    break;
                case Types.TIME:
                    type = DataProperty.TYPE_Time;
                    break;
                case Types.TIMESTAMP:
                    type = DataProperty.TYPE_DateTime;
                    break;
                case Types.BINARY:
                case Types.BLOB:
                    type = DataProperty.TYPE_Image;
                    break;
                default:
                    continue;
                }
                DataProperty ip = new DataProperty();
                ip.setType(type);
                ip.setName(col.columnName);
                ip.setMaxLength(col.maxLength);
                ip.setUnique(col.primaryKey);
                ic.getDataProperties().add(ip);
            }
        }
        
        for (int i=0,j=0; i < database.getTables().length; i++) {
            Table table = database.getTables()[i];
            if (table.bLink) continue;
            DataObject ic = hubDataObject.getAt(j++);
        
            for (Link link : table.getLinks()) {
                DataLink ilink = new DataLink();
                
                ilink.setName(link.propertyName);
                ilink.setFromDataObject(ic);
                ilink.setType( (link.fkeys == null || link.fkeys.length == 0) ? DataLink.TYPE_Many : DataLink.TYPE_One );
                
                Table toTable = link.toTable;
                if (toTable.bLink) {
                    for (Link linkx : toTable.getLinks()) {
                        if (link.toTable != table) {
                            toTable = link.toTable;
                            break;
                        }
                    }
                }
                
                for (DataObject icx : hubDataObject) {
                    if (icx.getName().equalsIgnoreCase(toTable.name)) {
                        ilink.setToDataObject(icx);
                        break;
                    }
                }
            }
        }
    }
}
