// Copied from OATemplate project by OABuilder 03/22/14 07:48 PM
package test.theice.tsam.resource;

import java.util.*;
import java.util.logging.Logger;
import java.net.URL;
import java.text.*;

import javax.swing.ImageIcon;

import com.viaoa.jfc.text.spellcheck.SpellChecker;
import com.viaoa.util.*;


/**
 * Used to setup Locale specific name/value pairs from 
 * runtime properties, System.properties,
 * server.ini, client.ini, and resourceBundle.
 * 
 * see: values.properties file for list of name/value pairs.
 * 
 *	Note: as a convention, 
 *      directory paths should not end with a pathSeparator character.
 *      any value that is a file path should be converted to correct pathSeparator
 *       
 */
public class Resource {
    private static Logger LOG = OALogger.getLogger(Resource.class);

    // type of resource
	public static final int TYPE_Server  = 0;  // server.ini
	public static final int TYPE_Client  = 1;  // client.ini
	public static final int TYPE_Runtime = 2;  // main args[]

	// startup type
	public static final int RUNTYPE_Client = 0;
    public static final int RUNTYPE_JWSClient = 1;
    public static final int RUNTYPE_Server = 2;
    public static final int RUNTYPE_Service = 3;
    private static int runType;
    public static final String[] STARTUP_TYPES = new String[] 
            { "CLIENT", "JWSCLIENT", "SERVER", "SERVICE" };
	
    private static OAProperties propsRuntime;   
    private static OAProperties propsSystem;   
    private static OAProperties propsServer; 	
    private static OAProperties propsClient; 	
    private static OAProperties propsBundle;   
    private static ResourceBundle resourceBundle;

    private static boolean bServerPropChanged;
    private static boolean bClientPropChanged;
    private static String rootDir;

    protected static Locale locale;
    protected static NumberFormat numberFormat, currencyFormat, decimalFormat, integerFormat;
	
	private static String runTimeName;
	
    private static SpellChecker spellCheckerInternal;
    private static SpellChecker spellChecker;
    
	// RESOURCE Bundle names
	// NOTE: These need to match up with values.properties
    public static final String APP_ApplicationName          = "ApplicationName";
	public static final String APP_ServerApplicationName    = "ServerApplicationName";
    public static final String APP_ClientApplicationName    = "ClientApplicationName";

    public static final String APP_ShortName         = "ShortName";
	public static final String APP_Version           = "Version";
    public static final String APP_Demo              = "Demo";
	public static final String APP_DataVersion       = "DataVersion";
	public static final String APP_Release           = "Release";
	public static final String APP_ReleaseDate       = "ReleaseDate";
	public static final String APP_ClassPath         = "ClassPath";
	public static final String APP_RootDir           = "RootDir";
	public static final String APP_JWSRootDir        = "JWSRootDir";

    public static final String APP_HostName          = "HostName";  // this computer    
    public static final String APP_HostIPAddress     = "HostIPAddress";  // this computer    
	public static final String APP_Server            = "Server";  // actual server host    
    public static final String APP_ServerPort        = "ServerPort";  
    public static final String APP_ServerDisplayName = "ServerDisplayName";

