// Generated by OABuilder
package test.hifive.model.oa.propertypath;
 
import java.io.Serializable;

import test.hifive.model.oa.*;
 
public class QuizResultPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private EmployeePPx employee;
    private PointsRequestPPx pointsRequest;
    private QuestionResultPPx questionResults;
    private QuizPPx quiz;
     
    public QuizResultPPx(String name) {
        this(null, name);
    }

    public QuizResultPPx(PPxInterface parent, String name) {
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

    public EmployeePPx employee() {
        if (employee == null) employee = new EmployeePPx(this, QuizResult.P_Employee);
        return employee;
    }

    public PointsRequestPPx pointsRequest() {
        if (pointsRequest == null) pointsRequest = new PointsRequestPPx(this, QuizResult.P_PointsRequest);
        return pointsRequest;
    }

    public QuestionResultPPx questionResults() {
        if (questionResults == null) questionResults = new QuestionResultPPx(this, QuizResult.P_QuestionResults);
        return questionResults;
    }

    public QuizPPx quiz() {
        if (quiz == null) quiz = new QuizPPx(this, QuizResult.P_Quiz);
        return quiz;
    }

    public String id() {
        return pp + "." + QuizResult.P_Id;
    }

    public String name() {
        return pp + "." + QuizResult.P_Name;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
