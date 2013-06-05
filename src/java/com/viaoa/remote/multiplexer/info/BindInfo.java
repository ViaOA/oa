package com.viaoa.remote.multiplexer.info;

import java.lang.annotation.Annotation;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;
import com.viaoa.remote.multiplexer.annotation.OARemoteMethod;
import com.viaoa.remote.multiplexer.annotation.OARemoteParameter;

/**
 * Internal information about a remote Object. 
 * One side (Client/Server) will have the real object, and the other side will have a proxy.
 * Any reference that is passed between the C/S will use the name, and then replaced with 
 * the real/proxy instance by the receiving side. 
 * @author vvia
 */
public class BindInfo {
    private static Logger LOG = Logger.getLogger(BindInfo.class.getName());

    // internal name of object, that is past instead of the real object
    public short id;
    public String name;
    public WeakReference weakRef;
    public Class interfaceClass; // used to create the proxy
    
    private HashMap<String, MethodInfo> hmNameToMethod;
    private HashMap<Method, MethodInfo> hmMethod;

    public void setObject(Object obj, ReferenceQueue referenceQueue) {
        if (referenceQueue == null) weakRef = new WeakReference<Object>(obj);
        else weakRef = new WeakReference<Object>(obj, referenceQueue);
    }
    public Object getObject() {
        if (weakRef != null) return weakRef.get();
        return null;
    }
    
    public MethodInfo getMethodInfo(String methodNameSig) {
        if (hmNameToMethod == null) loadMethodInfo();
        MethodInfo mi = hmNameToMethod.get(methodNameSig);
        return mi;
    }
    public MethodInfo getMethodInfo(Method method) {
        if (hmMethod == null) loadMethodInfo();
        MethodInfo mi = hmMethod.get(method);
        return mi;
    }
    
    /**
     * used to initialize the information about the methods for the bind class.
     */
    public synchronized void loadMethodInfo() {
        if (interfaceClass == null) return;
        hmNameToMethod = new HashMap<String, MethodInfo>(23, .75f);
        hmMethod = new HashMap<Method, MethodInfo>();

        /*
        RemoteInterface remoteInterface = (RemoteInterface) interfaceClass.getAnnotation(RemoteInterface.class);
        if (remoteInterface != null) {
        }
        */
        
        Method[] methods = interfaceClass.getMethods();
        for (Method method : methods) {
            int sig = 0;  // create a dummy signature, to recognize method overloading
            Class[] cs = method.getParameterTypes();
            for (int j=0; cs != null && j < cs.length; j++) {
                sig *= 10;
                sig += (cs[j].getName().hashCode() % 500);
            }
            MethodInfo mi = new MethodInfo();
            mi.method = method;
            mi.methodNameSignature = method.getName() + sig; 
            
            boolean bRemote = false;
            Class c = method.getReturnType();
            if (c != null && !c.isPrimitive()) {
                OARemoteInterface rc = (OARemoteInterface) c.getAnnotation(OARemoteInterface.class);
                bRemote = (rc != null);
            }
            if (bRemote) {
                mi.remoteReturn = c;
                if (!c.isInterface()) {
                    Class[] csx = c.getInterfaces();
                    Class cx;
                    if (csx != null && csx.length > 0) cx = csx[0];
                    else cx = c;
                    String s = "bindName="+name+", method="+method;
                    s += ", will use interface="+cx;
                    LOG.warning("return value must be a Java Interface, since returnValueIsRemote() is true, "+s);
                    mi.remoteReturn = cx;
                }
            }

            OARemoteMethod remoteMethod = method.getAnnotation(OARemoteMethod.class);
            if (remoteMethod != null) {
                if (remoteMethod.compressedReturnValue()) {
                    if (mi.remoteReturn == null) {
                        mi.compressedReturn = true;
                    }
                }
                mi.noReturnValue = remoteMethod.noReturnValue();
            }

            // check to see if any of the params are remote
            cs = method.getParameterTypes();
            Annotation[][] anns = method.getParameterAnnotations(); 
            
            int x = cs == null ? 0 : cs.length;

            for (int i=0; i<x; i++) {
                boolean bCompressed = false;
                
                OARemoteInterface rc = (OARemoteInterface) cs[i].getAnnotation(OARemoteInterface.class);
                bRemote = (rc != null) && !cs[i].isPrimitive();

                if (anns[i] != null && anns[i].length > i) {
                    OARemoteParameter rp = (OARemoteParameter)(anns[i][0]);
                    if (rp != null) {
                        bCompressed = rp.compressed();
                    }
                }
                if (bCompressed) {
                    if (mi.compressedParams == null) {
                        mi.compressedParams = new boolean[cs.length];
                    }
                    mi.compressedParams[i] = true;
                }
                
                if (bRemote) {
                    if (mi.remoteParams == null) {
                        mi.remoteParams = new Class[cs.length];
                    }
                    c = cs[i];
                    mi.remoteParams[i] = c; 
                    if (!c.isInterface()) {
                        Class[] csx = c.getInterfaces();
                        Class cx;
                        if (csx != null && csx.length > 0) cx = csx[0];
                        else cx = c;
                        String s = "bindName="+name+", method="+method;
                        s += ", param#" + i;
                        s += ", will use interface="+cx;
                        // callback must be defined as an Interface
                        LOG.warning("method "+method.getName()+" has a param annotated as remote, that is invalid - the param must be an Interface, "+s);
                        mi.remoteParams[i] = c; 
                    }
                }
            }
            hmMethod.put(method, mi);
            hmNameToMethod.put(mi.methodNameSignature, mi);
        }
    }
}


