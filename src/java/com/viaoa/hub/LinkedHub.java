package com.viaoa.hub;

/**
*/
public class LinkedHub<TYPE> extends Hub<TYPE> {

    /* this one is not a good idea - since the hub is not populated when created and it could need to be linked to hubTo.AO
    public LinkedHub(Class<TYPE> clazz, Hub<?> hubTo, String toPropertyName) {
    	super(clazz);
    	setLinkHub(hubTo, toPropertyName);
    }
    */
    public LinkedHub(Class<TYPE> clazz) {
        super(clazz);
    }
}

