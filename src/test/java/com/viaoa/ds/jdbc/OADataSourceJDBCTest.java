package com.viaoa.ds.jdbc;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.OASelect;
import com.viaoa.ds.jdbc.OADataSourceJDBC;
import com.viaoa.transaction.OATransaction;
import com.tmgsc.hifivetest.DataSource;
import com.tmgsc.hifivetest.Resource;
import com.tmgsc.hifivetest.model.oa.*;

public class OADataSourceJDBCTest extends OAUnitTest {
    private DataSource dsSqlServer;
    
    @Test
    public void testWithJDBC() {
        // test using the Hi5 sqlserver db
        init();
        
        Resource.setValue(Resource.TYPE_Server, Resource.DB_JDBC_Driver, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        String s = "jdbc:sqlserver://localhost;port=1433;database=gohi5;sendStringParametersAsUnicode=false;SelectMethod=cursor;ConnectionRetryCount=2;ConnectionRetryDelay=2";
        Resource.setValue(Resource.TYPE_Server, Resource.DB_JDBC_URL, s);
        
        Resource.setValue(Resource.TYPE_Server, Resource.DB_User, "gohi5");
        Resource.setValue(Resource.TYPE_Server, Resource.DB_Password, "gohi5");
        //Resource.setValue(Resource.TYPE_Server, Resource.DB_Password_Base64, "");
        Resource.setValue(Resource.TYPE_Server, Resource.DB_DBMD_Type, "2");
        Resource.setValue(Resource.TYPE_Server, Resource.DB_MinConnections, "3");
        Resource.setValue(Resource.TYPE_Server, Resource.DB_MaxConnections, "20");
        
        dsSqlServer = new DataSource();
        try {
            dsSqlServer.open();
            dsSqlServer.getOADataSource().setAssignNumberOnCreate(true);
        }
        catch (Exception e) {
            System.out.println("SQL Server test will not be done");
            e.printStackTrace();
            return;
        }

        OATransaction trans = new OATransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE);
        trans.start();
        try {
            _testSQL(dsSqlServer.getOADataSource());
        }
        finally {
            trans.rollback();
        }
        
        dsSqlServer.close();
        reset();
    }
    
    private void _testSQL(OADataSourceJDBC oj) {
        Merchant merchant = new Merchant(); 
        
        OASelect<Card> selx = new OASelect<Card>(Card.class);
        selx.setWhere(Card.P_Merchants + " = ?");
        selx.setParams(new Object[] {merchant});
        selx.select();
        
        String q1 = selx.getDataSourceQuery();
        // verify that it used Exist for the linkTable
        assertTrue(q1.indexOf("FROM [CARD] WHERE EXISTS (SELECT * FROM [MERCHANTCARD]") >= 0);
        String q2 = selx.getDataSourceQuery2();
        assertNull(q2);
        
        // verify that it used Join for the linkTable
        dsSqlServer.getOADataSource().getDBMetaData().setUseExists(false);
        selx = new OASelect<Card>(Card.class);
        selx.setWhere(Card.P_Merchants + " = ?");
        selx.setParams(new Object[] {merchant});
        selx.select();

        q1 = selx.getDataSourceQuery();
        //s = "SELECT DISTINCT Card.Id FROM ([CARD] LEFT OUTER JOIN [MERCHANTCARD] [MERCHANTCARD1] ON [CARD].[ID] = [MERCHANTCARD1].[CARDID]) WHERE [MERCHANTCARD1].[MERCHANTID] = "+merchant.getId()+"";        
        assertTrue(q1.indexOf("FROM ([CARD] LEFT OUTER JOIN [MERCHANTCARD") >= 0);
    }
    
}










