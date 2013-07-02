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

