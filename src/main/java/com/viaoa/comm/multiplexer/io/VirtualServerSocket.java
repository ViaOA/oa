/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
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
