package com.viaoa.cs;
 
import java.io.*;
import java.net.*;
import java.rmi.server.*;
import java.util.logging.Logger;

/**
 * Used by RMI for get Client and Server side Sockets.
 * @author vincevia
 */
public class OASocketFactory extends RMISocketFactory implements java.io.Serializable {
	private static Logger LOG = Logger.getLogger(OASocketFactory.class.getName());

	private static String host;
	private static int port;

	public OASocketFactory(String host, int port) {
		LOG.fine("host="+host+", port="+port);
		OASocketFactory.host = host;
		OASocketFactory.port = port;
	}
	
int qq=0;	
	/**
	 * This should only be called once, to get the ServerSocket that is used to
	 * accept connections.
	*/
	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		LOG.fine("asking port="+port+", using port="+OASocketFactory.port);
		ServerSocket ss = new ServerSocket(OASocketFactory.port) {
		  @Override
		    public Socket accept() throws IOException {
		        Socket socket = super.accept();
		        LOG.fine("new socket "+(++qq)+", from="+socket.getInetAddress()+":"+socket.getPort());
		        socket.setTcpNoDelay(true);
		        socket.setSoTimeout(0);
		        return socket;
		    }  
		};
		return ss;
	}
		
	/**
	 * This is called from RMI on the client to get a new connection to the server.
	 */
	public @Override Socket createSocket(String host, int port) throws IOException {
		LOG.fine("asking host,port="+host+","+port+", using host,port="+OASocketFactory.host+","+OASocketFactory.port);
		Socket socket = new Socket(OASocketFactory.host, OASocketFactory.port);
		socket.setTcpNoDelay(true);
		socket.setSoTimeout(0);
		return socket;
	}
}	

