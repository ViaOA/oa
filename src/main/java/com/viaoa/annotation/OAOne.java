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
 * Defines an OAObject relationship that is of type "One"
 * example: @OAOne  (reverse=Dept.P_Emps, required=false, cascadeSave=false, cascadeDelete=false)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAOne {
    String displayName() default "";
    String description() default "";

    /** @return true if this object is the owner of linked to object */
    boolean owner() default false;

    /** @return true if this is a recursive relationship. */
    // 20131013 removed, since Manys are marked as recursive
    // boolean recursive() default false;
    
    /** @return name used in the toClass that refers to this class. */
    String reverseName() default "";

    boolean required() default false;
    boolean verify() default false;
    
    /** @return true if saving this class will save the many objects */
    boolean cascadeSave() default false;
    
    /** @return true if deleting this class will delete the many objects */
    boolean cascadeDelete() default false;

    /** @return if true, then this object is not store in datasource. */
    boolean isTransient() default false;
    
    /** @return if false, then this object can not create, but must pick an existing. */
    boolean allowCreateNew() default true;

    /** @return if true, then this object is auto created. */
    boolean autoCreateNew() default false;
    
    /** @return if false, then an existing object can not be used - a new one must be created. */
    boolean allowAddExisting() default true;
    
    /** @return if true, then this must be empty (null) to delete the other object */
    boolean mustBeEmptyForDelete() default false; 
    
    
    String toolTip() default "";
    String help() default "";
    /** @return flag to know if the code for the metods has been modified. */
    boolean hasCustomCode() default false;
    
    /** @return true if this is a calculated reference. */
    boolean isCalculated() default false;
    
    boolean isImportMatch() default false;
    
    String[] calcDependentProperties() default {};

    /** flag to know if this is processed and will require User.editProcessed=true for it to be changed. */
    boolean isProcessed() default false;
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