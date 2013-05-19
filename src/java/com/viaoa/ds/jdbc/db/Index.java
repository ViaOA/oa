package com.viaoa.ds.jdbc.db;

/**
 * Defines indexes for JDBC databases.
 * @author vvia
 *
 */
public class Index {
	public String name;
	public String[] columns;

    public Index(String name, String[] columns) {
    	this.name = name;
    	this.columns = columns;
    }
    public Index(String name, String column) {
    	this.name = name;
    	this.columns = new String[] { column };
    }
}

