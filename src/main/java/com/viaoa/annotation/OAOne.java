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

/*
 * Defines an OAObject relationship that is of type "One"
 * example: @OAOne  (reverse=Dept.P_Emps, required=false, cascadeSave=false, cascadeDelete=false)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAOne {
    String displayName() default "";
    String description() default "";

    /** true if this object is the owner */
    boolean owner() default false;

    /** true if this is a recursive relationship. */
    // 20131013 removed, since Manys are marked as recursive
    // boolean recursive() default false;
    
    /** name used in the toClass that refers to this class. */
    String reverseName() default "";

    boolean required() default false;
    boolean verify() default false;
    
    /** true if saving this class will save the many objects */
    boolean cascadeSave() default false;
    
    /** true if deleting this class will delete the many objects */
    boolean cascadeDelete() default false;

    /** if true, then this object is not store in datasource. */
    boolean isTransient() default false;
    
    /** if false, then this object can not create, but must pick an existing. */
    boolean allowCreateNew() default true;

    /** if true, then this object is auto created. */
    boolean autoCreateNew() default false;
    
    /** if false, then an existing object can not be used - a new one must be created. */
    boolean allowAddExisting() default true;
    
    /** if true, then this must be empty (null) to delete the other object */
    boolean mustBeEmptyForDelete() default false; 
    
    
    String toolTip() default "";
    String help() default "";
    /** flag to know if the code for the metods has been modified. */
    boolean hasCustomCode() default false;
    
    /** true if this is a calculated reference. */
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