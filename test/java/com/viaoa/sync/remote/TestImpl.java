package com.viaoa.sync.remote;

import com.viaoa.hub.Hub;
import com.viaoa.sync.model.oa.Company;

public class TestImpl implements TestInterface {

    private Hub<Company> hubCompanies;
    
    @Override
    public Hub<Company> getCompanies() {
        if (hubCompanies == null) {
            hubCompanies = new Hub<Company>(Company.class);
        }
        return hubCompanies;
    }

}
