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
package com.viaoa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.viaoa.object.OAAnnotationDelegate;
import com.viaoa.object.OALinkInfo;

/*
 * Defines an OAObject relationship that is of type "Many"
 * example: @OAMany (clazz=Emp.class, owner=false, reverse=Emp.PROPERTY_Dept, cascadeSave=false, cascadeDelete=false)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAMany {
    static final class DEFAULT {}; // hack
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