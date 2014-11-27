/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.object;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.lang.ref.*;  // java1.2

import com.viaoa.hub.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;


/**
    OAObject is the Base Class used for Application Data Objects.  It is a central class for OA, where all other objects are
    designed to automatically work with the OAObject class, along with the Hub collection class.
    <p>
    OAObjects have built-in functionality to allow it to work with other Classes.  This includes other OAObjects,
    Hub Collections, any datasource/database, JFC component, JSP component, XML, other applications (distributed) and any other Class.
    <p>
    &nbsp;&nbsp;&nbsp;<img src="doc-files/ObjectAutomation1.gif">
    <br>
    Subclasses of OAObject can be created that add properties and methods for building customized software applications.  
    OAObject then supplies the capability for these subclasses to automatically work with any OA Enabled Class.

    <p>
    This is a summary of some of the features included in OAObject.
    <ul>
    <li>Object Key - property values that makes this object unique.
    <li>Reference Information - how objects are related to other object.  All references use the actual objects and not the key (or foreign key value).  
    		References types include one-one, one-many, many-many, recursive self references, owned and un-owned references, and more.
    <li>Manages reference objects when working with database/datasource.
    <li>"Moves" objects when changes are made to a reference property.
    <li>Methods to set and get properties and convert from and to Strings.
    <li>Store miscellaneous data in name/value pairs, where name is case insensitive.
    <li>Initialization during creation
    <li>Null Values - to know if a primitive property value is null
    <li>Knows which Hub Collections that an object is a member of.
    <li>Handles events for object, including property changes and calculated properties.
    <li>Knows if object is "new"
    <li>Cascading rules.  Cancel, Save, Delete can be cascaded to reference objects.
    <li>Works directly with OADataSource for storing and retrieving objects.
    <li>Save Method
    <li>Delete Method
    <li>Calculated Properties - properties that rely on other properties or objects for their value.
    <li>Serialization Support - to file/stream, other applications using RMI
    <li>XML support - reading and writing
    <li>Locking
    <li>Client/Server - changes to objects can be automatically updated on other computers.
    </ul>
    <p>

    This is a listing of the types of relationships that an OAObject can have with another OAObject.  This information
    is built into the object information. Relationships between objects are "two-way", meaning that both
    objects are related to each other.<br>
    <ul>
    <li>One-One relationship
    <li>One-Many relationship
    <li>Many-Many relationship
    <li>Recursive - this is where an object can have many children objects of the same class and each of these children can themselves have children, recursively.
    <li>An Owned relationship is one where the children can not exist without the parent (owner) and all are treated as a single unit.
    <li>Cascading Rules for save, delete, cancel
    </ul>
    <p>
    Managing Relationships<br>
    OAObject manages the relationships between objects, and is responsible for retrieving and populating reference
    objects and for managing changes. An OAObject subclass does not have to have any code to handle retrieving or
    storing reference objects, OAObject does it completely. If a reference property is changed, then OAObject manages
    the change so that other objects are updated correctly.
    <br>
    For example, if a Department has many Employees, and an Employee has one Department: if an Employee's Department
    is changed, then the Employee object is removed from the original Department collection and added to the new
    assigned Department collection. This also works when an Employee is added to a different Departments Employee
    collection - the Employee's Department property is changed to the newly assigned Department.
    <p>

    Working with DataSources<br>
    OAObjects work directly with OADataSource for initializing properties, saving, deleting.  This is all
    done so that the OAObjects are independent from datasource/database.
	<p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.

    @author Vince Via
    @see Hub for observable collection class that has "linkage" features for automatically managing relationships.
    @see OASelect for datasource independent queries based on object and property paths.
*/
public class OAObject implements java.io.Serializable, Comparable {
    
