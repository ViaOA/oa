package com.viaoa;

import com.theice.tsac.delegate.ModelDelegate;
import com.theice.tsac.model.oa.Environment;
import com.theice.tsac.model.oa.Server;
import com.theice.tsac.model.oa.ServerInfo;
import com.theice.tsac.model.oa.ServerInstall;
import com.theice.tsac.model.oa.ServerType;
import com.theice.tsac.model.oa.Silo;
import com.theice.tsac.model.oa.SiloType;
import com.theice.tsac.model.oa.Site;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAThreadLocalDelegate;

public class SampleDataGeneratorDelegate {

    public void createSampleData() {
        OAThreadLocalDelegate.setLoadingObject(true);
        for (int i = 0; i < 10; i++) {
System.out.println("site="+i);                
            Site site = new Site();
            site.setName("Site." + i);
            ModelDelegate.getSites().add(site);
            if (i == 0) site.setProduction(true);
            
            for (int ii = 0; ii < 50; ii++) {
                Environment env = new Environment();
System.out.println("site="+i+", env="+ii);                
                env.setName("Env." + i + "." + ii);
                site.getEnvironments().add(env);

                Site sitex = ModelDelegate.getSites().getAt(0);
                for (;;) {
                    if (sitex.getProduction()) break;
                    //System.out.println("waiting for Site[0].production=true before creating more sample data");
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception e) {
                    }
                }
                
                
                for (int iii = 0; iii < ModelDelegate.getSiloTypes().getSize(); iii++) {
                    Silo silo = new Silo();
                    SiloType siloType = ModelDelegate.getSiloTypes().find(SiloType.P_Type, iii);
                    silo.setSiloType(siloType);
                    env.getSilos().add(silo);
//System.out.println("site="+i+", env="+ii+", silo="+iii);                

                    for (int iiii = 0; iiii < 5; iiii++) {
                        Server server = new Server();
                        server.setName("Server." + i + "." + ii + "." + iii + "." + iiii);
                        Hub<ServerType> h = ModelDelegate.getServerTypes();
                        server.setServerType(h.find(ServerType.P_Type, iiii % h.getSize()));
                        silo.getServers().add(server);
                        for (int i5 = 0; i5 < 2; i5++) {
                            ServerInstall si = new ServerInstall();
                            server.getServerInstalls().add(si);
                        }                        
                    }
//                    silo.save(OAObject.CASCADE_ALL_LINKS);
                    try {
//                        Thread.sleep(1000);
                    }
                    catch (Exception e) {
                    }
                }
//                env.save(OAObject.CASCADE_ALL_LINKS);
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
            
            Hub h = ModelDelegate.getSites();
            
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
            Site sitex = ModelDelegate.getSites().getAt(0);
            
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
