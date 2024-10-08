// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class AnswerPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private QuestionPPx question;
    private AnswerResultPPx savedAnswerResults;
     
    public AnswerPPx(String name) {
        this(null, name);
    }

    public AnswerPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public QuestionPPx question() {
        if (question == null) question = new QuestionPPx(this, Answer.P_Question);
        return question;
    }

    public AnswerResultPPx savedAnswerResults() {
        if (savedAnswerResults == null) savedAnswerResults = new AnswerResultPPx(this, Answer.P_SavedAnswerResults);
        return savedAnswerResults;
    }

    public String id() {
        return pp + "." + Answer.P_Id;
    }

    public String answerText() {
        return pp + "." + Answer.P_AnswerText;
    }

    public String value() {
        return pp + "." + Answer.P_Value;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
