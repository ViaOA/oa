package com.viaoa.cs;

import java.util.ArrayList;
import java.util.logging.*;

import com.viaoa.object.OAThreadLocalDelegate;


/**
 * Processes messages from OAServer.
 * @author vvia
 */
public abstract class OAClientMessageHandler implements Runnable {

	private static Logger LOG = Logger.getLogger(OAClientMessageHandler.class.getName());

	private OAClientMessageReader msgReader;
	private volatile boolean bStop;
    private Object THREADLOCK = new Object();
    private ArrayList alThread = new ArrayList(23);  // pool of threads used to process messages
	private volatile int waitCnt;  // only changed inside of synchronized THREADLOCK
	private int osId;
    private ArrayList<OAObjectMessage> alSend = new ArrayList(37);
    private OAObjectServerInterface oaObjectServer;
    private volatile OAClientThread currentThread;
	private int cntThread;
	private long timeLastGet;

	private int msgReceivedCntLastUpdate;
	private int msgReceivedCnt;
	private int msgSentCnt;
	private long msgSentMs;
    private Thread.UncaughtExceptionHandler exceptionHandler;
    
	public OAClientMessageHandler(int osId, OAClientMessageReader msgReader, OAObjectServerInterface objectServer) {
		LOG.config("Starting, osId="+osId);
		this.osId = osId;
		this.msgReader = msgReader;
		this.oaObjectServer = objectServer;
	}
	
	public void start() {
		LOG.config("starting");
		bStop = false;
		if (alThread.size() == 0) startNextThread(); 		
	}
	public void stop() {
		stop(true);
	}	
	protected void stop(boolean bLog) {
		if (bStop) return;
		if (bLog) LOG.config("stopping");
		bStop = true;
		synchronized (THREADLOCK) {
			THREADLOCK.notifyAll();
		}
	}
	
	public boolean isStopped() {
		return bStop;
	}
	
	
	private int iWillNotifyWhenProcessed;  // counter to track msg that will notify when processing can continue
	
	public void messageCompleted(OAObjectMessage msg) {
		// LOG.finer("msg="+msg);
        /* 20130315 was:
		if (msg == null) return;
        if (!msg.bWillNotifyWhenProcessed) return;
        */
	    
	    if (msg != null && msg.bWillNotifyWhenProcessed) {
            synchronized (THREADLOCK) { 
            	if (this.iWillNotifyWhenProcessed == 0) {
            		LOG.severe("iWillNotifyWhenProcessed is 0, should be > 0 since there is a msg.bWillNotifyWhenProcessed=true");
            	}
            	else {
            	    this.iWillNotifyWhenProcessed--; // only place that it is decremented
            	}
            	msg.bWillNotifyWhenProcessed = false;
            }
	    }
	    else {
	        Thread t = Thread.currentThread();
            if (t != currentThread) return;
	    }
        synchronized (THREADLOCK) {
            currentThread = null;
            startNextThread();
        }
	}
	    
	
	private boolean bWakingUpThread; 
	
