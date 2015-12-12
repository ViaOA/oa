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
package com.viaoa.transaction;

import java.util.ArrayList;
import java.util.HashMap;

import com.viaoa.object.OAThreadLocalDelegate;


/**
 * Creates a transaction for a Thread.
 * 
     example:
 <code>
 
    OATransaction trans = new OATransaction(java.sql.Connection.TRANSACTION_SERIALIZABLE);
    trans.start();
    ..
    trans.commit();
    ||
    trans.rollback();
      
 </code>
  
 * 
 * @author vincevia
 *
 */
public class OATransaction {
    private int transactionLevel;
    private ArrayList<OATransactionListener> al = new ArrayList<OATransactionListener>();

    
    /**  java.sql.Connection isolation levels
        java.sql.Connection.X<br>
        TRANSACTION_NONE - level not set - some databases (ex: Derby) will throw and exception<br>
        TRANSACTION_READ_UNCOMMITTED - data changed by transaction will be used by other transactions that read<br>
        TRANSACTION_READ_COMMITTED - data changed by transaction is not "seen" until commited.  Other transactions will read "old" data.<br>
        TRANSACTION_REPEATABLE_READ - prevents others from writing<br>
        TRANSACTION_SERIALIZABLE - prevents others from reading & writing<br>
    */
    public OATransaction(int transactionLevel) {
        this.transactionLevel = transactionLevel;
    }
    
    public int getTransactionIsolationLevel() {
        return transactionLevel;
    }
    
    public void start() {
        OAThreadLocalDelegate.setTransaction(this);
    }
    
    public void rollback() {
        try {
            for (OATransactionListener tl : al) {
                tl.rollback(this);
            }
        }
        finally {
            OAThreadLocalDelegate.setTransaction(null);
        }
    }
          
    public void commit() {
        try {
            for (OATransactionListener tl : al) {
                tl.commit(this);
            }
        }
        finally {
            OAThreadLocalDelegate.setTransaction(null);
        }
    }
    
    public void addTransactionListener(OATransactionListener tl) {
        if (!al.contains(tl)) {
            al.add(tl);
        }
    }
    public void removeTransactionListener(OATransactionListener tl) {
        al.remove(tl);
    }

    // used by TransactionListeners to "store" information.
    private HashMap hm = new HashMap(); 
    public void put(Object key, Object value) {
        hm.put(key, value);
    }
    public Object get(Object key) {
        return hm.get(key);
    }
    public Object remove(Object key) {
        return hm.remove(key);
    }
}
