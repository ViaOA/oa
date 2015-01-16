package com.viaoa.util.filter;

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OALessOrEqualFilter implements OAFilter {

    private Object value;
    public OALessOrEqualFilter(Object value) {
        this.value = value;
    }
    @Override
    public boolean isUsed(Object obj) {
        return OACompare.isLessOrEqual(obj, value);
    }
}

