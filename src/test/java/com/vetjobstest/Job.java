package com.vetjobstest;

import java.util.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.object.*;

public class Job extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String reference;
    protected OADate createDate;
    protected OADate refreshDate;
    protected double rateFrom;
    protected double rateTo;
    protected boolean hourly;
    protected boolean contract;
    protected boolean fulltime;
    protected String title;
    protected String benefits;
    protected String description;
    protected String city;
    protected String region;
    protected String state;
    protected String country;
    protected String contact;
    protected String email;
    protected boolean autoResponse;
    protected int positionsAvailable;
    protected int searchCount, viewCount, clickCount;
    protected int searchCountMTD, viewCountMTD, clickCountMTD;
    protected int searchCountWTD, viewCountWTD, clickCountWTD;
    protected OADate lastMTD, lastWTD;
    
    
    // Links to other objects
    protected Hub hubLocation;
    protected Hub hubCategory;
    protected Employer employer;
    protected Folder folder;
    
    public Job() {
    	if (!isLoading()) {
    		setCreateDate(new OADate());
    		setRefreshDate(new OADate());
    	}
    }  
     
    public Job(int id) {
        setId(id);
    }

    
    public int getSearchCount() {
        return searchCount;
    }
    public void setSearchCount(int searchCount) {
        int old = this.searchCount;
        this.searchCount = searchCount;
        firePropertyChange("searchCount",old,searchCount);
    }
    public int getViewCount() {
        return viewCount;
    }
    public void setViewCount(int viewCount) {
        int old = this.viewCount;
        this.viewCount = viewCount;
        firePropertyChange("viewCount",old,viewCount);
    }
    public int getClickCount() {
        return clickCount;
    }
    public void setClickCount(int clickCount) {
        int old = this.clickCount;
        this.clickCount = clickCount;
        firePropertyChange("clickCount",old,clickCount);
    }

    // WTD & MTD counters need to be set back to 0 when date changes
    protected void updateCounters() {
        OADate d = getLastMTD();
        if (d != null) {
            OADate d1 = new OADate(d);
            d1 = new OADate(d1.getYear(), d1.getMonth(), d1.getDay());

            OADate d2 = new OADate(); // today
            d2 = new OADate(d2.getYear(), d2.getMonth(), 1); // first day of this month
                
            if (d1.compareTo(d2) < 0) {
                searchCountMTD = 0;
                viewCountMTD = 0;
                clickCountMTD = 0;
                setLastMTD(new OADate());   
            }
        }
        else {
            setLastMTD(new OADate());   
        }

        d = getLastWTD();
        if (d != null) {
            OADate d1 = new OADate(d);

            d1 = new OADate(d1.getYear(), d1.getMonth(), d1.getDay());
            
            OADate d2 = new OADate(); // today
            d2 = new OADate(d2.getYear(), d2.getMonth(), d2.getDay());

            GregorianCalendar cal = new GregorianCalendar();
            int dayInWeek = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            
            d2.addDays(-dayInWeek); // set to first day of this week
                
            if (d1.compareTo(d2) < 0) {
                searchCountWTD = 0;
                viewCountWTD = 0;
                clickCountWTD = 0;
                setLastWTD(new OADate());   
            }
        }
        else {
            setLastWTD(new OADate());   
        }
    
    
    }    

    
    public int getSearchCountMTD() {
        updateCounters();
        return searchCountMTD;
    }
    public void setSearchCountMTD(int searchCountMTD) {
        int old = this.searchCountMTD;
        this.searchCountMTD = searchCountMTD;
        firePropertyChange("searchCountMTD",old,searchCountMTD);
    }
    public int getViewCountMTD() {
        updateCounters();
        return viewCountMTD;
    }
    public void setViewCountMTD(int viewCountMTD) {
        int old = this.viewCountMTD;
        this.viewCountMTD = viewCountMTD;
        firePropertyChange("viewCountMTD",old,viewCountMTD);
    }
    public int getClickCountMTD() {
        updateCounters();
        return clickCountMTD;
    }
    public void setClickCountMTD(int clickCountMTD) {
        int old = this.clickCountMTD;
        this.clickCountMTD = clickCountMTD;
        firePropertyChange("clickCountMTD",old,clickCountMTD);
    }
    public OADate getLastMTD() {
        return lastMTD;
    }
    public void setLastMTD(OADate lastMTD) {
        OADate old = this.lastMTD;
        this.lastMTD = lastMTD;
        firePropertyChange("lastMTD",old,lastMTD);
    }


    public int getSearchCountWTD() {
        updateCounters();
        return searchCountWTD;
    }
    public void setSearchCountWTD(int searchCountWTD) {
if ("9678544V".equals(getReference())) {
	int xxx = 33;
}
        int old = this.searchCountWTD;
        this.searchCountWTD = searchCountWTD;
        firePropertyChange("searchCountWTD",old,searchCountWTD);
    }
    public int getViewCountWTD() {
        updateCounters();
        return viewCountWTD;
    }
    public void setViewCountWTD(int viewCountWTD) {
        int old = this.viewCountWTD;
        this.viewCountWTD = viewCountWTD;
        firePropertyChange("viewCountWTD",old,viewCountWTD);
    }
    public int getClickCountWTD() {
        updateCounters();
        return clickCountWTD;
    }
    public void setClickCountWTD(int clickCountWTD) {
        int old = this.clickCountWTD;
        this.clickCountWTD = clickCountWTD;
        firePropertyChange("clickCountWTD",old,clickCountWTD);
    }
    public OADate getLastWTD() {
        return lastWTD;
    }
    public void setLastWTD(OADate lastWTD) {
        OADate old = this.lastWTD;
        this.lastWTD = lastWTD;
        firePropertyChange("lastWTD",old,lastWTD);
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
    
    public OADate getCreateDate() {
        return createDate;
    }
    public void setCreateDate(OADate createDate) {
        OADate old = this.createDate;
        this.createDate = createDate;
        firePropertyChange("createDate",old,createDate);
    }
    
    public OADate getRefreshDate() {
        return refreshDate;
    }
    public void setRefreshDate(OADate refreshDate) {
        OADate old = this.refreshDate;
        this.refreshDate = refreshDate;
        firePropertyChange("refreshDate",old,refreshDate);
    }
    
    public double getRateFrom() {
        return rateFrom;
    }
    public void setRateFrom(double rateFrom) {
        double old = this.rateFrom;
        this.rateFrom = rateFrom;
        firePropertyChange("rateFrom",old,rateFrom);
    }
    
    public double getRateTo() {
        return rateTo;
    }
    public void setRateTo(double rateTo) {
        double old = this.rateTo;
        this.rateTo = rateTo;
        firePropertyChange("rateTo",old,rateTo);
    }
    
    public boolean getHourly() {
        return hourly;
    }
    public void setHourly(boolean hourly) {
        boolean old = this.hourly;
        this.hourly = hourly;
        firePropertyChange("hourly",old,hourly);
    }
    
    public boolean getContract() {
        return contract;
    }
    public void setContract(boolean contract) {
        boolean old = this.contract;
        this.contract = contract;
        firePropertyChange("contract",old,contract);
    }
    
    public boolean getFulltime() {
        return fulltime;
    }
    public void setFulltime(boolean fulltime) {
        boolean old = this.fulltime;
        this.fulltime = fulltime;
        firePropertyChange("fulltime",old,fulltime);
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        String old = this.title;
        this.title = title;
        firePropertyChange("title",old,title);
    }
    
    public String getBenefits() {
        return benefits;
    }
    public void setBenefits(String benefits) {
        String old = this.benefits;
        this.benefits = benefits;
        firePropertyChange("benefits",old,benefits);
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        String old = this.description;
        this.description = description;
        firePropertyChange("description",old,description);
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
    
    public boolean getAutoResponse() {
        return autoResponse;
    }
    public void setAutoResponse(boolean autoResponse) {
        boolean old = this.autoResponse;
        this.autoResponse = autoResponse;
        firePropertyChange("autoResponse",old,autoResponse);
    }
    
    public int getPositionsAvailable() {
        return positionsAvailable;
    }
    public void setPositionsAvailable(int positionsAvailable) {
        int old = this.positionsAvailable;
        this.positionsAvailable = positionsAvailable;
        firePropertyChange("positionsAvailable",old,positionsAvailable);
    }
    
    public Hub getLocations() {
        if (hubLocation == null) hubLocation = getHub("locations");
        return hubLocation;
    }
    
    public Hub getCategories() {
        if (hubCategory == null) hubCategory = getHub("categories");
        return hubCategory;
    }
    
    public Employer getEmployer() {
        if (employer == null) employer = (Employer) getObject("employer");
        return employer;
    }
    
    public void setEmployer(Employer employer) {
        Employer old = this.employer;
        this.employer = employer;
if (!isLoading()) {
    int xxx=4;
}
        firePropertyChange("employer",old,employer);
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
     
    public String getLocation() {
            String location = "";
            String s = getRegion();
            s = getCity();
            if (s != null) location += s + " ";
            s = getState();
            if (s != null) location += s + " ";
            s = getRegion();
            if (s != null) location += s + " ";
            return location;
    }     
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("batchRows",BatchRow.class,OALinkInfo.MANY, false,"job"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("locations",Location.class,OALinkInfo.MANY, false,"jobs"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("categories",Category.class,OALinkInfo.MANY, false,"jobs"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("employer",Employer.class,OALinkInfo.ONE, false,"jobs"));
        //was:  oaObjectInfo.addLinkInfo(new OALinkInfo("folder",Folder.class,OALinkInfo.ONE, false,"jobs"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("folder",Folder.class,OALinkInfo.ONE, false,"jobs"));
    }

    
    
    @Override
    public void save(int cascadeRule) {
        super.save(cascadeRule);
        if (getEmployer() == null) {
String s = "Job.save() - found Job that is does not have an employer, id="+id;            
System.out.println(s);
Exception e = new Exception(s);
e.printStackTrace();
        }
    }
    
}