    public static final String APP_JettyServer       = "JettyServer";
    public static final String APP_JettyPort         = "JettyPort";
    public static final String APP_JettySSLPort      = "JettySSLPort";
    public static final String APP_JettyDirectory    = "JettyDirectory";  // "webcontent"
    public static final String APP_Website           = "Website";   // url
    public static final String APP_WelcomePage       = "WelcomePage";
    public static final String APP_MainCardPanel     = "MainCardPanel";  // for client UI
    
    
	public static final String APP_JarImageDir       = "JarImageDir";
    public static final String APP_JarImageDir2      = "JarImageDir2";
	public static final String APP_FileImageDir      = "FileImageDir";
    public static final String APP_ReportDir         = "ReportDir";
	public static final String APP_Copyright         = "Copyright";
	public static final String APP_JarFileName       = "JarFileName";
	public static final String APP_Welcome           = "Welcome";
	public static final String APP_GoodBye           = "GoodBye";
	public static final String APP_JDKVersion        = "JDKVersion";
	public static final String APP_LogFileName       = "LogFileName";
    public static final String APP_LogLevel          = "LogLevel";
    public static final String APP_ConsoleLogLevel   = "ConsoleLogLevel";
	public static final String APP_LogErrorDays      = "LogErrorDays";   // days to keep Error log files
	public static final String APP_LogRegularDays    = "LogRegularDays"; // days to keep regular log files
	public static final String APP_HtmlOutputDir     = "HtmlOutputDir";  // where to write HTML text to.
	public static final String APP_MaxHeapMemServer  = "MaxHeapMemServer"; 
	public static final String APP_MaxHeapMemClient  = "MaxHeapMemClient"; 
	public static final String APP_HelpPath1         = "HelpPath1"; 
	public static final String APP_HelpPath2         = "HelpPath2"; 
	public static final String APP_HelpJarPath       = "HelpJarPath"; 
	public static final String APP_LookAndFeel       = "LookAndFeel"; 
	public static final String APP_IntegerFormat     = "IntegerFormat"; 
	public static final String APP_DecimalFormat     = "DecimalFormat"; 
	public static final String APP_MoneyFormat       = "MoneyFormat"; 
    public static final String APP_BooleanFormat     = "BooleanFormat"; 
	public static final String APP_DateTimeFormat    = "DateTimeFormat"; 
	public static final String APP_DateFormat        = "DateFormat"; 
	public static final String APP_TimeFormat        = "TimeFormat";
    public static final String APP_PhoneFormat       = "PhoneFormat";
    public static final String APP_ZipCodeFormat     = "ZipCodeFormat";
	public static final String APP_ClientMonitorInterval = "ClientMonitorInterval"; // number of seconds to sleep before sending message to server
    public static final String APP_DictionaryFileName = "DictionaryFileName";    // original dict of words
    public static final String APP_DictionaryFileName2 = "DictionaryFileName2";  // new approved words
    public static final String APP_NewWordsFileName  = "NewWordsFileName";

    public static final String APP_UniqueServerName  = "UniqueName"; // set by script (for server), and jettyController (for client)
   
	// ini file properties
	public static final String INI_AutoLogin         = "AutoLogin";	
    public static final String INI_AutoLogout        = "AutoLogout"; 
	public static final String INI_StoreLogin        = "StoreLogin";	
	public static final String INI_LanguageCode      = "LanguageCode";	
	public static final String INI_CountryCode       = "CountryCode";	
	public static final String INI_LogLevel          = "LogLevel";	
	public static final String INI_Location          = "Location";	
	public static final String INI_User              = "User";	
	public static final String INI_Password          = "Password";	
	public static final String INI_StorePassword     = "StorePassword";	
	public static final String INI_License           = "License";
	public static final String INI_Debug             = "Debug";
	public static final String INI_DBDebug           = "DBDebug";
	public static final String INI_SuperAdmin        = "SuperAdmin";
	public static final String INI_SaveDataToFile    = "SaveDataToFile";
    public static final String INI_SaveDataToDatabase = "SaveDataToDatabase";
	
    public static final String DB_Enabled            = "db.enabled";
    public static final String DB_JDBC_Driver        = "db.jdbcDriver";
    public static final String DB_JDBC_URL           = "db.jdbcUrl";
    public static final String DB_User               = "db.user";
    public static final String DB_Password           = "db.password";
    public static final String DB_Password_Base64    = "db.passwordBase64";
    public static final String DB_DBMD_Type          = "db.dbmdType";
    public static final String DB_MinConnections     = "db.minConnections";
    public static final String DB_MaxConnections     = "db.maxConnections";
    public static final String DB_BackupDirectory    = "db.backupDirectory"; 
    public static final String DB_CheckForCorruption = "db.checkForCorruption";
    public static final String DB_CacheFileName      = "db.cacheFileName";
    
