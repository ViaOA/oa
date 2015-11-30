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
package com.viaoa.object;

import java.util.logging.Logger;

import com.viaoa.annotation.OACalculatedProperty;

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
 */
public class OACalcInfo implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(OACalcInfo.class.getName());
    
    String name;
    String[] properties;  // dependent properties
    private OACalculatedProperty oaCalculatedProperty;

    /** 20131027
     *  true if this calcProp is for the whole Hub, and the method has a static method with a Hub param
     */
    boolean bIsForHub;  

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
    public OACalcInfo(String name, String[] props, boolean bIsForHub) {
        this.name = name;
        properties = props;
        this.bIsForHub = bIsForHub;
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

    public boolean getIsForHub() {
        return bIsForHub;
    }

    public OACalculatedProperty getOACalculatedProperty() {
        return oaCalculatedProperty;
    }
    public void setOACalculatedProperty(OACalculatedProperty c) {
        oaCalculatedProperty = c;
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

