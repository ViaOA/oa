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

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Describes OAObject information. 
 * @author vvia
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAClass {

    String shortName() default "";
    String displayName() default "";
    String description() default "";

    boolean isLookup() default false;
    boolean isPreSelect() default false;
    
    /** flag used to determine if object can be stored to datasource. */
    boolean useDataSource() default true;

    /** flag to know if objects should be added to cache */
    boolean addToCache() default true;
    
    /** if true, then changes are not sent to sever. */
	boolean localOnly() default false;
	
	/** if false, then objects are not initialized on creation */
	boolean initialize() default true;

    String displayProperty() default "";
    String sortProperty() default "";
    //String[] searchProperties() default "";
    String[] viewProperties() default "";
    long estimatedTotal() default 0;
}


/* used by OAObjectInfo
    protected boolean bUseDataSource = true;
    protected boolean bLocalOnly = false;  // dont send to OAServer
    protected boolean bAddToCache = true;  // add object to Cache
    protected boolean bInitializeNewObjects = true;  // initialize object properties (used by OAObject)
    protected String displayName;



*/