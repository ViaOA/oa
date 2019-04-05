package test.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

public class BatchRow extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String reference;
    protected String rateFrom;
    protected String rateTo;
    protected String hourly;
    protected String benefits;
    protected String city;
    protected String region;
    protected String state;
    protected String country;
    protected String title;
    protected String description;
    protected String contact;
    protected String email;
    protected String phone;
    protected String positions;
    protected String origCategory1;
    protected String origCategory2;
    protected String origCategory3;
    protected String origCategory4;
    protected String origCategory5;
    protected String contract;
    protected boolean errorFlag;
    protected boolean newFlag;
    protected String error;
    protected String fulltime;
    
    // Links to other objects
    protected Job job;
    protected Batch batch;
    protected Location location;
    protected Hub hubCategory;
    protected Folder folder;

  //  Conversion values
    protected double rateToDouble;
    protected double rateFromDouble;
    protected int positionsInteger;
    protected boolean hourlyBoolean;
    
    public BatchRow() {
    }
     
    public BatchRow(int id) {
        setId(id);
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }
    
    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        String old = this.reference;
        this.reference = reference;
        firePropertyChange("reference",old,reference);
    }
    
    public String getRateFrom() {
        return rateFrom;
    }
    public void setRateFrom(String rateFrom) {
        String old = this.rateFrom;
        this.rateFrom = rateFrom;
        firePropertyChange("rateFrom",old,rateFrom);
    }
    
    public String getRateTo() {
        return rateTo;
    }
    public void setRateTo(String rateTo) {
        String old = this.rateTo;
        this.rateTo = rateTo;
        firePropertyChange("rateTo",old,rateTo);
    }
    
    public String getHourly() {
        return hourly;
    }
    public void setHourly(String hourly) {
        String old = this.hourly;
        this.hourly = hourly;
        firePropertyChange("hourly",old,hourly);
    }
    
    public String getBenefits() {
        return benefits;
    }
    public void setBenefits(String benefits) {
        String old = this.benefits;
        this.benefits = benefits;
        firePropertyChange("benefits",old,benefits);
    }
    
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        String old = this.city;
        this.city = city;
        firePropertyChange("city",old,city);
    }
    
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        String old = this.region;
        this.region = region;
        firePropertyChange("region",old,region);
    }
    
    public String getState() {
        return state;
    }
    public void setState(String state) {
        String old = this.state;
        this.state = state;
        firePropertyChange("state",old,state);
    }
    
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        String old = this.country;
        this.country = country;
        firePropertyChange("country",old,country);
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        String old = this.title;
        this.title = title;
        firePropertyChange("title",old,title);
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        String old = this.description;
        this.description = description;
        firePropertyChange("description",old,description);
    }
    
    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        String old = this.contact;
        this.contact = contact;
        firePropertyChange("contact",old,contact);
    }
    
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        String old = this.email;
        this.email = email;
        firePropertyChange("email",old,email);
    }
    
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        String old = this.phone;
        this.phone = phone;
        firePropertyChange("phone",old,phone);
    }
    
    public String getPositions() {
        return positions;
    }
    public void setPositions(String positions) {
        String old = this.positions;
        this.positions = positions;
        firePropertyChange("positions",old,positions);
    }
    
    public String getOrigCategory1() {
        return origCategory1;
    }
    public void setOrigCategory1(String origCategory1) {
        String old = this.origCategory1;
        this.origCategory1 = origCategory1;
        firePropertyChange("origCategory1",old,origCategory1);
    }
    
    public String getOrigCategory2() {
        return origCategory2;
    }
    public void setOrigCategory2(String origCategory2) {
        String old = this.origCategory2;
        this.origCategory2 = origCategory2;
        firePropertyChange("origCategory2",old,origCategory2);
    }
    
    public String getOrigCategory3() {
        return origCategory3;
    }
    public void setOrigCategory3(String origCategory3) {
        String old = this.origCategory3;
        this.origCategory3 = origCategory3;
        firePropertyChange("origCategory3",old,origCategory3);
    }
    
    public String getOrigCategory4() {
        return origCategory4;
    }
    public void setOrigCategory4(String origCategory4) {
        String old = this.origCategory4;
        this.origCategory4 = origCategory4;
        firePropertyChange("origCategory4",old,origCategory4);
    }
    
    public String getOrigCategory5() {
        return origCategory5;
    }
    public void setOrigCategory5(String origCategory5) {
        String old = this.origCategory5;
        this.origCategory5 = origCategory5;
        firePropertyChange("origCategory5",old,origCategory5);
    }
    
    public String getContract() {
        return contract;
    }
    public void setContract(String contract) {
        String old = this.contract;
        this.contract = contract;
        firePropertyChange("contract",old,contract);
    }
    
    public boolean getErrorFlag() {
        return errorFlag;
    }
    public void setErrorFlag(boolean errorFlag) {
        boolean old = this.errorFlag;
        this.errorFlag = errorFlag;
        firePropertyChange("errorFlag",old,errorFlag);
    }
    
    public boolean getNewFlag() {
        return newFlag;
    }
    public void setNewFlag(boolean newFlag) {
        boolean old = this.newFlag;
        this.newFlag = newFlag;
        firePropertyChange("newFlag",old,newFlag);
    }
    
    public String getError() {
        return error;
    }
    public void setError(String error) {
        String old = this.error;
        this.error = error;
        firePropertyChange("error",old,error);
    }
    
    public String getFulltime() {
        return fulltime;
    }
    public void setFulltime(String fulltime) {
        String old = this.fulltime;
        this.fulltime = fulltime;
        firePropertyChange("fulltime",old,fulltime);
    }
    
    public Job getJob() {
        if (job == null) job = (Job) getObject("job");
        return job;
    }
    
    public void setJob(Job job) {
        Job old = this.job;
        this.job = job;
        firePropertyChange("job",old,job);
    }
    
    public Batch getBatch() {
        if (batch == null) batch = (Batch) getObject("batch");
        return batch;
    }
    
    public void setBatch(Batch batch) {
        Batch old = this.batch;
        this.batch = batch;
        firePropertyChange("batch",old,batch);
    }
    
    public Location getLocation() {
        if (location == null) location = (Location) getObject("location");
        return location;
    }
    
    public void setLocation(Location location) {
        Location old = this.location;
        this.location = location;
        firePropertyChange("location",old,location);
    }
    
    public Hub getCategories() {
        if (hubCategory == null) hubCategory = getHub("categories");
        return hubCategory;
    }
    
    public Folder getFolder() {
        if (folder == null) folder = (Folder) getObject("folder");
        return folder;
    }
    
    public void setFolder(Folder folder) {
        Folder old = this.folder;
        this.folder = folder;
        firePropertyChange("folder",old,folder);
    }

    public String getCategoryNames() {
        String catNames = "";
        String thisName;      
        Hub h = getCategories();
        for (int i=0; ;i++) {
        	Category cat = (Category) h.getAt(i);
        	if (cat == null) break;
        	thisName = cat.getName();
        	if (!thisName.equals("")){
                 if (catNames.equals("")){
                    catNames += thisName;                 
                 } else {   
                    catNames += ",";
                    catNames += thisName;
                 }   
           } 
        }    
        return catNames;
    }

