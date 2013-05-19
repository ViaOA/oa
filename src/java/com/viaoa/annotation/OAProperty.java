package com.viaoa.annotation;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines the properties in an OAObject class. 
 * @author vvia
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAProperty {
    String displayName() default "";

    String description() default "";

    String defaultValue() default "";
    boolean required() default false;

    int maxLength() default 0;
    int minLength() default 0;
    int decimalPlaces() default -1;
    int displayLength() default 0;
    
    /** length of the column in a table/grid UI component. */
    int columnLength() default 0;
    
    String inputMask() default "";
    String outputFormat() default "";
    
    boolean verify() default false;
    String validCharacters() default "";
    String invalidCharacters() default "";

    /** column name used for table/grid UI component */
    String columnName() default "";
    String toolTip() default "";
    String help() default "";
    boolean hasCustomCode() default false; 
    
    boolean isPassword() default false;
    boolean isReadOnly() default false;
    boolean isProcessed() default false;
    
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
    
    /** if true, then the property value must be unique. */
    boolean isUnique() default false;
    boolean isCurrency() default false;

    /** will be used to know if there is a validation method (not yet used at this time) */
    boolean hasValidationMethod() default false;//qqqq new, if true, then call delegate to verify? or put verify in code?    
    

    boolean isBlob() default false;
    boolean isNameValue() default false;
}





