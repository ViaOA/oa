/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.object;

import java.util.logging.Logger;

/** Used to define calculated properties for OAObject.  A Calculated property is a read only property that
    depends on other properties for its value. 
    <br>
    When a calclualted property is being used, a listener is set up that will listen for changes to any
    of the dependent properties.
    <p>
    Note: listeners for dependent properties are only created when there is a HubListener for the calculated
    property itself.
 
    <pre>
    Example:  
        Class Emp
            String firstName
            String lastName
            Dept dept;
            public String fullName() {
                return firstName + " " + lastName + " Dept " + dept.getName();   
            }
                
        OACalcInfo = new OACalcInfo("fullName",String { "firstName", "lastName", "dept.name" } );            
    </pre>
 
 
 
    OACalcInfo is created and then stored within an OAObjectInfo object and is used by an
    OAObject.  OAObject will automatically listen for property changes on any of the depended
    properties and will notify listeners whenever any change occurs that might affect the
    calculated property.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
 
    @see OAObject#getOAObjectInfo
    @see OAObjectInfo    
    @author Vince Via
    @version 1.0 12/01/98
 */
public class OACalcInfo implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(OACalcInfo.class.getName());
    
    String name;
    String[] properties;

    /** 
     Create new Calculated Property.  
     * <pre>
     * Example:  
     *   new CalcInfo("totalCostOfOrder",String { "orderItem.qty", "orderItem.product.cost", "customer.freight", "customer.state.taxRate" } );            
     * </pre>
     * @param propertyName name of calculated property
     * @param props array of depend property paths
     */
    public OACalcInfo(String name, String[] props) {
        this.name = name;
        properties = props;
    }

    /** get Calculated Property name */
    public String getName() {
        return name;
    }
    /** get property paths of all dependent properties */
    public String[] getProperties() {
        return properties;
    }
    public void setPropeties(String[] props) {
        properties = props;
    }

    
/* 20101218 replaced by HubListenerTree
    
    // set by HubEventDelegate.addHubListener(..., property) when a calc property is being used and prop changes need to be checked (here).    
    private int listenerCount;
    
    // used internal by OAObject and Hub to know how many listeners for each calc property
    public void addToListenerCount() {
        listenerCount++;   
    }
    // used internal by OAObject and Hub to know how many listeners for each calc property
    public void removeFromListenerCount() {
        listenerCount--;   
        if (listenerCount < 0) {
            LOG.warning("listenerCount < 0, setting back to 0");
            listenerCount = 0;
        }
    }
    // used internal by OAObject and Hub to know how many listeners for each calc property 
    protected int getListenerCount() {
        return listenerCount;
    }
*/    
}

