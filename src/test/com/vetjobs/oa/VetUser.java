package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;
 
 
@OAClass(
    shortName = "vu",
    displayName = "Vet User"
)
@OATable(
    indexes = {
        @OAIndex(name = "VetUserState", columns = {@OAIndexColumn(name = "State")}),
        @OAIndex(name = "VetUserLoginId", columns = {@OAIndexColumn(name = "LoginId")}),
        @OAIndex(name = "VetUserLastName", columns = {@OAIndexColumn(name = "LastName")}),
        @OAIndex(name = "VetUserResume", columns = { @OAIndexColumn(name = "ResumeId") }), 
    }
)
public class VetUser extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_CreateDate = "CreateDate";
    public static final String PROPERTY_LoginId = "LoginId";
    public static final String PROPERTY_Password = "Password";
    public static final String PROPERTY_FirstName = "FirstName";
    public static final String PROPERTY_LastName = "LastName";
    public static final String PROPERTY_Address1 = "Address1";
    public static final String PROPERTY_Address2 = "Address2";
    public static final String PROPERTY_City = "City";
    public static final String PROPERTY_State = "State";
    public static final String PROPERTY_Zip = "Zip";
    public static final String PROPERTY_Country = "Country";
    public static final String PROPERTY_Phone = "Phone";
    public static final String PROPERTY_Email = "Email";
    public static final String PROPERTY_Relocate = "Relocate";
    public static final String PROPERTY_MinIncomeYear = "MinIncomeYear";
    public static final String PROPERTY_MinIncomeHourly = "MinIncomeHourly";
    public static final String PROPERTY_AvailableDate = "AvailableDate";
    public static final String PROPERTY_InactiveDate = "InactiveDate";
    public static final String PROPERTY_SpouseName = "SpouseName";
    public static final String PROPERTY_LastYearService = "LastYearService";
    public static final String PROPERTY_TotalYears = "TotalYears";
    public static final String PROPERTY_MilitaryMoveAvail = "MilitaryMoveAvail";
    public static final String PROPERTY_VetFlag = "VetFlag";
     
     
    public static final String PROPERTY_Categories = "Categories";
    public static final String PROPERTY_PreferLocations = "PreferLocations";
    public static final String PROPERTY_RejectLocations = "RejectLocations";
    public static final String PROPERTY_Resume = "Resume";
    public static final String PROPERTY_Rank = "Rank";
    public static final String PROPERTY_Service = "Service";
    public static final String PROPERTY_Education = "Education";
    public static final String PROPERTY_VetSecurity = "VetSecurity";
    public static final String PROPERTY_ServiceStatus = "ServiceStatus";
    public static final String PROPERTY_EmpQueryVets = "EmpQueryVets";
     
    protected int id;
    protected OADate createDate;
    protected String loginId;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String address1;
    protected String address2;
    protected String city;
    protected String state;
    protected String zip;
    protected String country;
    protected String phone;
    protected String email;
    protected boolean relocate;
    protected int minIncomeYear;
    protected float minIncomeHourly;
    protected OADate availableDate;
    protected OADate inactiveDate;
    protected String spouseName;
    protected int lastYearService;
    protected int totalYears;
    protected boolean militaryMoveAvail;
    protected boolean vetFlag;
     
    // Links to other objects.
    protected transient Hub<Category> hubCategories;
    protected transient Hub<Location> hubPreferLocations;
    protected transient Hub<Location> hubRejectLocations;
    protected transient Resume resume;
    protected transient Rank rank;
    protected transient Service service;
    protected transient Education education;
    protected transient VetSecurity vetSecurity;
    protected transient ServiceStatus serviceStatus;
     
     
    public VetUser() {
    }
     
    public VetUser(int id) {
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
    
     
    @OAProperty(displayName = "Create Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(OADate newValue) {
        OADate old = createDate;
        this.createDate = newValue;
        firePropertyChange(PROPERTY_CreateDate, old, this.createDate);
    }
    
     
    @OAProperty(displayName = "Login Id", maxLength = 20, displayLength = 5)
    @OAColumn(maxLength = 20)
    public String getLoginId() {
        return loginId;
    }
    
    public void setLoginId(String newValue) {
        String old = loginId;
        this.loginId = newValue;
        firePropertyChange(PROPERTY_LoginId, old, this.loginId);
    }
    
     
    @OAProperty(maxLength = 20, displayLength = 8)
    @OAColumn(maxLength = 20)
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String newValue) {
        String old = password;
        this.password = newValue;
        firePropertyChange(PROPERTY_Password, old, this.password);
    }
    
     
    @OAProperty(displayName = "First Name", maxLength = 55, displayLength = 9)
    @OAColumn(maxLength = 55)
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String newValue) {
        String old = firstName;
        this.firstName = newValue;
        firePropertyChange(PROPERTY_FirstName, old, this.firstName);
    }
    
     
    @OAProperty(displayName = "Last Name", maxLength = 55, displayLength = 8)
    @OAColumn(maxLength = 55)
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String newValue) {
        String old = lastName;
        this.lastName = newValue;
        firePropertyChange(PROPERTY_LastName, old, this.lastName);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 8)
    @OAColumn(maxLength = 75)
    public String getAddress1() {
        return address1;
    }
    
    public void setAddress1(String newValue) {
        String old = address1;
        this.address1 = newValue;
        firePropertyChange(PROPERTY_Address1, old, this.address1);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 8)
    @OAColumn(maxLength = 75)
    public String getAddress2() {
        return address2;
    }
    
    public void setAddress2(String newValue) {
        String old = address2;
        this.address2 = newValue;
        firePropertyChange(PROPERTY_Address2, old, this.address2);
    }
    
     
    @OAProperty(maxLength = 55, displayLength = 4)
    @OAColumn(maxLength = 55)
    public String getCity() {
        return city;
    }
    
    public void setCity(String newValue) {
        String old = city;
        this.city = newValue;
        firePropertyChange(PROPERTY_City, old, this.city);
    }
    
     
    @OAProperty(maxLength = 25, displayLength = 5)
    @OAColumn(maxLength = 25)
    public String getState() {
        return state;
    }
    
    public void setState(String newValue) {
        String old = state;
        this.state = newValue;
        firePropertyChange(PROPERTY_State, old, this.state);
    }
    
     
    @OAProperty(maxLength = 18, displayLength = 3)
    @OAColumn(maxLength = 18)
    public String getZip() {
        return zip;
    }
    
    public void setZip(String newValue) {
        String old = zip;
        this.zip = newValue;
        firePropertyChange(PROPERTY_Zip, old, this.zip);
    }
    
     
    @OAProperty(maxLength = 45, displayLength = 5)
    @OAColumn(maxLength = 45)
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String newValue) {
        String old = country;
        this.country = newValue;
        firePropertyChange(PROPERTY_Country, old, this.country);
    }
    
     
    @OAProperty(maxLength = 55, displayLength = 5)
    @OAColumn(maxLength = 55)
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String newValue) {
        String old = phone;
        this.phone = newValue;
        firePropertyChange(PROPERTY_Phone, old, this.phone);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 5)
    @OAColumn(maxLength = 75)
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String newValue) {
        String old = email;
        this.email = newValue;
        firePropertyChange(PROPERTY_Email, old, this.email);
    }
    
     
    @OAProperty(displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getRelocate() {
        return relocate;
    }
    
    public void setRelocate(boolean newValue) {
        boolean old = relocate;
        this.relocate = newValue;
        firePropertyChange(PROPERTY_Relocate, old, this.relocate);
    }
    
     
    @OAProperty(displayName = "Min Income Year", displayLength = 13)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getMinIncomeYear() {
        return minIncomeYear;
    }
    
    public void setMinIncomeYear(int newValue) {
        int old = minIncomeYear;
        this.minIncomeYear = newValue;
        firePropertyChange(PROPERTY_MinIncomeYear, old, this.minIncomeYear);
    }
    
     
    @OAProperty(displayName = "Min Income Hourly", decimalPlaces = 6, displayLength = 15)
    @OAColumn(sqlType = java.sql.Types.FLOAT)
    public float getMinIncomeHourly() {
        return minIncomeHourly;
    }
    
    public void setMinIncomeHourly(float newValue) {
        float old = minIncomeHourly;
        this.minIncomeHourly = newValue;
        firePropertyChange(PROPERTY_MinIncomeHourly, old, this.minIncomeHourly);
    }
    
     
    @OAProperty(displayName = "Available Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getAvailableDate() {
        return availableDate;
    }
    
    public void setAvailableDate(OADate newValue) {
        OADate old = availableDate;
        this.availableDate = newValue;
        firePropertyChange(PROPERTY_AvailableDate, old, this.availableDate);
    }
    
     
    @OAProperty(displayName = "Inactive Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getInactiveDate() {
        return inactiveDate;
    }
    
    public void setInactiveDate(OADate newValue) {
        OADate old = inactiveDate;
        this.inactiveDate = newValue;
        firePropertyChange(PROPERTY_InactiveDate, old, this.inactiveDate);
    }
    
     
    @OAProperty(displayName = "Spouse Name", maxLength = 70, displayLength = 10)
    @OAColumn(maxLength = 70)
    public String getSpouseName() {
        return spouseName;
    }
    
    public void setSpouseName(String newValue) {
        String old = spouseName;
        this.spouseName = newValue;
        firePropertyChange(PROPERTY_SpouseName, old, this.spouseName);
    }
    
     
    @OAProperty(displayName = "Last Year Service", displayLength = 15)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getLastYearService() {
        return lastYearService;
    }
    
    public void setLastYearService(int newValue) {
        int old = lastYearService;
        this.lastYearService = newValue;
        firePropertyChange(PROPERTY_LastYearService, old, this.lastYearService);
    }
    
     
    @OAProperty(displayName = "Total Years", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getTotalYears() {
        return totalYears;
    }
    
    public void setTotalYears(int newValue) {
        int old = totalYears;
        this.totalYears = newValue;
        firePropertyChange(PROPERTY_TotalYears, old, this.totalYears);
    }
    
     
    @OAProperty(displayName = "Military Move Avail", displayLength = 17)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getMilitaryMoveAvail() {
        return militaryMoveAvail;
    }
    
    public void setMilitaryMoveAvail(boolean newValue) {
        boolean old = militaryMoveAvail;
        this.militaryMoveAvail = newValue;
        firePropertyChange(PROPERTY_MilitaryMoveAvail, old, this.militaryMoveAvail);
    }
    
     
    @OAProperty(displayName = "Vet Flag", displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getVetFlag() {
        return vetFlag;
    }
    
    public void setVetFlag(boolean newValue) {
        boolean old = vetFlag;
        this.vetFlag = newValue;
        firePropertyChange(PROPERTY_VetFlag, old, this.vetFlag);
    }
    
     
    @OAMany(toClass = Category.class, reverseName = Category.PROPERTY_VetUsers)
    @OALinkTable(name = "VetUserCategoryLink", indexName = "CategoryVetUser", columns = {"VetUserId"})
    public Hub<Category> getCategories() {
        if (hubCategories == null) {
            hubCategories = (Hub<Category>) getHub(PROPERTY_Categories);
        }
        return hubCategories;
    }
    
     
    @OAMany(displayName = "Prefer Locations", toClass = Location.class, reverseName = Location.PROPERTY_VetUsers)
    @OALinkTable(name = "VetUserLocationLink", indexName = "LocationVetUser", columns = {"VetUserId"})
    public Hub<Location> getPreferLocations() {
        if (hubPreferLocations == null) {
            hubPreferLocations = (Hub<Location>) getHub(PROPERTY_PreferLocations);
        }
        return hubPreferLocations;
    }
    
     
    @OAMany(displayName = "Reject Locations", toClass = Location.class, reverseName = Location.PROPERTY_VetUser2S)
    @OALinkTable(name = "VetUserLocationLink1", indexName = "LocationVetUser2", columns = {"VetUserId"})
    public Hub<Location> getRejectLocations() {
        if (hubRejectLocations == null) {
            hubRejectLocations = (Hub<Location>) getHub(PROPERTY_RejectLocations);
        }
        return hubRejectLocations;
    }
    
     
    @OAOne(reverseName = Resume.PROPERTY_VetUser)
    @OAFkey(columns = {"ResumeId"})
    public Resume getResume() {
        if (resume == null) {
            resume = (Resume) getObject(PROPERTY_Resume);
        }
        return resume;
    }
    
    public void setResume(Resume newValue) {
        Resume old = this.resume;
        this.resume = newValue;
        firePropertyChange(PROPERTY_Resume, old, this.resume);
    }
    
     
    @OAOne(reverseName = Rank.PROPERTY_VetUsers)
    @OAFkey(columns = {"RankId"})
    public Rank getRank() {
        if (rank == null) {
            rank = (Rank) getObject(PROPERTY_Rank);
        }
        return rank;
    }
    
    public void setRank(Rank newValue) {
        Rank old = this.rank;
        this.rank = newValue;
        firePropertyChange(PROPERTY_Rank, old, this.rank);
    }
    
     
    @OAOne(reverseName = Service.PROPERTY_VetUsers)
    @OAFkey(columns = {"ServiceId"})
    public Service getService() {
        if (service == null) {
            service = (Service) getObject(PROPERTY_Service);
        }
        return service;
    }
    
    public void setService(Service newValue) {
        Service old = this.service;
        this.service = newValue;
        firePropertyChange(PROPERTY_Service, old, this.service);
    }
    
     
    @OAOne(reverseName = Education.PROPERTY_VetUsers)
    @OAFkey(columns = {"EducationId"})
    public Education getEducation() {
        if (education == null) {
            education = (Education) getObject(PROPERTY_Education);
        }
        return education;
    }
    
    public void setEducation(Education newValue) {
        Education old = this.education;
        this.education = newValue;
        firePropertyChange(PROPERTY_Education, old, this.education);
    }
    
     
    @OAOne(displayName = "Vet Security", reverseName = VetSecurity.PROPERTY_VetUsers)
    @OAFkey(columns = {"VetSecurityId"})
    public VetSecurity getVetSecurity() {
        if (vetSecurity == null) {
            vetSecurity = (VetSecurity) getObject(PROPERTY_VetSecurity);
        }
        return vetSecurity;
    }
    
    public void setVetSecurity(VetSecurity newValue) {
        VetSecurity old = this.vetSecurity;
        this.vetSecurity = newValue;
        firePropertyChange(PROPERTY_VetSecurity, old, this.vetSecurity);
    }
    
     
    @OAOne(displayName = "Service Status", reverseName = ServiceStatus.PROPERTY_VetUsers)
    @OAFkey(columns = {"ServiceStatusId"})
    public ServiceStatus getServiceStatus() {
        if (serviceStatus == null) {
            serviceStatus = (ServiceStatus) getObject(PROPERTY_ServiceStatus);
        }
        return serviceStatus;
    }
    
    public void setServiceStatus(ServiceStatus newValue) {
        ServiceStatus old = this.serviceStatus;
        this.serviceStatus = newValue;
        firePropertyChange(PROPERTY_ServiceStatus, old, this.serviceStatus);
    }
    
     
    @OAMany(displayName = "Emp Query Vets", toClass = EmpQueryVet.class, reverseName = EmpQueryVet.PROPERTY_VetUser, createMethod = false)
    private Hub<EmpQueryVet> getEmpQueryVets() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
}
 
