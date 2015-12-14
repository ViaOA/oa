package com.tmgsc.hifivetest;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import java.text.*;
import javax.swing.ImageIcon;
import com.viaoa.jfc.text.spellcheck.SpellChecker;
import com.viaoa.util.*;

/**
 * Used to setup Locale specific values from ResourceBundle.
 * Also see values.properties file for list of name/value pairs.
 * 
 * This uses the following:
 * 1: hashtable  (args from runtime, etc.)
 * 2: client.ini
 * 3: server.ini
 * 4: resource bundle
 *  Note:  directory paths should not end with a pathSeparator character. 
 */
public class Resource {
    private static Logger LOG = OALogger.getLogger(Resource.class);

    public static final int TYPE_Server  = 0;
    public static final int TYPE_Client  = 1;
    public static final int TYPE_Runtime = 2;

    // startup type
    public static final int RUNTYPE_Client = 0;
    public static final int RUNTYPE_JWSClient = 1;
    public static final int RUNTYPE_Server = 2;
    public static final int RUNTYPE_Service = 3;
    public static final String[] STARTUP_TYPES = new String[] { "CLIENT", "JWSCLIENT", "SERVER", "SERVICE" };
    private static int runType;
    
    private static OAProperties serverProperties;   
    private static OAProperties clientProperties; 
    
    private static boolean bServerPropChanged;
    private static boolean bClientPropChanged;

    protected static Locale locale;
    protected static NumberFormat numberFormat, currencyFormat, decimalFormat, integerFormat;
    
    private static String runTimeName;
    
    private static Hashtable<String,String> hash = new Hashtable<String,String>(83, .75f);

    protected static ResourceBundle resourceBundle;

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
    public static final String APP_HostName          = "HostName";
    public static final String APP_DataVersion       = "DataVersion";
    public static final String APP_Release           = "Release";
    public static final String APP_ReleaseDate       = "ReleaseDate";
    public static final String APP_ClassPath         = "ClassPath";
    public static final String APP_RootDir           = "RootDir";
    public static final String APP_JWSRootDir        = "JWSRootDir";
    public static final String APP_JarImageDir       = "JarImageDir";
    public static final String APP_JarImageDir2      = "JarImageDir2";
    public static final String APP_FileImageDir      = "FileImageDir";
    public static final String APP_ReportDir         = "ReportDir";
    public static final String APP_Copyright         = "Copyright";
    public static final String APP_RemotePort        = "RemotePort";
    public static final String APP_JarFileName       = "JarFileName";
    public static final String APP_Welcome           = "Welcome";
    public static final String APP_GoodBye           = "GoodBye";
    public static final String APP_JDKVersion        = "JDKVersion";
    public static final String APP_LogFileName       = "LogFileName";
    public static final String APP_LogLevel          = "LogLevel";
    public static final String APP_LogErrorDays      = "LogErrorDays";   // days to keep Error log files
    public static final String APP_LogRegularDays    = "LogRegularDays"; // days to keep regular log files
    public static final String APP_ServerIniFileName = "ServerIniFileName";
    public static final String APP_ClientIniFileName = "ClientIniFileName";
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
    public static final String APP_JettyHost         = "JettyHost";
    public static final String APP_JettyPort         = "JettyPort";
    public static final String APP_JettySSLPort      = "JettySSLPort";
    public static final String APP_JettyDirectory    = "JettyDirectory";
    public static final String APP_Passcode          = "Passcode";
    public static final String APP_UsesSSLCertificate = "UsesSSLCertificate";
    
    // ini file properties
    public static final String INI_AutoLogin         = "AutoLogin"; 
    public static final String INI_AutoLogout        = "AutoLogout"; 
    public static final String INI_StoreLogin        = "StoreLogin";    
    public static final String INI_LanguageCode      = "LanguageCode";  
    public static final String INI_CountryCode       = "CountryCode";   
    public static final String INI_LogLevel          = "LogLevel";  
    public static final String INI_Location          = "Location";  
    public static final String INI_ServerName        = "ServerName";    
    public static final String INI_User              = "User";  
    public static final String INI_Password          = "Password";  
    public static final String INI_StorePassword     = "StorePassword"; 
    public static final String INI_License           = "License";
    public static final String INI_Debug             = "Debug";
    public static final String INI_DBDebug           = "DBDebug";
    public static final String INI_SuperAdmin        = "SuperAdmin";
    public static final String INI_SaveDataToFile    = "SaveDataToFile";
    public static final String INI_SaveDataToDatabase = "SaveDataToDatabase";
    public static final String INI_BackdoorPassword  = "backdoorPassword";
    
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
    
