package com.viaoa.util.filter;

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OAGreaterFilter implements OAFilter {

    private Object value;
    public OAGreaterFilter(Object value) {
        this.value = value;
    }
    @Override
    public boolean isUsed(Object obj) {
        return OACompare.isGreater(obj, value);
    }
}

