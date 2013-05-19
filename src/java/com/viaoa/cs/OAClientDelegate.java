package com.viaoa.cs;

import java.io.*;
import java.util.logging.Logger;

import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.util.*;


/**
 * Methods for OAClient.
 * @author vvia
 */
public class OAClientDelegate {

	private static Logger LOG = Logger.getLogger(OAClientDelegate.class.getName());
	
    /**
     * Used to update the currentThread, if it is a OAClientThread.
     * @param location
     * @param status 
     * @see OAClientThread
     */
    public static void updateThread(String location, int status) {
    	Thread t = Thread.currentThread();
    	if (t instanceof OAClientThread) {
    		OAClientThread ct = (OAClientThread) t;
    		ct.location = location;
    		ct.status = status;
    		ct.time = System.currentTimeMillis();
    	}
//    	LOG.finest("thread="+t+" location="+location+" status="+status+" time="+time);
    }

    
    public static boolean processIfNotClientThread() {
        Thread t = Thread.currentThread();
        return (!(t instanceof OAClientThread));
        
    }

    public static boolean serverSideCode() {
        return processIfServer();
    }    
    /** 
     * Used to create a "block" of code that should only run on server.
     * This is usually used after a property change is sent, and the
     * rest of the code should only be ran on the server.
     * If the current thread is an OAClientThread, then it will send messages to other clients.
     */
    public static boolean processIfServer() {
    	if (!OAClient.isServer()) return false;

        Thread t = Thread.currentThread();
    	if (t instanceof OAClientThread) {
    		OAClientThread ct = (OAClientThread) t;
    		if (ct.status != OAClientThread.STATUS_FinishingAsServer) {
    			ct.status = OAClientThread.STATUS_FinishingAsServer;
    			ct.time = System.currentTimeMillis();
    		}
    	}
    	return true;
    }

    /**
     * Used to create a "block" of code that should only run on server.
     * This is usually used after a property change is sent, and the
     * rest of the code should only be ran on the server.
     * If the current thread is an OAClientThread, then it will send messages to other clients.
     * @see #endServerOnly()
     */
    public static boolean beginServerOnly() {
    	return setProcessIfServer(true);
    }
    /**
     * Used to create a "block" of code that should only run on server.
     * This is usually used after a property change is sent, and the
     * rest of the code should only be ran on the server.
     * @see #beginServerOnly()
     */
    public static void endServerOnly() {
    	setProcessIfServer(false);
    }
    
    /**
     * This is used so that code will only be ran on the server.
     * If the current thread is an OAClientThread, then it will send messages to other clients.
     * @return true if this is the server, false if this is not a server and the code should not run.
     */
    protected static boolean setProcessIfServer(boolean b) {
    	if (!OAClient.isServer()) return false;

        Thread t = Thread.currentThread();
    	if (t instanceof OAClientThread) {
    		OAClientThread ct = (OAClientThread) t;
			if (b) {
			    if (ct.serverOnlyCnt == 0) ct.holdStatus = ct.status;
				ct.serverOnlyCnt++;
				if (ct.serverOnlyCnt > 7) {
					LOG.warning("ct.serverOnlyCnt="+ct.serverOnlyCnt);
				}
	            if (ct.status != OAClientThread.STATUS_FinishingAsServer) {
	                ct.status = OAClientThread.STATUS_FinishingAsServer;
	                ct.time = System.currentTimeMillis();
	            }
			}
			else {
				ct.serverOnlyCnt--;
				if (ct.serverOnlyCnt == 0) ct.status = ct.holdStatus;
				else if (ct.serverOnlyCnt < 0) {
					LOG.warning("ct.serverOnlyCnt="+ct.serverOnlyCnt);
				}
			}
    	}
    	return true;
    }

    
    public static boolean shouldSendMessage() {
    	if (OAClient.oaClient == null) return false;
    	if (OAThreadLocalDelegate.isSuppressCSMessages()) return false;
    	Thread t = Thread.currentThread();
    	if (!(t instanceof OAClientThread)) return true;
		OAClientThread ct = (OAClientThread) t;

		if (ct.serverOnlyCnt > 0) return true;
		if (ct.status == OAClientThread.STATUS_FinishingAsServer) return true;
		if (ct.status == OAClientThread.STATUS_Finishing) return false;
		if (ct.status != OAClientThread.STATUS_Processing) {
			LOG.log(OALogger.BUG, "OAClientThread status != Processing ***************");
		}
    	return false;
    }
    