    public static final String SERVER_RecordFileName    = "Server.RecordFileName";
    public static final String SERVER_PlayBackFileName  = "Server.PlaybackFileName";
    public static final String SERVER_PreloadData       = "Server.PreloadData";
    public static final String SERVER_PreloadDataMaxThreadCount = "Server.PreloadDataMaxThreadCount";

    // number of minutes to allow for AWTThread response before close client application.
    public static final String CLIENT_CheckAWTThreadMinutes = "Client.CheckAWTThreadMinutes";    
    
    
    // Images
	public static final String IMG_CSHelp          = "Image.CSHelp";
	public static final String IMG_Exit            = "Image.Exit";
	public static final String IMG_Help            = "Image.Help";
	public static final String IMG_AppIcon         = "Image.AppIcon";
    public static final String IMG_PrintPreview    = "Image.PrintPreview";
	public static final String IMG_Print           = "Image.Print";
	public static final String IMG_Save            = "Image.Save";
	public static final String IMG_Splash          = "Image.Splash";
	public static final String IMG_Login           = "Image.Login";
    public static final String IMG_Undo            = "Image.Redo";
    public static final String IMG_Redo            = "Image.Undo";
    public static final String IMG_Pdf             = "Image.Pdf";
    public static final String IMG_PdfBig          = "Image.PdfBig";
    public static final String IMG_ImageNotFound   = "Image.ImageNotFound";
    public static final String IMG_Search          = "Image.search";
    public static final String IMG_Goto            = "Image.Goto";
    public static final String IMG_GoBack          = "Image.GoBack";
    public static final String IMG_Open            = "Image.Open";
    public static final String IMG_OpenFile        = "Image.OpenFile";
    public static final String IMG_Filter          = "Image.Filter";
    public static final String IMG_NoFilter        = "Image.NoFilter";
    public static final String IMG_GroupBy         = "Image.GroupBy";
    public static final String IMG_Wizard          = "Image.Wizard";

	// Report
    public static final String RPT_PaperWidth   = "report.PaperWidth";  // => "estimate.report.PaperWidth"
    public static final String RPT_PaperHeight  = "report.PaperHeight";
    public static final String RPT_X            = "report.x";
    public static final String RPT_Y            = "report.y";
    public static final String RPT_Width        = "report.width";
    public static final String RPT_Height       = "report.height";
    public static final String RPT_Orientation  = "report.orientation";
    
    // Email
    public static final String EMAIL_SMTP_Host  = "email.smtp.host";
    public static final String EMAIL_SMTP_Port  = "email.smtp.port";
    public static final String EMAIL_UserId     = "email.userId";
    public static final String EMAIL_Password   = "email.password";
    
    
    public static final String MSG_InvalidJVM     = "msg.InvalidJVM";
    public static final String MSG_StartupError   = "msg.StartupError";

    
    
    // Custom
    public static final String TSAM_DataDirectory        = "DataDirectory";

    // these are to match the normal hostname values
    public static final String TSAM_Site          = "tsam.site";  // ex:  rv, pdk, ord
    public static final String TSAM_Env           = "tsam.env";   // ex:  dv2, ft3, pr, st
    public static final String TSAM_Silo          = "tsam.silo";  // ex:  ix/ice/empty, ex/endex, lx/liffe
    public static final String TSAM_Traps         = "tsam.traps"; // flag to send snmp traps
    
    // ========= Custom, for other servers ==========
    public static final String GSMR_Servers = "gsmr.servers";
    
    // Login/Logout settings
    public static final String LLAD_Servers                = "llad.servers";
    public static final String LLAD_Port                   = "llad.port";
    public static final String LLAD_HeartbeatInterval      = "llad.heartbeatInterval";
    public static final String LLAD_RequestTimeoutInMillis = "llad.requestTimeoutInMillis";
    
