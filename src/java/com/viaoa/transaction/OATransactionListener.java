package com.viaoa.transaction;


/**
 * Used by OATransaction so that datasources can register with a transaction
 * and be called at the end of the transaction.
 * 
 * A datasource will use OAThreadInfoDelegate.getTransaction to get the 
 * current OATransaction for the current thread.  It there is a transaction,
 * then the datasource will create and add a listener, to be notified at end of transaction.L 
 *
 */
public interface OATransactionListener {

    public void commit(OATransaction t);
    
    public void rollback(OATransaction t);
}

