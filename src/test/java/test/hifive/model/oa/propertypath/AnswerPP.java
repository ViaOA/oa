// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import test.hifive.model.oa.*;
 
public class AnswerPP {
    private static QuestionPPx question;
    private static AnswerResultPPx savedAnswerResults;
     

    public static QuestionPPx question() {
        if (question == null) question = new QuestionPPx(Answer.P_Question);
        return question;
    }

    public static AnswerResultPPx savedAnswerResults() {
        if (savedAnswerResults == null) savedAnswerResults = new AnswerResultPPx(Answer.P_SavedAnswerResults);
        return savedAnswerResults;
    }

    public static String id() {
        String s = Answer.P_Id;
        return s;
    }

    public static String answerText() {
        String s = Answer.P_AnswerText;
        return s;
    }

    public static String value() {
        String s = Answer.P_Value;
        return s;
    }
}
 
