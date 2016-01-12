package com.viaoa;

import com.theicetest.tsactest.model.Model;
import com.theicetest.tsactest.model.oa.Environment;
import com.theicetest.tsactest.model.oa.Server;
import com.theicetest.tsactest.model.oa.ServerInstall;
import com.theicetest.tsactest.model.oa.ServerStatus;
import com.theicetest.tsactest.model.oa.ServerType;
import com.theicetest.tsactest.model.oa.Silo;
import com.theicetest.tsactest.model.oa.SiloType;
import com.theicetest.tsactest.model.oa.Site;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAThreadLocalDelegate;

public class TsactestDataGenerator {

    
    public static final int MaxSiteLoop = 3;
    public static final int MaxEnviromentLoop = 3;
    public static final int MaxSiloLoop = 3;
    public static final int MaxServerLoop = 5;
    public static final int MaxServerInstallLoop = 2;

    protected Model model;
    
    public TsactestDataGenerator(Model model) {
        this.model = model;
    }
    
    public void createSampleData1() {
        OAThreadLocalDelegate.setLoadingObject(true);

        for (int i=0; i<10; i++) {
            ServerType st = new ServerType();
            st.setName("ServerType."+i);
            model.getServerTypes().add(st);
        }

        for (int i=0; i<MaxSiloLoop; i++) {
            SiloType siloType = new SiloType();
            siloType.setType(i);
            model.getSiloTypes().add(siloType);
            for (int ii=0; ii<5; ii++) {
                siloType.getServerTypes().add(model.getServerTypes().getAt(ii));
            }
        }
        
        for (int i=0; i<ServerStatus.hubType.getSize(); i++) {
            ServerStatus st = new ServerStatus();
            st.setName("ServerStatus."+i);
            st.setType(i);
            model.getServerStatuses().add(st);
        }
        
        int cntServer = 0;
        for (int i = 0; i < MaxSiteLoop; i++) {
            Site site = new Site();
            site.setName("Site." + i);
            model.getSites().add(site);
            if (i == 0) site.setProduction(true);
            
            for (int ii = 0; ii < MaxEnviromentLoop; ii++) {
                Environment env = new Environment();
                //System.out.println("site="+i+", env="+ii);                
                env.setName("Environment." + i + "." + ii);
                site.getEnvironments().add(env);

                for (int iii = 0; iii < model.getSiloTypes().getSize(); iii++) {
                    Silo silo = new Silo();
                    SiloType siloType = model.getSiloTypes().find(SiloType.P_Type, iii);
                    silo.setSiloType(siloType);
                    env.getSilos().add(silo);
                

                    for (int iiii = 0; iiii < MaxServerLoop; iiii++) {
                        Server server = new Server();
                        server.setId(++cntServer);
                        server.setName("Server." + i + "." + ii + "." + iii + "." + iiii);
                        Hub<ServerType> h = model.getServerTypes();
                        server.setServerType(h.getAt(iii));
                        silo.getServers().add(server);
                        for (int i5 = 0; i5 < MaxServerInstallLoop; i5++) {
                            ServerInstall si = new ServerInstall();
                            server.getServerInstalls().add(si);
                        }                        
                    }
                }
                env.save(OAObject.CASCADE_ALL_LINKS);
            }
            site.save(OAObject.CASCADE_ALL_LINKS);
        }
        OAThreadLocalDelegate.setLoadingObject(false);
    }

    public void runSampleThread() {
        Thread tx = new Thread(new Runnable() {
            int cnt;

            @Override
            public void run() {
                runRandomChanges();
            }

        });
        tx.start();

    }
    
    public void runRandomChanges() {
        for (int cnt=0;;cnt++) {
            
            Hub h = model.getSites();
            
            Site site = (Site) h.getAt(0); 
            if (site != null && site.getProduction()) {
                site = (Site) h.getAt(((int) (Math.random() * h.getSize())));
            }
            else site = null;
            
            if (site == null) {
                try {
                    //System.out.println("waiting for Site[0].production=true before create random changes");
                    Thread.sleep(1000);
                    continue;
                }
                catch (Exception e) {
                }
            }
            Site sitex = model.getSites().getAt(0);
            
            site.setName("Site cnt=" + cnt);

            h = site.getEnvironments();
            Environment env = (Environment) h.getAt(((int) (Math.random() * h.getSize())));
            env.setName("Env cnt=" + cnt);

            h = env.getSilos();
            int x = ((int) (Math.random() * h.getSize()));
            Silo silo = (Silo) h.getAt(x);

            if (silo == null) continue;
            
            h = silo.getServers();

            Server server = (Server) h.getAt(((int) (Math.random() * h.getSize())));
            if (server == null) continue;
            server.setName("Server cnt=" + cnt);

            switch (((int)Math.random() * 8)) {
            case 0:
                h.move(0, 1);
                break;
            case 1:
                server.delete();
                break;
            case 2:
                if (h.getSize() < 300) {
                    server = new Server();
                    server.setName("NEW cnt="+cnt);
                    if (cnt % 2 == 0) h.add(server);
                    else h.insert(server, h.getSize() / 2);
                }        
                break;
            }
            
            try {
                if (cnt % 50 == 0) Thread.sleep(5);
                //Thread.sleep(1);
            }
            catch (Exception e) {
                // TODO: handle exception
            }
        }
    }
}
