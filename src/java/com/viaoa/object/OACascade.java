package com.viaoa.object;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;

/**
 * Used for cascading methods, to be able to know if an object
 * has already been visited.
 * @author vvia
 */
public class OACascade {
    private static Logger LOG = Logger.getLogger(OACascade.class.getName());
    // convert to this?  private IdentityHashMap hmCascade;
    private TreeSet<Integer> mapCascade;
    private TreeSet<Hub> mapCascadeHub;
    private ReentrantReadWriteLock rwLock;
    private ReentrantReadWriteLock rwLockHub;
    
    public OACascade() {
        LOG.finer("new OACascade");
    }
    /**
     * 
     * @param bUseLocks true if this will be used by multiple threads
     */
    public OACascade(boolean bUseLocks) {
        LOG.finer("new OACascade");
        if (bUseLocks) {
            rwLock = new ReentrantReadWriteLock();
            rwLockHub = new ReentrantReadWriteLock();
        }
    }

    public void remove(OAObject oaObj) {
        if (mapCascade != null) {
            if (rwLock != null) rwLock.readLock().lock();
            mapCascade.remove(oaObj.guid);
            if (rwLock != null) rwLock.readLock().unlock();
        }
    }
    
    private HashSet<Class> hsIgnore; 
    public void ignore(Class clazz) {
        if (hsIgnore == null) hsIgnore = new HashSet<Class>();
        hsIgnore.add(clazz);
    }
    
    public boolean wasCascaded(OAObject oaObj, boolean bAdd) {
        if (oaObj == null) return false;
        if (hsIgnore != null && hsIgnore.contains(oaObj.getClass())) return true;
        if (mapCascade == null) {
            if (!bAdd) return false;
            if (rwLock != null) rwLock.writeLock().lock();
            mapCascade = new TreeSet<Integer>();
            if (rwLock != null) rwLock.writeLock().unlock();
        }
        
        if (rwLock != null) rwLock.readLock().lock();
        boolean b = mapCascade.contains(oaObj.guid);
        if (rwLock != null) rwLock.readLock().unlock();
        if (b) return true;

        if (bAdd) {
            if (rwLock != null) rwLock.writeLock().lock();
            mapCascade.add(oaObj.guid);
            if (rwLock != null) rwLock.writeLock().unlock();
        }
        return false;
    }
    
    public boolean wasCascaded(Hub hub, boolean bAdd) {
        if (hub == null) return false;

        if (mapCascadeHub == null) {
            if (!bAdd) return false;
            if (rwLockHub != null) rwLockHub.writeLock().lock();
            mapCascadeHub = new TreeSet<Hub>();
            if (rwLockHub != null) rwLockHub.writeLock().unlock();
        }
        
        if (rwLockHub != null) rwLockHub.readLock().lock();
        boolean b = mapCascadeHub.contains(hub);
        if (rwLockHub != null) rwLockHub.readLock().unlock();
        if (b) return true;
        
        if (bAdd) {
            if (rwLockHub != null) rwLockHub.writeLock().lock();
            mapCascadeHub.add(hub);
            if (rwLockHub != null) rwLockHub.writeLock().unlock();
        }
        return false;
    }
}
