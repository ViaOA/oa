package com.viaoa.util;


public class OAPropertyPathDelegate {

    /**
     * parse a propertyPath that has a leading "[ClassName]."
     * @param packageClass class that the from class is in the same package as. 
     */
    public static OAPropertyPath createRootPropertyPath(String sPropPath, Class packageClass) throws Exception {
        OAPropertyPath pp = new OAPropertyPath(packageClass, sPropPath);
        return pp;
    }
    
}
