package com.viaoa.util.filter;

import com.viaoa.util.OAPropertyPath;

public class OAFalseFilter extends OAEqualFilter {

    public OAFalseFilter(String pp) {
        super(pp, Boolean.FALSE);
    }
    public OAFalseFilter(OAPropertyPath pp) {
        super(pp, Boolean.FALSE);
    }

}
