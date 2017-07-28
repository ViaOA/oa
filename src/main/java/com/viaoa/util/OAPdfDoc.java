package com.viaoa.util;

public class OAPdfDoc {
    private byte[] bs;
    
    public OAPdfDoc() {
    }
    public OAPdfDoc(byte[] bs) {
        setBytes(bs);
    }
    
    public byte[] getBytes() {
        return bs;
    }
    public void setBytes(byte[] bs) {
        this.bs = bs;
    }
}
