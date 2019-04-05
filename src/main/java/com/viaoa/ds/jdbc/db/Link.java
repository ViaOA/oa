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
package com.viaoa.ds.jdbc.db;

import java.lang.reflect.*;
import com.viaoa.util.*;

/** 
    Used for defining relationships between tables.
    This maps the columns that are used to reference Tables.
*/
public class Link {
    // keep all fkeys that dont have a column-property pair
    /** name of reference property. */
    public String propertyName;    // name used by object.  ex: getDept() where name="dept"
    /** Table that this references. */
    public Table toTable;
    /** columns that are used to join other table. */
    public Column[] fkeys;  // foreign key columns that need to match pkey/fkey in toTable
    /** name of reference property in reference table that references this table.*/
    public String reversePropertyName;
    
    public Link() {
    }
    /** 
        Create a new reference from one table to another. 
        @param propertyName is name of reference property in object.
        @param reversePropertyName is name of reference property in the table that this table references.
        @param toTable table that this reference is for.
    */
    public Link(String propertyName,String reversePropertyName, Table toTable) {
        this.propertyName = propertyName;
        this.reversePropertyName = reversePropertyName;
        this.toTable = toTable;
    }

    /**
        Returns Link from toTable back to this table.
        This is used to map the columns together when building a JOIN clause.
    */
    public Link getReverseLink() {
        return toTable.getLink(reversePropertyName);
    }

    Method methodGet;
    Table table;
    
    /**
        Method used to get reference property.
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

}

