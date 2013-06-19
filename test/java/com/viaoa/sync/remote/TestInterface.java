package com.viaoa.sync.remote;

import com.viaoa.hub.Hub;
import com.viaoa.sync.model.oa.Company;

public interface TestInterface {
    Hub<Company> getCompanies();
}
