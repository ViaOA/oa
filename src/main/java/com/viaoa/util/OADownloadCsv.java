package com.viaoa.util;

import java.util.ArrayList;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;

/**
 * Create csv data from a Hub and properies.
 * 
 * todo:  allow for Many, and create multiple lines per hub object.
 * 
 * @author vvia
 */
public abstract class OADownloadCsv<F extends OAObject> {
    protected Hub<F> hub;
    private ArrayList<MyProperty> alProperty = new ArrayList<>();
    
    public OADownloadCsv(Hub<F> hub) {
        this.hub = hub;
    }

    protected static class MyProperty {
        String title;
        String propPath;
        OAPropertyPath pp;
    }

    public void addProperty(String title, String propPath) {
        MyProperty mp = new MyProperty();
        mp.title = title;
        mp.propPath = propPath;
        mp.pp = new OAPropertyPath(hub.getObjectClass(), propPath);
        
        mp.pp.getLinkInfos();
        alProperty.add(mp);
    }
    
    public void download() {
        writeHeading();
        for (F obj : hub) {
            writeData(obj);
        }
    }
    
    protected void writeHeading() {
        String txt = "";
        for (MyProperty mp : alProperty) {
            txt = OAString.csv(txt, mp.title);
        }
        onWriteLine(txt);
    }
    
    protected void writeData(F obj) {
        String txt = "";
        for (MyProperty mp : alProperty) {
            Object val = mp.pp.getValue(obj);
            txt = OAString.csv(txt, val);
        }
        onWriteLine(txt);
    }
    
    protected abstract void onWriteLine(String txt);
    
}
