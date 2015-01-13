// Generated by OABuilder
package com.theice.tsac.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsac.model.oa.filter.*;
import com.theice.tsac.model.oa.propertypath.*;
 
@OAClass(
    shortName = "sg",
    displayName = "Server Group",
    displayProperty = "name",
    sortProperty = "seq",
    rootTreePropertyPaths = {
        "[Site]."+Site.P_Environments+"."+Environment.P_Silos+"."+Silo.P_ServerGroups
    }
)
@OATable(
    indexes = {
        @OAIndex(name = "ServerGroupSilo", columns = { @OAIndexColumn(name = "SiloId") })
    }
)
public class ServerGroup extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Code = "Code";
    public static final String P_Code = "Code";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
     
     
    public static final String PROPERTY_MRADClients = "MRADClients";
    public static final String P_MRADClients = "MRADClients";
    public static final String PROPERTY_Schedules = "Schedules";
    public static final String P_Schedules = "Schedules";
    public static final String PROPERTY_Silo = "Silo";
    public static final String P_Silo = "Silo";
     
    protected int id;
    protected String code;
    protected String name;
    protected int seq;
     
    // Links to other objects.
    protected transient Hub<MRADClient> hubMRADClients;
    protected transient Hub<Schedule> hubSchedules;
    protected transient Silo silo;
     
    public ServerGroup() {
    }
     
    public ServerGroup(int id) {
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
    @OAProperty(maxLength = 15, displayLength = 10, columnLength = 8)
    @OAColumn(maxLength = 15)
    public String getCode() {
        return code;
    }
    
    public void setCode(String newValue) {
        fireBeforePropertyChange(P_Code, this.code, newValue);
        String old = code;
        this.code = newValue;
        firePropertyChange(P_Code, old, this.code);
    }
    @OAProperty(maxLength = 55, displayLength = 25, columnLength = 22)
    @OAColumn(maxLength = 55)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
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
    @OAMany(
        toClass = MRADClient.class, 
        reverseName = MRADClient.P_ServerGroup
    )
    public Hub<MRADClient> getMRADClients() {
        if (hubMRADClients == null) {
            hubMRADClients = (Hub<MRADClient>) getHub(P_MRADClients);
        }
        return hubMRADClients;
    }
    
    @OAMany(
        toClass = Schedule.class, 
        reverseName = Schedule.P_ServerGroup
    )
    public Hub<Schedule> getSchedules() {
        if (hubSchedules == null) {
            hubSchedules = (Hub<Schedule>) getHub(P_Schedules);
        }
        return hubSchedules;
    }
    
    @OAOne(
        reverseName = Silo.P_ServerGroups, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"SiloId"})
    public Silo getSilo() {
        if (silo == null) {
            silo = (Silo) getObject(P_Silo);
        }
        return silo;
    }
    
    public void setSilo(Silo newValue) {
        fireBeforePropertyChange(P_Silo, this.silo, newValue);
        Silo old = this.silo;
        this.silo = newValue;
        firePropertyChange(P_Silo, old, this.silo);
    }
    
    // start - Start
    public void start() {
    }
     
    // stop - Stop
    public void stop() {
    }
     
    // kill - Kill
    public void kill() {
    }
     
    // suspend - Suspend
    public void suspend() {
    }
     
    // resume - Resume
    public void resume() {
    }
     
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.code = rs.getString(2);
        this.name = rs.getString(3);
        this.seq = (int) rs.getInt(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, ServerGroup.P_Seq, true);
        }
        int siloFkey = rs.getInt(5);
        if (!rs.wasNull() && siloFkey > 0) {
            setProperty(P_Silo, new OAObjectKey(siloFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
