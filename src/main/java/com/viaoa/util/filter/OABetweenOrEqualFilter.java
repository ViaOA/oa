package com.viaoa.util.filter;

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OABetweenOrEqualFilter implements OAFilter {

    private Object value1, value2;
    public OABetweenOrEqualFilter(Object val1, Object val2) {
        this.value1 = val1;
        this.value2 = val2;
    }
    @Override
    public boolean isUsed(Object obj) {
        return OACompare.isBetweenOrEqual(obj, value1, value2);
    }
}

