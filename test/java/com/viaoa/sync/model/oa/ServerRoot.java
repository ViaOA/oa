package com.viaoa.sync.model.oa;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAMany;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;


/**
 * Root Object that is automatically updated between the Server and Clients.
 * ServerController will do the selects for these objects.
 * Model will share these hubs after the application is started.
 * */

@OAClass(
    useDataSource = false,
    displayProperty = "Id"
)
public class ServerRoot extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Companies = "Companies";

	private int id;
    private transient Hub<Company> hubCompanies;
    
	public ServerRoot() {
		setId(777);
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		int old = this.id;
		this.id = id;
		firePropertyChange(PROPERTY_Id, old, id);
	}

    @OAMany()
    public Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = (Hub<Company>) super.getHub(PROPERTY_Companies);
        }
        return hubCompanies;
    }

}



