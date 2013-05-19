package com.viaoa.test.ds;

import java.util.Iterator;

import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.connection.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.ds.jdbc.delegate.SelectDelegate;
import com.viaoa.ds.jdbc.query.*;

import com.viaoa.object.*;
import com.viaoa.test.data.*;
import com.viaoa.util.*;
import com.vetplan.oa.*;

public class SelectDelegateTest {

	private OADataSourceJDBC ds;
	
	
	
	void openDB() {
		com.viaoa.test.data.DataSource vpds = new com.viaoa.test.data.DataSource();
		Database db = vpds.getDatabase();

		DBMetaData dbmd = new DBMetaData(DBMetaData.ACCESS);
        dbmd.user = "";
        dbmd.password = "";
        dbmd.driverJDBC = "org.apache.derby.jdbc.EmbeddedDriver";
		String param = "C:\\projects\\data\\vetplan\\safari\\derby\\database";
        dbmd.urlJDBC = "jdbc:derby:" + param;

        ds = new OADataSourceJDBC(db, dbmd);
	}
	
	void testSelect() {
		methodName = "testSelect";
		String query = "items.autoselects.ExamItemStatus.name like ?";
		Object[] params = new Object[]{"ralph%"};
		query = "name != ?";
		
		// public static Iterator select(OADataSourceJDBC ds, Class clazz, String queryWhere, Object[] params, String queryOrder) {
		Iterator iter = SelectDelegate.select(ds, ItemCategory.class, query, params, "");
		for (int i=0;iter.hasNext(); i++) {
			ItemCategory ic = (ItemCategory) iter.next();
			System.out.println(i+") "+ic.getName());
	
			Iterator iter2 = SelectDelegate.select(ds, Item.class, ic, "", "items", "");
			for (int ii=0;iter2.hasNext(); ii++) {
				Item item = (Item) iter2.next();
				System.out.println("  "+ii+") "+item.getName());
			}
		}
	}

	
	
	
	
	String methodName;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}
	public void test() {
		try {
			openDB();
			testSelect();
			ds.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: "+e);
		}
	}
	public static void main(String[] args) {
		SelectDelegateTest test = new SelectDelegateTest();
		//test.test();
		test.kyle();
		System.out.println("SelectDelegateTest done");
	}
	
	
	
	
	
	
	
	public void kyle() {
		int a = 0;
		int b = 1;
		int c = a + b + 3;
		
		for (int i=0; i<50; i++) {
			System.out.println(""+(i+c));
		}
		
		// System.out.println("-> "+c);
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}


