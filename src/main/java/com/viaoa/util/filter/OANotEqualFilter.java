package com.viaoa.util.filter;

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OANotEqualFilter implements OAFilter {

    private Object value;
    private boolean bIgnoreCase;

    public OANotEqualFilter(Object value) {
        this.value = value;
    }
    public OANotEqualFilter(Object value, boolean bIgnoreCase) {
        this.value = value;
        this.bIgnoreCase = bIgnoreCase;
    }
    @Override
    public boolean isUsed(Object obj) {
        return !OACompare.isEqual(obj, value, bIgnoreCase);
    }
}