	// NOTE: a new thread can only be started after the currentThread has processed a message
	//      not while it is getting messages.
    private void startNextThread() {
        startNextThread(false);
    }
    private void startNextThread(boolean bSendMsg) {
    	// LOG.finest("called");
    	if (bStop) return;
    	synchronized (THREADLOCK) {
            if (bWakingUpThread) return;
            
            OAClientThread tx = currentThread;
            if (bSendMsg && tx != null && tx.isWaitingOnLock() && OAThreadLocalDelegate.hasLock()) {
                // a thread is waiting at sendMessage(..)
                OAThreadLocalDelegate.releaseAllLocks();
            }
            else {
                if (iWillNotifyWhenProcessed > 0) return;
        		// only start a new thread when the currentThread is processing
        		if (currentThread != null && !currentThread.isMessageProcessed()) return;
            }
            
			// LOG.finer("waitCnt="+waitCnt);
    		if (waitCnt > 0) {
    			bWakingUpThread = true;
    			currentThread = null;
	    		THREADLOCK.notify();  // wake up one thread
	        }
	        else {
	            if (alThread.size() > 30) {
	                LOG.fine("STARTING another thread, cnt="+alThread.size());
	                System.out.println("&&&&&&&&&&&&&&&&&& STARTING another thread, cnt="+alThread.size());
	                if (alThread.size() > 200) return;  // max threads to use at one time
	            }
	        	String name = "OAClientMessageHandler."+(cntThread++);
    			// LOG.finer("new Thread="+name);
	        	currentThread = new OAClientThread(this, name) {
	        	    @Override
	        	    public void setWaitingOnLock(boolean b) {
	        	        super.setWaitingOnLock(b);
	        	        if (b) OAClientMessageHandler.this.onWaitingOnLock(this);
	        	    }
	        	};
	        	currentThread.setUncaughtExceptionHandler(getExceptionHandler());
	        	currentThread.status = OAClientThread.STATUS_New;
        		currentThread.statusTime = System.currentTimeMillis();
	        	currentThread.location = "new";
	        	currentThread.setPriority(Thread.MAX_PRIORITY);
	        	currentThread.setDaemon(true);
	            alThread.add(currentThread);
	            LOG.fine("new thread "+name+"  alThread.size()="+alThread.size());	            
	            currentThread.start();
			}
    	}
    }

    protected void onWaitingOnLock(OAClientThread t) {
        if (t != currentThread) return;
        // need to wake up any threads waiting in sendMessage(..)
        synchronized (alSend) {
            int x = alSend.size();
            for (int i=0; i<x; i++) {
                OAObjectMessage om = alSend.get(i);
                synchronized (om) {
                    if (!om.bReceived) {
                        om.notify();
                    }
                }
            }
        }
    }
    
    protected Thread.UncaughtExceptionHandler getExceptionHandler() {
    	if (exceptionHandler == null) {
    		exceptionHandler = new Thread.UncaughtExceptionHandler() {
    			@Override
    			public void uncaughtException(Thread t, Throwable e) {
    				LOG.log(Level.SEVERE, "Exception with Thread="+t.getName(), e);
					OAClientThread ct = (OAClientThread) t;
					ct.status = OAClientThread.STATUS_Done;
	        		currentThread.statusTime = System.currentTimeMillis();
					ct.location = "Error: " + e.toString();
    			}
    		};
    	}
    	return exceptionHandler;
    }
    
    
    @Override
    public void run() {
		OAClientThread threadClient = (OAClientThread) Thread.currentThread(); 
    	for (;;) {
            synchronized (THREADLOCK) {
            	if (bStop || (threadClient != currentThread) || iWillNotifyWhenProcessed > 0 || bWakingUpThread) {
	            	if (bStop || waitCnt > 3) {
	            		if (waitCnt > 14 || ((System.currentTimeMillis() - threadClient.timeCreated) > 60000)) { 
		            		LOG.fine(threadClient.getName()+", waitCnt="+waitCnt+", stopping this thread");
		            		alThread.remove(threadClient);
		            		return;
	            		}
	            	}
	    			waitCnt++;
		        	threadClient.status = OAClientThread.STATUS_Wait;
	        		threadClient.statusTime = System.currentTimeMillis();
	        		threadClient.bWaitingOnLock = false;
	            	try {
	            		// LOG.finer(threadClient.getName()+" waiting, waitCnt="+waitCnt);
	            		THREADLOCK.wait();
	            		bWakingUpThread = false;
	            	}
	            	catch (Exception e) {
	            		e.printStackTrace();
	            		System.out.println("Wait Exception ="+e);
	            	}
	            	waitCnt--;
            		// LOG.finer(threadClient.getName()+" new currentThread");
	        	    currentThread = threadClient;
            	}
        	    if (bStop) continue;

        	    currentThread.status = OAClientThread.STATUS_GettingMessage;
        	    currentThread.statusTime = System.currentTimeMillis();
            }
    	    
    		// LOG.finer(threadClient.getName()+" getting message");
    		OAObjectMessage msg = msgReader.getNextMessage();
    		// LOG.finer(threadClient.getName()+" msg="+msg);
    		timeLastGet = System.currentTimeMillis();
        	if (msg == null) {
                if (msgReader.isStopped()) stop();
                continue;
        	}
    		msgReceivedCnt++;
    		threadClient.processCount++;

    		try {
    		    process(threadClient, msg);
    		}
    		catch (Throwable e) {
    		    LOG.log(Level.WARNING, "Error processing, msg="+msg, e);
    		}
    		finally {
    	        // 20090817 flag to know when server is done processing message, before client can get it.
    	        if (OAClient.isServer()) {
    	            msg.bAppliedOnServer = true;
    	            // 20130318 dont send blobs
    	            if (msg.type == OAObjectMessage.PROPERTY_CHANGE && msg.pos == 77) {
    	                msg.newValue = null;
    	            }
    	            synchronized (msg) {
    	                msg.notifyAll();
    	            }
    	        }
    		}
    	}
    }
    
