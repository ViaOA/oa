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
public class HubListenerAdapter implements HubListener {
    
    public void afterChangeActiveObject(HubEvent e) { }
    public void beforePropertyChange(HubEvent e) { }
    public void afterPropertyChange(HubEvent e) { }
    public void beforeInsert(HubEvent e) { }
    public void afterInsert(HubEvent e) { }
    public void beforeMove(HubEvent e) { }
    public void afterMove(HubEvent e) { }
    public void beforeAdd(HubEvent e) { }
    public void afterAdd(HubEvent e) { }
    public void beforeRemove(HubEvent e) { }
    public void afterRemove(HubEvent e) { }
    public void beforeRemoveAll(HubEvent e) { }
    public void afterRemoveAll(HubEvent e) { }
    public void afterSave(HubEvent e) { }
    public void beforeDelete(HubEvent e) { }
    public void afterDelete(HubEvent e) { }
    public void beforeSelect(HubEvent e) { }
    public void afterSort(HubEvent e) { }
    public void onNewList(HubEvent e) { }
    public void afterFetchMore(HubEvent e) { }
    
    private InsertLocation insertWhere;
    public void setLocation(InsertLocation pos) {
        this.insertWhere = pos;
    }
    public InsertLocation getLocation() {
        return this.insertWhere;
    }

    @Override
    public void afterLoad(HubEvent e) {}
}

