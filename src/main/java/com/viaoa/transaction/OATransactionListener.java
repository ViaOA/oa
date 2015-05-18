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


/**
 * Used by OATransaction so that datasources can register with a transaction
 * and be called at the end of the transaction.
 * 
 * A datasource will use OAThreadInfoDelegate.getTransaction to get the 
 * current OATransaction for the current thread.  If there is a transaction,
 * then the datasource will create and add a listener, to be notified at end of transaction. 
 *
 */
public interface OATransactionListener {

    public void commit(OATransaction t);
    
    public void rollback(OATransaction t);
}

