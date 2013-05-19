package com.viaoa.hub;

/**
 * A DetailHub to represent the parent hub.
 * Ex: ExamItems has an Exam, hubExamItem then can have a parentHub for Exam
*/
public class ParentHub<TYPE> extends DetailHub<TYPE> {

    public ParentHub(Hub hubMaster, String propertyPath) {
        super(hubMaster, propertyPath);
    }
}

