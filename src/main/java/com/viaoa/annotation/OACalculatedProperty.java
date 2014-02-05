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

/**
 * Used to describe OAObject calculated properties, and dependent information.
 *  
 * @author vvia
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OACalculatedProperty {
    String displayName() default "";
    String description() default "";

    String outputFormat() default "";
    String[] properties() default {};

    int displayLength() default 0;
    int columnLength() default 0;
    int decimalPlaces() default 0;
    
    boolean isEmail() default false;
    boolean isUrl() default false;
    boolean isImageName() default false;
    boolean isIconName() default false;
    boolean isXml() default false;
    boolean isFileName() default false;
    boolean isAutoSeq() default false;
    boolean isTimestamp() default false;
    boolean isCaseSensitive() default false;
    boolean isPhone() default false;
    boolean isZipCode() default false;
    boolean isCurrency() default false;

    String columnName() default "";
    String toolTip() default "";
    String help() default "";
}
