// Skeleton class generated by rmic, do not edit.
// Contents subject to change without notice.

package com.viaoa.cs;

public final class OAServerImpl_Skel
    implements java.rmi.server.Skeleton
{
    private static final java.rmi.server.Operation[] operations = {
	new java.rmi.server.Operation("com.viaoa.cs.OAObjectServer getOAObjectServer()"),
	new java.rmi.server.Operation("java.lang.String test()")
    };
    
    private static final long interfaceHash = -1732625379374270506L;
    
    public java.rmi.server.Operation[] getOperations() {
	return (java.rmi.server.Operation[]) operations.clone();
    }
    
    public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int opnum, long hash)
	throws java.lang.Exception
    {
	if (opnum < 0) {
	    if (hash == 6575162413065918761L) {
		opnum = 0;
	    } else if (hash == 8458333016746673636L) {
		opnum = 1;
	    } else {
		throw new java.rmi.UnmarshalException("invalid method hash");
	    }
	} else {
	    if (hash != interfaceHash)
		throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
	}
	
	com.viaoa.cs.OAServerImpl server = (com.viaoa.cs.OAServerImpl) obj;
	switch (opnum) {
	case 0: // getOAObjectServer()
	{
	    call.releaseInputStream();
	    com.viaoa.cs.OAObjectServer $result = server.getOAObjectServer();
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 1: // test()
	{
	    call.releaseInputStream();
	    java.lang.String $result = server.test();
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	default:
	    throw new java.rmi.UnmarshalException("invalid method number");
	}
    }
}
