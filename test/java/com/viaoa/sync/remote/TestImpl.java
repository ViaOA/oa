package com.viaoa.sync.remote;

import com.viaoa.sync.model.oa.ServerRoot;


public class TestImpl implements TestInterface {
    private ServerRoot serverRoot;

    @Override
    public ServerRoot getServerRoot() {
        if (serverRoot == null) {
            serverRoot = new ServerRoot();
        }
        return serverRoot;
    }
    

}
