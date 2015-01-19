package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class EmployerUser extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String description;
    protected String loginId;
    protected String password;
    protected String email;
    protected String name;
    protected boolean admin;

    // Links to other objects
    protected Employer employer;
    protected Hub hubEmpQuery;

    public EmployerUser() {
    }

    public EmployerUser(int id) {
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

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        String old = this.description;
        this.description = description;
        firePropertyChange("description",old,description);
    }

    public String getLoginId() {
        return loginId;
    }
    public void setLoginId(String loginId) {
        String old = this.loginId;
        this.loginId = loginId;
        firePropertyChange("loginId",old,loginId);
    }

    public boolean getAdmin() {
        return admin;
    }
    public void setAdmin(boolean b) {
        boolean old = this.admin;
        this.admin = b;
        firePropertyChange("admin",old,admin);
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        String old = this.password;
        this.password = password;
        firePropertyChange("password",old,password);
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        String old = this.email;
        this.email = email;
        firePropertyChange("email",old,email);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange("name",old,name);
    }

    public Employer getEmployer() {
        if (employer == null) employer = (Employer) getObject("employer");
        return employer;
    }

    public void setEmployer(Employer employer) {
        Employer old = this.employer;
        this.employer = employer;
        firePropertyChange("employer",old,employer);
    }

    public Hub getEmpQueries() {
        if (hubEmpQuery == null) {
            hubEmpQuery = getHub("empQueries");
        }
        return hubEmpQuery;
    }
    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("employer",Employer.class,OALinkInfo.ONE, false,"employerUsers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("empQueries",EmpQuery.class,OALinkInfo.MANY, true,"employerUser"));
    }
/**qq
    public boolean canSave() {
        if (isNew() || isChanged("loginId") || isChanged("password")) {

            String id = getLoginId();
            String pw = getPassword();

            if (pw == null || pw.length() < 5 || pw == "password"){
                throw new OAException(EmployerUser.class, "Password must have at lease 5 characters");
            }
            if (id == null || id.length() < 3 || id == "userid"){
                throw new OAException(EmployerUser.class, "Login Id must have at lease 3 characters");
            }

            Hub hub = new Hub(EmployerUser.class);
            hub.select("loginId = '" + id + "'");

            if (isNew() && hub.size() > 0){
                throw new OAException(EmployerUser.class, "Login Id/Password Invalid " + hub.size() + " " + id);
            }

            if (!isNew() && hub.size() > 1){
                throw new OAException(EmployerUser.class, "Login Id/Password Invalid " + hub.size() + " " + id);
            }

        }
        return true;
    }
**/
}

