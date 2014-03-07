package com.theice.mrad.control.server;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.theice.mrad.control.client.RemoteClientController;
import com.theice.mrad.delegate.ModelDelegate;
import com.theice.mrad.delegate.RemoteDelegate;
import com.theice.mrad.model.oa.ClientAppType;
import com.theice.mrad.model.oa.Company;
import com.theice.mrad.model.oa.LoginType;
import com.theice.mrad.model.oa.Router;
import com.theice.mrad.model.oa.User;
import com.theice.mrad.model.oa.UserLogin;
import com.theice.mrad.model.oa.cs.ClientRoot;
import com.theice.mrad.model.oa.cs.ServerRoot;
import com.theice.mrad.remote.RemoteLLOperatorInterface;
import com.theice.mrad.resource.Resource;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

/**
 * Connects to LLAD to get Router and User information.
 * 
 * @author vvia
 */
public class LLADClientControllerTest {
    private static Logger LOG = Logger.getLogger(LLADClientControllerTest.class.getName());

    private RemoteClientController remoteClient;
    private RemoteLLOperatorInterface remoteLLOperatorInterface;

    public LLADClientControllerTest() {
    }
    
Hub<UserLogin> hubUserLogins;    
    public void test() throws Exception {
        hubUserLogins = ModelDelegate.getAllUserLogins();
        /*
        for (int i = ModelDelegate.getRouters().getSize(); i < 500; i++) {
            Router r = new Router();
            r.setName("Router." + i);
            ModelDelegate.getRouters().add(r);
        }
        for ( ; ; ) {
            Router r = ModelDelegate.getRouters().getAt(0);
            if (r == null) break;
            ModelDelegate.getRouters().remove(r);
        }
        */
        
        for (int i = ModelDelegate.getRouters().getSize(); i < 10; i++) {
            Router r = new Router();
            r.setName("Router." + i);
            ModelDelegate.getRouters().add(r);
        }
        
        for (int i = ModelDelegate.getLoginTypes().getSize(); i < 5; i++) {
            LoginType lt = new LoginType();
            lt.setName("loginType." + i);
            ModelDelegate.getLoginTypes().add(lt);
        }
        for (int i = ModelDelegate.getClientAppTypes().getSize(); i < 5; i++) {
            ClientAppType at = new ClientAppType();
            at.setName("ClientAppType." + i);
            ModelDelegate.getClientAppTypes().add(at);
        }

//qqqqqqqqqqqqq set back to 10        
        for (int i = 0; i < 2; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                public void run() { //
                    try {
                        tester(id);
                    }
                    catch (Exception e) {
                        System.out.println("Error in thread: "+e);
                        e.printStackTrace();
                    }
                }
            },"LLAD_TEST."+i);
            t.start();
        }
    }

    AtomicInteger aiId = new AtomicInteger();
    UserLogin createUserLogin() {
        int i = aiId.getAndIncrement();
        UserLogin ul = new UserLogin();
        ul.setClientAppType(ModelDelegate.getClientApplicationTypes().getAt(i % 5));
        ul.setLoginType(ModelDelegate.getLoginTypes().getAt(i % 5));
        ul.setUser(createUser());
        return ul;
    }
    User createUser() {
        int id = aiId.getAndIncrement();
        User user = new User();
        user.setUserId("userId."+id);
        user.setFirstName("fname" + id);
        user.setLastName("lname" + id);
        user.setCompany(null);
        user.getUserLoginHistories();
        user.getUserLogins();
        return user;
    }
    
    public void tester(int id) throws Exception {
        for (int i=0; i<0 ;i++) {
            if (i % 100 == 0) System.out.println("Thread #"+id+" "+i);        
            Hub<Router> h = ModelDelegate.getRouters();
            Router r = h.getAt(0);
            r.setName(i+"."+OAString.getRandomString(3, 14));
            //Thread.sleep(5);
            
            //r = new Router();
            //h.add(r);
            //Thread.sleep(500);
            //r.setName(i+"."+OAString.getRandomString(3, 14));
            //Thread.sleep(750);
            //h.remove(r);
        }

        for (int i=0; i<0 ;i++) {
            Hub<Router> h = ModelDelegate.getRouters();
            
            Router r = h.getAt(0);
            Hub<UserLogin> h2 = r.getUserLogins();
            UserLogin ul = h2.getAt(0);

            User user = ul.getUser();
            user.setFirstName(OAString.getRandomString(3, 14));
            user.setLastName(OAString.getRandomString(3, 14));
            Thread.sleep(1000);
        }
        
        System.out.println("Starting");
        
        for (int i=0; ;i++) {
            if (i % 100 == 0) System.out.println("Thread #"+id+" "+i);        
            Hub<Router> h = ModelDelegate.getRouters();
            
            int x = (int) (Math.random() * h.getSize());
//x = 0;
//if (x > 200) break;
            Router r = h.getAt(x);
            
            Hub<UserLogin> hUserLogin = r.getUserLogins();
            x = hUserLogin.getSize();
if (i == 100) {
//    Thread.sleep(500);
}
//            if (i >= 100) {
            double d;
            if (x < 5) d = .18;
            else if (x < 10) d = .35;
            else d = .8;

            UserLogin ul;
            
            if (x > 0 && Math.random() < d) {
                x = (int) (Math.random()*x);
                ul = hUserLogin.getAt(x);
                if (ul == null) continue;
                User user = ul.getUser();
                ul.setGateway("*** Removed ***");
String s = ul.toString();
                hUserLogin.removeAt(x);
                continue;
            }
            
            if (x < 5) d = .8;
            else if (x < 10) d = .4;
            else d = .12;

            if (x == 0 || (x < 20 && Math.random() < .2)) {
                ul = createUserLogin();
                hUserLogin.add(ul);
                User user = ul.getUser();
                user.setFirstName("fname."+i);
                user.setLastName("fname."+i);
                continue;
            }
            else {
                x = (int) (Math.random() * x);
                ul = hUserLogin.getAt(x);
            }
            if (ul == null) continue;

            User user = ul.getUser();
            user.setFirstName(OAString.getRandomString(3, 14));
            user.setLastName(OAString.getRandomString(3, 14));
            
            ul.setGateway(OAString.getRandomString(3, 12));
            ul.setDtLogin(new OADateTime());

            if ( ((int)(Math.random()*10)) > 7) {
                ul.setClientAppType(null);
            }
            else ul.setClientAppType(ModelDelegate.getClientApplicationTypes().getAt(i % 5));
            
            if ( ((int)(Math.random()*10)) > 7) {
                ul.setLoginType(null);
            }
            ul.setLoginType(ModelDelegate.getLoginTypes().getAt(i % 5));

            if ( ((int)(Math.random()*10)) > 5) {
//                Thread.sleep(10);
            }
            if ( ((int)(Math.random()*10)) > 7) {
//                Thread.sleep(100);
            }
//Thread.sleep(20);//qqqqqqqq
        }
//        System.out.println("Thread #"+id+" is done");
    }


    protected void createRemoteClient() throws Exception {
        if (remoteClient != null) return;
        LOG.fine("creating remote client to MRAD server");
        remoteClient = new RemoteClientController() {
            @Override
            protected void onDisconnect(Exception e) {
                LOG.log(Level.INFO, "client has been disconnected from MRAD server, will exit", e);
                System.exit(0);
            }

            @Override
            protected JFrame getFrame() {
                return null;
            }
        };

        String host = Resource.getValue(Resource.INI_ServerName);
host = "127.0.0.1";        
        int port = Resource.getInt(Resource.APP_RemotePort);
port = 9000;        
        LOG.config(String.format("MRAD server=%s, port=%d", host, port));
        remoteClient.start(host, port);
        
        ServerRoot rootServer = RemoteDelegate.getRemoteApp().getServerRoot();
        int connectionId = OASyncDelegate.getSyncClient().getClientInfo().getConnectionId();
        ClientRoot rootClient = RemoteDelegate.getRemoteApp().getClientRoot(connectionId);
        OAObjectReflectDelegate.loadAllReferences(rootServer, 1, 1, false);
        ModelDelegate.initialize(rootServer, rootClient);
        LOG.config("connected to MRAD server was successful");
    }


    public static void main(String[] args) throws Exception {
        // create client to LLAdmin Server
        OALogUtil.consoleOnly(Level.FINE, "com.theice.mrad");
        LOG.config("LLADClientController starting");
        Resource.setRunType(Resource.TYPE_Client);
        Resource.loadArguments(args);

        LLADClientControllerTest cont = new LLADClientControllerTest();
        cont.createRemoteClient();
        cont.test();
    }
}
