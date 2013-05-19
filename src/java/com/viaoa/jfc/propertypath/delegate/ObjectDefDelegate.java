package com.viaoa.jfc.propertypath.delegate;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.propertypath.model.oa.*;
import com.viaoa.object.OACalcInfo;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAPropertyInfo;
import com.viaoa.util.OAString;


/**
 * 20130223 populate OAObjects from OAObjectInfo
 * @author vvia
 */
public class ObjectDefDelegate {

    public static ObjectDef getObjectDef(Hub<ObjectDef> hubObject, Class rootClass) {
        if (hubObject == null) return null;
        if (rootClass == null) return  null;
        
        ObjectDef od = null;
        for (ObjectDef odx : hubObject) {
            if (odx.getObjectClass() == rootClass) {
                od = odx;
                break;
            }
        }
        if (od != null) {
            return od;
        }
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(rootClass);
        od = new ObjectDef();
        od.setObjectClass(rootClass);
        String s = rootClass.getName();
        s = OAString.getClassName(rootClass);
        od.setName(s);
        od.setDisplayName(oi.getDisplayName());
        hubObject.add(od);
        
        for (OAPropertyInfo pi : oi.getPropertyInfos()) {
            PropertyDef pd = new PropertyDef();
            pd.setName(pi.getName());
            pd.setDisplayName(pi.getName());
            od.getPropertyDefs().add(pd);
        }
        for (OACalcInfo ci : oi.getCalcInfos()) {
            CalcPropertyDef cd = new CalcPropertyDef();
            cd.setName(ci.getName());
            cd.setDisplayName(ci.getName());
            od.getCalcPropertyDefs().add(cd);
        }
        for (OALinkInfo li : oi.getLinkInfos()) {
            if (li.getPrivateMethod()) continue;
            LinkPropertyDef lp = new LinkPropertyDef();
            lp.setName(li.getName());
            lp.setDisplayName(li.getName());
            lp.setType(li.getType()==OALinkInfo.ONE ? LinkPropertyDef.TYPE_One : LinkPropertyDef.TYPE_Many);
            od.getLinkPropertyDefs().add(lp);

            ObjectDef tod = getObjectDef(hubObject, li.getToClass());
            lp.setToObjectDef(tod);
        }
        
        return od;
    }

}


