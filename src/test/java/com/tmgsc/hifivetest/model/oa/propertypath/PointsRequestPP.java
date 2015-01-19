// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.propertypath;
 
import com.tmgsc.hifivetest.model.oa.*;
 
public class PointsRequestPP {
    private static EmployeePPx approvingEmployee;
    private static UserPPx approvingUser;
    private static EmailPPx emails;
    private static PointsApprovalPPx pointsApprovals;
    private static PointsAwardLevelPPx pointsAwardLevel;
    private static PointsRecordPPx pointsRecords;
    private static QuizResultPPx quizResult;
    private static EmployeePPx requestingEmployee;
     

    public static EmployeePPx approvingEmployee() {
        if (approvingEmployee == null) approvingEmployee = new EmployeePPx(PointsRequest.P_ApprovingEmployee);
        return approvingEmployee;
    }

    public static UserPPx approvingUser() {
        if (approvingUser == null) approvingUser = new UserPPx(PointsRequest.P_ApprovingUser);
        return approvingUser;
    }

    public static EmailPPx emails() {
        if (emails == null) emails = new EmailPPx(PointsRequest.P_Emails);
        return emails;
    }

    public static PointsApprovalPPx pointsApprovals() {
        if (pointsApprovals == null) pointsApprovals = new PointsApprovalPPx(PointsRequest.P_PointsApprovals);
        return pointsApprovals;
    }

    public static PointsAwardLevelPPx pointsAwardLevel() {
        if (pointsAwardLevel == null) pointsAwardLevel = new PointsAwardLevelPPx(PointsRequest.P_PointsAwardLevel);
        return pointsAwardLevel;
    }

    public static PointsRecordPPx pointsRecords() {
        if (pointsRecords == null) pointsRecords = new PointsRecordPPx(PointsRequest.P_PointsRecords);
        return pointsRecords;
    }

    public static QuizResultPPx quizResult() {
        if (quizResult == null) quizResult = new QuizResultPPx(PointsRequest.P_QuizResult);
        return quizResult;
    }

    public static EmployeePPx requestingEmployee() {
        if (requestingEmployee == null) requestingEmployee = new EmployeePPx(PointsRequest.P_RequestingEmployee);
        return requestingEmployee;
    }

    public static String id() {
        String s = PointsRequest.P_Id;
        return s;
    }

    public static String created() {
        String s = PointsRequest.P_Created;
        return s;
    }

    public static String approved() {
        String s = PointsRequest.P_Approved;
        return s;
    }

    public static String notes() {
        String s = PointsRequest.P_Notes;
        return s;
    }

    public static String errorNotes() {
        String s = PointsRequest.P_ErrorNotes;
        return s;
    }

    public static String filename() {
        String s = PointsRequest.P_Filename;
        return s;
    }

    public static String approvedDate() {
        String s = PointsRequest.P_ApprovedDate;
        return s;
    }

    public static String requestType() {
        String s = PointsRequest.P_RequestType;
        return s;
    }

    public static String totalValue() {
        String s = PointsRequest.P_TotalValue;
        return s;
    }

    public static String totalPoints() {
        String s = PointsRequest.P_TotalPoints;
        return s;
    }
}
 
