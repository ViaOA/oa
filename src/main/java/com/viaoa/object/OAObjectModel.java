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

import com.viaoa.annotation.OAClass;

@OAClass (addToCache=false, localOnly=true, useDataSource=false)
public class OAObjectModel extends OAObject {

    public static final String P_DisplayName = "DisplayName";
    public static final String P_DisplayNamePlural = "DisplayNamePlural";
    
    protected boolean bAllowGotoList;
    protected boolean bAllowGotoEdit;
    protected boolean bAllowSearch;
    protected boolean bAllowHubSearch;
    
    protected boolean bAllowAdd;
    protected boolean bAllowNew;
    protected boolean bAllowAutoCreate;
    
    protected boolean bAllowRemove;
    protected boolean bAllowDelete;

    protected boolean bAllowCut;
    protected boolean bAllowCopy;
    protected boolean bAllowPaste;
    protected boolean bViewOnly;
    
    protected String displayName;
    protected String displayNamePlural;

    public OAObjectModel() {
        if (isLoading()) return;
        setAllowGotoList(true);
        setAllowGotoEdit(true);
        setAllowSearch(true);
        setAllowHubSearch(false);
        setAllowAdd(true);
        setAllowNew(true);
        setAllowRemove(true);
        setAllowDelete(true);
        setAllowCut(true);
        setAllowCopy(true);
        setAllowPaste(true);
    }

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String newValue) {
        String old = displayName;
        this.displayName = newValue;
        firePropertyChange(P_DisplayName, old, this.displayName);
    }
    
    public String getDisplayNamePlural() {
        return displayNamePlural;
    }
    public void setDisplayNamePlural(String newValue) {
        String old = displayNamePlural;
        this.displayNamePlural = newValue;
        firePropertyChange(P_DisplayNamePlural, old, this.displayNamePlural);
    }
    
    // methods to enable commands
    public boolean getAllowGotoList() {
        return bAllowGotoList;
    }
    public void setAllowGotoList(boolean b) {
        bAllowGotoList = b;
    }
    public boolean getAllowGotoEdit() {
        return bAllowGotoEdit;
    }
    public void setAllowGotoEdit(boolean b) {
        bAllowGotoEdit = b;
    }
    public boolean getAllowSearch() {
        return bAllowSearch;
    }
    public void setAllowSearch(boolean b) {
        bAllowSearch = b;
    }
    public boolean getAllowHubSearch() {
        return bAllowHubSearch;
    }
    public void setAllowHubSearch(boolean b) {
        bAllowHubSearch = b;
    }
    public boolean getAllowAdd() {
        return bAllowAdd;
    }
    public void setAllowAdd(boolean b) {
        bAllowAdd = b;
    }
    public boolean getAllowNew() {
        return bAllowNew;
    }
    public void setAllowNew(boolean b) {
        bAllowNew = b;
    }

    public boolean getAllowAutoCreate() {
        return bAllowAutoCreate;
    }
    public void setAllowAutoCreate(boolean b) {
        bAllowAutoCreate = b;
    }
    
    
    public boolean getAllowRemove() {
        return bAllowRemove;
    }
    public void setAllowRemove(boolean b) {
        bAllowRemove = b;
    }
    public boolean getAllowDelete() {
        return bAllowDelete;
    }
    public void setAllowDelete(boolean b) {
        bAllowDelete = b;
    }
    public boolean getAllowCut() {
        return bAllowCut;
    }
    public void setAllowCut(boolean b) {
        bAllowCut = b;
    }
    public boolean getAllowCopy() {
        return bAllowCopy;
    }
    public void setAllowCopy(boolean b) {
        bAllowCopy = b;
    }
    public boolean getAllowPaste() {
        return bAllowPaste;
    }
    public void setAllowPaste(boolean b) {
        bAllowPaste = b;
    }
    
    public boolean getViewOnly() {
        return bViewOnly;
    }
    public void setViewOnly(boolean b) {
        bViewOnly = b;
    }
}
