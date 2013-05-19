package com.viaoa.ds.jdbc.delegate;

import java.lang.reflect.*;
import java.sql.Statement;

import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.ds.jdbc.query.*;
import com.viaoa.object.*;

/**
 * Used to get additional information about a JDBC DataSource.
 * @author vvia
 *
 */
public class Delegate {

    /**
	    Returns max length allowed for a property.  returns "-1" for any length
	*/
	public static int getPropertyMaxLength(OADataSourceJDBC ds, Class c, String propertyName) {
	    QueryConverter qc = new QueryConverter(ds);
	    Class[] classes = qc.getSelectClasses(c);
	
	    for (int i=0; classes != null && i < classes.length; i++) {
	        Table table = ds.getDatabase().getTable(classes[i]);
	        if (table == null) continue;
	        Column[] columns = table.getSelectColumns();
	        for (int ii=0; columns != null && ii < columns.length; ii++) {
	            if (propertyName.equalsIgnoreCase(columns[ii].propertyName)) {
	                return getMaxLength(columns[ii]);
	            }
	        }
	    }
	    return -1;
	}
	
	public static int getMaxLength(Column c) {
		if (c == null) return -1;
	    Method m = c.getGetMethod();
	    if (m != null) {
	        if (m.getReturnType().equals(String.class)) {
	            if (c.maxLength < 256) {
	            	int type = c.getSqlType();
	            	if (type == 0 || type == java.sql.Types.VARCHAR || type == java.sql.Types.CHAR) {
	            		return c.maxLength;
	            	}
	            }
	        }
	        return -1;
	    }
	    return c.maxLength;
	}
	

    public static void adjustDatabase(OADataSourceJDBC ds) {
	    if (ds == null) return;
    	Database database = ds.getDatabase();
    	DBMetaData dbmd = ds.getDBMetaData();

	    Table[] tables = database.getTables();
	    for (int i=0; i<tables.length; i++) {
	        Table t = tables[i];
	        Column[] columns = t.getColumns();
	        for (int j=0; j<columns.length; j++) {
	            Column c = columns[j];
	        	if (c.type != java.sql.Types.VARCHAR) continue;
	        	if (c.caseSensitive) continue;

        		boolean bLower = (c.columnLowerName != null && c.columnLowerName.toUpperCase().endsWith("LOWER"));
        		if (!bLower && !dbmd.caseSensitive) continue;

		        Index[] indexes = t.getIndexes();
		        for (int k=0; k<indexes.length; k++) {
	        		Index ind = indexes[k];
	        		for (int kk=0; kk<ind.columns.length; kk++) {
	        			if (!ind.columns[kk].equalsIgnoreCase(c.columnName)) {
		        			if (!ind.columns[kk].equalsIgnoreCase(c.columnLowerName)) continue;
	        			}
	        			if (dbmd.caseSensitive) {
        					c.columnLowerName = c.columnName + "Lower";
        					ind.columns[kk] = c.columnName+  "Lower";
	        			}
	        			else {
    						ind.columns[kk] = c.columnName;
        					c.columnLowerName = null;
	        			}
	        		}
		        }
	        }
	    }
    }
    
}



