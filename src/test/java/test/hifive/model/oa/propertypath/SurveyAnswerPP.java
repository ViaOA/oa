// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class SurveyAnswerPP {
    private static EmployeeSurveyQuestionPPx employeeSurveyQuestions;
    private static SurveyQuestionPPx surveyAnswerQuestion;
    private static SurveyQuestionPPx surveyQuestion;
     

    public static EmployeeSurveyQuestionPPx employeeSurveyQuestions() {
        if (employeeSurveyQuestions == null) employeeSurveyQuestions = new EmployeeSurveyQuestionPPx(SurveyAnswer.P_EmployeeSurveyQuestions);
        return employeeSurveyQuestions;
    }

    public static SurveyQuestionPPx surveyAnswerQuestion() {
        if (surveyAnswerQuestion == null) surveyAnswerQuestion = new SurveyQuestionPPx(SurveyAnswer.P_SurveyAnswerQuestion);
        return surveyAnswerQuestion;
    }

    public static SurveyQuestionPPx surveyQuestion() {
        if (surveyQuestion == null) surveyQuestion = new SurveyQuestionPPx(SurveyAnswer.P_SurveyQuestion);
        return surveyQuestion;
    }

    public static String id() {
        String s = SurveyAnswer.P_Id;
        return s;
    }

    public static String text() {
        String s = SurveyAnswer.P_Text;
        return s;
    }
}
 
