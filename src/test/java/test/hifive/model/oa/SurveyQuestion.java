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
    shortName = "sq",
    displayName = "Survey Question",
    displayProperty = "text"
)
@OATable(
    indexes = {
        @OAIndex(name = "SurveyQuestionSurvey", columns = { @OAIndexColumn(name = "SurveyId") })
    }
)
public class SurveyQuestion extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Type = "Type";
    public static final String P_Type = "Type";
    public static final String PROPERTY_TypeAsString = "TypeAsString";
    public static final String P_TypeAsString = "TypeAsString";
    public static final String PROPERTY_AllowTextResponse = "AllowTextResponse";
    public static final String P_AllowTextResponse = "AllowTextResponse";
    public static final String PROPERTY_Text = "Text";
    public static final String P_Text = "Text";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
     
     
    public static final String PROPERTY_CorrectSurveyAnswer = "CorrectSurveyAnswer";
    public static final String P_CorrectSurveyAnswer = "CorrectSurveyAnswer";
    public static final String PROPERTY_EmployeeSurveyQuestions = "EmployeeSurveyQuestions";
    public static final String P_EmployeeSurveyQuestions = "EmployeeSurveyQuestions";
    public static final String PROPERTY_Survey = "Survey";
    public static final String P_Survey = "Survey";
    public static final String PROPERTY_SurveyAnswers = "SurveyAnswers";
    public static final String P_SurveyAnswers = "SurveyAnswers";
     
    protected int id;
    protected int type;
    public static final int TYPE_pickOne = 0;
    public static final int TYPE_TextOnly = 1;
    public static final Hub<String> hubType;
    static {
        hubType = new Hub<String>(String.class);
        hubType.addElement("Pick One");
        hubType.addElement("Text Only");
    }
    protected boolean allowTextResponse;
    protected String text;
    protected int seq;
     
    // Links to other objects.
    protected transient SurveyAnswer correctSurveyAnswer;
    protected transient Survey survey;
    protected transient Hub<SurveyAnswer> hubSurveyAnswers;
     
    public SurveyQuestion() {
    }
     
    public SurveyQuestion(int id) {
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
    @OAProperty(displayLength = 5, isNameValue = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getType() {
        return type;
    }
    
    public void setType(int newValue) {
        fireBeforePropertyChange(P_Type, this.type, newValue);
        int old = type;
        this.type = newValue;
        firePropertyChange(P_Type, old, this.type);
    }
    public String getTypeAsString() {
        if (isNull(P_Type)) return "";
        String s = hubType.getAt(getType());
        if (s == null) s = "";
        return s;
    }
    @OAProperty(displayName = "Allow Text Response", displayLength = 5, columnLength = 9)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getAllowTextResponse() {
        return allowTextResponse;
    }
    
    public void setAllowTextResponse(boolean newValue) {
        fireBeforePropertyChange(P_AllowTextResponse, this.allowTextResponse, newValue);
        boolean old = allowTextResponse;
        this.allowTextResponse = newValue;
        firePropertyChange(P_AllowTextResponse, old, this.allowTextResponse);
    }
    @OAProperty(displayName = "Question", maxLength = 254, displayLength = 30, columnLength = 22)
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
    @OAProperty(displayLength = 5, isAutoSeq = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSeq() {
        return seq;
    }
    
    public void setSeq(int newValue) {
        fireBeforePropertyChange(P_Seq, this.seq, newValue);
        int old = seq;
        this.seq = newValue;
        firePropertyChange(P_Seq, old, this.seq);
    }
    @OAOne(
        displayName = "Correct Survey Answer", 
        reverseName = SurveyAnswer.P_SurveyAnswerQuestion, 
        allowCreateNew = false
    )
    public SurveyAnswer getCorrectSurveyAnswer() {
        if (correctSurveyAnswer == null) {
            correctSurveyAnswer = (SurveyAnswer) getObject(P_CorrectSurveyAnswer);
        }
        return correctSurveyAnswer;
    }
    
    public void setCorrectSurveyAnswer(SurveyAnswer newValue) {
        fireBeforePropertyChange(P_CorrectSurveyAnswer, this.correctSurveyAnswer, newValue);
        SurveyAnswer old = this.correctSurveyAnswer;
        this.correctSurveyAnswer = newValue;
        firePropertyChange(P_CorrectSurveyAnswer, old, this.correctSurveyAnswer);
    }
    
    @OAMany(
        displayName = "Employee Survey Questions", 
        toClass = EmployeeSurveyQuestion.class, 
        reverseName = EmployeeSurveyQuestion.P_SurveyQuestion, 
        createMethod = false
    )
    private Hub<EmployeeSurveyQuestion> getEmployeeSurveyQuestions() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = Survey.P_SurveyQuestions, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"SurveyId"})
    public Survey getSurvey() {
        if (survey == null) {
            survey = (Survey) getObject(P_Survey);
        }
        return survey;
    }
    
    public void setSurvey(Survey newValue) {
        fireBeforePropertyChange(P_Survey, this.survey, newValue);
        Survey old = this.survey;
        this.survey = newValue;
        firePropertyChange(P_Survey, old, this.survey);
    }
    
    @OAMany(
        displayName = "Survey Answers", 
        toClass = SurveyAnswer.class, 
        owner = true, 
        reverseName = SurveyAnswer.P_SurveyQuestion, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<SurveyAnswer> getSurveyAnswers() {
        if (hubSurveyAnswers == null) {
            hubSurveyAnswers = (Hub<SurveyAnswer>) getHub(P_SurveyAnswers);
        }
        return hubSurveyAnswers;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.type = (int) rs.getInt(2);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, SurveyQuestion.P_Type, true);
        }
        this.allowTextResponse = rs.getBoolean(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, SurveyQuestion.P_AllowTextResponse, true);
        }
        this.text = rs.getString(4);
        this.seq = (int) rs.getInt(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, SurveyQuestion.P_Seq, true);
        }
        int surveyFkey = rs.getInt(6);
        if (!rs.wasNull() && surveyFkey > 0) {
            setProperty(P_Survey, new OAObjectKey(surveyFkey));
        }
        if (rs.getMetaData().getColumnCount() != 6) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
