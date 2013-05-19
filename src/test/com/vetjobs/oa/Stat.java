package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;
 
 
@OAClass(
    shortName = "sta",
    displayName = "Stat"
)
@OATable(
    indexes = {
        @OAIndex(name = "StatDate", columns = {@OAIndexColumn(name = "Date")})
    }
)
public class Stat extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Date = "Date";
    public static final String PROPERTY_EmployerLoginCount = "EmployerLoginCount";
    public static final String PROPERTY_VetUserLoginCount = "VetUserLoginCount";
     
     
    public static final String PROPERTY_BannerStats = "BannerStats";
     
    protected int id;
    protected OADate date;
    protected int employerLoginCount;
    protected int vetUserLoginCount;
     
    // Links to other objects.
    protected transient Hub<BannerStat> hubBannerStats;
     
     
    public Stat() {
    }
     
    public Stat(int id) {
        this();
        setId(id);
    }
    @OAProperty(displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        int old = id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    @OAProperty(displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getDate() {
        return date;
    }
    
    public void setDate(OADate newValue) {
        OADate old = date;
        this.date = newValue;
        firePropertyChange(PROPERTY_Date, old, this.date);
    }
    
     
    @OAProperty(displayName = "Employer Login Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getEmployerLoginCount() {
        return employerLoginCount;
    }
    
    public void setEmployerLoginCount(int newValue) {
        int old = employerLoginCount;
        this.employerLoginCount = newValue;
        firePropertyChange(PROPERTY_EmployerLoginCount, old, this.employerLoginCount);
    }
    
     
    @OAProperty(displayName = "Vet User Login Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getVetUserLoginCount() {
        return vetUserLoginCount;
    }
    
    public void setVetUserLoginCount(int newValue) {
        int old = vetUserLoginCount;
        this.vetUserLoginCount = newValue;
        firePropertyChange(PROPERTY_VetUserLoginCount, old, this.vetUserLoginCount);
    }
    
     
    @OAMany(displayName = "Banner Stats", toClass = BannerStat.class, reverseName = BannerStat.PROPERTY_Stat)
    public Hub<BannerStat> getBannerStats() {
        if (hubBannerStats == null) {
            hubBannerStats = (Hub<BannerStat>) getHub(PROPERTY_BannerStats);
        }
        return hubBannerStats;
    }
    
     
}
 