    // MRAD settings
    public static final String MRAD_Port                   = "mrad.port";
    
        
    
    /**
     * Location of internally used images, uses getResourceVaue(MSG_JarImagePath)
     */
	public static String getJarImageDir() {
		return getValue(APP_JarImageDir);
	}

    /**
     * Location of images stored by User, uses getResourceVaue(MSG_FileImagePath)
     * Note: does not convert PathSep chars
     */
	public static String getFileImageDir() {
		return getValue(APP_FileImageDir);
	}
	
    public static void setRunTimeName(String name) {
        runTimeName = name;
    }
	public static String getRunTimeName() {
		if (runTimeName == null) {
	        runTimeName = getValue(APP_ApplicationName);
		}
        return runTimeName;
	}
	
    public static void setRunType(int x) {
        runType = x;
    }
    public static int getRunType() {
        return runType;
    }

	
	/**
	 * This will return a directory path that is correct for OS and ends in a directory separator.
	 */
	public static String getDirectory(String name) {
		String s = getValue(name, (String) null);
		if (s != null) {
			if (!s.endsWith("/") && !s.endsWith("\\")) s += "/";
			s = OAString.convertFileName(s);
		}
		else s = "";
		return s;
	}
	
	public static boolean getBoolean(String name) {
		return OAConv.toBoolean(getValue(name, "false")); 
	}
    public static boolean getBoolean(String name, boolean bDefault) {
        return OAConv.toBoolean(getValue(name, new Boolean(bDefault).toString()) ); 
    }
    public static int getInt(String name) {
        return OAConv.toInt(getValue(name, "0")); 
    }
    public static int getInt(String name, int i) {
        String s = getValue(name, i+"");
        if (s == null) return i;
        return OAConv.toInt(s); 
    }
    public static double getDouble(String name) {
        return OAConv.toDouble(getValue(name, "0.0")); 
    }
    public static double getDouble(String name, double d) {
        String s = Resource.getValue(name, (String) null);
        if (s == null) return d;
        return OAConv.toDouble(s); 
    }
	
	public static String getValue(String name, Object[] args) {
		return getValue(name, args, name+" not in Resource Bundle file \"values.properties\"");
	}

    public static String getValue(String name, String defaultValue) {
        return getValue(name, null, defaultValue);
    }

    public static String getValue(String name) {
        return getValue(name, null, null);
    }
    
    /**
     * Check to see if name is defined.
     */
    public static boolean isDefined(String name) {
        String s = getValue(name, null, null);
        return s != null;
    }
	
	/**
	 * Case-insensitive way to get name/value.  
	 * Checks  runtime args, system properties, client.ini, server.ini, values.properties
	 */
    protected static String getValue(String name, Object[] args, String defaultValue) {
        String value = _getValue(name, args, defaultValue);
        if (value != null) {
            value = value.trim();
            if (args != null && args.length > 0) {
                value = MessageFormat.format(value, args);
            }
        }
        else {
            value = defaultValue;
        }
        return value;
    }
    
    private static String _getValue(String name, Object[] args, String defaultValue) {
        if (name == null) return null;
        String value;

        value = getRuntimeProperties().getString(name);
        if (value != null) return value;

        value = getSystemProperties().getString(name);
        if (value != null) return value;
        
        if (runType == RUNTYPE_Server || runType == RUNTYPE_Service) {
            value = getServerProperties().getString(name);
            if (value != null) return value;
        }
        else {
            value = getClientProperties().getString(name);
            if (value != null) return value;
        }
        
        value = getBundleProperties().getString(name);
        if (value != null) return value;

        return value;
    }   

    
	/**
	 * load server.ini
	 */
	public static OAProperties getServerProperties() {
        if (propsServer == null) {
            propsServer = new OAProperties();
            String s = getRootDir() + "/server.ini";
        	s = OAString.convertFileName(s);
        	propsServer.setFileName(s);
        	propsServer.load();
            resetLocale();
        }
        return propsServer;
    }
	
