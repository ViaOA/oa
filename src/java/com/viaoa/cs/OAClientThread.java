package com.viaoa.cs;

import com.viaoa.object.OAThreadLocalDelegate;

/**
 * Threads used to process messages from server. The thread knows when the another thread can
 * be started, while the current thread continues.
 * @author vvia
 *
 */
public class OAClientThread extends Thread {
	String location;
	long timeCreated;
	int status, holdStatus;
	long time;
	private OAClientMessageHandler clientMessageHandler;

	long statusTime;
	OAObjectMessage msg;
	int processCount;
	int serverOnlyCnt;
	boolean bWaitingOnLock; // using OAThreadLocal
	
	String[] statuses = new String[] {"Unknown","New","Done","Wait","GettingMessage","Processing","Finishing","FinishingAsServer"}; 
	public static final int STATUS_Unknown = 0;
	public static final int STATUS_New = 1;
	public static final int STATUS_Done = 2;
	public static final int STATUS_Wait = 3;
	public static final int STATUS_GettingMessage = 4;
	public static final int STATUS_Processing = 5;  // Note: if a threads status > Processing, then startNextThread() will work.
	public static final int STATUS_Finishing = 6;  
	public static final int STATUS_FinishingAsServer = 7;  

	public OAClientThread(Runnable run, String name) {
        super(run, name);
		this.timeCreated = System.currentTimeMillis();
    }
	
	public void update(String loc, int status) {
		this.location = loc;
		this.status = status;
		this.statusTime = System.currentTimeMillis();
		OAThreadLocalDelegate.setStatus(loc);
	}

	// NOTE: anything above/equal processing
	public boolean isMessageProcessed() {
		return status >= STATUS_Processing;
	}

	public boolean isWaitingOnLock() {
	    return bWaitingOnLock;
	}
	public void setWaitingOnLock(boolean b) {
	    this.bWaitingOnLock = false;
	}
	
	/*
	private boolean bErrorFlag;
	protected String getInfo() {
		String result = null;
		if (status == STATUS_Processing || status == STATUS_Finishing || status == STATUS_FinishingAsServer) {
			long t = (System.currentTimeMillis() - processStartTime);
			if (t > 2000) {
				if (t > 10000 && !bErrorFlag) {
					bErrorFlag = true;
					System.out.println("OAClientThread "+getName()+" has been processing over 10 seconds");
					this.dumpStack();
				}
				result = getName()+" has been processing " + t +"ms";
			}
			else bErrorFlag = false;
		}
		return result;
	}	
	*/
	
	protected String getInfo() {
		String s = getName(); 
		s += " loc:"+location;
		s += " processed:"+processCount;
		s += " status:";
		s += statuses[status];
		
		long now = System.currentTimeMillis();
		s += " ms="+(now - statusTime);
		s += " msg=" + msg;
		s += " ThreadState="+this.getState();		
		return s;
	}
}


