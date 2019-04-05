package com.viaoa.ds;

import java.util.Iterator;

import com.viaoa.object.OASiblingHelper;

public interface OADataSourceIterator extends Iterator {
    

    public String getQuery();
    public String getQuery2();
    
    public default OASiblingHelper getSiblingHelper() {
        return null;
    }
    

}
