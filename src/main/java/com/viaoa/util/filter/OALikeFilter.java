package com.viaoa.util.filter;

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OALikeFilter implements OAFilter {

    private Object value;
    public OALikeFilter(Object value) {
        this.value = value;
    }
    @Override
    public boolean isUsed(Object obj) {
        return OACompare.isLike(obj, value);
    }
}

