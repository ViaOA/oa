// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class SurveyPP {
    private static EmployeeSurveyPPx employeeSurveys;
    private static ProgramPPx managerSurveyProgram;
    private static ProgramPPx program;
    private static ProgramPPx quizProgram;
    private static SurveyQuestionPPx surveyQuestions;
     

    public static EmployeeSurveyPPx employeeSurveys() {
        if (employeeSurveys == null) employeeSurveys = new EmployeeSurveyPPx(Survey.P_EmployeeSurveys);
        return employeeSurveys;
    }

    public static ProgramPPx managerSurveyProgram() {
        if (managerSurveyProgram == null) managerSurveyProgram = new ProgramPPx(Survey.P_ManagerSurveyProgram);
        return managerSurveyProgram;
    }

    public static ProgramPPx program() {
        if (program == null) program = new ProgramPPx(Survey.P_Program);
        return program;
    }

    public static ProgramPPx quizProgram() {
        if (quizProgram == null) quizProgram = new ProgramPPx(Survey.P_QuizProgram);
        return quizProgram;
    }

    public static SurveyQuestionPPx surveyQuestions() {
        if (surveyQuestions == null) surveyQuestions = new SurveyQuestionPPx(Survey.P_SurveyQuestions);
        return surveyQuestions;
    }

    public static String id() {
        String s = Survey.P_Id;
        return s;
    }

    public static String created() {
        String s = Survey.P_Created;
        return s;
    }

    public static String name() {
        String s = Survey.P_Name;
        return s;
    }

    public static String descripiton() {
        String s = Survey.P_Descripiton;
        return s;
    }

    public static String allowText() {
        String s = Survey.P_AllowText;
        return s;
    }
}
 
