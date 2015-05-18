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
import java.util.*;

import com.viaoa.util.ClassModifier;
import com.viaoa.util.OAArray;

/** 
    Used for mapping database Tables with OAObjects. 
*/
public class Table {
    public String name;
    public Class clazz;
    private Link[] links = new Link[0];
    private Column[] columns = new Column[0];
    private Index[] indexes = new Index[0];
    public boolean bLink;  // is this a link table
    public Class[] subclasses;  // set by Database when all tables are loaded
    Constructor constructor;

    // runtime use only
    public transient Class[] selectClasses;
    public transient Column[] selectColumnArray;
    public transient String selectColumns;
    public transient String selectPKColumns;
    public transient DataAccessObject dataAccessObject;
    
    
    
    public Table() {
    }
    public Table(String name, Class clazz) {
        this.name = name;
        this.clazz = clazz;
    }
    public Table(String name, boolean isLinkTable) {
        this.name = name;
        this.bLink = isLinkTable;
    }

    public void setIndexes(Index[] indexes) {
    	this.indexes = indexes;
    }
    public void addIndex(Index index) {
    	int x = indexes.length;
    	Index[] ixs = new Index[x+1];
    	System.arraycopy(indexes, 0, ixs, 0, x);
    	ixs[x] = index;
    	indexes = ixs;
    }
    public Index[] getIndexes() {
    	return indexes;
    }
    
    public Link getLink(Class clazz) {
        for (int i=0; links != null && i<links.length; i++) {
            if (links[i].toTable.clazz.equals(clazz)) return links[i];
        }
        return null;
    }
    public Link getLink(String name) {
        for (int i=0; links != null && i<links.length; i++) {
            if (links[i].propertyName.equalsIgnoreCase(name)) return links[i];
        }
        return null;
    }

    public Link[] getLinks() {
        return links;
    }
    public void setLinks(Link[] links) {
        if (links == null) links = new Link[] {};
        this.links = links;
        updateLinks(true);
    }
    
    public void addLink(String propertyName, Table toTable, String reversePropertyName, int columnFkey) {
        addLink(propertyName, toTable, reversePropertyName, new int[] { columnFkey });
    }
    public void addLink(String propertyName, Table toTable, String reversePropertyName, int[] columnFkeys) {
        Link link = new Link(propertyName, reversePropertyName, toTable);
        int x = columnFkeys.length;
        Column[] cols = new Column[x];
        for (int i=0; i<x; i++) {
            cols[i] = getColumns()[columnFkeys[i]];
        }
        link.fkeys = cols;
        
        if (links == null) links = new Link[] { link };
        else {
            x = links.length;
            Link[] newLinks = new Link[x + 1];
            System.arraycopy(links, 0, newLinks, 0, x);
            newLinks[x] = link;
            links = newLinks;
        }
        updateLinks(true);
    }

    public Class getSupportClass() {
        return clazz;
    }
    public void setSupportClass(Class clazz) {
        this.clazz = clazz;
    }
    public void setColumns(Column[] columns) {
        for (int i=0; columns !=null && i < columns.length; i++) {
            addColumn(columns[i]);
        }
    }
    
    public void addColumn(Column column) {
        this.columns = (Column[]) OAArray.add(Column.class, this.columns, column);
        if (column.table != this) {
            column.foreignKey = false;
            column.table = this;
            Method method = column.getSetMethod();
            if (method != null) {
                Class[] cs = method.getParameterTypes();
                if (cs.length > 0) {
                    Class c = ClassModifier.getClassWrapper(cs[0]);
                    column.clazz = c; 
                }
            }
        }
    }
    
    
    protected void updateLinks(boolean bUpdateToLinks) {
        // 1: flag all columns that are a Fkey
        for (int i=0; links!=null && i<links.length; i++) {
            links[i].table = this;
            for (int k=0; links[i].fkeys != null && k < links[i].fkeys.length; k++) {
                if (!links[i].fkeys[k].primaryKey) {
                	links[i].fkeys[k].foreignKey = true;
                }
            }
        }
        
        // update column type info to match the fkey type
        for (int i=0; links!=null && i<links.length; i++) {
            Link link = links[i];
	    	Column[] cols1 = link.fkeys;
	        Column[] cols2 = getLinkToColumns(link, link.toTable);
	        if (cols1 != null && cols2 != null) {
	        	if (cols1.length != cols2.length) throw new RuntimeException("Links do not have same amount of fkeys and pkeys");
	            for (int j=0; j<cols1.length; j++) {
	                if (cols1[j].primaryKey) {
	                    if (bUpdateToLinks) link.toTable.updateLinks(false);
	                    continue;
	                }
	            	// 20090301
                    cols1[j].type = cols2[j].type;
	            	cols1[j].clazz = cols2[j].clazz;
	            	cols1[j].fkeyLink = link;
                    cols1[j].fkeyLinkPos = j;
                    cols1[j].fkeyToColumn = cols2[j];
	            }
	        }
        }
    }
    public Column[] getColumns() {
        return columns;
    }

