package com.viaoa.cs;

import java.util.logging.*;

import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAThreadLocalDelegate;

/**
 * Retrieves messages from server, uses polling to get messages
 * from OAServer queue.
 * @author vvia
 */
public class OAClientMessageReader {

	private static Logger LOG = Logger.getLogger(OAClientMessageReader.class.getName());
	
    private static int QueueSize = 1500;
    private OAObjectMessage[] msgQueue = new OAObjectMessage[QueueSize];
    private boolean bStop;
    private Thread thread;
	protected OAObjectServer oaObjectServer;  // rmi object used to send messages
    private Object LOCK = new Object();
    private int nextPos;
    private int loadPos;
    private boolean bIsRunning;
    private boolean bWaiting, bServerWaiting;


    protected OAClientMessageReader(OAObjectServer oaObjectServer) {
    	this.oaObjectServer = oaObjectServer;
    	LOG.config("max size for client Queue="+QueueSize);
    }

    protected void stop() {
    	stop(true);
    }
    protected void stop(boolean bLog) {
    	if (bLog) LOG.config("stopping");
    	bStop = true;
		synchronized (LOCK) {
			LOCK.notifyAll();
		}    	
    }
    
    boolean isStopped() {
    	return bStop;
    }

    private int startCount;
    protected void start() {
    	LOG.config("starting");
        bIsRunning = true;
    	thread = new Thread("OAClientMessageReader"+(startCount>0?startCount:"")) {
    		public void run() {
    			if (!bStop) loadServerMessages();
    		}
    	};
    	thread.setDaemon(true);
    	
		thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOG.log(Level.SEVERE, "Exception with Thread="+t.getName(), e);
				if (startCount < 5) OAClientMessageReader.this.start();
			}
		});
    	startCount++;
    	thread.start();
    }

    
    protected boolean isRunning() {
    	return bIsRunning;
    }
    
    
    protected OAObjectMessage getNextMessage() {
    	// LOG.finer("called");
    	OAObjectMessage msg = null;

		synchronized (LOCK) {
			if (bWaiting) {
				LOG.severe("more then one thread waiting on next message");
			}
	    	if (!bIsRunning) return null;
			for (; !bStop; ) {
				if (nextPos < loadPos) {
				    int pos = (nextPos % QueueSize);
		    		msg = msgQueue[pos];
		    		msgQueue[pos] = null; // so msg can be gc'd
					nextPos++;
					if (bServerWaiting) LOCK.notify();
					break;
				}
		        try {
		        	bWaiting = true;
	    	    	// LOG.finer("waiting for msg from loadServerMessages");
		        	LOCK.wait();
		        }
		        catch (Exception e) {
		        	LOG.log(Level.WARNING, "exception in wait", e);
		        }
		        finally {
	    	    	// LOG.finer("notified");
		        	bWaiting = false;
		        }
			}
		}
		if (bStop) LOG.config("stopping called");
		// if (msg != null) LOG.finer(msg.toString());
		return msg;
    }

    private void loadServerMessages() {
    	LOG.config("starting");
    	OAThreadLocalDelegate.setObjectCacheAddMode(OAObjectCacheDelegate.IGNORE_DUPS); // else readObject will throw a duplicate exception when reading objects that already are loaded
        bIsRunning = true;
        OAObjectMessage msg = null;
    	OAObjectMessage[] msgs = null;
    	int msgsPos = 0;

    	int getCnt = 0;
    	int lastGetCnt = 0;
		int nextSeq = 0;
		
    	for ( ;!bStop; ) {
	    	try {
	    		if (msgs == null || msgsPos == msgs.length) {
	    	    	// LOG.finer("calling oaObjectServer.getMessages()");
	    			msgs = oaObjectServer.getMessages();  // this will wait until a message is ready in the clients queue on the server
	    			msgsPos = 0;
	    			getCnt++;
	    	    	// LOG.finer("recevd "+msgs.length+" messages");
	    		}
	    		msg = msgs[msgsPos++];
	    		if (bStop) break;

	    		// verify
	    		if (!msg.bPrivate) {
	        		if (msg.seq != nextSeq) {
	        			if (nextSeq != 0 && msg.seq != 0) {
	            			LOG.severe("Message seq error, expecting:"+nextSeq+", recv:"+msg.seq+", will reset seq and continue");
	            			nextSeq = msg.seq;
	        			}
	        		}
	        		nextSeq = msg.seq + 1;
	    		}
		    	
	    		
	            synchronized (LOCK) {
	            	int pos = (loadPos % QueueSize);
		            msgQueue[pos] = msg;
					loadPos++;
					if (loadPos == Integer.MAX_VALUE) {
						LOG.info("loadPos reached Integer.MAX_VALUE, rolling over, was: nextPos="+nextPos+", loadPos="+loadPos);
						nextPos = nextPos % QueueSize;
						loadPos = loadPos % QueueSize;
						LOG.info("new: nextPos="+nextPos+", loadPos="+loadPos);
					}
            		if (bWaiting) {
    	    	    	// LOG.finer("notify wait thread in getNextObjectMessage()");
            			LOCK.notify();
            		}

            		// LOG.finer("messages in queue="+(loadPos - nextPos));
            		for (; loadPos >= (nextPos + (QueueSize-10)); ) {
		            	if (getCnt > lastGetCnt) {
                            LOG.info("queue almost full, messages in queue="+(loadPos - nextPos));
		            		// LOG.info("queue almost full, waiting on OAClientMessageHandler to call getNextObjectMessage, messages in queue="+(loadPos - nextPos));
		            		lastGetCnt = getCnt + 50;
		            	}
            			bServerWaiting = true;
            			try {
            			    LOCK.wait();
            			}
            			catch (Exception e) {
                            LOG.log(Level.WARNING, "exception in wait", e);
            			}
    	            	bServerWaiting = false;
    				}
	        	}
	    	}
	        catch (Exception e) {
	        	bIsRunning = false;
	            if (bStop) break;
	            LOG.log(Level.SEVERE, "error getting message", e);
	            stop();
	            handleGetMessageException(msg, e);
	            break;
	        }
	    }
    	bIsRunning = false;
	}

    
    public void updateClientInfo(OAClientInfo ci) {
    	ci.queueSize = loadPos - nextPos;
    }
    
    public void handleGetMessageException(OAObjectMessage msg, Exception e) {
    	LOG.log(Level.SEVERE, "Error processing msg="+msg, e);
    }
    
}



