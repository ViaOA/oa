package com.viaoa.util.filter;

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OAEmptyFilter implements OAFilter {

    public OAEmptyFilter() {
    }
    @Override
    public boolean isUsed(Object obj) {
        return OACompare.isEmpty(obj, true);
    }
}

