package com.vetplan.oa;
 
import com.viaoa.annotation.*; 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.OADate;
import com.viaoa.util.OATime;
import java.math.BigDecimal;

@OAClass (addToCache=true, initialize=true, localOnly=true)
@DBTable (name="Exam")
@DBIndexes ( 
    indexes=
    {
        @DBIndex(name="test", columns={@DBIndexColumn(name="col1", ascend=true), @DBIndexColumn(name="col2", ascend=true)}), 
        @DBIndex(name="test2", columns={@DBIndexColumn(name="col2", ascend=true)}) 
    } 
)
public class Exam extends OAObject {
    private static final long serialVersionUID = 1L;
    
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Date = "Date";
    public static final String PROPERTY_Report = "Report";
    public static final String PROPERTY_Note = "Note";
    public static final String PROPERTY_Temp = "Temp";
    public static final String PROPERTY_Weight = "Weight";
    public static final String PROPERTY_Pulse = "Pulse";
    public static final String PROPERTY_Resp = "Resp";
    public static final String PROPERTY_MmColor = "MmColor";
    public static final String PROPERTY_Hydration = "Hydration";
    public static final String PROPERTY_Crt = "Crt";
    public static final String PROPERTY_EndDate = "EndDate";
    public static final String PROPERTY_EndTime = "EndTime";
    public static final String PROPERTY_Title = "Title";
    public static final String PROPERTY_PmsId = "PmsId";
    public static final String PROPERTY_PickupDate = "PickupDate";
    public static final String PROPERTY_PickupTime = "PickupTime";
     
    public static final String PROPERTY_TotalPrice = "TotalPrice";
    public static final String PROPERTY_Reason = "Reason";
     
    public static final String PROPERTY_ExamItems = "ExamItems";
    public static final String PROPERTY_VetUser = "VetUser";
    public static final String PROPERTY_TechUser = "TechUser";
    public static final String PROPERTY_ExamTemplates = "ExamTemplates";
    public static final String PROPERTY_Pet = "Pet";
    public static final String PROPERTY_Clinic = "Clinic";
    public static final String PROPERTY_ReceptionistUser = "ReceptionistUser";
    public static final String PROPERTY_PreparedByUser = "PreparedByUser";
     
    @OAProperty ()
    @OAId (autoAssign=true, guid=false, pos=0)
    @DBColumn (sqlType=java.sql.Types.INTEGER)
    protected String id;
    
    @OAProperty (defaultValue="new OADate()")
    @DBColumn (sqlType=java.sql.Types.INTEGER)
    protected OADate date;
    protected String report;
    protected String note;
    protected String temp;
    protected String weight;
    protected String pulse;
    protected String resp;
    protected String mmColor;
    protected String hydration;
    protected String crt;
    protected OADate endDate;
    protected OATime endTime;
    protected String title;
    protected String pmsId;
    protected OADate pickupDate;
    protected OATime pickupTime;
     
    // Links to other objects.
    
    @OAMany (clazz=ExamItem.class, owned=true, reverse=ExamItem.PROPERTY_Exam, cascadeSave=true, cascadeDelete=true)
    @DBLinkTable (name="ExamUserLinkTable", fkeyColumns= {"Id"})
    protected transient Hub hubExamItems;

    @OAOne (reverse=User.PROPERTY_VetExams, required=false, cascadeSave=false, cascadeDelete=false)
    protected transient User vetUser;
    
    protected transient User techUser;
    protected transient Hub hubExamTemplates;
    protected transient Pet pet;
    protected transient Clinic clinic;
    protected transient User receptionistUser;
    protected transient User preparedByUser;
     
     
    public Exam() {
    }
     