    // Report
    public static final String RPT_PaperWidth   = "report.PaperWidth";  // => "estimate.report.PaperWidth"
    public static final String RPT_PaperHeight  = "report.PaperHeight";
    public static final String RPT_X            = "report.x";
    public static final String RPT_Y            = "report.y";
    public static final String RPT_Width        = "report.width";
    public static final String RPT_Height       = "report.height";
    public static final String RPT_Orientation  = "report.orientation";
    
    
    public static final String MSG_InvalidJVM     = "msg.InvalidJVM";
    public static final String MSG_StartupError   = "msg.StartupError";

    // Email
    public static final String EMAIL_SMTP_Host  = "email.smtp.host";
    public static final String EMAIL_SMTP_Port  = "email.smtp.port";
    public static final String EMAIL_UserId     = "email.userId";
    public static final String EMAIL_Password   = "email.password";
    public static final String EMAIL_ToAddress  = "email.toAddress";
    
    
    // Hinda
    public static final String HINDA_UserId   = "hinda.userId";
    public static final String HINDA_Password = "hinda.password";
    public static final String HINDA_ImageUrl = "hinda.imageUrl";
    
    
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
    
    public static void setRunType(int x) {
        runType = x;
    }
    public static int getRunType() {
        return runType;
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
    

    public static String getResourceBundleFileName() {
        String cn = Resource.class.getName();
        int x = cn.lastIndexOf('.');
        if (x >= 0) cn = cn.substring(0, x);
        cn = cn.replace('.', '/');
        return cn+"/values.properties";
    }
    
    /**
     * This will return a directory path that is correct for OS and ends in a directory seperator.
     * @param name
     * @return
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
        String s = getValue(name);
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
    
    public static String getValue(String name) {
        return getValue(name, (String) null);
    }

    public static String getValue(String name, Object[] args) {
        return getValue(name, args, null);
    }

    /**
     * Find value, by searching for name in this order: server.ini, client.ini, resourceBundle
     * @param name
     * @return
     */
    public static String getValue(String name, String defaultValue) {
        return getValue(name, null, defaultValue);
    }

    /**
     * Find value, by searching for name in this order: server.ini, client.ini, resourceBundle
     * @param name
     * @return
     */
    public static String getValue(String name, String defaultValue, boolean bIncludeResourceBundle) {
        return getValue(name, null, defaultValue, bIncludeResourceBundle);
    }

    /**
     * Find value, by searching for name in this order: server.ini, client.ini, resourceBundle
     * @param name
     * @return
     */
    public static String getValue(String name, boolean bIncludeResourceBundle) {
        return getValue(name, null, null, bIncludeResourceBundle);
    }
    
    
    /**
     * Case-insensitive way to get name/value.  Checks first runtime args, client.ini, server.ini, values.properties
     */
    protected static String getValue(String name, Object[] args, String defaultValue) {
        return getValue(name, args, defaultValue, true);
    }
    protected static String getValue(String name, Object[] args, String defaultValue, boolean bIncludeResourceBundle) {
        if (name == null) return null;
        String nameU = name.toUpperCase();
        String s = (String) hash.get(nameU);  // runtime
        if (s != null) {
            if (args != null && args.length > 0) {
                s = MessageFormat.format(s, args);
            }
            return s; 
        }

        OAProperties props = getClientProperties();
        if (props != null) s = props.getString(name);
        if (s == null) {
            props = getServerProperties();
            if (props != null) s = props.getString(name);
        }
        
        try {
            if (s == null && bIncludeResourceBundle) {
                s = getResourceBundle().getString(name);
                if (s == null) {
                    for (String sx : resourceBundle.keySet()) {
                        if (name.equalsIgnoreCase(s)) {
                            s = getResourceBundle().getString(sx);
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            s = null;
        }

        if (s == null) {
            s = defaultValue;
            if (bIncludeResourceBundle) LOG.fine("Resource.getValue(\""+name+"\" not in resource bundle, will use: "+s);
        }
        else {
            s = s.trim();  // jdk bug?  resourceBundle.getString(x) could append space to end
        }
        
        if (s == null) return null;
        hash.put(nameU, s);
        return getValue(name, args, defaultValue);
    }   


    public static ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            String fname = getResourceBundleFileName();
            fname = fname.substring(0, fname.lastIndexOf('.'));
            fname = fname.replace('/', '.');
            Locale loc;
            if (locale == null && serverProperties == null && clientProperties == null) {
                loc = Locale.getDefault();
            }
            else  {
                loc = Resource.getLocale();
            }
            
            resourceBundle = ResourceBundle.getBundle(fname, loc, Resource.class.getClassLoader());
            
            // resourceBundle = Utf8ResourceBundle.getBundle(fname, loc, Resource.class.getClassLoader());
        }
        return resourceBundle;
    }
    
    
    /**
     * Uses APP_ServerIniFileName to get name of file to use as server.ini
     */
    public static OAProperties getServerProperties() {
        if (serverProperties == null) {
            serverProperties = new OAProperties();  // this needs to be before the next line: getValue()
            String s = getRootDir() + "/" + getValue(APP_ServerIniFileName, "server.ini");
            s = OAString.convertFileName(s);
            serverProperties.setFileName(s);
            serverProperties.load();
            resourceBundle = null;
        }
        return serverProperties;
    }
    
    /**
     * Uses APP_ClientIniFileName to get name of file to use as server.ini
     */
    public static OAProperties getClientProperties() {
        if (clientProperties == null) {
            clientProperties = new OAProperties(); // this needs to be before the next line: getValue()
            String s = getRootDir() + "/" + getValue(APP_ClientIniFileName, "client.ini");
            s = OAString.convertFileName(s);
            clientProperties.setFileName(s);
            clientProperties.load();
            resourceBundle = null;
        }
        return clientProperties;
    }

    /**
     * If the server or client *.ini file has been change, then it will be saved.
     */
    public static void save() {
        if (bServerPropChanged) {
            serverProperties.save();
        }
        if (bClientPropChanged) {
            clientProperties.save();
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
        String nameU = name.toUpperCase();
        if (type == TYPE_Server) {
            if (getServerProperties() != null) {
                if (value == null) getServerProperties().remove(name);
                else getServerProperties().setProperty(name, value);
                bServerPropChanged = true;
            }
        }
        else if (type == TYPE_Client) {
            if (getClientProperties() != null) {
                if (value == null) getClientProperties().remove(name);
                else getClientProperties().setProperty(name, value);
                bClientPropChanged = true;
            }
        }
        if (value == null) hash.remove(nameU);
        else hash.put(nameU, value);
    }

    /**
     * Note: this is set by StartupController
     */ 
    public static String getRootDir() {
        return getValue(APP_RootDir, (String) null);
    }
    public static void setRootDir(String dir) {
        setValue(Resource.TYPE_Runtime, Resource.APP_RootDir, dir);         
        // need to have ini and resource files reloaded
        clientProperties = null;
        serverProperties = null;
        resourceBundle = null;
    }
    
    public static Locale getLocale() {
        if (locale != null) return locale;
        
        // Setup default Locale and Formatting
        String strLang = getValue(Resource.INI_LanguageCode, null, null, false);    // dont check resource bundle
        String strCountry = getValue(Resource.INI_CountryCode, null, null, false);  // dont check resource bundle

        Locale loc = null;
        if (strLang != null || strCountry != null) {
            loc = new Locale(strLang, strCountry); // Ex: ("en","GB") or ("en", "US")
        }
        if (loc == null) loc = Locale.getDefault();
        setLocale(loc);
        return locale;
    }

    public static void setLocale(Locale loc) {
        if (loc == null) loc = Locale.getDefault();

        locale = loc;
        // need to resource file reloaded
        resourceBundle = null; 
        
        String s = Resource.getValue(Resource.APP_DateTimeFormat, "");
        if (s == null || s.length() == 0) OADateTime.setLocale(locale);
        else OADateTime.setGlobalOutputFormat(s);

        s = Resource.getValue(Resource.APP_DateFormat, "");
        if (s == null || s.length() == 0) OADate.setLocale(locale);
        else OADate.setGlobalOutputFormat(s);
        
        s = Resource.getValue(Resource.APP_TimeFormat, "");
        if (s == null || s.length() == 0) OADate.setLocale(locale);
        else OATime.setGlobalOutputFormat(s);
        
        numberFormat = null;
        getNumberFormatter();  // initialize to fit Locale

        
        // OAConverter setup
        s = Resource.getValue(Resource.APP_IntegerFormat, "");
        if (s == null || s.length() == 0) s = ("#,###");
        OAConv.setIntegerFormat(s);

        s = Resource.getValue(Resource.APP_DecimalFormat, "");
        if (s == null || s.length() == 0) s = ("#,##0.00");
        OAConv.setDecimalFormat(s);
        OAConv.setBigDecimalFormat(s);

        // ¤ = $symbol, see javadoc DecimalFormat.  OAConverterNumber will allow for '$' and will substitute '¤' when converting
        s = Resource.getValue(Resource.APP_MoneyFormat, "");
        if (s == null || s.length() == 0) s = ("¤#,##0.00");
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

    public static void loadSystemProperties() {
        Enumeration enumx = System.getProperties().keys(); 
        for ( ;enumx.hasMoreElements(); ) {
            String key = (String) enumx.nextElement();
            String value = System.getProperties().getProperty(key);
            if (value != null) {
                Resource.setValue(Resource.TYPE_Runtime, key, value);
            }
        }
    }
    
    public static void loadArgumentsAsProperties(String[] args) {
        // load args[] into properties
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
            Resource.setValue(Resource.TYPE_Runtime, name, value);
        }
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