	/**
     * load client.ini
	 */
	public static OAProperties getClientProperties() {
        if (propsClient == null) {
            propsClient = new OAProperties();
            String s = getRootDir() + "/client.ini";
        	s = OAString.convertFileName(s);
    		propsClient.setFileName(s);
    		propsClient.load();
            resetLocale();
        }
        return propsClient;
    }

	/**
	 * Load System.properties to be used case-insensitive
	 */
    public static OAProperties getSystemProperties() {
        if (propsSystem == null) {
            propsSystem = new OAProperties();
            Enumeration enumx = System.getProperties().keys(); 
            for ( ;enumx.hasMoreElements(); ) {
                String key = (String) enumx.nextElement();
                String value = System.getProperties().getProperty(key);
                if (value != null) {
                    propsSystem.put(key, value);
                }
            }
            resetLocale();
        }
        return propsSystem;
    }

    /**
     * runtime arguments.
     */
    public static OAProperties getRuntimeProperties() {
        if (propsRuntime == null) {
            propsRuntime = new OAProperties(); 
        }
        return propsRuntime;
    }
    // load args[] into properties
    public static void loadArguments(String[] args) {
        for (int i=0; args != null && i < args.length; i++) {
            String name = args[i];
            if (name.length() < 2) continue;
            if (name.charAt(0) == '-') {
                name = name.substring(1);
            }
            String value = null;
            int pos = name.indexOf('=');
            if (pos > 0) {
                value = name.substring(pos+1);
                name = name.substring(0, pos);
            }
            else {
                value = "true";
            }
            getRuntimeProperties().put(name, value);
        }
        resetLocale();
    }

