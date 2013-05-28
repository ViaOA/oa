package com.viaoa.remote.multiplexer.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used internally for remoting objects between clients and servers.
 * Since we are controlling both sides of the comm, we are able to reduce a lot
 * of the overhead - ex: sending header information and class descriptions,  
 * @author vvia
 */
public class RemoteObjectInputStream extends ObjectInputStream {
    private ConcurrentHashMap<Integer, ObjectStreamClass> hmClassDesc;

    public RemoteObjectInputStream(Socket socket, 
            ConcurrentHashMap<Integer, ObjectStreamClass> hmClassDesc) throws IOException {
        super(socket.getInputStream());

        this.hmClassDesc = hmClassDesc;
    }
    @Override
    protected void readStreamHeader() throws IOException, StreamCorruptedException {
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass osc;
        int id = readInt();
        if (id >= 0) {
            osc = hmClassDesc.get(id);
        }
        else {
            id = readInt();
            osc = super.readClassDescriptor();
            if (id >= 0) {
                hmClassDesc.put(id, osc);
            }
        }
        return osc;
    }

    // faster then using readUTF
    public String readAsciiString() throws IOException {
        short x = readShort();
        if (x == 0) return null;
        byte[] bs = new byte[x];
        readFully(bs);
        String s = new String(bs, 0); // ascii only
        return s;
    }
}
