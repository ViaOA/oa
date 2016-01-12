package test.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.object.*;

public class VetUser extends OAObject {
    private static final long serialVersionUID = 1L;
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
    protected double minIncomeHourly;
    protected OADate availableDate;
    protected OADate inactiveDate;
    protected String spouseName;
    protected int lastYearService;
    protected int totalYears;
    protected boolean militaryMoveAvail;
    protected boolean vetFlag;

    // Links to other objects
    protected Resume resume;
    protected Education education;
    protected Hub hubCategory;
    protected VetSecurity vetSecurity;
    protected Rank rank;
    protected Service service;
    protected ServiceStatus serviceStatus;
    protected Hub hubPreferLocation;
    protected Hub hubRejectLocation;
    protected Hub hubEmpQueryVet;

    public VetUser() {
    }

    public VetUser(int id) {
        setId(id);
    }

    public String getCityState() {
    	String csz = "";
    	String s = city;
    	if (city != null) csz = city;
    	if (state != null) {
    		if (csz.length() > 0) csz += ", ";
    		csz += state;
    	}
    	return csz;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }

    public OADate getCreateDate() {
        return createDate;
    }
    public void setCreateDate(OADate createDate) {
        OADate old = this.createDate;
        this.createDate = createDate;
        firePropertyChange("createDate",old,createDate);
    }

    public String getLoginId() {
        return loginId;
    }
    public void setLoginId(String loginId) {
        String old = this.loginId;
        this.loginId = loginId;
        firePropertyChange("loginId",old,loginId);
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        String old = this.password;
        this.password = password;
        firePropertyChange("password",old,password);
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        String old = this.firstName;
        this.firstName = firstName;
        firePropertyChange("firstName",old,firstName);
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        String old = this.lastName;
        this.lastName = lastName;
        firePropertyChange("lastName",old,lastName);
    }

    public String getAddress1() {
        return address1;
    }
    public void setAddress1(String address1) {
        String old = this.address1;
        this.address1 = address1;
        firePropertyChange("address1",old,address1);
    }

    public String getAddress2() {
        return address2;
    }
    public void setAddress2(String address2) {
        String old = this.address2;
        this.address2 = address2;
        firePropertyChange("address2",old,address2);
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        String old = this.city;
        this.city = city;
        firePropertyChange("city",old,city);
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        String old = this.state;
        this.state = state;
        firePropertyChange("state",old,state);
    }

    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        String old = this.zip;
        this.zip = zip;
        firePropertyChange("zip",old,zip);
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        String old = this.country;
        this.country = country;
        firePropertyChange("country",old,country);
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        String old = this.phone;
        this.phone = phone;
        firePropertyChange("phone",old,phone);
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        String old = this.email;
        this.email = email;
        firePropertyChange("email",old,email);
    }

    public boolean getRelocate() {
        return relocate;
    }
    public void setRelocate(boolean relocate) {
        boolean old = this.relocate;
        this.relocate = relocate;
        firePropertyChange("relocate",old,relocate);
    }

    public int getMinIncomeYear() {
        return minIncomeYear;
    }
    public void setMinIncomeYear(int minIncomeYear) {
        int old = this.minIncomeYear;
        this.minIncomeYear = minIncomeYear;
        firePropertyChange("minIncomeYear",old,minIncomeYear);
    }

    public double getMinIncomeHourly() {
        return minIncomeHourly;
    }
    public void setMinIncomeHourly(double minIncomeHourly) {
        double old = this.minIncomeHourly;
        this.minIncomeHourly = minIncomeHourly;
        firePropertyChange("minIncomeHourly",old,minIncomeHourly);
    }

    public OADate getAvailableDate() {
        return availableDate;
    }
    public void setAvailableDate(OADate availableDate) {
        OADate old = this.availableDate;
        this.availableDate = availableDate;
        firePropertyChange("availableDate",old,availableDate);
    }

    public OADate getInactiveDate() {
        return inactiveDate;
    }
    public void setInactiveDate(OADate inactiveDate) {
        OADate old = this.inactiveDate;
        this.inactiveDate = inactiveDate;
        firePropertyChange("inactiveDate",old,inactiveDate);
    }

    public String getSpouseName() {
        return spouseName;
    }
    public void setSpouseName(String spouseName) {
        String old = this.spouseName;
        this.spouseName = spouseName;
        firePropertyChange("spouseName",old,spouseName);
    }

    public int getLastYearService() {
        return lastYearService;
    }
    public void setLastYearService(int lastYearService) {
        int old = this.lastYearService;
        this.lastYearService = lastYearService;
        firePropertyChange("lastYearService",old,lastYearService);
    }

    public int getTotalYears() {
        return totalYears;
    }
    public void setTotalYears(int totalYears) {
        int old = this.totalYears;
        this.totalYears = totalYears;
        firePropertyChange("totalYears",old,totalYears);
    }

    public boolean getMilitaryMoveAvail() {
        return militaryMoveAvail;
    }
    public void setMilitaryMoveAvail(boolean militaryMoveAvail) {
        boolean old = this.militaryMoveAvail;
        this.militaryMoveAvail = militaryMoveAvail;
        firePropertyChange("militaryMoveAvail",old,militaryMoveAvail);
    }

    public boolean getVetFlag() {
        return vetFlag;
    }
    public void setVetFlag(boolean vetFlag) {
        boolean old = this.vetFlag;
        this.vetFlag = vetFlag;
        firePropertyChange("vetFlag",old,vetFlag);
    }

    public Hub getPreferLocations() {
        if (hubPreferLocation == null) hubPreferLocation = getHub("preferLocations");
        return hubPreferLocation;
    }
    public Hub getRejectLocations() {
        if (hubRejectLocation == null) hubRejectLocation = getHub("rejectLocations");
        return hubRejectLocation;
    }


    public Hub getPreferLocation() {
        if (hubPreferLocation == null) hubPreferLocation = getHub("preferLocation");
        return hubPreferLocation;
    }

    public Resume getResume() {
        if (resume == null) {
            resume = (Resume) getObject("resume");
        }
        return resume;
    }

    public void setResume(Resume resume) {
        Resume old = this.resume;
        this.resume = resume;
        firePropertyChange("resume",old,resume);
    }

    public Education getEducation() {
        if (education == null) education = (Education) getObject("education");
        return education;
    }

    public void setEducation(Education education) {
        Education old = this.education;
        this.education = education;
        firePropertyChange("education",old,education);
    }

    public Hub getCategories() {
        if (hubCategory == null) hubCategory = getHub("categories");
        return hubCategory;
    }

    public VetSecurity getVetSecurity() {
        if (vetSecurity == null) vetSecurity = (VetSecurity) getObject("vetSecurity");
        return vetSecurity;
    }

    public void setVetSecurity(VetSecurity vetSecurity) {
        VetSecurity old = this.vetSecurity;
        this.vetSecurity = vetSecurity;
        firePropertyChange("vetSecurity",old,vetSecurity);
    }

    public Rank getRank() {
        if (rank == null) rank = (Rank) getObject("rank");
        return rank;
    }

    public void setRank(Rank rank) {
        Rank old = this.rank;
        this.rank = rank;
        firePropertyChange("rank",old,rank);
    }

    public Service getService() {
        if (service == null) service = (Service) getObject("service");
        return service;
    }

    public void setService(Service service) {
        Service old = this.service;
        this.service = service;
        firePropertyChange("service",old,service);
    }

    public ServiceStatus getServiceStatus() {
        if (serviceStatus == null) serviceStatus = (ServiceStatus) getObject("serviceStatus");
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        ServiceStatus old = this.serviceStatus;
        this.serviceStatus = serviceStatus;
        firePropertyChange("serviceStatus",old,serviceStatus);
    }


    public String getFullName() {
        String fullName = "";
        String s = firstName;
        if (s != null) fullName += s + " ";
        s = lastName;
        if (s != null) fullName += s + " ";
        return fullName;
    }
    
    public String getCategoryNames() {
        String catNames = "";
        String thisName;      
        Hub h = getCategories();
        for (int i=0; ;i++) {
        	Category cat = (Category) h.getAt(i);
        	if (cat == null) break;
           thisName = cat.getName();
           if (thisName == null) continue;
           if (!thisName.equals("")){
                 if (catNames.equals("")){
                    catNames += thisName;                 
                 } else {   
                    catNames += ", ";
                    catNames += thisName;
                 }   
           } 
        }    
        return catNames;
    }

