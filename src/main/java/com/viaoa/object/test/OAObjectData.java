package com.viaoa.object.test;

import java.util.HashMap;
import java.util.Hashtable;

import com.viaoa.object.OAObjectKey;
import com.viaoa.util.OANullObject;

public class OAObjectData {
	public boolean bServerCache;
	public int guid;
	public Hashtable htLink;
	public boolean changedFlag;
	public Hashtable hashNull;
	public HashMap hmProperty;
	public Hashtable hashTransientProperty;
	public boolean newFlag;
	public OAObjectKey objectKey;
	public Object[] weakHubs;
	
	
	public void addHashLink(String prop, Object value) {
		if (prop == null) return;
		if (htLink == null) htLink = new Hashtable();
		if (value == null) value = OANullObject.instance;
		htLink.put(prop.toUpperCase(), value);
	}
	
	public void setHashNull(String[] props) {
		if (hashNull == null) hashNull = new Hashtable();
		else hashNull.clear();
		for (int i=0; props != null && i<props.length; i++) {
			String s = props[i];
			if (s == null) continue;
			s = s.toUpperCase();
			hashNull.put(s, s);
		}
	}
	public void addHashNull(String prop) {
		if (prop == null) return;
		if (hashNull == null) hashNull = new Hashtable();
		prop = prop.toUpperCase();
		hashNull.put(prop, prop);
	}
	public void removeHashNull(String prop) {
		if (prop == null || hashNull == null) return;
		prop = prop.toUpperCase();
		hashNull.remove(prop);
	}
}
