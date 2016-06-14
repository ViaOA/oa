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
    public void afterNewList(HubEvent<T> e) { }
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
    
    
    
    
    @Override
    public boolean canAdd(HubEvent<T> e) {
        return true;
    }
    @Override
    public boolean canRemove(HubEvent<T> e) {
        return true;
    }
    @Override
    public boolean canDelete(HubEvent<T> e) {
        return true;
    }
    @Override
    public boolean canChangeProperty(HubEvent<T> e) {
        return true;
    }
}