    private static final long serialVersionUID = 1L; // internally used by Java Serialization to identify this version of OAObject.

    
    public static final int version = 1411260;  
    static {
        Properties props = System.getProperties();
        
        System.out.println("OA version 3.5.10-SNAPSHOT_" + version + " ViaOA, all rights reserved");
        System.out.println(String.format("Java version=%s, name=%s", 
                System.getProperty("java.version"), 
                System.getProperty("java.vm.name")
        ));
    }
    
    
    // system wide to track all changes to OAObject
    public static final Logger OALOG = OALogger.getLogger("OAObject");
    
	private static final Logger LOG = OALogger.getLogger(OAObject.class);
    
    protected int         guid;          // global identifier for this object
    protected OAObjectKey objectKey;     // Object identifier, used by Hub/HubController for hashing, etc.
    protected boolean 	  changedFlag;   // flag to know if this object has been changed
    protected volatile boolean newFlag=true;  // flag to know if this object is new (not yet saved).  The object key properties can be changed as long as isNew is true.
    protected byte[]      nulls;         // keeps track of which primitive type properties that are NULL. Uses bit position, based on OAObjectInfo getPrimitiveProperties() position
    protected boolean     deletedFlag; 
//    protected transient WeakReference<Hub<?>>[] weakHubs;       // list of Hub Collections that this object is a member of.  OAObject uses these Hubs for sending events.  See: OAObjectHubDelegate
    
    // list of Hub Collections that this object is a member of.  
    // OAObject uses these Hubs for sending events.  See: OAObjectHubDelegate
    // elements will be one of the following: 
    //   Hub - if a reference to object needs to be maintained, so that it wont be GCd and can be saved
    //   null - empty slot
    //   WeakReference<Hub> (default) - so that it does not hold the Hub from being GCd
    protected transient WeakReference<Hub<?>>[] weakhubs;   
    
    // 20120827 flags per many(hub) reference, to know if the size is 0.
    //   uses bitwise operation to flag hub referencs that are empty (size=0) with a '1'
    //   see: OAObjectHubDelegate
    // fyi: this is a DataSource performance helper, and can be set to 0 at anytime with no affect on data integrity
    // protected int hubEmptyFlags;  
    
    /** 
     Link/reference properties that have been loaded.  Stores uppercase name of property.  
	 Possible values:
	   ONE:  OAObjectKey (by calling setProperty(), the value used will be converted to an OAObjectKey
			 OAObject for the value of the reference
	   MANY: WeakReference to Hub.  The objects in the Hub can be OAObjectKey values that will automatically
	   	                            be retrieved and converted to the correct class of object.
    */
    
    /** managed by OAObjectPropertyDelegate.java */
    protected volatile transient Object[] properties;  // stores references (oaobj, hub, oaobjkey), or misc property for object.  ex: [0]="Employee" [1]=Emp [2]="Order" [3]=oakey
    
    /** Cascade rule where no reference objects will be included. */
    public static final int CASCADE_NONE = 0;

    /** Cascade rule where all defined rules for references will be included.  This is default for save() and delete(). */
    public static final int CASCADE_LINK_RULES = 1;

    /** Cascade rule where all only the owned references will be included. */
    public static final int CASCADE_OWNED_LINKS = 2;

    /** Cascade rule where all reference objects are followed, even if cascade rule is false. */
    public static final int CASCADE_ALL_LINKS = 4;
    

    public static volatile int cntNew;  
    public static volatile int cntFinal;  
    /**
      	Creates new OAObject and calls OAObjectDelegate.initialize()
        @see OAObjectDelegate#initialize
    */
    public OAObject() {
        OAObjectDelegate.initialize(this);

        cntNew++;
    	if (cntNew % 500 == 0) {
    	    System.out.println(cntNew+") new OAObject.guid="+guid+" "+this);
    	}

        // 20141127 Note: call oaObject.toString(), until the object is loaded, since it will create an objectKey with Id=0
    	if (objectKey != null) objectKey = null; // in case it was generated before the Id was loaded.
    	
        /*qqqqqqqqqqqqqqqqqqqqqqqqqqq    	
        Exception ex = new Exception();
        for (StackTraceElement ste : ex.getStackTrace()) {
            stackTrace += ste.getFileName()+"."+ste.getMethodName()+"("+ste.getLineNumber()+")\n";
        }
        stackTrace = ((new OADateTime()).toString())+"\n"+stackTrace;
        */
    }
    //String stackTrace="";
    