    public static OAProperties getBundleProperties() {
        if (propsBundle == null) {
            propsBundle = new OAProperties();

            String fname = getResourceBundleFileName();

            Locale loc = locale;
            if (loc == null) loc = Locale.getDefault();
                
            resourceBundle = null;
            ResourceBundle rb = getResourceBundle();
            // use this for UTF-8 
            // resourceBundle = Utf8ResourceBundle.getBundle(fname, loc, Resource.class.getClassLoader());

            for (String key : rb.keySet()) {  // now try for case insensitive match
                String value = resourceBundle.getString(key);
                propsBundle.put(key, value);
            }
        }
        return propsBundle;
    }
    public static String getResourceBundleFileName() {
        String fname = Resource.class.getName();
        int x = fname.lastIndexOf('.');
        if (x >= 0) fname = fname.substring(0, x);
        fname = fname.replace('.', '/');
        fname += "/values.properties";
        return fname;
    }
    public static ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            String fname = getResourceBundleFileName();
            fname = fname.substring(0, fname.lastIndexOf('.'));
            fname = fname.replace('/', '.');
            Locale loc = locale;
            if (loc == null) loc = Locale.getDefault();
            try {
                resourceBundle = ResourceBundle.getBundle(fname, loc, Resource.class.getClassLoader());
            }
            catch (Throwable t) {
                loc = new Locale("en", "US");
                resourceBundle = ResourceBundle.getBundle(fname, loc, Resource.class.getClassLoader());
            }
        }
        return resourceBundle;
    }
    
    
	/**
	 * If the server or client *.ini file has been change, then it will be saved.
	 */
	public static void save() {
		if (bServerPropChanged) {
		    propsServer.save();
		}
		if (bClientPropChanged) {
		    propsClient.save();
		}
		bServerPropChanged = false;
		bClientPropChanged = false;
	}

	
	public static boolean isSuperAdmin() {
		return OAConv.toBoolean(getValue(INI_SuperAdmin)); 
	}
	
    /**
     * Get image from jar file.  Uses getDirectory(APP_JarImagePath)
     * @param name of image.  Use IMG_xxx names.
     * 
     * @return
     */
    public static ImageIcon getJarIcon(String imageName) {
        String fullName = getValue(APP_JarImageDir) + "/" + imageName;
        
        URL url = Resource.class.getResource(fullName);
        if (url == null) {
            fullName = getValue(APP_JarImageDir2) + "/" + imageName;
            url = Resource.class.getResource(fullName);
            if (url == null) {
                fullName = getValue(APP_JarImageDir) + "/" + getValue(imageName);
                url = Resource.class.getResource(fullName);
                if (url == null) {
                    fullName = getValue(APP_JarImageDir2) + "/" + getValue(imageName);
                    url = Resource.class.getResource(fullName);
                }
            }
        }
        
        if (url == null) {
            LOG.warning("Image not found, name="+imageName);
            imageName = getValue(IMG_ImageNotFound);
            fullName = getValue(APP_JarImageDir) + "/" + imageName;
            url = Resource.class.getResource(fullName);
        }
        if (url == null) return null;
        
        ImageIcon ii = new ImageIcon(url);
        return ii;
    }
	
	/**
	 * Update a property in an *.ini file.
	 * @param type either TYPE_Server, Client, Runtime
	 * @param name
	 * @param value if null then value will be removed.
	 * @see com.RemoteDelegate.delegate.ServerDelegate#setResourceValue(int, String, String) as a delegate that
	 * will call this method and also update other computers (clients/server).
	 */
	public static void setValue(int type, String name, String value) {
		if (name == null) return;
		if (type == TYPE_Server) {
			if (getServerProperties() != null) {
				if (value == null) {
				    if (getServerProperties().remove(name) != null) {
		                bServerPropChanged = true;
				    }
				}
				else {
                    Object objx = getServerProperties().getProperty(name);
                    if (objx == null || !objx.equals(value)) {
                        getServerProperties().setProperty(name, value);
                        bServerPropChanged = true;
                    }
				}
			}
		}
		else if (type == TYPE_Client) {
			if (getClientProperties() != null) {
				if (value == null) {
				    if (getClientProperties().remove(name) != null) {
				        bClientPropChanged = true;;
				    }
				}
				else {
				    Object objx = getClientProperties().getProperty(name);
				    if (objx == null || !objx.equals(value)) {
				        getClientProperties().setProperty(name, value);
                        bClientPropChanged = true;;
				    }
				}
			}
		}
        else {
            if (value == null) getRuntimeProperties().remove(name);
            else getRuntimeProperties().put(name, value);
        }
	}
    public static void setValue(int type, String name, int val) {
        setValue(type, name, ""+val);
    }

    /**
     * Note: this is set by StartupController
     */ 
    public static String getRootDir() {
        if (rootDir == null) {
            rootDir = ".";
            rootDir = getValue(APP_RootDir, ".");
        }
        return rootDir;
    }
    public static void setRootDir(String dir) {
        rootDir = dir;
        setValue(Resource.TYPE_Runtime, Resource.APP_RootDir, dir);         
        // need to have ini and resource files reloaded
        propsClient = null;
        propsServer = null;
        propsBundle = null;
        resourceBundle = null;
    }


    private static boolean bLocaleManuallySet;
    public static void setLocale(Locale loc) {
        if (loc == null) loc = Locale.getDefault();
        locale = loc;
        bLocaleManuallySet = true;
        resourceBundle = null;
        propsBundle = null;
        resetLocaleDefaults();
    }

    // called when any properties are loaded
    protected static void resetLocale() {
        if (!bLocaleManuallySet && resourceBundle != null) {
            locale = null;
            getLocale();
        }
    }
    
    public static Locale getLocale() {
        if (locale != null) return locale;
        
        locale = Locale.getDefault();
        resourceBundle = null;
        propsBundle = null;
        
        // Setup default Locale
        String strLang = _getValue(Resource.INI_LanguageCode, null, null);
        String strCountry = _getValue(Resource.INI_CountryCode, null, null);
        if (strLang != null || strCountry != null) {
            locale = new Locale(strLang, strCountry); // Ex: ("en","GB") or ("en", "US")
        }
        resetLocaleDefaults();
        return locale;
    }

    protected static void resetLocaleDefaults() {
        if (locale == null) return;
        String s = Resource.getValue(Resource.APP_DateTimeFormat);
        if (s == null || s.length() == 0) OADateTime.setLocale(locale);
        else OADateTime.setGlobalOutputFormat(s);

        s = Resource.getValue(Resource.APP_DateFormat);
        if (s == null || s.length() == 0) OADate.setLocale(locale);
        else OADate.setGlobalOutputFormat(s);
        
        s = Resource.getValue(Resource.APP_TimeFormat);
        if (s == null || s.length() == 0) OADate.setLocale(locale);
        else OATime.setGlobalOutputFormat(s);
        
        numberFormat = null;
        getNumberFormatter();  // initialize to fit Locale

        // OAConverter setup
        s = Resource.getValue(Resource.APP_IntegerFormat);
        if (s == null || s.length() == 0) s = ("#,###");
        OAConv.setIntegerFormat(s);

        s = Resource.getValue(Resource.APP_DecimalFormat);
        if (s == null || s.length() == 0) s = ("#,##0.00");
        OAConv.setDecimalFormat(s);
        OAConv.setBigDecimalFormat(s);

        // \u00A4 = $symbol, see javadoc DecimalFormat.  OAConverterNumber will allow for '$' and will substitute '\u00A4' when converting
        s = Resource.getValue(Resource.APP_MoneyFormat);
        if (s == null || s.length() == 0) s = ("\u00A4#,##0.00");
        OAConv.setMoneyFormat(s);
        
        s = Resource.getValue(Resource.APP_BooleanFormat);
        if (s == null || s.length() == 0) s = ("true;false;null");
        OAConv.setBooleanFormat(s);
    }
    
    
    public static NumberFormat getNumberFormatter() {
        if (numberFormat == null) {
            numberFormat = NumberFormat.getInstance(getLocale());
            numberFormat.setCurrency(Currency.getInstance(getLocale()));

            integerFormat = NumberFormat.getIntegerInstance(getLocale());
            // integerFormat.setMaximumFractionDigits(0);

            decimalFormat = NumberFormat.getCurrencyInstance(getLocale());
            
            currencyFormat = numberFormat.getCurrencyInstance(getLocale());
        }
        return numberFormat;
    }

    public static NumberFormat getIntegerFormatter() {
        if (integerFormat == null) getNumberFormatter();
        return integerFormat;
    }

    public static NumberFormat getDecimalFormatter() {
        if (decimalFormat == null) getNumberFormatter();
        return decimalFormat;
    }
    
    public static NumberFormat getCurrencyFormatter() {
        if (currencyFormat == null) getNumberFormatter();
        return currencyFormat;
    }
	
    public static void setSpellChecker(SpellChecker sc) {
        spellChecker = sc;
    }
    public static SpellChecker getSpellChecker() {
        if (spellCheckerInternal == null) {
            spellCheckerInternal = new SpellChecker() {
                @Override
                public boolean isWordFound(String word) {
                    if (spellChecker != null) return spellChecker.isWordFound(word);
                    return false;
                }
                @Override
                public String[] getSoundexMatches(String text) {
                    if (spellChecker != null) return spellChecker.getSoundexMatches(text);
                    return null;
                }
                @Override
                public String[] getMatches(String text) {
                    if (spellChecker != null) return spellChecker.getMatches(text);
                    return null;
                }
                @Override
                public void addNewWord(String word) {
                    if (spellChecker != null) spellChecker.addNewWord(word);
                }
            };
        }
        return spellCheckerInternal;
    }
}
