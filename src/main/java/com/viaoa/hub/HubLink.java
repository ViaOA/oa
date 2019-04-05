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
package com.viaoa.hub;

/**
    Used to connect Hubs together based on a reference property.
    A Hub can be linked to a reference property of the active object
    in another Hub.

    <p>
    Types of linking:<br>
    1: link the active object in a Hub to a property in another Hub, where the property type is the
    same Class as the objects in this Hub.<br>
    2: link the <i>position</i> of the active object in a Hub to a property (numeric) in another Hub.<br>
    3: link a property in a Hub to a property in another Hub.<br>
    4: a link that will automatically create a new object in another Hub and set the link property,
    whenever the active object in a Hub is changed.

    <p>
    Examples:<br>
    <pre>
    * // Link department Hub to the department property in a Employee Hub
    * Hub hubDept = new Hub(Department.class);   // create new Hub for Department objects
    * hubDept.select();      // select all departments from datasource
    * Hub hubEmp = new Hub(Employee.class);
    * hubEmp.select();   // select all employees from datasource
    * new HubLink(hubDept, hubEmp, "Department");
    *
    * // Link the position of a value to a property in another Hub
    * Hub hub = new Hub(String.class);
    * hub.add("Yes");
    * hub.add("No");
    * hub.add("Maybe");
    * new HubLink(hub, true, hubEmployee, "retiredStatus");  // values will be set to 0,1, or 2
    *
    * // Link a the property value of active object to a property in the link Hub
    * Hub hub = new Hub(State.class);  // Class that stores information about all 50 states
    * hub.select();   // select all
    * new HubLink(hub, "stateName", hubEmp, "state");  // set the state property to name of state
    *
    * // automatically create an object and set link property when active object is changed
    * Hub hubItem = new Hub(Item.class);
    * Hub hubOrder = new Hub(Order.class);
    * Hub hubOrderItem = new HubDetail(hubOrder, "OrderItems");  // create detail Hub for
    *                                                            // order items
    * new HubLink(hubItem, hubOrderItem, "item", true);  // whenever hubItem's active object is
    *                                       // changed, a new OrderItem object will
    *                                       // be created with a reference to the
    *                                       // selected Item object.
    </pre>

    <p>
    Example:<br>
    &nbsp;&nbsp;&nbsp;<img src="doc-files/Hub4.gif" alt="">
    <br>
    The Hub on the left is a collection of Employee objects. The Hub on the right has Department objects. <br>
    if the (A) active object is changed to "Emp 2", the (B) active object is changed to "Dept B",
    since that is the Dept for "Emp 2".<br>
    If the (C) active object is changed to "Dept C", the (D) Dept for the active object will be set to "Dept C".
    <p>
    This is a common setup when using ComboBoxes. In the above example, a form that displays an Employee would
    have a ComboBox that is a dropdown list of Departments to choose from. When the active object in the
    Employee Hub is changed, the Employees Department is retrieved and used to set the active object in
    the Department Hub - this will then display the correct Department in the ComboBox. When the user selects a
    different Department using the CombBox, the active object in the Department Hub is changed, which automatically
    changes the Department for the Employee.
    <pre>
    Hub hubDepartment = new Hub(Department.class);
    hubDepartment.select();
    Hub hubEmployee = new Hub(Employee.class);
    hubEmployee.select();
    HubLink hl = new HubLink(hubDepartment, hubEmployee, "Department");
    </pre>
    <p>
    Note: HubLink finalize (called during garbage collection) will remove a HubLink.
    @since 2004/03/19 using methods built into Hub.  see {@link Hub#setLink}
*/
public class HubLink {
    private Hub fromHub, toHub;
    private String fromProperty, toProperty;
    private boolean bUseHubPosition;
    private boolean bAutoCreate;

    /**
        Link a Hub to the active object of a property of the same Class in another Hub.
    */
    public HubLink(Hub fromHub, Hub toHub, String toProperty) {
        if (fromHub != null && toHub != null) fromHub.setLinkHub(toHub, toProperty);
        this.fromHub = fromHub;
        this.toHub = toHub;
        this.toProperty = toProperty;
    }

    /**
        Link the position of the active object in a Hub to a numeric property of the active object in another Hub.
        @param bUseHubPosition if true, then use the position of the object in the fromHub.
    */
    public HubLink(Hub fromHub, boolean bUseHubPosition, Hub toHub, String toProperty) {
        if (fromHub != null && toHub != null) fromHub.setLinkHub(toHub, toProperty);
        this.fromHub = fromHub;
        this.bUseHubPosition = bUseHubPosition;
        this.toProperty = toProperty;
    }

    /**
        Link the value of a property of the active object in a Hub to a property of the active object in another Hub.
        @param fromProperty property in fromHub to use.
        @param toProperty property in toHub to use.
    */
    public HubLink(Hub fromHub, String fromProperty, Hub toHub, String toProperty) {
        if (fromHub != null && toHub != null) fromHub.setLinkHub(toHub);
        this.fromHub = fromHub;
        this.toHub = toHub;
        this.fromProperty = fromProperty;
        this.toProperty = toProperty;
    }
    
    /**
        Link the active object in a Hub to a property of the active object in another Hub.
    */
    public HubLink(Hub fromHub, Hub toHub) {
        if (fromHub != null && toHub != null) fromHub.setLinkHub(toHub);
        this.fromHub = fromHub;
        this.toHub = toHub;
    }

    /**
        Used to automatically create a new Object in "to" Hub whenever the
        active object in "from" Hub is changed.
        @param bAutoCreate if true then a new object will be created and added to linkHub.
        @param toProperty is name of property in "to" Hub that will be set.
    */
    public HubLink(Hub fromHub, Hub toHub, String toProperty, boolean bAutoCreate) {
        if (fromHub != null && toHub != null) {
        	fromHub.setLinkHub(toHub, toProperty, bAutoCreate);
        }
        this.fromHub = fromHub;
        this.toHub = toHub;
        this.toProperty = toProperty;
        this.bAutoCreate = bAutoCreate;
    }

    /**
        Removes HubLink from toHub.
    */
    protected void finalize() throws Throwable {
        if (fromHub != null) fromHub.removeLinkHub();
        super.finalize();
    }
    
    /**
        Returns Hub that is linked to another Hub.
    */
    public Hub getFromHub() {
        return fromHub;
    }
    
    /**
        Returns Hub that is updated by toHub.
    */
    public Hub getToHub() {
        return toHub;
    }

    /**
        Returns the property name in fromHub that is linked to the active object of a property in the toHub.
        If null, then the actual object in the fromHub is used.
    */
    public String getFromProperty() {
        return fromProperty;
    }

    /**
        Returns property in toHub that is automatically update by linkHub.
    */
    public String getToProperty() {
        return toProperty;
    }

    /**
        Returns true if the position of the active object in the fromHub is used to update the
        property in the toHub.
    */
    public boolean getUseHubPosition() {
        return bUseHubPosition;
    }

    /**
        Returns true if a new object is created and added to the toHub when the active object in 
        the fromHub is changed.
    */
    public boolean getAutoCreate() {
        return bAutoCreate;
    }
}

