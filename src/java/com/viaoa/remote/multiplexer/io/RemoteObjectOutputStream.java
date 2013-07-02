/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.remote.multiplexer.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.comm.multiplexer.io.VirtualSocket;

/**
 * Used internally for remoting objects between clients and servers.
 * @author vvia
 */
public class RemoteObjectOutputStream extends ObjectOutputStream {
    private ConcurrentHashMap<String, Integer> hmClassDesc;
    private AtomicInteger aiClassDesc;
    private HashMap<String, Integer> hmTemp; 

    public RemoteObjectOutputStream(VirtualSocket socket) throws IOException {
        this(socket, null, null);
    }
    
    public RemoteObjectOutputStream(
            VirtualSocket socket, 
            ConcurrentHashMap<String, Integer> hmClassDesc, 
            AtomicInteger aiClassDesc) throws IOException {

        // slowest  207000ns rt, no buffering        
        // super(socket.getOutputStream());
        
        // 95000ns rt
        // super( new BufferedOutputStream(socket.getOutputStream()) );
        
        // fastest: 76000 rt (plus less gc)
        super(new RemoteBufferedOutputStream(socket.getOutputStream()));
        this.hmClassDesc = hmClassDesc;
        this.aiClassDesc = aiClassDesc;
    }
    
    @Override
    protected void writeStreamHeader() throws IOException, StreamCorruptedException {
        // do nothing
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        if (hmTemp == null) return;
        
        // now that the objects have been sent with any new classDesc, add them to the hm 
        // has to be done after it has been fully written, to avoid race condition
        for (Map.Entry<String, Integer> entry : hmTemp.entrySet()) {
           hmClassDesc.put(entry.getKey(), entry.getValue()); 
        }
    }
    
    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        String s = desc.getName();
 
        Object objx; 
        if (hmClassDesc != null) {
            objx = hmClassDesc.get(s);
            if (objx == null && hmTemp != null) objx = hmTemp.get(s);
        }
        else {
            objx = null;
        }
        
        int id;
        if (objx == null) {
            if (hmClassDesc == null || aiClassDesc == null) {
                id = -1;
            }
            else {
                id = aiClassDesc.getAndIncrement();
                if (hmTemp == null) hmTemp = new HashMap<String, Integer>();
                hmTemp.put(s, id);
            }
            writeInt(-1);
            writeInt(id);
            super.writeClassDescriptor(desc);
        }
        else {
            id = ((Integer) objx).intValue();
            writeInt(id);
        }
    }
    
    public void writeAsciiString(String s) throws IOException, StreamCorruptedException {
        if (s == null) {
            writeShort(0);
        }
        else {
            short x = (short) s.length();
            writeShort(x);
            byte[] bs = new byte[x];
            s.getBytes(0, x, bs, 0);
            write(bs);
        }
    }
}