    public static void messageProcessed(OAObjectMessage msg) {
    	if (OAClient.oaClient == null) return;

        OAClient.oaClient.clientMessageHandler.messageCompleted(msg);
        /* 20130315 was:
    	if (msg != null) {
			OAClient.oaClient.clientMessageHandler.messageCompleted(msg);
    	}
        */
    	Thread t = Thread.currentThread();
    	if ((t instanceof OAClientThread)) {
        	if (OAThreadLocalDelegate.isSuppressCSMessages()) return;
			OAClientThread ct = (OAClientThread) t;
			if (ct.status != OAClientThread.STATUS_Processing) {
				return;
			}
			ct.status = OAClientThread.STATUS_Finishing;
			ct.time = System.currentTimeMillis();
    	}
    }

    
	public static int getMessageSize(OAObjectMessage msg) {
	    ByteArrayOutputStream bos = null;
	    ObjectOutputStream os = null;
	    try {
	        bos = new ByteArrayOutputStream();
	        os = new ObjectOutputStream(bos);
	        os.writeObject(msg);
	        return bos.size();
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	    finally {
	        try {
	            if (os != null) os.close();
	            if (bos != null) bos.close();
	        }
	        catch(Exception e) {}
	    }
	    return -1;
	}

	// dummy outputstream to count how many bytes are written.
	static class NullOutputStream extends OutputStream {
	    int total;
	    @Override
	    public void write(int b) throws IOException {
	        total++;
	    }
	}
	
    public static int getObjectSize(Object msg) {
        NullOutputStream nos = new NullOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(nos);
            os.writeObject(msg);
            return nos.total;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (os != null) os.close();
            }
            catch(Exception e) {}
        }
        return -1;
    }
	
	
	public static byte[] getObjectAsBytes(Object msg, int estimate) {
	    ByteArrayOutputStream bos = null;
	    ObjectOutputStream os = null;
	    try {
	        bos = new ByteArrayOutputStream(estimate);
	        os = new ObjectOutputStream(bos);
	        os.writeObject(msg);
	        return bos.toByteArray();
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	    finally {
	        try {
	            if (os != null) os.close();
	            if (bos != null) bos.close();
	        }
	        catch(Exception e) {}
	    }
	    return null;
	}

	
    /**
	    Used internally to display an OAObjectMessage.
	*/
	public static String getDisplayMessage(String title, OAObjectMessage msg) {
        String ss = "";
        if (msg.masterClass != null) ss += " mc:" + msg.masterClass.getName();
        if (msg.masterObjectKey != null) ss += " mk:" + msg.masterObjectKey;
        if (msg.objectClass != null) ss += " oc:" + msg.objectClass.getName();
        if (msg.objectKey != null) ss += " ok:" + msg.objectKey;
        if (msg.property != null) ss += " p:" + msg.property;
        if (msg.newValue != null) ss += " nv:"+msg.newValue;
        if (msg.pos > 0) ss += " pos:"+msg.pos;
        ss = title+" message "+OAObjectMessage.msgTypes[msg.type]+ss;
        return ss;
	}
	
	
    /**
	   Display an OAObjectMessage.
	*/
	public static void displayMessage(String type, OAObjectMessage msg) {
	    if (type == null) return;
	    System.out.println("displayMessage ......");
	    ByteArrayOutputStream bos = null;
	    ObjectOutputStream os = null;
	    try {
	        bos = new ByteArrayOutputStream();
	        os = new ObjectOutputStream(bos);
	        os.writeObject(msg);
	
	        String ss = "";
	        if (msg.masterClass != null) ss += " mc:" + msg.masterClass.getName();
	        if (msg.masterObjectKey != null) ss += " mk:" + msg.masterObjectKey;
	        if (msg.objectClass != null) ss += " oc:" + msg.objectClass.getName();
	        if (msg.objectKey != null) ss += " ok:" + msg.objectKey;
	        if (msg.property != null) ss += " p:" + msg.property;
	        if (msg.newValue != null) ss += " nv:"+msg.newValue;
	        if (msg.pos > 0) ss += " pos:"+msg.pos;
	        System.out.println("OAClient "+type+" message "+OAObjectMessage.msgTypes[msg.type]+ss+"  "+bos.size());
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	    finally {
	        try {
	            if (os != null) os.close();
	            if (bos != null) bos.close();
	        }
	        catch(Exception e) {}
	    }
	}
	
	public static boolean isLocal(OAObjectServer os) {
	    OAClient c = OAClient.getClient();
	    return (c != null && c.oaObjectServer == os);
	}
	
	
    public static String asString() {
    	String s = "qqqqqq";
/*qqqqqqqqqq
        long t = System.currentTimeMillis();
		String s = (objectMessageLoadPos-objectMessagePos) + " messages in queue, ";

		if (lastNextMessageTime == 0) t = 0;
		else t -= lastNextMessageTime;

		s += t + " ms since last server.getNext() ";

		s += alThread.size()+" total threads, ";
		s += iProcessingCount + " threads processing, ";
		s += threadWait + " threads waiting, ";
		s += "inWaitQueue="+bInWaitQueue;

		
		for (int ix=0 ; ix<alThread.size(); ix++) {
			OAClientThread tx = (OAClientThread) alThread.get(ix);
			s += ("\n"+ix+") " + tx.getName() + "  "+ tx.location);
		}
*/
		return s;
    }
	
	
}

/*        

Thread t = new Thread(new Runnable() {
	@Override
	public void run() {
		try {
			Thread.sleep(10000);
			displayStats();
		}
		catch (Exception e) {
		}
	}
}, "OAClientMessageHandler.displayStats");
t.start();








int xx = OAClientDelegate.getMessageSize(msg);
if (xx > 500) {
	//System.out.println(xx+" bytes ***** OUT ****** MSG SIZE *************** msg:"+msg);//qqqqqqqqqqqqqqq
	LOG.finer("message size="+xx+"  msg="+msg);
	
	xx = OAClientDelegate.getObjectSize(msg.newValue);
	if (xx > 50) System.out.println(xx+" bytes newValue="+msg.newValue); 
	xx = OAClientDelegate.getObjectSize(msg.objectClass);
	if (xx > 50) System.out.println(xx+" bytes objectClass="+msg.objectClass); 
	xx = OAClientDelegate.getObjectSize(msg.objectKey);
	if (xx > 50) System.out.println(xx+" bytes objectKey="+msg.objectKey);
}
*/ 