    public Column getColumn(String name, String propName) {
        if (name != null && name.length() == 0) name = null;
        if (propName != null && propName.length() == 0) propName = null;
        Column[] cols = getColumns();
        for (int i=0; cols != null && i<cols.length; i++) {
            if (name != null && name.equalsIgnoreCase(cols[i].columnName)) return cols[i];
            if (propName != null && propName.equalsIgnoreCase(cols[i].propertyName)) return cols[i];
        }
        return null;
    }
    public Column getPropertyColumn(String propName) {
        Column[] cols = getColumns();
        for (int i=0; cols != null && i<cols.length; i++) {
            if (propName != null && propName.equalsIgnoreCase(cols[i].propertyName)) return cols[i];
        }
        return null;
    }

    public Constructor getConstructor() {
        if (constructor == null) {
            try {
                if (clazz != null) constructor = clazz.getConstructor(new Class[] {});
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException("OADataSourceJDBC.update() cant get constructor() for class "+clazz.getName(), e);
            }
        }
        
        return constructor;
    }


    /** columns that are needed to retrieve when selecting;
        these include all columns that are mapped to a property and 
        all columns that are needed as fkeys to other objects
    */
    public Column[] getSelectColumns() {
        ArrayList<Column> al = new ArrayList<Column>(15);
        for (int i=0; columns != null && i < columns.length; i++) {
            Column column = columns[i];
            if (column.propertyName == null || column.propertyName.length() == 0) {
                // get all columns that are foreign keys or primary keys
                if (!column.primaryKey && !column.foreignKey) continue;
            }        
            al.add(column);
        }
        Column[] cols = new Column[al.size()];
        al.toArray(cols);
        return cols;
    }

    /** columns that are needed to retrieve primary key.
    */
    public Column[] getPrimaryKeyColumns() {
        ArrayList<Column> al = new ArrayList<Column>(3);
        for (int i=0; columns != null && i < columns.length; i++) {
            Column column = columns[i];
            if (column.primaryKey) al.add(column);
        }
        Column[] cols = new Column[al.size()];
        al.toArray(cols);
        return cols;
    }


    // get the matching link columns in the "to" table
    public Column[] getLinkToColumns(Link link, Table toTable) {
        String revProp = link.reversePropertyName;
        Link[] links = toTable.getLinks();
        Column[] hold = null;
        for (int i=0; links!=null && i < links.length; i++) {
            if (links[i].toTable == this) {
                hold = links[i].fkeys;
                if (revProp != null && links[i].propertyName.equalsIgnoreCase(revProp)) break;
            }
        }
        return hold;
    }

    public Link getReverseLink(Link link) {
        String revProp = link.reversePropertyName;
        Link[] links = link.toTable.getLinks();
        Column[] hold = null;
        for (int i=0; links!=null && i < links.length; i++) {
            if (links[i].toTable == this) {
                if (revProp != null && links[i].propertyName.equalsIgnoreCase(revProp)) return links[i];
            }
        }
        return null;
    }
    
    public void setDataAccessObject(DataAccessObject dao) {
        dataAccessObject = dao;        
    }
    public DataAccessObject getDataAccessObject() {
        return dataAccessObject;
    }
}

