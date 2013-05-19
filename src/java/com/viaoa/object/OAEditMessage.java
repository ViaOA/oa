package com.viaoa.object;


public class OAEditMessage {
    static final long serialVersionUID = 1L;
    private String msg;   
    
    public OAEditMessage() {
    }
    public OAEditMessage(String msg) {
        this.msg = msg;
    }
    public void setMessage(String msg) {
        this.msg = msg;
    }
    public String getMessage() {
        return this.msg;
    }
}	
