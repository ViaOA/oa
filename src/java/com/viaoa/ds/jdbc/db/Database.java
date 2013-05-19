/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.ds.jdbc.db;
import java.util.*;

import com.viaoa.object.*;
import com.viaoa.util.OAArray;
import com.viaoa.hub.*;

/** 
    Used for defining a Database for OADataSourceJDBC.
*/
public class Database {
    
    private Table[] tables = new Table[0];
    private Hashtable hash = new Hashtable();
    
    /** type of database is generic (currently not used). */
    public static final int DATABASE_GENERIC = 0;
    /** type of database is generic (currently not used). */
    public static final int DATABASE_ACCESS = 1;
    /** maximum number of database types defined (currently not used).  */
    public static final int DATABASE_MAX = 2;

    /**
        Returns that Table that is mapped to a Class.
    */
    public Table getTable(Class clazz) {
        return (Table) hash.get(clazz);
    }

    public Table getTable(String name) {
        if (name == null) return null;
        
        if (hash != null) {
            Enumeration enumx = hash.elements();
            for ( ;enumx.hasMoreElements(); ) {
                Table t = (Table) enumx.nextElement();
                if (name.equalsIgnoreCase(t.name)) return t;
            }
        }
        for (int i=0; tables != null && i<tables.length; i++) {
            if (name.equalsIgnoreCase(tables[i].name)) return tables[i];
        }
        return null;
    }


    public void addTable(Table table) {
        if (table == null) return;
        this.tables = (Table[]) OAArray.add(Table.class, this.tables, table);

        if (table.clazz != null) {
            hash.put( table.clazz, table);
            
            Class sc = table.clazz.getSuperclass();
            if (sc != null && !sc.equals(OAObject.class)) {
                Table stable = (Table) hash.get(sc);
                if (stable != null) {
                    int x = (stable.subclasses == null) ? 0: stable.subclasses.length;
                    Class[] cc = new Class[x+1];
                    if (x > 0) System.arraycopy(stable.subclasses, 0, cc, 0, x);
                    cc[x] = table.clazz;
                    stable.subclasses = cc;
                }
            }
        }
    }
    
    /**
        Sets the Tables that are used in this Database.
    */
    public void setTables(Table[] tables) {
        if (tables == null) tables = new Table[0];
        hash.clear();

        for (int i=0; tables!=null && i<tables.length; i++) {
            addTable(tables[i]);
        }
    }

    /**
        Returns the Tables that are used in this Database.
    */
    public Table[] getTables() {
        return tables;   
    }

}