// Validate positions Conversion    
    public boolean checkPositions() {
       if (getPositions() != null && !getPositions().equals("")) {
            try {
                setPositionsInteger((Integer.valueOf(getPositions()).intValue()));
            }
            catch (Exception e){   
                return false;
            }
       }
       return true;
    }
    
    public int getPositionsInteger() {
       return positionsInteger;
    }
    
    public void setPositionsInteger(int x) {
        int old = this.positionsInteger;
        this.positionsInteger = x;
        firePropertyChange("positionsInteger",old,x);
    }
    
    // Validate hourly Conversion    
    public boolean checkHourly() {
       if (getHourly() != null && !getHourly().equals("")) {
            try {
                setHourlyBoolean((Boolean.valueOf(getHourly()).booleanValue()));
            }
            catch (Exception e){   
                return false;
            }
       }
       return true;
    }
    
    public boolean getHourlyBoolean() {
       return hourlyBoolean;
    }
    
    public void setHourlyBoolean(boolean x) {
        boolean old = this.hourlyBoolean;
        this.hourlyBoolean = x;
        firePropertyChange("hourlyBoolean",old,x);
    }
    
    
    // Validate rateTo Conversion
    public boolean checkRateTo() {
       if (getRateTo() != null && !getRateTo().equals("")) {
            try {  
                setRateToDouble((Double.valueOf(getRateTo()).doubleValue()));
            }
            catch (Exception e){   
                return false;
            }
       }
       return true;
    }
    
    public double getRateToDouble() {
       return rateToDouble;
    }
    
    public void setRateToDouble(double x) {
        double old = this.rateToDouble;
        this.rateToDouble = x;
        firePropertyChange("rateToDouble",old,x);
    }
    
    // Validate rateFrom Conversion
    public boolean checkRateFrom() {
       if (getRateFrom() != null && !getRateFrom().equals("")) {
            try {
                setRateFromDouble((Double.valueOf(getRateFrom()).doubleValue()));
            }
            catch (Exception e){   
                return false;
            }
       }
       return true;
    }
    
    public double getRateFromDouble() {
       return rateFromDouble;
    }
    
    public void setRateFromDouble(double x) {
        double old = this.rateFromDouble;
        this.rateFromDouble = x;
        firePropertyChange("rateFromDouble",old,x);
    }
    
    public boolean checkHourlyValue() {
        if (getHourly() != null && !getHourly().equals("") && !getHourly().equalsIgnoreCase("hourly") && !getHourly().equalsIgnoreCase("salary") && !getHourly().equalsIgnoreCase("s") && !getHourly().equalsIgnoreCase("h")) {
            return false;
        }
        return true;
    }
    
    public boolean checkContract() {
        if (getContract() != null && !getContract().equals("") && !getContract().equalsIgnoreCase("permanent") && !getContract().equalsIgnoreCase("contract") && !getContract().equalsIgnoreCase("p") && !getContract().equalsIgnoreCase("c")) {
            return false;
        }
        return true;
    }
    
    public boolean checkFullTime() {
        if (getFulltime() != null && !getFulltime().equals("") && !getFulltime().equalsIgnoreCase("full-time") && !getFulltime().equalsIgnoreCase("part-time") && !getFulltime().equalsIgnoreCase("ft") && !getFulltime().equalsIgnoreCase("pt") && !getFulltime().equalsIgnoreCase("f") && !getFulltime().equalsIgnoreCase("p")) {
            return false;
        }
        return true;
    }
    
    public boolean checkNewCat(String inCategory) {
    	if (inCategory == null) return false;
        String thisCategory;
        Hub h = getCategories();
        for (int i=0; ;i++) {
        	Category cat = (Category) h.getAt(i);
        	if (cat == null) break;
            thisCategory = cat.getName();
            if (thisCategory.equals(inCategory)) return false;
        }
        return true;
    }

