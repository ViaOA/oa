// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package com.theice.tsam.model.oa.search;

import com.theice.tsam.model.*;
import com.theice.tsam.model.oa.ErrorInfo;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

@OAClass(addToCache=false, initialize=true, useDataSource=false, localOnly=true)
public class ExceptionTypeSearch extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_ErrorInfos = "ErrorInfos";

    protected ErrorInfo errorInfos;

    public ErrorInfo getErrorInfos() {
        if (errorInfos == null) {
            errorInfos = (ErrorInfo) getObject(PROPERTY_ErrorInfos);
        }
        return errorInfos;
    }
    public void setErrorInfos(ErrorInfo newValue) {
        ErrorInfo old = this.errorInfos;
        fireBeforePropertyChange(PROPERTY_ErrorInfos, old, newValue);
        this.errorInfos = newValue;
        firePropertyChange(PROPERTY_ErrorInfos, old, this.errorInfos);
    }

}