    public Exam(String id) {
        this();
        setId(id);
    }
    public String getId() {
        return id;
    }
    public void setId(String newValue) {
        String old = this.id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    public OADate getDate() {
        return date;
    }
    public void setDate(OADate newValue) {
        OADate old = this.date;
        this.date = newValue;
        firePropertyChange(PROPERTY_Date, old, this.date);
    }
    
     
    public String getReport() {
        return report;
    }
    public void setReport(String newValue) {
        String old = this.report;
        this.report = newValue;
        firePropertyChange(PROPERTY_Report, old, this.report);
    }
    
     
    public String getNote() {
        return note;
    }
    public void setNote(String newValue) {
        String old = this.note;
        this.note = newValue;
        firePropertyChange(PROPERTY_Note, old, this.note);
    }
    
     
    public String getTemp() {
        return temp;
    }
    public void setTemp(String newValue) {
        String old = this.temp;
        this.temp = newValue;
        firePropertyChange(PROPERTY_Temp, old, this.temp);
    }
    
     
    public String getWeight() {
        return weight;
    }
    public void setWeight(String newValue) {
        String old = this.weight;
        this.weight = newValue;
        firePropertyChange(PROPERTY_Weight, old, this.weight);
    }
    
     
    public String getPulse() {
        return pulse;
    }
    public void setPulse(String newValue) {
        String old = this.pulse;
        this.pulse = newValue;
        firePropertyChange(PROPERTY_Pulse, old, this.pulse);
    }
    
     
    public String getResp() {
        return resp;
    }
    public void setResp(String newValue) {
        String old = this.resp;
        this.resp = newValue;
        firePropertyChange(PROPERTY_Resp, old, this.resp);
    }
    
     
    public String getMmColor() {
        return mmColor;
    }
    public void setMmColor(String newValue) {
        String old = this.mmColor;
        this.mmColor = newValue;
        firePropertyChange(PROPERTY_MmColor, old, this.mmColor);
    }
    
     
    public String getHydration() {
        return hydration;
    }
    public void setHydration(String newValue) {
        String old = this.hydration;
        this.hydration = newValue;
        firePropertyChange(PROPERTY_Hydration, old, this.hydration);
    }
    
     
    public String getCrt() {
        return crt;
    }
    public void setCrt(String newValue) {
        String old = this.crt;
        this.crt = newValue;
        firePropertyChange(PROPERTY_Crt, old, this.crt);
    }
    
     
    public OADate getEndDate() {
        return endDate;
    }
    public void setEndDate(OADate newValue) {
        OADate old = this.endDate;
        this.endDate = newValue;
        firePropertyChange(PROPERTY_EndDate, old, this.endDate);
    }
    
     
    public OATime getEndTime() {
        return endTime;
    }
    public void setEndTime(OATime newValue) {
        OATime old = this.endTime;
        this.endTime = newValue;
        firePropertyChange(PROPERTY_EndTime, old, this.endTime);
    }
    
     
    public String getTitle() {
        return title;
    }
    public void setTitle(String newValue) {
        String old = this.title;
        this.title = newValue;
        firePropertyChange(PROPERTY_Title, old, this.title);
    }
    
     
    public String getPmsId() {
        return pmsId;
    }
    public void setPmsId(String newValue) {
        String old = this.pmsId;
        this.pmsId = newValue;
        firePropertyChange(PROPERTY_PmsId, old, this.pmsId);
    }
    
     
    public OADate getPickupDate() {
        return pickupDate;
    }
    public void setPickupDate(OADate newValue) {
        OADate old = this.pickupDate;
        this.pickupDate = newValue;
        firePropertyChange(PROPERTY_PickupDate, old, this.pickupDate);
    }
    
     
    public OATime getPickupTime() {
        return pickupTime;
    }
    public void setPickupTime(OATime newValue) {
        OATime old = this.pickupTime;
        this.pickupTime = newValue;
        firePropertyChange(PROPERTY_PickupTime, old, this.pickupTime);
    }
    
    @OACalculatedProperty (properties= {"prop", "prop2"}) 
    public BigDecimal getTotalPrice() {
        getExamItems();
        BigDecimal bd = new BigDecimal(0);
        for (int i=0; ;i++) {
            ExamItem ei = (ExamItem) hubExamItems.elementAt(i);
            if (ei == null) break;
            if (ei.getExamItemStatusType() == ExamItemStatus.TYPE_DONE) {
                BigDecimal price = ei.getPrice();
                if (price != null) bd = bd.add(price.multiply(new BigDecimal(ei.getQuantity())));
            }
        }
        return bd;
    }
     
    public String getReason() {
        return "<html>qqqqqqqqqqqqq</html>";
    }
     
    public Hub getExamItems() {
        if (hubExamItems == null) {
            hubExamItems = getHub(PROPERTY_ExamItems, "seq");
            hubExamItems.setAutoSequence("seq");
        }
        return hubExamItems;
    }
    
     
    public User getVetUser() {
        if (vetUser == null) {
            vetUser = (User) getObject(PROPERTY_VetUser);
        }
        return vetUser;
    }
    
    public void setVetUser(User newValue) {
        User old = this.vetUser;
        this.vetUser = newValue;
        firePropertyChange(PROPERTY_VetUser, old, this.vetUser);
    }
     
    public User getTechUser() {
        if (techUser == null) {
            techUser = (User) getObject(PROPERTY_TechUser);
        }
        return techUser;
    }
    
    public void setTechUser(User newValue) {
        User old = this.techUser;
        this.techUser = newValue;
        firePropertyChange(PROPERTY_TechUser, old, this.techUser);
    }
     
    public Hub getExamTemplates() {
        if (hubExamTemplates == null) {
            hubExamTemplates = getHub(PROPERTY_ExamTemplates, "seq");
            hubExamTemplates.setAutoSequence("seq");
        }
        return hubExamTemplates;
    }
    
     
    public Pet getPet() {
        if (pet == null) {
            pet = (Pet) getObject(PROPERTY_Pet);
        }
        return pet;
    }
    
    public void setPet(Pet newValue) {
        Pet old = this.pet;
        this.pet = newValue;
        firePropertyChange(PROPERTY_Pet, old, this.pet);
    }
     
    public Clinic getClinic() {
        if (clinic == null) {
            clinic = (Clinic) getObject(PROPERTY_Clinic);
        }
        return clinic;
    }
    
    public void setClinic(Clinic newValue) {
        Clinic old = this.clinic;
        this.clinic = newValue;
        firePropertyChange(PROPERTY_Clinic, old, this.clinic);
    }
     
    public User getReceptionistUser() {
        if (receptionistUser == null) {
            receptionistUser = (User) getObject(PROPERTY_ReceptionistUser);
        }
        return receptionistUser;
    }
    
    public void setReceptionistUser(User newValue) {
        User old = this.receptionistUser;
        this.receptionistUser = newValue;
        firePropertyChange(PROPERTY_ReceptionistUser, old, this.receptionistUser);
    }
     
    public User getPreparedByUser() {
        if (preparedByUser == null) {
            preparedByUser = (User) getObject(PROPERTY_PreparedByUser);
        }
        return preparedByUser;
    }
    
    public void setPreparedByUser(User newValue) {
        User old = this.preparedByUser;
        this.preparedByUser = newValue;
        firePropertyChange(PROPERTY_PreparedByUser, old, this.preparedByUser);
    }
     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"id"});
         
        // OALinkInfo(property, toClass, ONE/MANY, cascade [Save,Delete], reverseProperty, thisOwner)
        OALinkInfo li = new OALinkInfo(PROPERTY_ExamItems, ExamItem.class, OALinkInfo.MANY, true, ExamItem.PROPERTY_Exam, true);
        li.setCacheSize(5); // testing
        oaObjectInfo.addLink(li);
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_VetUser, User.class, OALinkInfo.ONE, false, User.PROPERTY_VetExams));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_TechUser, User.class, OALinkInfo.ONE, false, User.PROPERTY_TechExams));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ExamTemplates, ExamTemplate.class, OALinkInfo.MANY, true, true, ExamTemplate.PROPERTY_Exam, true));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Pet, Pet.class, OALinkInfo.ONE, false, false, Pet.PROPERTY_Exams));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_Clinic, Clinic.class, OALinkInfo.ONE, false, false, Clinic.PROPERTY_Exams));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_ReceptionistUser, User.class, OALinkInfo.ONE, false, false, User.PROPERTY_ReceptionistExams));
        oaObjectInfo.addLink(new OALinkInfo(PROPERTY_PreparedByUser, User.class, OALinkInfo.ONE, false, false, User.PROPERTY_PreparedByExams));
         
        // OACalcInfo(calcPropertyName, String[] { propertyPath1, propertyPathN })
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_TotalPrice, new String[] {} ));
        oaObjectInfo.addCalc(new OACalcInfo(PROPERTY_Reason, new String[] {} ));
         
        oaObjectInfo.addRequired(PROPERTY_Id);
    }
}
 












