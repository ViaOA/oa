package com.viaoa.object;


public interface OATrigger<T> {

    public boolean isUsed(T obj);

    public void onTrigger(T obj);

}