    private void process(OAClientThread threadClient, OAObjectMessage msg) {
    	// LOG.finer("processing, msg="+msg);
		threadClient.msg = msg;
        if (msg.objectServerId == this.osId) {
    		// LOG.finer(threadClient.getName()+" sent message");
    		
    		OAObjectMessage om = null;
            int x = alSend.size();
            
            for (int i=0; ;i++) {
            	if (i == x) {
					LOG.warning("cant find matching send message that was sent, msg="+msg);
            		return;
            	}
                om = (OAObjectMessage) alSend.get(i);
                if (om.id == msg.id) {
                	synchronized (alSend) {
                		alSend.remove(i);
                	}
	                break;
                }
            }

	    	if (om.bWillNotifyWhenProcessed) {
	    		// LOG.finer("bWillNotifyWhenProcessed=true");
	            synchronized (THREADLOCK) {
	            	iWillNotifyWhenProcessed++; 
	            	currentThread = null;
	            }
	    	}
            synchronized(om) {
                om.newValue = msg.newValue; // GET[PUBLISHER]OBJECT message will return object in msg.newValue
                om.bReceived = true;
        		// LOG.finer(threadClient.getName()+" calling Notify for sendMessage, msg="+msg);
            	om.notify();
            }
        }
        else {
            if (msg.bPrivate) {
                // this will only happen on the server, so that msg.bAppliedOnServer can be set to true
                return;
            }
        	threadClient.status = OAClientThread.STATUS_Processing;
    	    threadClient.statusTime = System.currentTimeMillis();
        	process(msg);
        	if (threadClient.serverOnlyCnt > 0) {
                Exception ex = new Exception("");
                LOG.log(Level.WARNING, "threadClient.serverOnlyCnt="+threadClient.serverOnlyCnt+", should be 0.  Setting to 0.  msg="+msg, ex);
        		threadClient.serverOnlyCnt = 0;
        	}
        }
    }
    
