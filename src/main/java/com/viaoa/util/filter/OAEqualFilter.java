package com.viaoa.util.filter;

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OAEqualFilter implements OAFilter {

    private Object value;
    private boolean bIgnoreCase;
    
    public OAEqualFilter(Object value) {
        this.value = value;
    }
    public OAEqualFilter(Object value, boolean bIgnoreCase) {
        this.value = value;
        this.bIgnoreCase = bIgnoreCase;
    }
    @Override
    public boolean isUsed(Object obj) {
        return OACompare.isEqual(obj, value, bIgnoreCase);
    }
}

