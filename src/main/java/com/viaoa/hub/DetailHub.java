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
    A DetailHub is a Hub that automatically contains the object(s) of a property from the active object
    of another Hub (master).  This is referred as a <i>Master/Detail</i> relationship.
    <p>
    Whenever the active object of the master Hub is changed, the Detail Hub will automatically be updated
    to include the objects of a property in the active object.
    <p>
    Example:<br>
    A Department Class has many Employees (using a Hub).  A DetailHub can be created using a Hub of Department objects
    as the master Hub.  This DetailHub will automatically contain the Employee objects for whichever Department is
    currenly the active object in the master Hub.
    <br>
    &nbsp;&nbsp;&nbsp;<img src="doc-files/Hub3.gif" alt="">
    <br>
    Using the diagram, the detail Hub is populated with the Employee objects from the Department that is the
    active object in the master Hub. Actually, the detail Hub is not really populated, but rather it uses
    the same Data that the Dept B Employee Hub is using. If an Employee object is added to the active Department's
    Employee Hub, the Detail Hub would also contain this Employee. 
    <p>
    In this example, a UI Component (ex: JTable) could be setup to list the Department objects and another UI Component
    (ex: JTable) could list the Employee objects from the Department that is selected. If another Department is
    selected, then the JTable listing the Employees will show that Departments Employee objects.
    <pre>
    * Hub hubDept = new Hub(Department.class);   // create new Hub for Department objects
    * hubDept.select();      // select all departments from datasource
    * Hub hubEmp = new HubDetail(hubDept, "Employees"); // create Hub that will automatically
    *                                                   //  contain the Employee objects
    *                                                   //  for the active Department
    * // Or
    * Hub hubEmp = new HubDetail(hubDept,"Employees", "lastName, firstName"); // sets sort order
    *
    </pre>
    @see Hub
*/
public class DetailHub<TYPE> extends Hub<TYPE> {

    /**
        Create a new DetailHub based on a property path from a master Hub.
    */
    public DetailHub(Hub hubMaster, String propertyPath) {
        setMasterHub(hubMaster, null, propertyPath, false, null);
    }

    /**
        Create a new DetailHub based on a property path from a master Hub.
        @param bShareActiveObject if true, then detail Hub uses same active object as
        the property (if it is a Hub) that it is using.
    */
    public DetailHub(Hub hubMaster, String propertyPath, boolean bShareActiveObject) {
        setMasterHub(hubMaster, null, propertyPath, bShareActiveObject, null);
    }

    /**
        Create a new DetailHub based on a property path from a master Hub.
        @param selectOrder if value from property path has not been created/selected, then this
        will be the sort order used when it is selected.
    */
    public DetailHub(Hub hubMaster, String propertyPath, String selectOrder) {
        setMasterHub(hubMaster, null, propertyPath, false, selectOrder);
    }

    /**
        Create a new DetailHub based on a property path from a master Hub.
        @param bShareActiveObject if true, then detail Hub uses same active object as
        @param selectOrder if value from property path has not been created/selected, then this
        will be the sort order used when it is selected.
    */
    public DetailHub(Hub hubMaster, String propertyPath, boolean bShareActiveObject, String selectOrder) {
        setMasterHub(hubMaster, null, propertyPath, bShareActiveObject, selectOrder);
    }

    /**
        Create a new DetailHub based on a reference Class from a master Hub.
        will be the sort order used when it is selected.
    */
    public DetailHub(Hub hubMaster, Class<TYPE> clazz) {
        setMasterHub(hubMaster, clazz, null, false, null);
    }

    /**
        Create a new DetailHub based on a reference Class from a master Hub.
        @param bShareActiveObject if true, then detail Hub uses same active object as
        will be the sort order used when it is selected.
    */
    public DetailHub(Hub hubMaster, Class<TYPE> clazz, boolean bShareActiveObject) {
        setMasterHub(hubMaster, clazz, null, bShareActiveObject, null);
    }

    /**
        Create a new DetailHub based on a reference Class from a master Hub.
        @param selectOrder if value from property path has not been created/selected, then this
        will be the sort order used when it is selected.
    */
    public DetailHub(Hub hubMaster, Class<TYPE> clazz, String selectOrder) {
        setMasterHub(hubMaster, clazz, null, false, selectOrder);
    }

    /**
        Create a new DetailHub based on a reference Class from a master Hub.
        @param bShareActiveObject if true, then detail Hub uses same active object as
        @param selectOrder if value from property path has not been created/selected, then this
        will be the sort order used when it is selected.
    */
    public DetailHub(Hub hubMaster, Class<TYPE> clazz, boolean bShareActiveObject, String selectOrder) {
        setMasterHub(hubMaster, clazz, null, bShareActiveObject, selectOrder);
    }



    /*  Note:Dont need to finalize
        masterHub has a DetailHub that has a weak reference to this Hub, that will be removed when this object "goes away"
    */
}