    public OAObjectMessage sendMessage(OAObjectMessage msg) throws Exception {
    	msgSentCnt++;
    	
    	
    	long beginMs = System.currentTimeMillis();
    	if (bStop) return msg;
    	int result = 0;

    	
    	
        Thread thread = Thread.currentThread();
        // LOG.fine(msgSentCnt+") "+msg+", thread="+thread.getName());//qqqqqqqqqqqqqqqqqqqqqqqqqqqq        	
        
        if (thread == currentThread) {
//System.out.println("OAClientMessageHandler currentThread ==>SEND "+msg);//qqqqqqqqqqqqqqq        
        	synchronized (THREADLOCK) {
                if (Thread.currentThread() == currentThread) {
					if (currentThread.status <= OAClientThread.STATUS_Processing) { 
	                    Exception ex = new Exception("Call to sendMessage() using currentThread that was not done with main processing");
						LOG.log(Level.FINE, "", ex);
					}
					else if (!currentThread.isMessageProcessed()) {
					    Exception ex = new Exception("current thread isMessageProcessed=false");
		    		    LOG.log(Level.FINE, "", ex);
		    		}
                	startNextThread();
                }
        	}
        }
        else {
//System.out.println("OAClientMessageHandler ==>SEND "+msg);//qqqqqqqqqqqqqqq
        }

        /* 20130319 can return directly, must be async so that it comes back from the queue
        // 20120627 
        if (msg.bPrivate && msg.bSync) {
            OAObjectMessage om = oaObjectServer.sendMessage(msg);
            msg.newValue = om.newValue; // GET[PUBLISHER]OBJECT message will return object in msg.newValue
            om.bReceived = true;
            msg.bReceived = true;
            return om;
        }
        */
        
    	if (bStop) throw new Exception("Call to sendMessage when MessageHandler has already been stopped.  Message="+msg);
    	try {
        	synchronized (alSend) {
        		alSend.add(msg);
        	}
    		oaObjectServer.sendMessage(msg);
    	}
    	catch (Exception e) {
    		LOG.log(Level.SEVERE, "error calling sendMessage", e);
    		stop();
    		handleSendMessageException(msg, e);
    	}
    	if (bStop) return msg;

        // this will loop until the message is returned.
        for (int icnt=0; !bStop; icnt++) {
            if (msg.bReceived) break;
            startNextThread(true);  // will only start if the conditions are right
            synchronized(msg) {
                if (msg.bReceived) break;
                try {
            		// if (icnt==0) LOG.finer("waiting");
                	msg.wait(500);
                }
                catch (InterruptedException e) {
                }
            }
            if (msg.bReceived) break;
    		if (icnt > 20 &&  icnt%20 == 0) {
                if (msg.bReceived) break;
    			LOG.warning("waited "+(icnt/2)+" seconds for message - "+msg);
    			if (icnt == 60) {
    			    Exception e = new Exception("waited "+(icnt/2)+" seconds for message - "+msg);
                    LOG.log(Level.FINE, "waited "+icnt+" seconds for message - "+msg, e); // dont use WARNING, else it will send to server
    			}
    		}
        }
    	msgSentMs += (System.currentTimeMillis() - beginMs);
// System.out.println("OAClientMessageHandler <==SEND.DONE "+msg);//qqqqqqqqqqqqqqq        
    	return msg;
    }
    
    protected void clearSentMessages(OAObjectMessage msg) {
		for (int k=0; k<2; k++) {
		    if (msg == null) k = 1;
		    for (int i=0; i<alSend.size(); i++) {
		        OAObjectMessage om = (OAObjectMessage) alSend.get(i);
		        if (k == 0 && om.id != msg.id) continue;
			    if (k != 0) {
	                switch (msg.type) {
	                case OAObjectMessage.DATASOURCE:
	                case OAObjectMessage.GETDETAIL:
	                case OAObjectMessage.GETOBJECT:
	                case OAObjectMessage.GETPUBLISHEROBJECT:
	                    continue;
	                }
	            }
		        om.bReceived = true;
		        alSend.remove(i);
		        if (k == 0) om.newValue = msg.newValue; // GET[PUBLISHER]OBJECT message will return object in msg.newValue
		        om.bReceived = true;
		        synchronized (om) {
		            om.notifyAll();
		        }
		        i--;
		        if (k == 0) {
		            k = 5;
		            break;
		        }
		    }
		}
    }
    
    protected void updateClientInfo(OAClientInfo ci) {
    	ci.threadCount = alThread.size();
    	ci.threadWaiting = waitCnt;
    	if (msgSentCnt > 0) {
    		ci.msgSentMs = (msgSentMs / msgSentCnt);
    	}
    	synchronized (THREADLOCK) {
    		int x = alThread.size();
    		ci.threadInfo = new String[x];
			for (int i=0; i<x; i++) {
				OAClientThread ct = (OAClientThread) alThread.get(i);
				ci.threadInfo[i] = "  "+i+") "+ct.getInfo();
			}
    	}
    	
    }
    
    public int getMessageSentCount() {
    	return msgSentCnt;
    }
    public long getMessageSentMs() {
    	return msgSentMs;
    }
    public int getMessageReceivedCount() {
    	return msgReceivedCnt;
    }
    protected abstract void process(OAObjectMessage msg);
   
    public void handleSendMessageException(OAObjectMessage msg, Exception e) {
    	LOG.log(Level.SEVERE, "Error sending msg="+msg, e);
    }
}


