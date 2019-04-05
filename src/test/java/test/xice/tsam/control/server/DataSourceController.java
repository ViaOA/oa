// Copied from OATemplate project by OABuilder 03/22/14 07:48 PM
package test.xice.tsam.control.server;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.*;
import java.util.zip.*;

import test.xice.tsam.model.oa.Developer;
import test.xice.tsam.model.oa.propertypath.DeveloperPP;
import test.xice.tsam.datasource.*;
import test.xice.tsam.resource.Resource;
import com.viaoa.comm.io.OAObjectInputStream;
import com.viaoa.ds.jdbc.*;
import com.viaoa.ds.jdbc.db.DBMetaData;
import com.viaoa.ds.objectcache.*;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDelegate;
import com.viaoa.hub.HubSaveDelegate;
import com.viaoa.object.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.OASyncServer;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.*;

import test.xice.tsam.delegate.*;
import test.xice.tsam.delegate.oa.*;
import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.cs.*;
import test.xice.tsam.model.oa.propertypath.*;

/**
   Used to manage object persistence, includes serialization and JavaDB support.
   See doc/database.txt
    
**/       
public class DataSourceController {
	private static Logger LOG = OALogger.getLogger(DataSourceController.class);
	private DataSource dataSource;
	
	private ServerRoot serverRoot;
	private Hub<ClientRoot> hubClientRoot;
	
    private OADate dateLastSerialize;
    private OATime timeLastSerialize;
    private String cacheFileName;
	
