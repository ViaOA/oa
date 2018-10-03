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

import java.lang.reflect.Method;
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
 

    @see OAObjectInfo    
    @author Vince Via
 */
public class OACalcInfo implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(OACalcInfo.class.getName());
    
    String name;
    String[] dependentProperties;  // dependent properties
    private OACalculatedProperty oaCalculatedProperty;
    private Class classType;

    /** 20131027
     *  true if this calcProp is for the whole Hub, and the method has a static method with a Hub param
     */
    boolean bIsForHub;  
    private Method editQueryMethod;

    private String[] viewDependentProperties;
    private String[] userDependentProperties;

    private String enabledProperty;
    private boolean enabledValue;
    private String visibleProperty;
    private boolean visibleValue;

    private String userEnabledProperty;
    private boolean userEnabledValue;
    private String userVisibleProperty;
    private boolean userVisibleValue;
    
    /** 
     Create new Calculated Property.  
     * <pre>
     * Example:  
     *   new CalcInfo("totalCostOfOrder",String { "orderItem.qty", "orderItem.product.cost", "customer.freight", "customer.state.taxRate" } );            
     * </pre>
     * @param name name of calculated property
     * @param props array of depend property paths
     */
    public OACalcInfo(String name, String[] props) {
        this.name = name;
        dependentProperties = props;
    }
    public OACalcInfo(String name, String[] props, boolean bIsForHub) {
        this.name = name;
        dependentProperties = props;
        this.bIsForHub = bIsForHub;
    }

    public Class getClassType() {
        return classType;
    }
    public void setClassType(Class classType) {
        this.classType = classType;
    }
    
    /** get Calculated Property name */
    public String getName() {
        return name;
    }
    /** get property paths of all dependent properties */
    public String[] getDependentProperties() {
        return dependentProperties;
    }
    public void setDependentProperties(String[] props) {
        dependentProperties = props;
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

    public void setViewDependentProperties(String[] ss) {
        this.viewDependentProperties = ss;
    }
    public String[] getViewDependentProperties() {
        return this.viewDependentProperties;
    }

    public void setUserDependentProperties(String[] ss) {
        this.userDependentProperties = ss;
    }
    public String[] getUserDependentProperties() {
        return this.userDependentProperties;
    }
    
    public String getEnabledProperty() {
        return enabledProperty;
    }
    public void setEnabledProperty(String s) {
        enabledProperty = s;
    }
    public boolean getEnabledValue() {
        return enabledValue;
    }
    public void setEnabledValue(boolean b) {
        enabledValue = b;
    }
    public String getVisibleProperty() {
        return visibleProperty;
    }
    public void setVisibleProperty(String s) {
        visibleProperty = s;
    }
    public boolean getVisibleValue() {
        return visibleValue;
    }
    public void setVisibleValue(boolean b) {
        visibleValue = b;
    }

    public String getUserEnabledProperty() {
        return userEnabledProperty;
    }
    public void setUserEnabledProperty(String s) {
        userEnabledProperty = s;
    }
    public boolean getUserEnabledValue() {
        return userEnabledValue;
    }
    public void setUserEnabledValue(boolean b) {
        userEnabledValue = b;
    }
    public String getUserVisibleProperty() {
        return userVisibleProperty;
    }
    public void setUserVisibleProperty(String s) {
        userVisibleProperty = s;
    }
    public boolean getUserVisibleValue() {
        return userVisibleValue;
    }
    public void setUserVisibleValue(boolean b) {
        userVisibleValue = b;
    }

    public void setEditQueryMethod(Method m) {
        this.editQueryMethod = m;
    }
    public Method getEditQueryMethod() {
        return editQueryMethod;
    }

}

