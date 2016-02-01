package com.viaoa.comm.multiplexer.io;

import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.theice.tsac3.model.oa.*;

public class VirtualSocketTest extends OAUnitTest {
    VirtualSocket vsocket;
    
    @Test
    public void test() {
    
        
    }
    
    public VirtualSocket createVirtualSocket() {
        vsocket = new VirtualSocket(1, 1, "") {
            
            @Override
            public void write(int b) throws IOException {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void write(byte[] bs, int off, int len) throws IOException {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public int read() throws IOException {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            public int read(byte[] bs, int off, int len) throws IOException {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            public void close(boolean bSendCommand) throws IOException {
                // TODO Auto-generated method stub
                
            }
        };
        return vsocket;
    }
    
}