    /** Read OAObject data.  Note: This method must stay "private" or it will never be called.  It does
        not need to be subclassed because any object that is a subclass should have its own readObject()
        method.  ObjectInputStream.readObject() calls the readObject() for each class, superClass,
        and subClass individually.
    */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	OAObjectSerializeDelegate._readObject(this, in);
    }
    /** 
     * This is called by serialization to check if object already exists in the Cache.
     * @see OAObjectSerializeDelegate#_readResolve
     */
    protected Object readResolve() throws ObjectStreamException {
    	Object obj = OAObjectSerializeDelegate._readResolve(this);
    	return obj;
    }
    /**
     *  Used to serialize and object.  
     *  @see OAObjectSerializeDelegate#_writeObject to see how objects can be custom written by selecting the properties that will be sent 
     *  in the object graph.
     */
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		OAObjectSerializeDelegate._writeObject(this, stream);
	}

    /** calls setProperty()
	    @see #setProperty(String, Object, String)
	*/
	public void setProperty(String propName, boolean value) {
		OAObjectReflectDelegate.setProperty(this, propName,value?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE,null);
	}
    /** calls setProperty()
        @see #setProperty(String, Object, String)
    */
    public void setProperty(String propName, int value) {
    	OAObjectReflectDelegate.setProperty(this, propName,new Integer(value),null);
    }
    /** calls setProperty()
        @see #setProperty(String, Object, String)
    */
    public void setProperty(String propName, long value) {
    	OAObjectReflectDelegate.setProperty(this, propName,new Long(value),null);
    }
    /** calls setProperty()
        @see #setProperty(String, Object, String)
    */
    public void setProperty(String propName, double value) {
    	OAObjectReflectDelegate.setProperty(this, propName, new Double(value),null);
    }
    /** calls setProperty()
        @see #setProperty(String, Object, String)
	*/
	public void setProperty(String propName, Object value) {
    	OAObjectReflectDelegate.setProperty(this, propName, value, null);
	}

	public void setNull(String propName) {
        OAObjectReflectDelegate.setProperty(this, propName, null, null);
	}

    /**
        Generic way for setting any property and storing name/value pairs.<br>
        If propertyName is a valid property, then the setX method will be called, where X is the name of property.<br>
        If propertyName is a valid property and obj is a String, obj will be converted to the correct obj that
        the method needs.  Ex: if property method is for an OADate and obj = "05/04/65", then obj will be converted
        to a date.<br>
        If propertyName is a valid property that is an OAObject reference, then obj can be the ObjectKey value instead of the
        object itself.<br>
        if value is a String and property is a reference property, then value will be converted to match objectId type.<br>
        <br>
        Ex: if employee.setProperty("Dept", 12), where Dept is a reference object, 12 will be used to find correct Dept obj<br>
        Ex: if employee.setProperty("Dept", "12"), "12" will be converted to int 12<br>
        Ex: employee.setSalary("32,500"), where salary property is an int.  "32,500" will be converted to (int) 32500<br>
        Ex: employee.setHireDate("05/24/1988"), where HireDate property is an OADate and will be converted<br>
        <br>
        <ol>If property is a Hub, then the following is done:
        <li> if hub has already loaded by calling getHub(propName), then value is converted to object and then
           added into Hub.
        <li> if hub has not been loaded by calling getHub(propName), but has been created:<br>
           a: if value is an OAObject, then getHub(propName) is called and value is added to Hub.<br>
           b: Hub for class OAObjectKey is created and value is converted to OAObjectKey and then added.  When
              getHub(propName) is called, then all of the ObjectKeys will be converted to objects.
        </ol>
        @see OAObjectReflectDelegate#setProperty
    */
    public void setProperty(String propName, Object value, String fmt) {
    	OAObjectReflectDelegate.setProperty(this, propName, value, fmt);
    }

    /**
	    Generic way for getting any property or value from a name/value pair.
	    This will first look for get"PropName" method in this object (including superclass OAObject)
	    Note: this supports property paths.   For example: "dept.manager.lastname" from an Employee.class
	    Note: if the property is of a primitive type, it can return null.
	    @param propName can be a property path.  If a Hub property is in the path and is not the last property, then the ActiveObject will be used.
     */
    public Object getProperty(String propName) {
    	return OAObjectReflectDelegate.getProperty(this, propName);
    }

    /**
        Generic way for getting any property or value as a String value.
    */
    public String getPropertyAsString(String propName) {
        return getPropertyAsString(propName, null);
    }

    public String getPropertyAsString(String propName, boolean bUseDefaultFormatting) {
        Object obj = getProperty(propName);
        return OAConverter.toString(obj, bUseDefaultFormatting);
    }

    /**
        Generic way for getting any property or value as a String value.
        @return if value is null then "", else formatted value using OAConverter.toString(value,fmt)
    */
    public String getPropertyAsString(String propName, String fmt) {
        Object obj = getProperty(propName);
        if (obj == null) return ""; // note: if null is sent to OAConvert.toString(...), it wont know the correct class to use to - since obj=null
        return OAConverter.toString(obj, fmt);
    }

    /**
	    Generic way for getting any property or value as a String value.
	    @return if value is null then nullValue, else formatted value using OAConverter.toString(value,fmt)
	*/
	public String getPropertyAsString(String propName, String fmt, String nullValue) {
	    Object obj = getProperty(propName);
	    if (obj == null) return nullValue; 
	    return OAConverter.toString(obj, fmt);
	}
    
    /** removing property.  If this property caused isChanged() to be true, then isChanged() will be false.
        @param name of property to remove.  (case insensitive)
    */
    public void removeProperty(String name) {
        OAObjectPropertyDelegate.removeProperty(this, name, true);
    }

    /**
        Flag to know if object is new and has not been saved.
    */
    public boolean getNew() {
        return newFlag;
    }
    public boolean isNew() {
        return newFlag;
    }
    public void setNew(boolean b) {
        OAObjectDelegate.setNew(this, b);
    }

    /**
        Flag to know if object was deleted.
    */
    public boolean getDeleted() {
        return deletedFlag;
    }
    public boolean wasDeleted() {
        return deletedFlag;
    }
    public boolean isDeleted() {
        return deletedFlag;
    }
    public void setDeleted(boolean tf) {
        OAObjectDeleteDelegate.setDeleted(this, tf);
    }
    
    
    /** OAObjects are equal if:
        <ul>
        <li> the objects are the same address.
        <li> the objects are the same class and the values of the propertyIds are equal.
           If both objects isNew() and either one has a its propertyId.isNull(), then they will never be equal.
        <li> if the object being compared to is equal to the objectId property of this object.
        @param key object to compare to, object or objects[] to compare this object's objectId(s) with or OAObjectKey to compare with this object's objectId
        @see OAObject#getPropertyIdValues
        @see OAObjectKey#OAObjectKey
     */
    public final boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;

        //20141125 if obj is oaObj, then need to make sure that they are same class 
        if (obj instanceof OAObject) {
            if (!obj.getClass().equals(this.getClass())) return false;
        }
        
        return OAObjectKeyDelegate.getKey(this).equals(obj);
    }

    //20140128 add hashCode
    @Override
    public int hashCode() {
        return OAObjectKeyDelegate.getKey(this).hashCode();
    }

    public int compareTo(Object obj) {
        if (obj == null) return 1;
        if (obj == this) return 0;
        if (!obj.getClass().equals(this.getClass())) return -1;
        return OAObjectKeyDelegate.getKey(this).compareTo(obj);
	}
    
    
    /**
        Returns true if this object is new or any changes have been made to this object or
        any objects in Links that are CASCADE=true
    */
    public boolean getChanged() {
    	return getChanged(CASCADE_NONE);
    }
    public boolean isChanged() {
        return getChanged(CASCADE_NONE);
    }

    
    public boolean getChanged(boolean bIncludeLinks) {
        return getChanged(bIncludeLinks?CASCADE_LINK_RULES:CASCADE_NONE);
    }
    public boolean isChanged(boolean bIncludeLinks) {
        return getChanged(bIncludeLinks?CASCADE_LINK_RULES:CASCADE_NONE);
    }
    
    
    /**
        Returns true if this object is new or any changes have been made to this object or
        any objects in Links that are TYPE=MANY and CASCADE=true that match the relationshipType parameter.
    */
    public boolean getChanged(int relationshipType) {
    	return OAObjectDelegate.getChanged(this, relationshipType);
    }

    /** Flag to know if object has been changed.
        <p>
        This is automatically set to true whenever firePropertyChange.  It is set to false when save() is called.
        @param tf if false then all original values of changed properties will be removed.
    */
    public void setChanged(boolean tf) {
        if (changedFlag != tf) {
            boolean bOld = changedFlag;
            changedFlag = tf;
        	OAObjectEventDelegate.firePropertyChange(this, OAObjectDelegate.WORD_Changed, bOld?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE, changedFlag?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE, false, false);

            // 20141030
            if (changedFlag && isServer()) {
                Hub[] hubs = OAObjectHubDelegate.getHubReferences(this);
                if (hubs != null) {
                    for (Hub h : hubs) {
                        HubDelegate.setReferenceable(h, true);
                    }
                }
            }
        }
    }

    
    /** Copies the properties and some of the links from a source object (this) to a new object. 
        For links of type One, all of the links are used, the same ref object from the source object is used.
        For links of type Many, only the owned links are used, and clones of the objects are created in the Hub of the new object.
        Note: this is done on the server.
        @see OAObjectReflectDelegate#copyInto(OAObject, OAObject, String[], OACopyCallback)
    */
    public OAObject createCopy() {
        return OAObjectReflectDelegate.createCopy(this, null);
    }
    public Object createCopy(String[] excludePropertyNames) {
        return OAObjectReflectDelegate.createCopy(this, excludePropertyNames);
    }


    /** 
        @see OAObjectReflectDelegate#copyInto(OAObject, OAObject, String[], OACopyCallback)
    */
    public void copyInto(OAObject toObject) {
        OAObjectReflectDelegate.copyInto(this, toObject, (String[])null, null);
    }
    
    /**
     * Option to have finalized objects automatically saved to datasource.  Default is false.
     * @param b
     */
    public static void setFinalizeSave(boolean b) {
    	OAObjectDelegate.bFinalizeSave = b;
    }
    public static boolean getFinalizeSave() {
    	return OAObjectDelegate.bFinalizeSave;
    }
    
    /**
        Removes object from HubController and calls super.finalize().
    */
    protected void finalize() throws Throwable {
    	OAObjectDelegate.finalizeObject(this);
        super.finalize();
        cntFinal++;
        if (cntFinal % 500 == 0) System.out.println(cntFinal+") finalize OAObject.guid="+guid+" "+this);
    }

    /**
	    True if this object is in process of being loaded.
        @see OAObjectDelegate#isLoading
     */
    public boolean isLoading() {
    	return OAThreadLocalDelegate.isLoadingObject();
    }

    /**
     * @return true if the current thread is from the OAClient.getMessage().
     */
    public boolean isClientThread() {
        return OAObjectCSDelegate.isRemoteThread(); 
    }
    
    /**
        Used to manage property changes.
        Sends a "beforePropertyChange()" to all listeners of the Hubs that this object is a member of.  <br>
        The original value is saved and can be retreived by calling getOriginalPropertyValue or canel.

        @param saveChanges save original value
        @param property is not case sensitive
        @see #cancel
        @see #getOriginalPropertyValue
    */
    protected void fireBeforePropertyChange(String propertyName, Object oldObj, Object newObj, boolean bLocalOnly) {
    	OAObjectEventDelegate.fireBeforePropertyChange(this, propertyName, oldObj, newObj, bLocalOnly, true);
    }
    protected void fireBeforePropertyChange(String propertyName, Object oldObj, Object newObj) {
    	OAObjectEventDelegate.fireBeforePropertyChange(this, propertyName, oldObj, newObj, false, true);
    }

    /** @see #fireBeforePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange */
    protected void fireBeforePropertyChange(String property, boolean oldObj, boolean newObj) {
        fireBeforePropertyChange( property, oldObj?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE,  newObj?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE);
    }

    /** @see #fireBeforePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange */
    protected void fireBeforePropertyChange(String property, int oldObj, int newObj) {
        fireBeforePropertyChange( property, new Integer(oldObj),  new Integer(newObj));
    }

    /** @see #fireBeforePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange */
    protected void fireBeforePropertyChange(String property, long oldObj, long newObj) {
        fireBeforePropertyChange( property, new Long(oldObj),  new Long(newObj));
    }

    /** @see #fireBeforePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange */
    protected void fireBeforePropertyChange(String property, double oldObj, double newObj) {
        fireBeforePropertyChange( property, new Double(oldObj),  new Double(newObj));
    }

    
    protected void firePropertyChange(String propertyName, Object oldObj, Object newObj, boolean bLocalOnly) {
    	OAObjectEventDelegate.firePropertyChange(this, propertyName, oldObj, newObj, bLocalOnly, true);
    }
    protected void firePropertyChange(String propertyName, Object oldObj, Object newObj) {
    	OAObjectEventDelegate.firePropertyChange(this, propertyName, oldObj, newObj, false, true);
    }

    /** @see #firePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange */
    protected void firePropertyChange(String property, boolean oldObj, boolean newObj) {
        firePropertyChange( property, oldObj?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE,  newObj?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE);
    }

    /** @see #firePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange */
    protected void firePropertyChange(String property, int oldObj, int newObj) {
        firePropertyChange( property, new Integer(oldObj),  new Integer(newObj));
    }

    /** @see #firePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange */
    protected void firePropertyChange(String property, long oldObj, long newObj) {
        firePropertyChange( property, new Long(oldObj),  new Long(newObj));
    }

    /** @see #firePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange */
    protected void firePropertyChange(String property, double oldObj, double newObj) {
        firePropertyChange( property, new Double(oldObj),  new Double(newObj));
    }
    
    
    /**
        Version of firePropertyChange that will not send to OAServer.
        @see #firePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange
    */
    protected void fireLocalPropertyChange(String property, Object oldObj, Object newObj) {
    	firePropertyChange(property,oldObj,newObj,true);
    }
    /**
        Version of firePropertyChange that will not send to OAServer.
        @see #firePropertyChange(String, Object, Object, boolean, boolean) firePropertyChange
    */
    protected void fireLocalPropertyChange(String property, int oldObj, int newObj) {
    	firePropertyChange( property, new Integer(oldObj),  new Integer(newObj),true);
    }
    
    /**
        Retreives reference property that is for a Hub Collection.
        @see #getHub(String, String)
    */
    protected Hub getHub(String linkPropertyName) {
    	return OAObjectReflectDelegate.getReferenceHub(this, linkPropertyName, null, false, null);
    }

    // 20130728
    public void setHub(String linkPropertyName, Hub hub) {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(this);
        OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, linkPropertyName);
        OALinkInfo liReverse;
        if (linkInfo != null) liReverse = OAObjectInfoDelegate.getReverseLinkInfo(linkInfo);
        else liReverse = null;
        HubDetailDelegate.setMasterObject(hub, this, liReverse);
        
        if (OAObjectInfoDelegate.cacheHub(linkInfo, hub)) {
            OAObjectPropertyDelegate.setProperty(this, linkPropertyName, new WeakReference(hub));
        }
        else {
            OAObjectPropertyDelegate.setProperty(this, linkPropertyName, hub);
        }
    }


    /**
        DataSource independent method to retrieve a reference property that is a Hub Collection.
        @param linkPropertyName name of property to retrieve. (case insensitive)
        @param sortOrder
        @param bStore if true (default), then reference object/Hub is stored internally.
        <p>
        If Hub is not already loaded then, hub is created by:</br>
        Hub h = new Hub(linkClass, this, linkPropertyName);<br>
        h.setSelectOrder(sortOrder);<br>
        h.executeSelectLater();<br>
    */
    protected Hub getHub(String linkPropertyName, String sortOrder) {
    	return OAObjectReflectDelegate.getReferenceHub(this, linkPropertyName, sortOrder, false, null);
    }

    /**
     * @param bSequence if true, then a setAutoSequence will be called on the hub
     */
    protected Hub getHub(String linkPropertyName, String sortOrder, boolean bSequence) {
        return OAObjectReflectDelegate.getReferenceHub(this, linkPropertyName, sortOrder, bSequence, null);
    }
    
    /**
     * @param bSequence if true, then a setAutoSequence will be called on the hub
     */
    protected Hub getHub(String linkPropertyName, String sortOrder, boolean bSequence, Hub hubMatch) {
        return OAObjectReflectDelegate.getReferenceHub(this, linkPropertyName, sortOrder, bSequence, null);
    }
    protected Hub getHub(String linkPropertyName, String sortOrder, Hub hubMatch) {
        return OAObjectReflectDelegate.getReferenceHub(this, linkPropertyName, sortOrder, false, hubMatch);
    }
    protected Hub getHub(String linkPropertyName, Hub hubMatch) {
        return OAObjectReflectDelegate.getReferenceHub(this, linkPropertyName, null, false, hubMatch);
    }
    
    /**
        DataSource independent method to retrieve a reference property.
        <p>
        If reference object is not already loaded, then OADataSource will be used to retreive object.
    */
    protected Object getObject(String linkPropertyName) {
    	Object obj = OAObjectReflectDelegate.getReferenceObject(this, linkPropertyName);
    	return obj;
    }

    /**
        DataSource independent method to retrieve a blob/byte[] property.
        <p>
        If reference object is not already loaded, then OADataSource will be used to retreive object.
    */
    protected byte[] getBlob(String linkPropertyName) {
        return OAObjectReflectDelegate.getReferenceBlob(this, linkPropertyName);
    }
    
    /** This is used to save object to OADataSource and flag object as
        being saved, removing all changes.
        <p>
        It does the following:
        <ol>
        <li> calls canSave() to verify that object and its LinkInfo objects
           that TYPE=MANY and CASCADE=true can be saved.  This will call this objects
           beforeSave() method.  It will then call hubBeforeSave() for all listeners .
        <li> calls this Objects "onSave()" method.  The default onSave() will call OADataSource
           to save.
        <li> calls listeners hubAfterSave()
        <li> calls "cascadeSave()" to save all Links with TYPE=ONE and CASCADE=true
        </ol>
        @see #isChanged
        @see Hub#saved
    */
    public void save() {
    	this.save(CASCADE_LINK_RULES);
    }

    /**
        @param iCascadeRule OR combination of CASCADE, ALL, FORCE, NOCHECK
        @see #save()
        @see Hub#saved
    */
    public void save(int iCascadeRule) {
    	OAObjectSaveDelegate.save(this, iCascadeRule);  // this will save on server if using OAClient
    }
    /**
     * Cascade save all links.
     */
    public void saveAll() {
        OAObjectSaveDelegate.save(this, OAObject.CASCADE_ALL_LINKS); 
    }
    /**
     * Method that can be overwritten to "know" when it is saved.
     * Note: overwritting the save() method will not always work,
     * since the object could be saved during a cascade save that
     * started from another objects call to delete.
     */
    public void saved() {
    }

    
    
    /** Remove this object from all hubs and deletes object from OADataSource.
        @see Hub#deleted
    */
    public void delete() {
    	OAObjectDeleteDelegate.delete(this);
    }
    /**
     * Method that can be overwritten to "know" when it is deleted.
     * Note: overwritting the delete() method will not always work,
     * since the object could be deleted during a cascade delete that
     * started from another objects call to delete.
     */
    public void deleted() {
    }



    /** Creates a lock on this object.
        @see OALock#lock(Object,Object,Object)
    */
    public void lock() {
        OAObjectLockDelegate.lock(this);
    }

    /** Unlocks this object.
        @see OALock#unlock(Object)
    */
    public void unlock() {
    	OAObjectLockDelegate.unlock(this);
    }

    /** Checks to see if object is locked.
        @see OALock#isLocked(Object)
    */
    public boolean isLocked() {
        return OAObjectLockDelegate.isLocked(this);
    }

    /**
        Using a propertyPath from this object, find the first matching object.<p>
        Example: find a SectionItem from a SectionItem<br>
        SectionItem si = (SectionItem) secItem.find("section.templateRow.template.templateRows.sections.sectionItems.item", item);
        @see #findAll(String,Object)
    */
    public Object find(String propertyPath, Object value) {
        Object[] objs = OAObjectDelegate.find(this, propertyPath, value, false);
        if (objs != null && objs.length > 0) return objs[0];
        return null;
    }

    /**
        Using a propertyPath from this object, find all of the matching objects.<p>
        Example: find a SectionItem from a SectionItem<br>
        SectionItem si = (SectionItem) secItem.find("section.templateRow.template.templateRows.sections.sectionItems.item", item);
        @see OAObject#find(String,Object)
    */
    public Object[] findAll(String propertyPath, Object value) {
        return OAObjectDelegate.find(this, propertyPath, value, true);
    }

