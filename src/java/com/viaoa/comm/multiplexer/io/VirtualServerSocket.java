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
