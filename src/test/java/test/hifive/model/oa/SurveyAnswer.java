// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;

import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "sa",
    displayName = "Survey Answer",
    displayProperty = "text"
)
@OATable(
    indexes = {
        @OAIndex(name = "SurveyAnswerSurveyAnswerQuestion", columns = { @OAIndexColumn(name = "SurveyAnswerQuestionId") }), 
        @OAIndex(name = "SurveyAnswerSurveyQuestion", columns = { @OAIndexColumn(name = "SurveyQuestionId") })
    }
)
public class SurveyAnswer extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Text = "Text";
    public static final String P_Text = "Text";
     
     
    public static final String PROPERTY_EmployeeSurveyQuestions = "EmployeeSurveyQuestions";
    public static final String P_EmployeeSurveyQuestions = "EmployeeSurveyQuestions";
    public static final String PROPERTY_SurveyAnswerQuestion = "SurveyAnswerQuestion";
    public static final String P_SurveyAnswerQuestion = "SurveyAnswerQuestion";
    public static final String PROPERTY_SurveyQuestion = "SurveyQuestion";
    public static final String P_SurveyQuestion = "SurveyQuestion";
     
    protected int id;
    protected String text;
     
    // Links to other objects.
    protected transient SurveyQuestion surveyAnswerQuestion;
    protected transient SurveyQuestion surveyQuestion;
     
    public SurveyAnswer() {
    }
     
    public SurveyAnswer(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        fireBeforePropertyChange(P_Id, this.id, newValue);
        int old = id;
        this.id = newValue;
        firePropertyChange(P_Id, old, this.id);
    }
    @OAProperty(displayName = "Answer", maxLength = 254, displayLength = 40)
    @OAColumn(name = "TextValue", sqlType = java.sql.Types.CLOB)
    public String getText() {
        return text;
    }
    
    public void setText(String newValue) {
        fireBeforePropertyChange(P_Text, this.text, newValue);
        String old = text;
        this.text = newValue;
        firePropertyChange(P_Text, old, this.text);
    }
    @OAMany(
        displayName = "Employee Survey Questions", 
        toClass = EmployeeSurveyQuestion.class, 
        reverseName = EmployeeSurveyQuestion.P_SurveyAnswer, 
        createMethod = false
    )
    private Hub<EmployeeSurveyQuestion> getEmployeeSurveyQuestions() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Survey Answer Question", 
        reverseName = SurveyQuestion.P_CorrectSurveyAnswer, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"SurveyAnswerQuestionId"})
    public SurveyQuestion getSurveyAnswerQuestion() {
        if (surveyAnswerQuestion == null) {
            surveyAnswerQuestion = (SurveyQuestion) getObject(P_SurveyAnswerQuestion);
        }
        return surveyAnswerQuestion;
    }
    
    public void setSurveyAnswerQuestion(SurveyQuestion newValue) {
        fireBeforePropertyChange(P_SurveyAnswerQuestion, this.surveyAnswerQuestion, newValue);
        SurveyQuestion old = this.surveyAnswerQuestion;
        this.surveyAnswerQuestion = newValue;
        firePropertyChange(P_SurveyAnswerQuestion, old, this.surveyAnswerQuestion);
    }
    
    @OAOne(
        displayName = "Survey Question", 
        reverseName = SurveyQuestion.P_SurveyAnswers, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"SurveyQuestionId"})
    public SurveyQuestion getSurveyQuestion() {
        if (surveyQuestion == null) {
            surveyQuestion = (SurveyQuestion) getObject(P_SurveyQuestion);
        }
        return surveyQuestion;
    }
    
    public void setSurveyQuestion(SurveyQuestion newValue) {
        fireBeforePropertyChange(P_SurveyQuestion, this.surveyQuestion, newValue);
        SurveyQuestion old = this.surveyQuestion;
        this.surveyQuestion = newValue;
        firePropertyChange(P_SurveyQuestion, old, this.surveyQuestion);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.text = rs.getString(2);
        int surveyAnswerQuestionFkey = rs.getInt(3);
        if (!rs.wasNull() && surveyAnswerQuestionFkey > 0) {
            setProperty(P_SurveyAnswerQuestion, new OAObjectKey(surveyAnswerQuestionFkey));
        }
        int surveyQuestionFkey = rs.getInt(4);
        if (!rs.wasNull() && surveyQuestionFkey > 0) {
            setProperty(P_SurveyQuestion, new OAObjectKey(surveyQuestionFkey));
        }
        if (rs.getMetaData().getColumnCount() != 4) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
