/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;

/**
 * Used for cascading methods, to be able to know if an object
 * has already been visited.
 * 
 * Since this is a recursive visitor, it could cause stack overflows.  To handle this, there is
 * a depth that can be used.  When/if the max depth is reached, then objects can be added to an array
 * and then restarted once the stack unwinding is done.
 * 
 * 
 * @author vvia
 */
public class OACascade {
    private static Logger LOG = Logger.getLogger(OACascade.class.getName());
    // convert to this?  private IdentityHashMap hmCascade;
    private TreeSet<Integer> treeObject;
    private TreeSet<Hub> treeHub;
    private ReentrantReadWriteLock rwLock;
    private ReentrantReadWriteLock rwLockHub;
  
    // 20140821 todo: allow for max depth, restrart
    private int depth;
    private ArrayList<Object> al;

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

    public void depthAdd() {
        depth++;
    }
    public void depthSubtract() {
        depth--;
    }
    public int getDepth() {
        return depth;
    }
    public void setDepth(int d) {
        this.depth = d;
    }
    public void add(Object obj) {
        if (al == null) al = new ArrayList<Object>();
        al.add(obj);
    }
    public ArrayList<Object> getList() {
        return al;
    }
    public void clearList() {
        al = null;
    }
    
    public OACascade() {
        // LOG.finer("new OACascade");
    }

    /** 20160126 not used.  confusing: remove from tree or list
    public void remove(OAObject oaObj) {
        if (treeObject != null) {
            if (rwLock != null) rwLock.readLock().lock();
            treeObject.remove(oaObj.guid);
            if (rwLock != null) rwLock.readLock().unlock();
        }
    }
    */
    
    private HashSet<Class> hsIgnore; 
    public void ignore(Class clazz) {
        if (hsIgnore == null) hsIgnore = new HashSet<Class>();
        hsIgnore.add(clazz);
    }
    
    public boolean wasCascaded(OAObject oaObj, boolean bAdd) {
        if (oaObj == null) return false;
        if (hsIgnore != null && hsIgnore.contains(oaObj.getClass())) return true;
        if (treeObject == null) {
            if (!bAdd) return false;
            if (rwLock != null) rwLock.writeLock().lock();
            treeObject = new TreeSet<Integer>();
            if (rwLock != null) rwLock.writeLock().unlock();
        }
        
        boolean b;
        try {
            if (rwLock != null) rwLock.readLock().lock();            
            b = treeObject.contains(oaObj.guid);
        }
        finally {
            if (rwLock != null) rwLock.readLock().unlock();
        }
        if (b) return true;

        if (bAdd) {
            if (rwLock != null) rwLock.writeLock().lock();
            treeObject.add(oaObj.guid);
            if (rwLock != null) rwLock.writeLock().unlock();
        }
        return false;
    }
    
    public boolean wasCascaded(Hub hub, boolean bAdd) {
        if (hub == null) return false;

        if (treeHub == null) {
            if (!bAdd) return false;
            if (rwLockHub != null) rwLockHub.writeLock().lock();
            treeHub = new TreeSet<Hub>();
            if (rwLockHub != null) rwLockHub.writeLock().unlock();
        }
        
        boolean b = false;
        try {
            if (rwLockHub != null) rwLockHub.readLock().lock();
            b = treeHub.contains(hub);
        }
        finally {
            if (rwLockHub != null) rwLockHub.readLock().unlock();
        }
        if (b) return true;
        
        if (bAdd) {
            if (rwLockHub != null) rwLockHub.writeLock().lock();
            treeHub.add(hub);
            if (rwLockHub != null) rwLockHub.writeLock().unlock();
        }
        return false;
    }
}