/******qqqqqqqqqqqqqqqqqqqqqqqqq   
    public boolean canSave() {
        if (getNew() || getChanged()) {
            Hub hub;
// Set flag for new or update and update batch counters when necessary           
            if ((isNew() || isChanged("reference")) && getBatch() != null) {
                hub = new Hub(Job.class);
                String s = "employerId = " + getBatch().getEmployer().getId() + " and reference = '" + getReference() + "'";
                hub.select(s);
                if (hub.size() > 0){
                    setNewFlag(false);
                } else {
                    setNewFlag(true);
                }
            }   
// Validate State
            hub = new Hub(State.class);
            hub.select("name = '" + getState() + "'");
            if (hub.size() != 1) {
                hub.select("abbrev = '" + getState() + "'");
                if (hub.size() == 1) {                    
                    setState(((State)hub.elementAt(0)).getName());
                }  
            }
            
// Assign Categories
            if (isNew()){
                hub = new Hub(Category.class);
                
                hub.select("name = '" + getOrigCategory1() + "'");
                if (hub.size() == 1 && checkNewCat(getOrigCategory1())) {
                    getCategories().addElement(hub.elementAt(0));
                }

                hub.select("name = '" + getOrigCategory2() + "'");
                if (hub.size() == 1 && checkNewCat(getOrigCategory2())) {
                    getCategories().addElement(hub.elementAt(0));
                }
                
                hub.select("name = '" + getOrigCategory3() + "'");
                if (hub.size() == 1 && checkNewCat(getOrigCategory3())) {
                    getCategories().addElement(hub.elementAt(0));
                }

                hub.select("name = '" + getOrigCategory4() + "'");
                if (hub.size() == 1 && checkNewCat(getOrigCategory4())) {
                    getCategories().addElement(hub.elementAt(0));
                }

                hub.select("name = '" + getOrigCategory5() + "'");
                if (hub.size() == 1 && checkNewCat(getOrigCategory5())) {
                    getCategories().addElement(hub.elementAt(0));
                }
            }

// Set Folder
            if (isNew()) {
                hub = new Hub(Folder.class);
                hub.select("Id = 1");
                setFolder((Folder)hub.elementAt(0));
            }
            
// Validate rateFrom
            if (!isNew() && isChanged("rateFrom")) {
                if (getRateFromDouble() > 1000.00){
                    setHourlyBoolean(false);
                } else setHourlyBoolean(true);
            }   
        }
        return true;
    }

    public void afterSave() {
        String eflag = new String();
        Hub hub;
        
// Set flag for new or update and update batch counters when necessary           
        if (getCategories() == null){
            if (!getOrigCategory1().equals("") && !getOrigCategory1().equals(" ")){
                eflag = eflag +  ",category";
            }    
        }

// Validate State
        hub = new Hub(State.class);
        hub.select("name = '" + getState() + "'");
        if (hub.size() != 1) {
            eflag = eflag + ",state";
        }
// Validate positions
        if (!checkPositions()) {
            eflag = eflag + ",positions";
        }
// Validate rateTo
        if (!checkRateTo()) {
                eflag = eflag + ",rateto";
        }
// Validate rateFrom
        if (!checkRateFrom()) {
                eflag = eflag + ",ratefrom";
        }    
// Validate reference
        if (getReference() == null || getReference().equals("")) {
            eflag = eflag + ",reference";
        }
// Validate Hourly/Salary
        if (checkHourlyValue() == false) {
            eflag = eflag + ",hourly";
        }
// Validate Contract/Permanent
        if (checkContract() == false) {
            eflag = eflag + ",contract";
        }
// Validate Full-Time/Part-Time
        if (checkFullTime() == false) {
            eflag = eflag + ",fulltime";
        }
        
// set Flags
        setError(eflag);
        if(getError().equals("")){
            setErrorFlag(false);
        } else setErrorFlag(true);
        save();
    }
**********/    


    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("job",Job.class,OALinkInfo.ONE, false,"batchRows"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("batch",Batch.class,OALinkInfo.ONE, false,"batchRows"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("location",Location.class,OALinkInfo.ONE, false,"batchRows"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("categories",Category.class,OALinkInfo.MANY, false,"batchRows"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("folder",Folder.class,OALinkInfo.ONE, false,"batchRows"));
    }
}