	public DataSourceController(ServerRoot serverRoot, Hub<ClientRoot> hubClientRoot) throws Exception {
	    this.serverRoot = serverRoot;
	    this.hubClientRoot = hubClientRoot;
        // OAObject.setFinalizeSave(true); 

        cacheFileName = Resource.getValue(Resource.DB_CacheFileName);
        String driver = Resource.getValue(Resource.DB_JDBC_Driver);

        if (!Resource.getBoolean(Resource.DB_Enabled, false)) {
            String msg = "NOTE: DataSourceController ********* not using database **********";
            LOG.fine(msg);
            for (int i=0;i<10;i++) {
                //System.out.println(msg);
            }
        }
        else {
            dataSource = new DataSource();
            dataSource.open();
            dataSource.getOADataSource().setAssignIdOnCreate(true);
        }

        // for non-DB objects
        new OADataSourceObjectCache();
        

        if (!Resource.getBoolean(Resource.INI_SaveDataToDatabase, true)) {
            LOG.fine("NOT saving to Database, " + Resource.INI_SaveDataToDatabase + " is false");        
            for (int i=0; i<20; i++) {
                // System.out.println("DataSourceController. is NOT! saving to Database, " + Resource.INI_SaveDataToDatabase + " is false");        
            }
        }
        
        if (!OAString.isEmpty(cacheFileName)) {
            cacheFileName = OAFile.convertFileName(cacheFileName);
            File file = new File(cacheFileName);
            if (file.exists()) {
                LOG.config("OAObjectEmptyHubDelegate.load("+cacheFileName+");");
                try {
                    OAObjectEmptyHubDelegate.load(file);
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "error loading "+cacheFileName+"", e);
                }
                file.delete();
            }
        }
	}

	public OADataSourceJDBC getOADataSourceJDBC() {
	    if (this.dataSource == null) return null;
	    return this.dataSource.getOADataSource();
	}
	
	public boolean loadServerRoot() throws Exception {
        if (this.dataSource == null) return false;
		LOG.log(Level.CONFIG, "selecting Server data");

		/*$$Start: DatasourceController.loadServerRoot $$*/
        serverRoot.getAdminUsers().select("");
        serverRoot.getAdminUsers().loadAllData();
        serverRoot.getApplicationStatuses().select("", ApplicationStatusPP.type());
        serverRoot.getApplicationStatuses().loadAllData();
        serverRoot.getApplicationTypes().select("");
        serverRoot.getApplicationTypes().loadAllData();
        serverRoot.getCommands().select("");
        serverRoot.getCommands().loadAllData();
        serverRoot.getDevelopers().select("", DeveloperPP.lastName());
        serverRoot.getDevelopers().loadAllData();
        serverRoot.getEnvironmentTypes().select("");
        serverRoot.getEnvironmentTypes().loadAllData();
        serverRoot.getIDLs().select("", IDLPP.seq());
        serverRoot.getIDLs().loadAllData();
        serverRoot.getOperatingSystems().select("");
        serverRoot.getOperatingSystems().loadAllData();
        serverRoot.getPackageTypes().select("");
        serverRoot.getPackageTypes().loadAllData();
        serverRoot.getSiloTypes().select("");
        serverRoot.getSiloTypes().loadAllData();
        serverRoot.getSites().select("");
        serverRoot.getSites().loadAllData();
        serverRoot.getTimezones().select("", TimezonePP.utcOffset());
        serverRoot.getTimezones().loadAllData();
        /*$$End: DatasourceController.loadServerRoot $$*/
		
		// dont need to have these Hubs as selectAll in objectCache
		OAObjectCacheDelegate.removeSelectAllHub(serverRoot.getAdminUsers());
		
        return true;
	}

    public void saveData() {
        serverRoot.save(OAObject.CASCADE_ALL_LINKS);
        hubClientRoot.saveAll(OAObject.CASCADE_ALL_LINKS);
        
        OACascade cascade = new OACascade();
        OAObjectSaveDelegate.save(serverRoot, OAObject.CASCADE_ALL_LINKS, cascade);

        HubSaveDelegate.saveAll(hubClientRoot, OAObject.CASCADE_ALL_LINKS, cascade);
        
        OASyncServer ss = OASyncDelegate.getSyncServer();
        if (ss != null) {
            OASyncDelegate.getSyncServer().saveCache(cascade, OAObject.CASCADE_ALL_LINKS);
            OASyncDelegate.getSyncServer().performDGC();
        }
    }

    
    
	// custom
    public void writeAdminUsersToXMLFile() throws Exception {
        String fn = Resource.getValue(Resource.TSAM_DataDirectory, "data");

        fn = OAFile.convertFileName(fn + "/user.xml");
        OAFile.mkdirsForFile(fn);

        File file = new File(fn);
        if (file.exists()) {
            // create backup
            String fnBak = OAFile.convertFileName(fn + ".bak");
            File fileBak = new File(fnBak);
            if (fileBak.exists()) fileBak.delete();
            file.renameTo(fileBak);
        }
        
        OAXMLWriter w = new OAXMLWriter(fn);
        w.setIndentAmount(2);
        
        Hub<AdminUser> hub = ModelDelegate.getAdminUsers();
        w.write(hub);
        w.close();
        LOG.log(Level.CONFIG, "AdminUser data saved to "+fn);
    }
    // custom
    public void readAdminUsersFromXMLFile() throws Exception {
        String fn = Resource.getValue(Resource.TSAM_DataDirectory, "data");
        fn = OAFile.convertFileName(fn + "/user.xml");
        File file = new File(fn);
        boolean bUseLocal = false;
        if (!file.exists()) {
            fn = OAFile.convertFileName("./user.xml");
            file = new File(fn);
            if (!file.exists()) return;
            bUseLocal = true;
        }
        
        OAXMLReader r = new OAXMLReader();
        Object[] objs = r.readFile(fn);
        if (objs == null || objs.length != 1 || !(objs[0] instanceof Hub)) {
            LOG.warning("AdminUser data could not be read, file="+fn);
            return;
        }
        
        for (Object obj : ((Hub)objs[0])) {
            if (!(obj instanceof AdminUser)) continue;
            AdminUser au = (AdminUser) obj;
            ModelDelegate.getAdminUsers().add(au);
        }

        if (bUseLocal) file.delete();
        LOG.log(Level.CONFIG, "AdminUser data read from "+fn);
    }
    

    public void loadYamlData() throws Exception {
        // load initialization data
        try {
            PackageTypeDelegate.importYaml();
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "error trying to load PackageTypes from yaml", e);
        }
        try {
            ApplicationTypeDelegate.importYaml();
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "error trying to load ApplicationTypes from yaml", e);
        }
    }
    
    
    // custom
    public void writeAppTypesToXMLFile() throws Exception {
        String fn = Resource.getValue(Resource.TSAM_DataDirectory, "data");

        fn = OAFile.convertFileName(fn + "/apptype.xml");
        OAFile.mkdirsForFile(fn);

        File file = new File(fn);
        if (file.exists()) {
            // create backup
            String fnBak = OAFile.convertFileName(fn + ".bak");
            File fileBak = new File(fnBak);
            if (fileBak.exists()) fileBak.delete();
            file.renameTo(fileBak);
        }
        
        OAXMLWriter w = new OAXMLWriter(fn);
        w.setIndentAmount(2);
        
        Hub<ApplicationType> hub = ModelDelegate.getApplicationTypes();
        w.write(hub);
        w.close();
        LOG.log(Level.CONFIG, "AppType data saved to "+fn);
    }
    public void readAppTypesFromXMLFile() throws Exception {
        String fn = Resource.getValue(Resource.TSAM_DataDirectory, "data");
        fn = OAFile.convertFileName(fn + "/apptype.xml");
        File file = new File(fn);
        if (!file.exists()) return;
        
        OAXMLReader r = new OAXMLReader();
        Object[] objs = r.readFile(fn);

        if (objs == null || objs.length != 1 || !(objs[0] instanceof Hub)) {
            LOG.warning("AppTypes data could not be read, file="+fn);
            return;
        }
        
        for (Object obj : ((Hub)objs[0])) {
            if (!(obj instanceof ApplicationType)) continue;
            ApplicationType at = (ApplicationType) obj;
            ModelDelegate.getApplicationTypes().add(at);
        }
        LOG.log(Level.CONFIG, "AppType data read from "+fn);
    }
    
    // config file from installer
    public void readAppTypesFromXMLFileInRoot() throws Exception {
        String fn = OAFile.convertFileName("./apptype.xml");
        File file = new File(fn);
        if (!file.exists()) return;

        OAXMLReader r = new OAXMLReader();
        Object[] objs = r.readFile(fn);
        if (objs != null && objs.length != 1 && !(objs[0] instanceof Hub)) {
            LOG.warning("reading ./apptype.xml did not return a hub");
        }
        
        for (Object obj : (Hub)objs[0]) {
            if (!(obj instanceof ApplicationType)) continue;
            ApplicationType at = (ApplicationType) obj;
            ModelDelegate.getApplicationTypes().add(at);
        }
        
        file.delete();
        LOG.log(Level.CONFIG, "AppType data read from "+fn);
    }
    
    // custom
    public void writeDevelopersToXMLFile() throws Exception {
        String fn = Resource.getValue(Resource.TSAM_DataDirectory, "data");

        fn = OAFile.convertFileName(fn + "/developer.xml");
        OAFile.mkdirsForFile(fn);

        File file = new File(fn);
        if (file.exists()) {
            // create backup
            String fnBak = OAFile.convertFileName(fn + ".bak");
            File fileBak = new File(fnBak);
            if (fileBak.exists()) fileBak.delete();
            file.renameTo(fileBak);
        }
        
        OAXMLWriter w = new OAXMLWriter(fn) {
            @Override
            public int shouldWriteProperty(Object obj, String propertyName, Object value) {
                if (obj instanceof Developer) {
                    if (value instanceof OAObject) return OAXMLWriter.WRITE_NO;
                    if (value instanceof Hub) return OAXMLWriter.WRITE_NO;
                    return OAXMLWriter.WRITE_YES;
                }
                return OAXMLWriter.WRITE_NO;
            }
        };
        w.setIndentAmount(2);
        
        Hub<Developer> hub = ModelDelegate.getDevelopers();
        w.write(hub);
        w.close();
        LOG.log(Level.CONFIG, "Developer data saved to "+fn);
    }
    public void readDevelopersFromXMLFile() throws Exception {
        String fn = Resource.getValue(Resource.TSAM_DataDirectory, "data");
        fn = OAFile.convertFileName(fn + "/developer.xml");
        File file = new File(fn);
        if (!file.exists()) return;
        
        OAXMLReader r = new OAXMLReader();
        Object[] objs = r.readFile(fn);
        
        if (objs == null || objs.length != 1 || !(objs[0] instanceof Hub)) {
            LOG.warning("Developers data could not be read, file="+fn);
            return;
        }
        
        for (Object obj : ((Hub)objs[0])) {
            if (!(obj instanceof Developer)) continue;
            Developer dev = (Developer) obj;
            ModelDelegate.getDevelopers().add(dev);
        }
        LOG.log(Level.CONFIG, "Developer data read from "+fn);
    }
    public void readDevelopersFromXMLFileInRoot() throws Exception {
        String fn = OAFile.convertFileName("./developer.xml");
        File file = new File(fn);
        if (!file.exists()) return;
        
        OAXMLReader r = new OAXMLReader();
        Object[] objs = r.readFile(fn);
        
        if (objs == null || objs.length != 1 || !(objs[0] instanceof Hub)) {
            LOG.warning("Developer data could not be read, file="+fn);
            return;
        }
        
        for (Object obj : ((Hub)objs[0])) {
            if (!(obj instanceof Developer)) continue;
            Developer dev = (Developer) obj;
            ModelDelegate.getDevelopers().add(dev);
        }

        file.delete();
        LOG.log(Level.CONFIG, "Developer data read from "+fn);
    }
    
    
	public void writeSerializeToFile(boolean bErrorMode) throws Exception {
        String dirName = Resource.getValue(Resource.TSAM_DataDirectory, "data");
        LOG.config("Saving data to file tsamtemp.bin in "+dirName+", will rename when done");

	    File f1 = new File(dirName);
	    if (f1.exists()) {
	        if (!f1.isDirectory()) {
	            File f2;
	            for (int i=0; ;i++) {
	                f2 = new File(dirName+"_"+i);
	                if (!f2.exists()) break;
	            }
	            f1.renameTo(f2);
	        }
	    }
	    else {
	        f1.mkdir();
	    }
        
        File fileTemp = new File(OAString.convertFileName(dirName + "/tsamtemp.bin"));
        _writeSerializeToFile(fileTemp);
        
	    if (bErrorMode) {
	        OADateTime dt = new OADateTime();
	        String s = dt.toString("yyyyMMdd_HHmmss");
	        File file = new File(OAFile.convertFileName(dirName + "/databaseDump_"+s+"_tsam.bin"));
            if (file.exists()) file.delete();
	        fileTemp.renameTo(file);
            LOG.log(Level.CONFIG, "ErrorMode=true, Data has been saved to " + file.getName());
            return;
        }
        
        File dataFile = new File(OAString.convertFileName(dirName + "/tsam.bin"));
        
        if (dataFile.exists()) {
            String backupName = null;
        
            // save to daily/hourly/5minute 
            OADate d = new OADate();
            if (dateLastSerialize == null || !d.equals(dateLastSerialize)) {
                dateLastSerialize = d;
                backupName = "tsam_daily_" + (d.toString("yyMMdd")) + ".bin";
                File f = new File(OAFile.convertFileName(dirName+"/"+backupName));
                if (f.exists()) backupName = null;
            }
            
            if (backupName == null || backupName.length() == 0) {
                OATime t = new OATime(); 
                if (timeLastSerialize == null || t.getHour() != timeLastSerialize.getHour()) {
                    timeLastSerialize = t;
                    backupName = "tsam_hourly_" + (t.toString("HH")) + ".bin";
                }
                else {
                    // save to nearest 5 minutes, otherwise it could have 60 files over time.
                    int m = t.getMinute();
                    m = m==0 ? 0 : ((m/5) * 5);
                    backupName = "tsam_min_" + OAString.format(m,"00") + ".bin";
                }
            }
            
            backupName = OAFile.convertFileName(dirName + "/"+backupName);
            File backupFile = new File(backupName);
            if (backupFile.exists()) backupFile.delete();
            dataFile.renameTo(backupFile);
            dataFile = new File(OAString.convertFileName(dirName + "/tsam.bin"));
        }
        
        fileTemp.renameTo(dataFile);
        LOG.config("Saved data to file " + dataFile);
	}
	
	@SuppressWarnings("unchecked")
    protected void _writeSerializeToFile(File fileTemp) throws Exception {
		FileOutputStream fos = new FileOutputStream(fileTemp);

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fos, deflater, 1024*5);

		/** Notes on serialization:
		 *  The ObjectStream will make sure that objects are only saved/visited once.
		 *  These 3 wrappers all use the same ObjectStream.
		 */
        ObjectOutputStream oos = new ObjectOutputStream(deflaterOutputStream);
        OAObjectSerializer wrap;
        
        // wrap = new OAObjectSerializer(serverRoot, false, true);
        
        wrap = new OAObjectSerializer(serverRoot, false, new OAObjectSerializerCallback() {
            //@Override
            protected void beforeSerialize(OAObject obj) {
                if (obj instanceof ServerRoot) {
                    /*
                    includeProperties(
                        ServerRoot.P_AdminUsers,
                        ServerRoot.P_ClientAppTypes,
                        ServerRoot.P_EnvironmentTypes,
                        ServerRoot.P_ExecuteCommands,
                        ServerRoot.P_LoginTypes,
                        ServerRoot.P_ServerStatuses,
                        ServerRoot.P_ServerTypes,
                        ServerRoot.P_SiloTypes,
                        ServerRoot.P_Sites,
                        ServerRoot.P_Timezones
                    );
                    excludeProperties(ServerRoot.P_RemoteClients);
                    */
                }
                else if (obj instanceof ApplicationStatus) {
                    excludeProperties(
                            ApplicationStatus.P_Applications
                    );
                }
            }
            /*
            protected void setup(OAObject arg0) {
                // replaced by beforeSerialize()
            }
            */
        });
        oos.writeObject(wrap);

		String s = Resource.getValue(Resource.APP_DataVersion);
		oos.writeObject(s);
		
        deflaterOutputStream.finish();
		oos.close();
		fos.close();
	}

    public boolean readSerializeFromFile() throws Exception {
        String dirName = Resource.getValue(Resource.TSAM_DataDirectory, "data");
        LOG.log(Level.CONFIG, "Reading from file tsam.bin in "+dirName);
        File file = new File(OAFile.convertFileName(dirName+"/tsam.bin"));
        if (!file.exists()) {
            LOG.log(Level.CONFIG, "file tsam.bin does not exist in "+dirName);
            return false;
        }
        FileInputStream fis = new FileInputStream(file);
    
        Inflater inflater = new Inflater();
        InflaterInputStream inflaterInputStream = new InflaterInputStream(fis, inflater, 1024*3);
        
        OAObjectInputStream ois = new OAObjectInputStream(inflaterInputStream);
        
        OAObjectSerializer wrap = (OAObjectSerializer) ois.readObject(); 
        ServerRoot sr = (ServerRoot) wrap.getObject();
        //Note: sr will be the same as this.serverRoot, since it has the same Id.

        try {
            String s = (String) ois.readObject();
            Resource.setValue(Resource.TYPE_Server, Resource.APP_DataVersion, s);
        }
        catch (EOFException e) {
        }
        
        ois.close();
        fis.close();
        LOG.log(Level.CONFIG, "reading from file tsam.bin completed");
        
        // custom, after read
        // clear server.status
        String cpp = SitePP.environments().silos().servers().applications().pp; 
        OAFinder<Site, Application> finder = new OAFinder<Site, Application>(sr.getSites(), cpp) {
            @Override
            protected void onFound(Application application) {
                application.setApplicationStatus(null);
            }
        };
        finder.find(sr.getSites());

        return true;
    }
		
	public void close() {
		if (dataSource != null) {
			dataSource.close();  // this will call JavaDB shutdown, and remove datasource from list of available datasources
		}
        if (!OAString.isEmpty(cacheFileName)) {
            try {
                LOG.config("saving "+cacheFileName);
                OAFile.mkdirsForFile(cacheFileName);
                OAObjectEmptyHubDelegate.save(new File(cacheFileName));
                LOG.config("saved "+cacheFileName);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error while saving "+cacheFileName, e);
            }
        }
	}

	public boolean verifyDataSource() throws Exception {
		LOG.config("Verifying database structure");
		if (dataSource == null) return true;
		dataSource.getOADataSource().verify();
		return true;
	}

    public String getInfo() {
        Vector vec = new Vector();

        OADataSourceJDBC oads = getOADataSourceJDBC();
        vec.addElement("DataSource ============================");
        oads.getInfo(vec);
        
        StringBuffer sb = new StringBuffer(4096);
        int x = vec.size(); 
        for (int i=0; i<x; i++) {
            String s = (String) vec.elementAt(i);
            sb.append(s + "\r\n");
        }
        return new String(sb);
    }

    public boolean isDataSourceReady() {
        if (dataSource == null) return true; 
    	return dataSource.getOADataSource().isAvailable();
    }
    
    public void disconnect() {
        LOG.fine("called");
        if (dataSource != null) {
            dataSource.close();
        }
    }
    public void reconnect() {
        LOG.fine("called");
        if (dataSource != null) {
            dataSource.getOADataSource().reopen(0);
        }
    }
    
    
    public void updateDataSource() {
    	//qqqqqqqqqqqqqqqq
    }
    

    
    private int cacheCnt;
    public void insertAllObjectsToDatabase() throws Exception {
        LOG.config("Loading database");
        
        final HashMap<Class, Integer> hash = new HashMap<Class, Integer>();     
        OAObjectCacheDelegate.callback(new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                cacheCnt++;
                OAObjectDelegate.setNew((OAObject)obj, true);
                Object objx = ((OAObject)obj).getProperty("id");
                if (objx != null && objx instanceof Number) {
                    int id = ((Number) objx).intValue();
                    Integer n = hash.get(objx.getClass());
                    if (n == null || id > n) hash.put(obj.getClass(), id);
                }
                return true;
            }  
        });

        OADataSourceJDBC oads = getOADataSourceJDBC();
        for (Entry<Class, Integer> entry : hash.entrySet()) {
            oads.setNextNumber(entry.getKey(), entry.getValue()+1);
        }
        System.out.println(cacheCnt+" objects updated from Cache");

        OATransaction tran = new OATransaction(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
        try {
            tran.start();
            serverRoot.save(OAObject.CASCADE_ALL_LINKS);
            tran.commit();
        }
        catch (Exception e) {
            System.out.println("Exception: "+e);
            e.printStackTrace();
            tran.rollback();
            throw e;
        }
    }       

    /**
     * Calls verifyDatabaseTables to check for table/index/file corruption.
     * @return true if database is good, else false if there is corruption.
     * @see #forwardRestoreBackupDatabase() to restore from backup and include log files for zero data loss.
     * @see #backupDatabase(String) to perform database files, and include log files - which will enable forward restores.
     */
    public boolean isDatabaseCorrupted() {
        boolean b;
        try {
            checkForDatabaseCorruption();
            b = false;
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "Error while checking database, will return false", e);
            b = true;
        }
        return b;
    }

    /**
     * @see #isDataSourceReady() for descriptions.
     */
    protected void checkForDatabaseCorruption() throws Exception {
        OADataSourceJDBC ds = getOADataSourceJDBC();
        if (ds == null) return;
        DBMetaData dbmd = ds.getDBMetaData();
        if (dbmd == null || dbmd.getDatabaseType() != DBMetaData.DERBY) {
            return;
        }
        LOG.config("Starting Database verification");

        
        String sql;
        Statement statement = null;
        try {
            statement = ds.getStatement("verify database");

            sql = "SELECT t.tablename from sys.sysschemas s, sys.systables t " +
            		"where CAST(s.schemaname AS VARCHAR(128)) = 'APP' AND s.schemaid = t.schemaid " +
            		"ORDER BY t.tablename";
            
            ResultSet rs = statement.executeQuery(sql);
            ArrayList<String> alTable = new ArrayList<String>();
            for (int i=0; rs.next(); i++) {
                alTable.add(rs.getString(1));
            }
            rs.close();

            int i = 0;
            for (String tableName : alTable) {
                LOG.config("Verifiying database table "+tableName);
                LOG.fine((++i)+") verify " + tableName);
                try {
                    sql = "SELECT t.tablename, SYSCS_UTIL.SYSCS_CHECK_TABLE('APP', t.tablename) " +
                    		"from sys.systables t " +
                    		"where CAST(t.tablename AS VARCHAR(128)) = '"+tableName+"'";                    
                    rs = statement.executeQuery(sql);
                    for ( ; rs.next(); ) {
                        LOG.finest(i+") " + rs.getString(1) + " = " + rs.getShort(2));
                    }
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "database verification for table " + tableName + "failed", e);
                    throw e;
                }
            }
            
            LOG.config("Completed Database verification");
        }
        finally {
            ds.releaseStatement(statement);
        }
    }
    
    /**
     * This will make a backup of the live database, with rollforward support.  
     * The database will be under backupDirectory
     * @param backupDirectory example: DB20100428
     * @throws Exception
     */
    public void backupDatabase(String backupDirectory) throws Exception {
        OADataSourceJDBC ds = getOADataSourceJDBC();
        if (ds == null) return;

        DBMetaData dbmd = ds.getDBMetaData();
        if (dbmd == null || dbmd.getDatabaseType() != DBMetaData.DERBY) {
            return;
        }
        
        LOG.config("Starting Database backup to "+backupDirectory);

        Statement statement = null;
        try {
            statement = ds.getStatement("backup database");
            // statement.execute("call SYSCS_UTIL.SYSCS_CHECKPOINT_DATABASE()");
            // statement.execute("call SYSCS_UTIL.SYSCS_BACKUP_DATABASE('"+backupDirectory+"')");
            
            // create a backup, that will store rollforward log files in the current db log directory.  The '1' will delete previous log files
            String sql = "call SYSCS_UTIL.SYSCS_BACKUP_DATABASE_AND_ENABLE_LOG_ARCHIVE_MODE('"+backupDirectory+"', 1)";
            statement.execute(sql);
            
            // this is the commad to disable log archive.  The '1' will delete previous log files
            // SYSCS_UTIL.SYSCS_DISABLE_LOG_ARCHIVE_MODE(1)
            
            // use this to restore 
            // connect 'jdbc:derby:wombat;rollForwardRecoveryFrom=d:/backup/wombat';            
            
            
            LOG.config("Completed Database backup to "+backupDirectory);
        }
        finally {
            ds.releaseStatement(statement);
        }
    }

    /**
     * @see #isDataSourceReady() for descriptions.
     */
    public void forwardRestoreBackupDatabase(String backupDirectory) throws Exception {
        OADataSourceJDBC ds = getOADataSourceJDBC();
        if (ds == null) return;

        DBMetaData dbmd = ds.getDBMetaData();
        if (dbmd == null || dbmd.getDatabaseType() != DBMetaData.DERBY) {
            return;
        }
        LOG.config("Starting forwardRestoreBackupDatabase from "+backupDirectory);
        disconnect();
        
        Class.forName(dbmd.getDriverJDBC()).newInstance();

        if (backupDirectory != null) backupDirectory = backupDirectory.replace('\\', '/');
        String jdbcUrl = dbmd.getUrlJDBC() + ";rollForwardRecoveryFrom=" + backupDirectory;

        String s = dbmd.getUrlJDBC();
        s = OAString.field(s, ":", OAString.dcount(s, ":"));
        jdbcUrl += "/" + s;
        
        
        /// this will open the database and perform a rollForward 
        Connection connection = DriverManager.getConnection(jdbcUrl, dbmd.user, dbmd.password);
        connection.close();

        LOG.config("Completed Database forward restore from "+backupDirectory);
        reconnect();
    }
    
    public void compressDatabaseFiles() throws Exception {
        OADataSourceJDBC ds = getOADataSourceJDBC();
        if (ds == null) return;

        DBMetaData dbmd = ds.getDBMetaData();
        if (dbmd == null || dbmd.getDatabaseType() != DBMetaData.DERBY) {
            return;
        }
        LOG.config("Starting Database compression");
        
        String sql;
        Statement statement = null;
        Connection connection = null;
        try {
            statement = ds.getStatement("compress database");

            sql = "SELECT t.tablename from sys.sysschemas s, sys.systables t " +
                    "where CAST(s.schemaname AS VARCHAR(128)) = 'APP' AND s.schemaid = t.schemaid " +
                    "ORDER BY t.tablename";
            
            ResultSet rs = statement.executeQuery(sql);
            ArrayList<String> alTable = new ArrayList<String>();
            for (int i=0; rs.next(); i++) {
                alTable.add(rs.getString(1));
            }
            rs.close();
            ds.releaseStatement(statement);
            
            connection = ds.getConnection();
            int i = 0;
            for (String tableName : alTable) {
                LOG.fine((++i)+") compressing table " + tableName);
                try {
                    sql = "call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('APP', '" + tableName + "', 1)";
                    CallableStatement cs = connection.prepareCall(sql);
                    cs.execute();
                    cs.close();
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "database compression for table " + tableName + "failed", e);
                    throw e;
                }
            }
            
            LOG.config("Completed Database verification");
        }
        finally {
            ds.releaseConnection(connection);
        }
        
    }
    public void writeScriptFiles() throws Exception {
        try {
            _writeScriptFiles();
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "error writing script file", e);
        }
    }

    protected void _writeScriptFiles() throws Exception {
        String fn = Resource.getValue(Resource.TSAM_DataDirectory, "data");
        fn = OAFile.convertFileName(fn + "/manual.sh");
        OAFile.mkdirsForFile(fn);
        
        File file = new File(fn);
        if (file.exists()) file.delete();

        final PrintWriter pw = new PrintWriter(file);

        pw.println("#!/bin/bash");
        pw.println("# created "+(new OADateTime()));
        pw.println("# mkdir console");
        
        
        String cpp = SiloPP.mradServer().mradClients().pp; 
        OAFinder<Silo, MRADClient> finder = new OAFinder<Silo, MRADClient>(cpp) {
            int cnt;
            @Override
            protected void onFound(MRADClient mc) {
                Application app = mc.getApplication();
                if (app == null) return;

                Server server = app.getServer();
                if (server == null) return;

                String s = "App="+OAString.toString(app.getName());
                s += ", host="+server.getHostName();
                
                ApplicationType appType = app.getApplicationType();
                if (appType != null) {
                    if (appType == ApplicationTypeDelegate.getCEApplicationType()) return;
                    if (appType == ApplicationTypeDelegate.getTEApplicationType()) return;
                    s += ", type="+appType.getCode()+" "+OAString.toString(appType.getDescription());
                }
                
                pw.println("# ");
                pw.println("# "+(++cnt)+") "+s);

                String cmd = "";
                /*
                cmd = SSHExecuteDelegate.getSSHCommandLine(mc, mc.getStartScript());
                cmd += " > console/start"+cnt+".log 2>&1 &";
                pw.println("# START: "+cmd);

                cmd = SSHExecuteDelegate.getSSHCommandLine(mc, mc.getStopScript());                        
                cmd += " > console/stop"+cnt+".log 2>&1 &";
                */
                pw.println("#  STOP: "+cmd);
            }
        };
        finder.find(ModelDelegate.getDefaultSiloHub());
        
        pw.flush();
        pw.close();
        LOG.log(Level.CONFIG, "writeScriptFiles saved to "+fn);
        
    }
    
    /*
    public static void main(String[] args) throws Exception {
        DataSourceController dsc = new DataSourceController();

        dsc.loadServerRoot();
        
        // dsc.backupDatabase("c:\\temp\\dbBackDerby");
        dsc.isDataSourceReady();
        dsc.isDatabaseCorrupted();
        
        System.out.println("Done");
    }
    */
}

