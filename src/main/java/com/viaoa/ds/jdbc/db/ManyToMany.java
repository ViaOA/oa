package com.viaoa.ds.jdbc.db;

import com.viaoa.object.OAObjectKey;

public class ManyToMany {

    public OAObjectKey ok1;
    public OAObjectKey ok2;

    public ManyToMany(OAObjectKey ok1, OAObjectKey ok2) {
        this.ok1 = ok1;
        this.ok2 = ok2;
    }
}
