package com.viaoa.object;

public class OAObjectLocal extends OAObject {


	
	

	
	
	// Object Info 
	protected static OAObjectInfo oaObjectInfo;
	public static OAObjectInfo getOAObjectInfo() {
	    return oaObjectInfo;
	}
	static {
	    oaObjectInfo = new OAObjectInfo(new String[] {});
	     
	    oaObjectInfo.setLocalOnly(true);
	    oaObjectInfo.setUseDataSource(false);
	    oaObjectInfo.setAddToCache(false);
	    oaObjectInfo.setInitializeNewObjects(false);
	}
	
}
