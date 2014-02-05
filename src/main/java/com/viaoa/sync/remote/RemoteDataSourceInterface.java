package com.viaoa.sync.remote;

public interface RemoteDataSourceInterface {

    Object datasource(int command, Object[] objects);
    
}
