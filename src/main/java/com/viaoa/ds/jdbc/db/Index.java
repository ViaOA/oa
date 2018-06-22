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

/**
 * Defines indexes for JDBC databases.
 * @author vvia
 *
 */
public class Index {
	public String name;
	public String[] columns;
	public boolean fkey; // is this index for an foreign key (note: some DBs auto create indexes for fkeys)

    public Index(String name, String[] columns) {
    	this.name = name;
    	this.columns = columns;
    }
    public Index(String name, String[] columns, boolean fkey) {
        this.name = name;
        this.columns = columns;
        this.fkey = fkey;
    }
    public Index(String name, String column) {
    	this.name = name;
    	this.columns = new String[] { column };
    }
    public Index(String name, String column, boolean fkey) {
        this.name = name;
        this.columns = new String[] { column };
        this.fkey = fkey;
    }
}

