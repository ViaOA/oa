package com.viaoa.comm.multiplexer.io;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import test.theice.tsac3.model.oa.*;

public class MultiplexerInputStreamControllerTest extends OAUnitTest {

    private MultiplexerInputStreamController isc;
    
    @Test
    public void test() {
        
        
    }
    
    public MultiplexerInputStreamController createMultiplexerInputStreamController() {
        
        MultiplexerInputStreamController isc = new MultiplexerInputStreamController(1) {
            @Override
            protected VirtualSocket getSocket(int id) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            protected int getMaxSocketId() {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            protected void createNewSocket(int connectionId, int id, String serverSocketName) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            protected void closeSocket(int id, boolean bSendCommand) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            protected void closeRealSocket() {
                // TODO Auto-generated method stub
                
            }
        };
        return isc;
    }
    
}
