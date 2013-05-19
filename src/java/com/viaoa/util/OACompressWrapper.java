/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/

package com.viaoa.util;

import java.io.*;
import java.util.logging.Logger;
import java.util.zip.*;

/**
 * Object wrapper used to compress an object during serialization.
 * @author vvia
 */
public final class OACompressWrapper implements Serializable {
    static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(OACompressWrapper.class.getName());
    
    private Object object; // object to serialize

    /**
     * @param object to serialize
     */
    public OACompressWrapper(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
    
    /**
     * Called by objectStream to serialize wrapper.  
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeBoolean(object != null);
        if (object != null) {
            Deflater d = new Deflater(Deflater.DEFAULT_COMPRESSION);//BEST_SPEED BEST_COMPRESSION);
        	DeflaterOutputStream dos = new DeflaterOutputStream(stream, d, 1024*2);
        	ObjectOutputStream oos = new ObjectOutputStream(dos);
        	oos.writeObject(object);
            //oos.flush();
            dos.finish();
            //dos.flush();
            // dos.close(); // might affect stream by closing it (?? not sure)
            // long sizeBefore = d.getBytesRead();
            // long sizeAfter = d.getBytesWritten();
        }        
    }


    /**
     * Called by objectStream to deserialize a wrapper. 
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        if (stream.readBoolean()) {
    		Inflater inflater = new Inflater();
        	InflaterInputStream iis = new InflaterInputStream(stream, inflater, 1024*2);
        	
        	ObjectInputStream ois = new ObjectInputStream(iis);
        	object = ois.readObject();
    	}
    }

    

}


