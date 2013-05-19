package com.viaoa.object;

import java.util.Hashtable;
import java.util.logging.*;

import sun.util.LocaleServiceProviderPool.LocalizedObjectGetter;

import com.viaoa.hub.Hub;
import com.viaoa.util.*;

public class OAObjectLogDelegate {
    private static Logger LOG = Logger.getLogger(OAObjectLogDelegate.class.getName());
    private static OAXMLWriter writerXml;

    // methods to handle writing save/delete to log file.
    public static void createXMLLogFile(String fname) {
        if (writerXml != null) {
            writerXml.close();
            writerXml = null;
        }
        if (fname != null) {
            fname = OAString.convertFileName(fname);
            writerXml = new OAXMLWriter(fname) {
                public int writeProperty(Object obj, String propertyName, Object value) {
                    if (obj instanceof OALogRecord) return OAXMLWriter.WRITE_YES;
                    
                    if (value instanceof OAObject) return OAXMLWriter.WRITE_KEYONLY;
                    if (!(value instanceof Hub)) return OAXMLWriter.WRITE_YES;
                    
                    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj.getClass());
                    OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, propertyName);
                    if (li != null && li.getType() == OALinkInfo.MANY) {
                        li = OAObjectInfoDelegate.getLinkInfo(oi, propertyName);
                        li = OAObjectInfoDelegate.getReverseLinkInfo(li);
                        if (li != null && li.getType() == OALinkInfo.MANY) {
                            // M2M dont write any new object, since it does not exist when this file is restored.
                            //        the restore will update/complete the M2M link tables when the other object
                            //        has it's M2M updated/loaded.
                            return OAXMLWriter.WRITE_NONEW_KEYONLY;
                        }
                    }
                    return OAXMLWriter.WRITE_NO;
                }           
            };
        }
    }
    public static void closeXMLLogFile() {
        createXMLLogFile(null);
    }
    
    /**
     * creates a XML log file for all save/deletes done on OAObjects.
     * @param bSave if true then save, else delete
     */
    protected static void logToXmlFile(OAObject oaObj, boolean bSave) {
        if (writerXml == null) return;
        OALogRecord rec = new OALogRecord();
        rec.setObject(oaObj);
        rec.setCommand(bSave ? OALogRecord.COMMAND_SAVE : OALogRecord.COMMAND_DELETE);
        synchronized (writerXml) {
            writerXml.write(rec);
            writerXml.flush();
        }
    }
    
    public static void restoreXMLLogFile(String fname) throws Exception {
        if(fname == null) return;
        fname = OAString.convertFileName(fname);
        OAXMLReader reader = new OAXMLReader(fname) {
            public void endObject(OAObject obj, boolean bHasParent) {
                if (!(obj instanceof OALogRecord)) return;
                OALogRecord lr = (OALogRecord) obj;
                if (lr.getCommand().equals(OALogRecord.COMMAND_SAVE)) {
                    lr.getObject().save(OAObject.CASCADE_NONE);
                }
                else lr.getObject().delete();
            }
        };
        try {
            // OAObjectFlagDelegate.setThreadIgnoreEvents(true);
            reader.parse();
        }
        finally {
            // OAObjectFlagDelegate.setThreadIgnoreEvents(false);
        }
    }
    
}
