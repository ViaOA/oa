package com.viaoa.ds;

import com.viaoa.util.filter.OAQueryFilter;

public class OASelectFilter<T> extends OAQueryFilter<T> {

    public OASelectFilter(Class<T> clazz, String query, Object[] args) {
        super(clazz, query, args);
    }
    public OASelectFilter(Class<T> clazz, String query) {
        super(clazz, query);
    }

}
