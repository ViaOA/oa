package com.viaoa.test.ds;

import java.sql.*;

import com.viaoa.ds.jdbc.OADataSourceJDBC;
import com.viaoa.ds.jdbc.connection.*;
import com.viaoa.ds.jdbc.db.*;
import com.viaoa.ds.jdbc.query.*;

import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.test.data.DataSource;
import com.viaoa.util.OAString;
import com.vetplan.oa.*;

public class QueryConverterTest {

	public void testConnectionPool() throws Exception {
		methodName = "ConnectionPool";
		DBMetaData dbmd = new DBMetaData();
		dbmd.databaseType = dbmd.ACCESS;
		dbmd.user = "vince";
		dbmd.password = "83is83";
		dbmd.driverJDBC = "sun.jdbc.odbc.JdbcOdbcDriver";
		dbmd.urlJDBC = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};Dbq=c:\\temp\\vetplan.mdb";
		
		ConnectionPool cp = new ConnectionPool(dbmd);
		
		Statement st = cp.getStatement("test", java.sql.Connection.TRANSACTION_NONE);
		
		OAConnection c = cp.getConnection(java.sql.Connection.TRANSACTION_NONE);
		cp.stopThread();
	}
	
	public void testResultSetIterator() throws Exception {
		methodName = "testResultSetIterator";
		
        String user = "vince";
        String password = "83is83";
        String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
        String jdbcUrl = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};Dbq=c:\\temp\\vetplan.mdb";
		
        Class.forName(driver).newInstance();
        Connection connection = DriverManager.getConnection(jdbcUrl,user,password);
        // connection.setAutoCommit(true);
		
        Statement st = connection.createStatement();
        Statement st2 = connection.createStatement();
        
        DataSource ds = new DataSource();
		Database db = ds.getDatabase();
		DBMetaData dbmd = new DBMetaData();
		dbmd.databaseType = dbmd.ACCESS;
		QueryConverter qc = new QueryConverter(db, dbmd);
		
		// String query = "exams";
		String query = null;
		String sql = qc.convertToSql(Pet.class, query, null, null);

		
		// sql = "select " + qc.getSelectColumns(Pet.class) + " " + sql;
		sql = "select " + qc.getPrimaryKeyColumns(Pet.class) + " " + sql;

		
		String sql2 = qc.convertToSql(Pet.class, "id = 777", null, null);
		sql2 = "select " + qc.getSelectColumns(Pet.class) + " " + sql2;
		sql2 = OAString.convert(sql2, "777", "?");
		
		
		Column[] cols = qc.getSelectColumnArray(Pet.class);
		ResultSetIterator ri = new ResultSetIterator(Pet.class, cols, sql, st, sql2, st2, 0);

		for (int i=0; i<100; i++) {
			Pet pet = (Pet) ri.next();
			if (pet == null) break;
			Client c = pet.getClient();
			Species sp = pet.getSpecies();
			System.out.println(i+") "+pet.getName());
			Pet p = (Pet) OAObjectCacheDelegate.get(Pet.class, pet);
			if (p != pet) error("8");
			p = (Pet) OAObjectCacheDelegate.get(Pet.class, pet.getId());
			if (p != pet) error("8.1");
		}
		connection.close();
		
	}
		
	public void testQuery() {
		methodName = "testQuery";
		DataSource ds = new DataSource();
		Database db = ds.getDatabase();
		DBMetaData dbmd = new DBMetaData();
		dbmd.databaseType = dbmd.ACCESS;
		
		QueryConverter qc = new QueryConverter(db, dbmd);

		String s = qc.getPrimaryKeyColumns(Pet.class); 
		
		s = qc.getSelectColumns(Pet.class);
		
		Client c = new Client();
		c.setId("777");
		
	    // convertToSql(Class clazz, String where, Object[] params, String orderBy) {
		String query = "(client.firstname = 'jones' and nickname='big dog\\'s') or (name!=?|| species=?)";
		Object[] params = new Object[] {"dude's",new Integer(1)};
		
		s = qc.convertToSql(Pet.class, query, params, null);
		String s2 = "FROM (PET INNER JOIN CLIENT CLIENT3 ON PET.CLIENTID = CLIENT3.ID) WHERE ( CLIENT3.FIRSTNAME = 'jones' AND PET.NICKNAME = 'big dog''s' ) OR ( PET.NAME <> 'dude''s' OR PET.SPECIESID = '1' )";
		if (!s.equals(s2)) {
			error("1");
		}

		dbmd.databaseType = dbmd.ORACLE;
		s = qc.convertToSql(Pet.class, query, params, null);
		s2 = "FROM PET,CLIENT CLIENT3 WHERE PET.CLIENTID = CLIENT3.ID(+) AND (( CLIENT3.FIRSTNAME = 'jones' AND PET.NICKNAME = 'big dog''s' ) OR ( PET.NAME <> 'dude''s' OR PET.SPECIESID = '1' ))";		
		if (!s.equals(s2)) {
			error("2");
		}
		
		
		query = "client.firstname = 'jones' and (nickname='buffy' || species=?) & Exams.date = '11/15/2007'";
		params = new Object[]{"1"};

		dbmd.databaseType = dbmd.ACCESS;
		s = qc.convertToSql(Pet.class, query, params, null);
		s2 = "FROM (PET LEFT OUTER JOIN CLIENT CLIENT3 ON PET.CLIENTID = CLIENT3.ID) WHERE CLIENT3.FIRSTNAME = 'jones' AND ( PET.NICKNAME = 'buffy' OR PET.SPECIESID = '1' ) AND EXISTS (SELECT * FROM EXAM EXAM1 WHERE PET.id = EXAM1.petId AND EXAM1.DATEVALUE = #2007-11-15#)";		
		if (!s.equals(s2)) {
			error("3");
		}

		dbmd.databaseType = dbmd.ORACLE;
		s = qc.convertToSql(Pet.class, query, params, null);
		s2 = "FROM PET,CLIENT CLIENT3 WHERE PET.CLIENTID = CLIENT3.ID(+) AND (CLIENT3.FIRSTNAME = 'jones' AND ( PET.NICKNAME = 'buffy' OR PET.SPECIESID = '1' ) AND EXISTS (SELECT * FROM EXAM EXAM1 WHERE PET.id = EXAM1.petId AND EXAM1.DATEVALUE = {d '2007-11-15'}))";		
		if (!s.equals(s2)) {
			error("4");
		}

		dbmd.databaseType = dbmd.SQLSERVER;
		s = qc.convertToSql(Pet.class, query, params, null);
		s2 = "FROM (PET LEFT OUTER JOIN CLIENT CLIENT3 ON PET.CLIENTID = CLIENT3.ID) WHERE CLIENT3.FIRSTNAME = 'jones' AND ( PET.NICKNAME = 'buffy' OR PET.SPECIESID = '1' ) AND EXISTS (SELECT * FROM EXAM EXAM1 WHERE PET.id = EXAM1.petId AND EXAM1.DATEVALUE = {d '2007-11-15'})";
		if (!s.equals(s2)) {
			error("5");
		}
		

		dbmd.databaseType = dbmd.SQLSERVER;
		query = "client.firstname = 'jones' and (nickname='buffy' || species=?) & (Exams.date = '11/15/2007' OR exams.examitems.user=?)";
		params = new Object[]{"1", "abc"};
		s = qc.convertToSql(Pet.class, query, params, null);
		s2 = "FROM (PET LEFT OUTER JOIN CLIENT CLIENT3 ON PET.CLIENTID = CLIENT3.ID) WHERE CLIENT3.FIRSTNAME = 'jones' AND ( PET.NICKNAME = 'buffy' OR PET.SPECIESID = '1' ) AND ( EXISTS (SELECT * FROM EXAM EXAM1 WHERE PET.id = EXAM1.petId AND EXAM1.DATEVALUE = {d '2007-11-15'} OR EXISTS (SELECT * FROM EXAMITEM EXAMITEM6 WHERE EXAM1.id = EXAMITEM6.examId AND EXAMITEM6.USERID = 'abc' )))";
		if (!s.equals(s2)) {
			error("6");
		}
		
		dbmd.databaseType = dbmd.ACCESS;
		s = qc.convertToSql(Pet.class, query, params, null);
		s2 = "FROM (PET LEFT OUTER JOIN CLIENT CLIENT3 ON PET.CLIENTID = CLIENT3.ID) WHERE CLIENT3.FIRSTNAME = 'jones' AND ( PET.NICKNAME = 'buffy' OR PET.SPECIESID = '1' ) AND ( EXISTS (SELECT * FROM EXAM EXAM1 WHERE PET.id = EXAM1.petId AND EXAM1.DATEVALUE = #2007-11-15# OR EXISTS (SELECT * FROM EXAMITEM EXAMITEM6 WHERE EXAM1.id = EXAMITEM6.examId AND EXAMITEM6.USERID = 'abc' )))";
		if (!s.equals(s2)) {
			error("7");
		}
		
		dbmd.databaseType = dbmd.ORACLE;
		s = qc.convertToSql(Pet.class, query, params, null);
		s2 = "FROM PET,CLIENT CLIENT3 WHERE PET.CLIENTID = CLIENT3.ID(+) AND (CLIENT3.FIRSTNAME = 'jones' AND ( PET.NICKNAME = 'buffy' OR PET.SPECIESID = '1' ) AND ( EXISTS (SELECT * FROM EXAM EXAM1 WHERE PET.id = EXAM1.petId AND EXAM1.DATEVALUE = {d '2007-11-15'} OR EXISTS (SELECT * FROM EXAMITEM EXAMITEM6 WHERE EXAM1.id = EXAMITEM6.examId AND EXAMITEM6.USERID = 'abc' ))))";
		if (!s.equals(s2)) {
			error("8");
		}
		
		dbmd.databaseType = dbmd.SQLSERVER;
		query = "itemCategories.name = ?";
		params = new Object[]{"household"};
		s = qc.convertToSql(Item.class, query, params, null);
		s2 = "FROM ITEM WHERE EXISTS (SELECT * FROM (LINKITEMSITEMCATEGORIES LINKITEMSITEMCATEGORIES4 INNER JOIN ITEMCATEGORY ITEMCATEGORY18 ON LINKITEMSITEMCATEGORIES4.ITEMCATEGORIESID = ITEMCATEGORY18.ID) WHERE ITEM.id = LINKITEMSITEMCATEGORIES4.itemsId AND ITEMCATEGORY18.NAME = 'household')";
		if (!s.equals(s2)) {
			error("9");
		}
		
		dbmd.databaseType = dbmd.SQLSERVER;
		query = "items.autoselects.ExamItemStatus.name like ?";
		params = new Object[]{"ralph%"};
		s = qc.convertToSql(ItemCategory.class, query, params, null);
		s2 = "FROM ITEMCATEGORY WHERE EXISTS (SELECT * FROM (LINKITEMSITEMCATEGORIES LINKITEMSITEMCATEGORIES1 INNER JOIN ITEM ITEM4 ON LINKITEMSITEMCATEGORIES1.ITEMSID = ITEM4.ID) WHERE ITEMCATEGORY.id = LINKITEMSITEMCATEGORIES1.itemCategoriesId AND EXISTS (SELECT * FROM (AUTOSELECT AUTOSELECT13 INNER JOIN EXAMITEMSTATUS EXAMITEMSTATUS22 ON AUTOSELECT13.EXAMITEMSTATUSID = EXAMITEMSTATUS22.ID) WHERE ITEM4.id = AUTOSELECT13.itemId AND EXAMITEMSTATUS22.NAME like 'ralph%'))";		
		if (!s.equals(s2)) {
			error("10");
		}

		dbmd.databaseType = dbmd.ACCESS;
		dbmd.useExists = false;
		s = qc.convertToSql(ItemCategory.class, query, params, null);
		s2 = "FROM ((((ITEMCATEGORY LEFT OUTER JOIN LINKITEMSITEMCATEGORIES LINKITEMSITEMCATEGORIES1 ON ITEMCATEGORY.ID = LINKITEMSITEMCATEGORIES1.ITEMCATEGORIESID) LEFT OUTER JOIN ITEM ITEM4 ON LINKITEMSITEMCATEGORIES1.ITEMSID = ITEM4.ID) LEFT OUTER JOIN AUTOSELECT AUTOSELECT13 ON ITEM4.ID = AUTOSELECT13.ITEMID) LEFT OUTER JOIN EXAMITEMSTATUS EXAMITEMSTATUS22 ON AUTOSELECT13.EXAMITEMSTATUSID = EXAMITEMSTATUS22.ID) WHERE EXAMITEMSTATUS22.NAME like 'ralph%'";
		if (!s.equals(s2)) {
			error("11");
		}
		
		int x=4;
	}
	
	
	String methodName;
	void error(String msg) {
		System.out.println(methodName+" Error: "+msg);
	}

	public void test() {
		try {
			testConnectionPool();
			testResultSetIterator();
			testQuery();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: "+e);
		}
	}
	
	public static void main(String[] args) {
		QueryConverterTest test = new QueryConverterTest();
		test.test();
		System.out.println("QueryConverterTest done");
	}
	
}









