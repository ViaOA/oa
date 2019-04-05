package com.viaoa.util.filter;

import com.viaoa.util.OAPropertyPath;

public class OATrueFilter extends OAEqualFilter {

    public OATrueFilter(String pp) {
        super(pp, Boolean.TRUE);
    }
    public OATrueFilter(OAPropertyPath pp) {
        super(pp, Boolean.TRUE);
    }

}
