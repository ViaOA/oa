package com.viaoa.object;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.OADataSourceIterator;
import com.viaoa.ds.OASelect;
import com.viaoa.ds.jdbc.OADataSourceJDBC;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDataDelegate;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.OAFilter;

import test.hifive.DataSource;
import test.hifive.Resource;
import test.hifive.model.oa.*;

public class OAObjectDeleteDelegateTest extends OAUnitTest {

    @Test
    public void testPrivate() {
        /*
         * hi5.ImageStore has many private links. 
         */
        init();
        
        final AtomicInteger ai = new AtomicInteger();
        final AtomicInteger aiSelect = new AtomicInteger();
        dsCache = new OADataSourceObjectCache() {
            @Override
            public void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propertyNameFromMaster) {
                // make sure that this called once
                if (masterObject instanceof Program && "LogoImageStores".equalsIgnoreCase(propertyNameFromMaster)) {
                    ai.incrementAndGet();
                }
                super.updateMany2ManyLinks(masterObject, adds, removes, propertyNameFromMaster);
            }
            
            @Override
            public OADataSourceIterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, OAObject whereObject,
                    String propertyFromWhereObject, String extraWhere, int max, OAFilter filter, boolean bDirty) {
                aiSelect.incrementAndGet();
                return super.select(selectClass, queryWhere, params, queryOrder, whereObject, propertyFromWhereObject, extraWhere, max, filter, bDirty);
            }
        };
        
        
        ImageStore is = new ImageStore();
        assertEquals(is.getId(), 1); // auto assigned
        
        // 1toM private program has images, image has private program
        Program program = new Program();
        Hub h = program.getLogoImageStores();
        h.add(is);
        //when deleting:  img needs to ds.select linkTable to find program and delete linkTable records
        
        Object objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_LogoProgram);
        assertEquals(objx, program);

        // 1to1 private  loc has image, image has private location
        Location loc = new Location();
        loc.setCeoImageStore(is);
        //when deleting:  img needs to ds.select to find location and set loc.img=null

        // Mto1 private awardType has image, image has private awardTypes
        ArrayList<AwardType> alAt = new ArrayList<AwardType>();
        for (int i=0; i<5; i++) {
            AwardType at = new AwardType();
            alAt.add(at);
            at.setCeoSignatureImageStore(is);
            //when deleting:  img needs to ds.select to find awardTypes and remove references, and remove records in linkTable
        }
        
        
        AwardType awardType = alAt.get(0); 
        // 1to1 private awardType has image, image has privete awardType
        awardType.setCeoImageStore(is);
        //when deleting:  img needs to ds.select to find awardType
        
        // dsObjCache uses cache finder
        
        int cntSelect = 0;
        assertEquals(cntSelect, aiSelect.get());
        is.save();
        assertEquals(cntSelect, aiSelect.get());

        // has to call ds to get it
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_CeoLocation);
        assertEquals(++cntSelect, aiSelect.get());
        assertEquals(objx, loc);
        
        is.delete();
        assertEquals(cntSelect+=19, aiSelect.get());

        // make sure that ds will remove references, and link table records        
        for (AwardType at : alAt) {
            assertNull(at.getCeoSignatureImageStore());
        }
        
        assertNull(awardType.getCeoImageStore());
        
        assertEquals(1, ai.get());
        OAObject[] objs = HubDataDelegate.getRemovedObjects(program.getLogoImageStores());
        assertTrue(objs == null || objs.length == 0);
        
        assertTrue(is.isDeleted());
        assertTrue(is.isNew());
        assertEquals(is.getId(), 1); // auto assigned
        
        assertEquals(0, program.getLogoImageStores().getSize());
        assertNull(loc.getCeoImageStore());
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_LogoProgram);
        assertNull(objx);
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_CeoLocation);
        assertNull(objx);
        
        objx = OAObjectPropertyDelegate.getProperty(is, is.P_CeoLocation);
        assertNull(objx);
        
        dsCache.close();
        reset();
    }        

    
    private DataSource dsSqlServer;
    
    @Test
    public void testWithJDBC() throws Exception {
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
 
        dsSqlServer.open();
        if (!dsSqlServer.getOADataSource().verify()) {
            System.out.println("SQL Server test will not be done");
            return;
        }
        dsSqlServer.getOADataSource().setAssignNumberOnCreate(true);

        OATransaction trans = new OATransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE);
        trans.start();
        try {
            _testM2MWithJDBC(dsSqlServer.getOADataSource());
            _testPrivateWithJDBC();
        }
        finally {
            trans.rollback();
        }
        
        dsSqlServer.close();
        reset();
    }
    
    private void _testM2MWithJDBC(OADataSourceJDBC oj) {
        Merchant merchant = new Merchant(); 
        assertTrue(merchant.isNew());
        ArrayList<Card> al = new ArrayList<Card>();
        for (int i=0; i<5; i++) {
            Card card = new Card();
            al.add(card);
            card.getMerchants().add(merchant);
            assertTrue(merchant.getCards().contains(card));
        }
        merchant.save();
        assertFalse(merchant.isNew());
        
        for (Card card : al) {
            assertFalse(card.isNew());
            assertFalse(card.isChanged());
        }
        
        OAObjectCacheDelegate.removeObject(merchant);
        for (Card card : al) {
            OAObjectCacheDelegate.removeObject(card);
        }
        
        OASelect<Merchant> sel = new OASelect<Merchant>(Merchant.class);
        sel.setWhere("id = ?");
        sel.setParams(new Object[] {merchant.getId()});
        sel.select();
        Merchant m = sel.next();
        assertEquals(merchant.getId(), m.getId());
        Hub h = m.getCards();
        assertEquals(5, h.getSize());
        
        OAObjectCacheDelegate.removeObject(m);
        for (Card card : m.getCards()) {
            OAObjectCacheDelegate.removeObject(card);
        }

        
        // make sure linkTable was written
        OASelect<Card> selx = new OASelect<Card>(Card.class);
        selx.setWhere(Card.P_Merchants + " = ?");
        selx.setParams(new Object[] {merchant});
        selx.select();
        
        for (int i=0; i<5; i++) {
            Card cx = selx.next();
            assertNotNull(cx);
            OAObjectCacheDelegate.removeObject(cx);
        }
        
        merchant.delete();
        sel = new OASelect<Merchant>(Merchant.class);
        sel.setWhere("id = ?");
        sel.setParams(new Object[] {merchant.getId()});
        sel.select();
        m = sel.next();
        assertNull(m);

        selx = new OASelect<Card>(Card.class);
        selx.setWhere(Card.P_Merchants + " = ?");
        selx.setParams(new Object[] {merchant});
        selx.select();
        Card cx = selx.next();
        assertNull(cx);
    }
    
    private void _testPrivateWithJDBC() {
        ImageStore is = new ImageStore();
        final int isId = is.getId();
        
        is.save();
        
        // 1toM private program has images, image has private program
        final Program program = new Program();
        program.save();
        Hub h = program.getLogoImageStores();
        h.add(is);
        //when deleting:  img needs to ds.select linkTable to find program and delete linkTable records
        program.save();
        Object objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_LogoProgram);
        assertEquals(objx, program);

        // 1to1 private  loc has image, image has private location
        final Location loc = new Location();
        loc.setCeoImageStore(is);
        loc.save();
        //when deleting:  img needs to ds.select to find location and set loc.img=null

        // Mto1 private awardType has image, image has private awardTypes
        final ArrayList<AwardType> alAt = new ArrayList<AwardType>();
        for (int i=0; i<5; i++) {
            AwardType at = new AwardType();
            alAt.add(at);
            at.setCeoSignatureImageStore(is);
            at.save();
            //when deleting:  img needs to ds.select to find awardTypes and remove references, and remove records in linkTable
        }
        
        
        AwardType awardType = alAt.get(0); 
        // 1to1 private awardType has image, image has privete awardType
        awardType.setCeoImageStore(is);
        awardType.save();
        //when deleting:  img needs to ds.select to find awardType
        

        // has to call ds to get it
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_CeoLocation);
        assertEquals(objx, loc);
        
        is.delete();
        
        assertEquals(0, program.getLogoImageStores().size());
        assertNull(loc.getCeoImageStore());
        
        // make sure that ds will remove references, and link table records        
        for (AwardType at : alAt) {
            assertNull(at.getCeoSignatureImageStore());
        }
        
        assertNull(awardType.getCeoImageStore());
        
        OAObject[] objs = HubDataDelegate.getRemovedObjects(program.getLogoImageStores());
        assertTrue(objs == null || objs.length == 0);
        
        assertTrue(is.isDeleted());
        assertTrue(is.isNew());
        
        assertEquals(0, program.getLogoImageStores().getSize());
        assertNull(loc.getCeoImageStore());
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_LogoProgram);
        assertNull(objx);
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_CeoLocation);
        assertNull(objx);
        
        objx = OAObjectPropertyDelegate.getProperty(is, is.P_CeoLocation);
        assertNull(objx);
        
        program.delete();
        loc.delete();
        for (AwardType at : alAt) {
            at.delete();
        }
        
        assertTrue(program.isDeleted());
        assertTrue(program.isNew());
        assertTrue(loc.isDeleted());
        assertTrue(loc.isNew());
    }        
    
    
}










