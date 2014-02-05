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
package com.viaoa.comm.multiplexer.io;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by MultiplexerServerSocketController to create new ServerSockets that work through multiplexed connections.
 * 
 * @see com.MultiplexerServer.comm.server.MultiplexerServer#createServerSocket(String, MultiplexerSocket.Type)
 * @author vvia
 */
public class VirtualServerSocket extends ServerSocket {
    /**
     * registered name for the socket, that is used by MultiplexerClient to create a MultiplexerSocket connection.
     */
    private String _name;

    /**
     * Create a new ServerSocket.
     * 
     * @param name
     *            name for MultiplexerClient to use to create a connection.
     * @param type
     *            type of socket to create.
     * @throws IOException
     */
    public VirtualServerSocket(String name) throws IOException {
        this._name = name;
    }

    /**
     * Registered name for the socket, that is used by MultiplexerClient when creating a MultiplexerSocket connection.
     */
    public String getName() {
        return _name;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
