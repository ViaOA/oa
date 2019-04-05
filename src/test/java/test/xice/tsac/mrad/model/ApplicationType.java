package test.xice.tsac.mrad.model;

import java.io.Serializable;



/**
 * Information about the type of application.
 * @author vvia
 *
 * @see Application#getApplicationType() to get/set the tpp type
 * @see MRADClient#update(Application) to send the App info to tsac server.
 */
public class ApplicationType implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int serverTypeId;
    /**
     * Unique code that identifies the app type.
     */
    protected String code;
    protected String name;
    protected String description;

    
    protected String directory;
    protected String startCommand;
    protected String stopCommand;
    protected String snapshotStartCommand;
    
    
    /**
     * See IServerId.java for list of servers.
     * see: http://icespace.cpex.com/docs/DOC-24614
     */
    public int getServerTypeId() {
        return serverTypeId;
    }
    public void setServerTypeId(int serverTypeId) {
        this.serverTypeId = serverTypeId;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDirectory() {
        return directory;
    }
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    public String getStartCommand() {
        return startCommand;
    }
    public void setStartCommand(String startCommand) {
        this.startCommand = startCommand;
    }
    public String getStopCommand() {
        return stopCommand;
    }
    public void setStopCommand(String stopCommand) {
        this.stopCommand = stopCommand;
    }
    public String getSnapshotStartCommand() {
        return snapshotStartCommand;
    }
    public void setSnapshotStartCommand(String snapshotStartCommand) {
        this.snapshotStartCommand = snapshotStartCommand;
    }
    
}
