/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
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
            cd.setIsForHub(ci.getIsForHub());
            od.getCalcPropertyDefs().add(cd);
        }
        for (OALinkInfo li : oi.getLinkInfos()) {
            if (li.getPrivateMethod()) continue;
            if (!li.getUsed()) continue;
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