/******qqqqq    
    public boolean canSave() {
        if (isNew() || isChanged("loginId") || isChanged("password")) {

            String id = getLoginId();
            String pw = getPassword();

            if (pw == null || pw.length() < 5 || pw == "password"){
                throw new OAException(VetUser.class, "Password must have at lease 5 characters");
            }
            if (id == null || id.length() < 3 || id == "loginid"){
                throw new OAException(VetUser.class, "Login Id must have at lease 3 characters");
            }

            Hub hub = new Hub(VetUser.class);
            hub.select("loginId = '" + id + "'");

            Object obj = hub.elementAt(0);
            if (hub.size() > 1 || (obj != null && obj != this)) {
                throw new OAException(VetUser.class, "Login Id/Password Invalid ");
            }

       }
        return true;
    }
***/
    
    public Hub getEmpQueryVets() {
        if (hubEmpQueryVet == null) {
            hubEmpQueryVet = getHub("empQueryVets");
        }
        return hubEmpQueryVet;
    }

/* 20090703 was:    
    @Override
    protected void onSave(boolean fullSave, boolean force) {
        if (isNew()) {
        	Resume resume = getResume();
        	if (resume != null) {
        		int idx = resume.getId();
        		if (idx != 0) this.setId(idx);
        	}
        }
    	super.onSave(fullSave, force);
    }
*/    
    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("preferLocations",Location.class,OALinkInfo.MANY, false,"vetUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("resume",Resume.class,OALinkInfo.ONE, true,"vetUser"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("education",Education.class,OALinkInfo.ONE, false,"vetUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("categories",Category.class,OALinkInfo.MANY, false,"vetUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("rejectLocations",Location.class,OALinkInfo.MANY, false,"vetUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetSecurity",VetSecurity.class,OALinkInfo.ONE, false,"vetUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("rank",Rank.class,OALinkInfo.ONE, false,"vetUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("service",Service.class,OALinkInfo.ONE, false,"vetUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("serviceStatus",ServiceStatus.class,OALinkInfo.ONE, false,"vetUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("empQueryVets",EmpQueryVet.class,OALinkInfo.MANY, false,"vetUser"));
    }
}

