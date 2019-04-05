package test.xice.tsac.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.oa.*;

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
