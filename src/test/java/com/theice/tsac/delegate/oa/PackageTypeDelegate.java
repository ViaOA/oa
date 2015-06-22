package com.theice.tsac.delegate.oa;

import com.theice.tsac.delegate.ModelDelegate;
import com.theice.tsac.model.oa.*;
import com.viaoa.util.OAString;

public class PackageTypeDelegate {

    public static PackageType getPackageTypeUsingCode(String code, boolean bAutoCreate) {
        if (OAString.isEmpty(code)) return null;

        PackageType pt = ModelDelegate.getPackageTypes().find(PackageType.P_Code, code);
        
        if (pt == null && bAutoCreate) {
            pt = new PackageType();
            pt.setCode(code);
            ModelDelegate.getPackageTypes().add(pt);
        }
        return pt;
    }
    
    public static PackageType getPackageTypeUsingPackageName(String packageName, boolean bAutoCreate) {
        if (OAString.isEmpty(packageName)) return null;

        PackageType pt = ModelDelegate.getPackageTypes().find(PackageType.P_PackageName, packageName);
        
        if (pt == null && bAutoCreate) {
            pt = new PackageType();
            pt.setPackageName(packageName);
            ModelDelegate.getPackageTypes().add(pt);
        }
        return pt;
    }
}
