package com.viaoa.remote.multiplexer;

public class OARemoteThreadDelegate {

    public static boolean isRemoteThread() {
        Thread t = Thread.currentThread();
        return (t instanceof OARemoteThread);
    }

    public static boolean shouldSendMessages() {
        Thread t = Thread.currentThread();
        if (!(t instanceof OARemoteThread)) return true;
        return ((OARemoteThread) t).getSendMessages();
    }

    public static void startNextThread() {
        Thread t = Thread.currentThread();
        if (t instanceof OARemoteThread) {
            ((OARemoteThread) t).startNextThread();
        }
    }
    
    public static void sendMessages() {
        sendMessages(true);
    }
    public static void sendMessages(boolean b) {
        Thread t = Thread.currentThread();
        if (!(t instanceof OARemoteThread)) return;
        ((OARemoteThread) t).setSendMessages(b);
    }
}
