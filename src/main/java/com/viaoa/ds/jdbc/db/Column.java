/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.ds.jdbc.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.lang.reflect.*;

import com.viaoa.object.OAObject;
import com.viaoa.util.*;

/** 
    Used for mapping database Columns with OAObject properties. 
    Column objects are stored in Table.columns[].
*/
public class Column {  // need to select all with properyName!=null 

    /** table that this column belongs to. */
    public Table table; // set by Table.setColumns()
    /** name of column in table */
    public String columnName;
    /** name of column in table that stores in lowercase, for case sensitive Databases */
    public String columnLowerName;
    /** name of property that column is mapped to. */
    public String propertyName;
    /** is this a primary key column. */
    public boolean primaryKey;
    /** flag to know if this is a foreign key. */
    public boolean foreignKey;  // set by Table.setColumns() & Table.setLinks() if this column is a fkey in a link

    public Class clazz; // 20090301 set by Table.setColumns() & Table.setLinks() if this column is a fkey in a link
    /** type of column variable, java.sql.Types. */
    public int type;  // from sql.Types
    /** maximum length of column. */
    public int maxLength;
    /** amount of decimal places for a numeric column. */
    public int decimalPlaces=-1;

    /** flag to know if column is a autonumber. */
    public boolean assignNextNumber;  // assign seq number to a new object
    /** flag to know if column is a global unique identifier. */
    public boolean guid;
    
    
    
    public Link fkeyLink; // 20090301 set by Table.setColumns() & Table.setLinks() if this column is a fkey in a link
    public int fkeyLinkPos; // 20090301 set by Table.setColumns() & Table.setLinks() if this column is a fkey in a link
    public Column fkeyToColumn; // 20090301 set by Table.setColumns() & Table.setLinks() if this column is a fkey in a link
    
    // 2007/03/08
    public boolean caseSensitive;
    
    // 2006/06/01
    public boolean assignedByDatabase;
    public boolean readOnly;

    /** methods to get property value. */
    Method methodGet;
    /** methods to set property value. */
    Method methodSet;

    
    public Column() {
    }
    public Column(String columnName) {
        this(columnName, "", 0, 0);
    }
    public Column(String columnName, boolean fkey) {
        this(columnName, "", 0, 0);
        foreignKey = true;
    }
    public Column(String columnName, String propertyName) {
        this(columnName, propertyName, 0, 0);
    }

    public Column(String columnName, String propertyName, int type) {
        this.columnName = columnName;
        this.propertyName = propertyName;
        this.type = type;
    }

    public Column(String columnName, String propertyName, int type, int maxLength) {
        this.columnName = columnName;
        this.propertyName = propertyName;
        this.type = type;
        this.maxLength = maxLength;
    }

    public int getSqlType() {
    	return type;
    }
    
    
   /**
        Method used to get property value.
    */
    public Method getGetMethod() {
        if (methodGet == null && table != null) {
            Class clazz = table.getSupportClass();
            if (clazz != null && propertyName != null && propertyName.length() != 0) {
                methodGet = OAReflect.getMethod(clazz, "get"+propertyName);
            }
        }
        return methodGet;
    }
    /**
        Method used to set property value.
    */
    public Method getSetMethod() {
        if (methodSet == null && table != null) {
            Class clazz = table.getSupportClass();
            if (clazz != null && propertyName != null && propertyName.length() != 0) {
                methodSet = OAReflect.getMethod(clazz, "set"+propertyName);
            }
        }
        return methodSet;
    }
    
}