//qqqqqqqq needs to work for all properties, not just primitives ??
    public boolean isNull(String prop) {
        return OAObjectReflectDelegate.getPrimitiveNull(this, prop);
    }
    

    /**
     * This is used so that code will only be ran on the server.
     * If the current thread is an OAClientThread, it will still send messages to other clients.
     */
    public static boolean isServer() {
        return OASyncDelegate.isServer();
    }
    public static boolean isRemoteThread() {
        return OARemoteThreadDelegate.isRemoteThread();
    }
    
   
    /**
     *  Called after an object has been loaded from a datasource.
     */
    public void afterLoad() {
        OAObjectEmptyHubDelegate.initialize(this);
        OAObjectEventDelegate.fireAfterLoadEvent(this);
    }
    
    public OAObjectKey getObjectKey() {
        return OAObjectKeyDelegate.getKey(this);
    }
    
    // 20130630
    /**
     * Used to determine if an object should be added to a reference/master hub when one
     * of it's OAObject properties is set.  If false, then the object will not be added to
     * masterHubs until this is called with "true" or when oaObj is saved.
     * @param bEnabled (default is true)
     */
    public void setAutoAdd(boolean b) {
        OAObjectDelegate.setAutoAdd(this, b);
    }
    public boolean getAutoAdd() {
        return OAObjectDelegate.getAutoAdd(this);
    }
    
    public boolean isEmpty(Object obj) {
        return OAString.isEmpty(obj);
    }
    
    public boolean isHubLoaded(String name) {
        Object objx = OAObjectPropertyDelegate.getProperty(this, name, true, true);
        if (objx == OANotExist.instance) return false;
        if (objx == null) return true;
        if (objx instanceof WeakReference) {
            if ( ((WeakReference) objx).get() == null) return false;
        }
        return true;
    }
}

