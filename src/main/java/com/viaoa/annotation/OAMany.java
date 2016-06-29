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
package com.viaoa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Defines an OAObject relationship that is of type "Many"
 * example: @OAMany (clazz=Emp.class, owner=false, reverse=Emp.P_Dept, cascadeSave=false, cascadeDelete=false)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAMany {
    
    public static final class DEFAULT { // hack for fake class
    }; 
    
    Class toClass() default DEFAULT.class;  // see: OAAnnotationDelegate.getHubObjectClass(..), OAObjectReflectDelegate.getHubObjectClass(..)
    String displayName() default "";
    String description() default "";
    
    /** true if this object is the owner */
    boolean owner() default false;
    
    /** true if this is a recursive relationship. */
    boolean recursive() default false;
    
    /** name used in the toClass that refers to this class. */
    String reverseName() default "";
    
    /** true if saving this class will save the many objects */
    boolean cascadeSave() default false;

    /** true if deleting this class will delete the many objects */
    boolean cascadeDelete() default false;
    
    /** property name used to store the order position for each object in the Hub */
    String seqProperty() default "";
    
    String toolTip() default "";
    String help() default "";
    boolean hasCustomCode() default false;
    int cacheSize() default 0;
    
    /** true if there is a a method for this method. */
    boolean createMethod() default true;
    
    /** path to find another hub to use for autocreating objects in this hub. */
    String matchHub() default "";  
    /** works with matchHub, to know what property should match the objects in the matchHub. */
    String matchProperty() default "";   // property that matchHub will use
    
    /** true if this must be empty (hub.size=0) to delete the other object */
    boolean mustBeEmptyForDelete() default false;
    
    /** true if this is a calculated Hub. */
    boolean isCalculated() default false;

    /** true if calc hub is to be done on server side. */
    boolean isServerSideCalc() default false;
    
    String uniqueProperty() default ""; 
    
    String sortProperty() default "";
    boolean sortAsc() default false;
}

/*  OALinkInfo

    public static final int ONE = 0;
    public static final int MANY = 1;

    String name;
    Class toClass;
    int type;
    boolean cascadeSave;  // save, delete of this object will do same with link hub
    boolean cascadeDelete;  // save, delete of this object will do same with link hub
    // property that needs to be updated in an inserted object.  same as Hub.propertyToMaster
    protected String reverseName;  // reverse property name
    boolean bOwner;  // this object is the owner of relationship
    private boolean bTransient;
    
    // runtime
    protected transient int cacheSize;
    protected OALinkInfo revLinkInfo;


*/