package com.viaoa.util.filter;

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OAGreaterOrEqualFilter implements OAFilter {

    private Object value;
    public OAGreaterOrEqualFilter(Object value) {
        this.value = value;
    }
    @Override
    public boolean isUsed(Object obj) {
        return OACompare.isGreaterOrEqual(obj, value);
    }
}

