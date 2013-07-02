/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.transaction;

import java.util.ArrayList;
import java.util.HashMap;

import com.viaoa.object.OAThreadLocalDelegate;


/**
 * Creates a transaction for a Thread.
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
