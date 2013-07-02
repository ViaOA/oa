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
package com.viaoa.object;

import com.viaoa.annotation.OAClass;

@OAClass (addToCache=false, localOnly=true, useDataSource=false)
public class OAObjectModel extends OAObject {

    public static final String PROPERTY_DisplayName = "DisplayName";
    public static final String PROPERTY_DisplayNamePlural = "DisplayNamePlural";
    
    protected boolean bAllowGotoList;
    protected boolean bAllowGotoEdit;
    protected boolean bAllowSearch;
    
    protected boolean bAllowAdd;
    protected boolean bAllowNew;
    protected boolean bAllowAutoCreate;
    
    protected boolean bAllowRemove;
    protected boolean bAllowDelete;

    protected boolean bAllowCut;
    protected boolean bAllowCopy;
    protected boolean bAllowPaste;
    
    protected String displayName;
    protected String displayNamePlural;
    
    public OAObjectModel() {
        if (isLoading()) return;
        setAllowGotoList(true);
        setAllowGotoEdit(true);
        setAllowSearch(true);
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
        firePropertyChange(PROPERTY_DisplayName, old, this.displayName);
    }
    
    public String getDisplayNamePlural() {
        return displayNamePlural;
    }
    public void setDisplayNamePlural(String newValue) {
        String old = displayNamePlural;
        this.displayNamePlural = newValue;
        firePropertyChange(PROPERTY_DisplayNamePlural, old, this.displayNamePlural);
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
    
    
    
    
}
