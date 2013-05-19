package com.viaoa.object;

public interface OASaveDeleteListener {
	public void onInsert(OAObject obj);
	public void onUpdate(OAObject obj);
	public void onDelete(OAObject obj);
}
