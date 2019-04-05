package test.xice.tsac3.model.oa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.viaoa.ds.OADataSourceIterator;
import com.viaoa.ds.autonumber.OADataSourceAuto;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDelegate;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.sync.*;
import com.viaoa.util.OAFilter;

public class Test {

    public void server() throws Exception {
        OASyncServer ss = new OASyncServer(1099);

        OADataSourceAuto ds = new OADataSourceAuto() {
            @Override
            public boolean supportsStorage() {
                return true;
            }
            int cnt;
            @Override
            public OADataSourceIterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromWhereObject, String queryOrder, int max, OAFilter filter, boolean bDirty) {
                if (whereObject instanceof Silo && Server.class.equals(selectClass)) {
                    return new OADataSourceIterator() {
                        int x = 0;
                        @Override
                        public boolean hasNext() {
                            return (x++ < 10);
                        }
                        public Object next() {
                            Server s = new Server();
                            System.out.println((++cnt)+") New server created ******");                            
                            return s;
                        }
                        @Override
                        public void remove() {
                        }
                        @Override
                        public String getQuery() {
                            // TODO Auto-generated method stub
                            return null;
                        }
                        @Override
                        public String getQuery2() {
                            // TODO Auto-generated method stub
                            return null;
                        }
                    };
                }
                return null;
            }            
        };
        ds.setAssignIdOnCreate(true);

        ss.start();

        env = new Environment();
        
        // env.setName("env");  keep null until done
        
        env.setNew(false);
        for (int j=0; j<5; j++) {
            Environment enx;
            if (j == 0) enx = env;
            else enx = new Environment();
            
            for (int i=0; i<500; i++) {
                Silo s = new Silo();
                s.setNetworkMask("SILO#"+i);
                // System.out.println("creating servers "+i);
                enx.getSilos().add(s);
                s.getServers();
                for (int ii=0; ii<500; ii++) {
                    Server serv = new Server();
                    s.getServers().add(serv);

                    for (int i3=0; i3<1; i3++) {
                        ServerInstall si = new ServerInstall();
                        serv.getServerInstalls().add(si);
                    }
                }
            }
        }
        System.out.println("server ready");
    }

    Environment env;
    public void client() throws Exception {
        OASyncClient client = new OASyncClient("localhost", 1099);
        client.start();
        env = (Environment) client.getRemoteServer().getObject(Environment.class, new OAObjectKey(1));
        client2();
    }
    
    String getRandom() {
        int x = 3 + ((int) (Math.random() * 12));
        String s = "";
        for (int i=0; i<x; i++) {
            char ch = (char) ('A' + ((int) (Math.random() * 26)));
            s += ch;
        }
        return s;
    }
    
    public void client2() throws Exception {
        int cnt = 0;
        if (env.getName() != null) {
            long x = 0;
            for (Silo silo : env.getSilos()) {
                for (Server serv : silo.getServers()) {
                    x += serv.getId() * 1e5;
                    String s = serv.getName(); 
                    if (s != null) x += s.hashCode();
                }
            }
            System.out.println("checksum is =======> "+x + " <========");
            return;
        }
        Object[] objs = env.getSilos().toArray();
        for (int i=0; i<objs.length; i++) {
            Silo silo = (Silo) objs[i];
            Hub h = (Hub) OAObjectPropertyDelegate.getProperty(silo, Silo.P_Servers, false, true);
            if (h == null) {
                ArrayList al = new ArrayList();
                Server sx = (Server) OAObjectCacheDelegate.find(Server.class, Server.P_Silo, silo);
                for (;sx!=null;) {
                    al.add(sx);
                    sx = (Server) OAObjectCacheDelegate.findNext(sx, Server.P_Silo, silo);
                }
                
                int xx = 4;
                xx++;
            }
            silo.getServers();
            int xx = 4;
            xx++;
            for (Server serv : silo.getServers()) {
                serv.setName(getRandom());
            }
            if (Math.random() > .5) {
                Server sx = new Server();
                silo.getServers().add(sx);
            }
            else {
                silo.getServers().removeAt(0);
            }
            
        }
        // test lazy loading
        // get all and then let gc
    }

    public static void main(String[] args) throws Exception {
        Test test = new Test();
        
        boolean bIsServer = (args.length > 0);
        if (bIsServer) test.server();
        else test.client();
        
        ArrayList<byte[]> al = new ArrayList<byte[]>();  
        for (int i=0;;i++) {
            System.gc();
            System.out.println(String.format("total=%,d free=%,d", Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory()));
            if (bIsServer) {
                Thread.sleep(1000);
                //qqqqqq
                if (false && i % 30 == 0 && i > 0) {
                    if (test.env.getName() == null) test.env.setName("test");
                    else test.env.setName(null);
                }

                if (test.env.getName() != null) test.client2();
                else if (i % 500 == 0) {
                  //  HashSet<Hub> hs = HubDelegate.clearReferenceableCache();
                    test.env.save(OAObject.CASCADE_ALL_LINKS);
                }
            }
            else {
                byte[] bs = new byte[512 * 1024];
                al.add(bs);
                if (i > 60) {
                    al.remove(0);
                    // Thread.sleep(100);
                }
                else System.out.println("i= "+i);
                if (test.env.getName() != null) Thread.sleep(1000);
                test.client2();
            }
        }
    }

}
