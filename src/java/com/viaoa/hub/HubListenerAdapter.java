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
package com.viaoa.hub;

/** 
    Adapter class that implements HubListener Interface.  
    <p>
    @See HubListener
*/
public class HubListenerAdapter<T> implements HubListener<T> {
    
    public void afterChangeActiveObject(HubEvent<T> e) { }
    public void beforePropertyChange(HubEvent<T> e) { }
    public void afterPropertyChange(HubEvent<T> e) { }
    public void beforeInsert(HubEvent<T> e) { }
    public void afterInsert(HubEvent<T> e) { }
    public void beforeMove(HubEvent<T> e) { }
    public void afterMove(HubEvent<T> e) { }
    public void beforeAdd(HubEvent<T> e) { }
    public void afterAdd(HubEvent<T> e) { }
    public void beforeRemove(HubEvent<T> e) { }
    public void afterRemove(HubEvent<T> e) { }
    public void beforeRemoveAll(HubEvent<T> e) { }
    public void afterRemoveAll(HubEvent<T> e) { }
    public void beforeSave(HubEvent<T> e) { }
    public void afterSave(HubEvent<T> e) { }
    public void beforeDelete(HubEvent<T> e) { }
    public void afterDelete(HubEvent<T> e) { }
    public void beforeSelect(HubEvent<T> e) { }
    public void afterSort(HubEvent<T> e) { }
    public void onNewList(HubEvent<T> e) { }
    public void afterFetchMore(HubEvent<T> e) { }
    
    private InsertLocation insertWhere;
    public void setLocation(InsertLocation pos) {
        this.insertWhere = pos;
    }
    public InsertLocation getLocation() {
        return this.insertWhere;
    }

    @Override
    public void afterLoad(HubEvent<T> e) {}
}

