package test.xice.tsac.delegate.oa;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;

/**
 * Commands to have RCExecute.run(..) to be ran on RemoteRCClient.
 * Client -> Server -> RemoteRCClient -> RCExecuteDelegate.run(ex)
 * @author vvia
 */
public class RCExecuteDelegate {
    private static Logger LOG = Logger.getLogger(RCExecuteDelegate.class.getName());

    /**
     * This is called by Admin Client or Server, to have it sent to the RemoteRCClient to run.
     */
    public static void runOnRCInstance(RCExecute execute) throws Exception {
    }
    
    /**
     * run the command, called by RemoteRCClient to run on a separate instance on the
     * same server as the tsac server.
     */
    public static void runOnLocal(RCExecute execute) throws Exception {
        if (execute == null) throw new Exception("execute is null");
        try {
            _run(execute);
        }
        catch (Exception e) {
            String s = execute.getOutput();
            if (s == null) s = "";
            else s += "\n";
            execute.setOutput(s + e.getMessage());
            throw e;
        }
    }
    
    private static void _run(final RCExecute execute) throws Exception {
        if (execute == null) throw new RuntimeException("execute can not be null");
        execute.setConsole("RCExecute is starting coammnd ... please wait ...");
        
        String cmdLine = execute.getCommandLine();
        if (OAString.isEmpty(cmdLine)) {
            RCCommand cmd = execute.getRCCommand();
            if (cmd == null) return;
            cmdLine = cmd.getCommandLine();
            if (OAString.isEmpty(cmd)) return;
            execute.setCommandLine(cmdLine);
        }

        Future<Boolean> future = getExecutorService().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean b = _run2(execute);
                return b;
            }
        });
        
        Object objx = future.get(180, TimeUnit.SECONDS);
        if (objx != null) execute.setCompleted(new OADateTime());
    }
    
    private static boolean _run2(final RCExecute execute) throws Exception {
        if (execute == null) return false;
        
        String fname = execute.getConfigFileName();
        String config = execute.getInput();

        if (!OAString.isEmpty(config) && !OAString.isEmpty(fname)) {
            File file = new File(OAFile.convertFileName(fname));
            OAFile.writeTextFile(file, config);
        }

        String cmd = execute.getCommandLine();
        if (OAString.isEmpty(cmd)) {
            RCCommand ecmd = execute.getRCCommand();
            if (ecmd == null) return false;
            cmd = ecmd.getCommandLine();
            if (OAString.isEmpty(cmd)) return false;
        }
        LOG.fine("begin cmd="+cmd);
                
        String[] cmds;
        String osName = System.getProperty("os.name");        
        if (osName != null && osName.toLowerCase().startsWith("windows")) {
            // dos:
            cmds = new String[] {"cmd", "/c", cmd};
        }
        else {
            // unix:
            cmds = new String[] {"bash", "-c", cmd};
        }
        
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.redirectErrorStream(true);

        execute.setStarted(new OADateTime());
        Process pr = pb.start();
        

        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String output = null;
        for (int i=1 ;; i++) {
            String s = in.readLine();
            if (s == null) break;
            execute.setConsole(s);
            LOG.fine("line="+s);
            if (output == null) output = "";
            else output += "\n";
            output += s;
        }

        execute.setOutput(output);
        in.close();
        
        pr.waitFor();
        in.close();
        return true;
    }


    private static volatile ThreadPoolExecutor executorService;
    private static ExecutorService getExecutorService() {
        if (executorService != null) return executorService;
        // min/max must be equal, since new threads are only created when queue is full
        executorService = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, 
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE)) 
        {
            @Override
            public Future<?> submit(Runnable task) {
                LOG.fine("running task in thread="+Thread.currentThread().getName());
                return super.submit(task);
            }
        };
        executorService.allowCoreThreadTimeOut(true);
        
        return executorService;
    }

    
    
    public static final String JsonObjectName = "_jsonObjectName";  // used to store json object name
    public static Object[] getJsonObjects(final RCExecute execute) {
        if (execute == null) return null;

        String txt = execute.getOutput();
        if (OAString.isEmpty(txt)) return null;
        
        
        OAJsonReader jr = new OAJsonReader() {
            String lastName;
            @Override
            protected String getClassName(String className) {
                lastName = className;
                return "com.viaoa.object.OAObject";
            }
            
            @Override
            protected Object getValue(OAObject obj, String name, Object value) {
                if (lastName != null) {
                    OAObjectPropertyDelegate.unsafeSetProperty(obj, JsonObjectName, lastName);
                    lastName = null;
                }
                if (value instanceof String) {
                    OAObjectPropertyDelegate.unsafeSetProperty(obj, name, (String) value);
                }
                
                return super.getValue(obj, name, value);
            }
        };

        Object[] objs = jr.parse(txt, OAObject.class);
        
        return objs;
    }
    
    public static Object[] getYamlObjects(final RCExecute execute) {
        if (execute == null) return null;
        String txt = execute.getOutput();
        return getYamlObjects(txt);
    }

    public static Object[] getYamlObjects(final String txt) {
        if (OAString.isEmpty(txt)) return null;
        
        OAYamlReader yamlReader = new OAYamlReader("Service", "name1", "name2") {
            @Override
            protected String getClassName(String className) {
                return "com.viaoa.object.OAObject";
            }
            
            @Override
            protected Object getValue(OAObject obj, String name, Object value) {
                if (value instanceof String) {
                    OAObjectPropertyDelegate.unsafeSetProperty(obj, name, (String) value);
                }
                return super.getValue(obj, name, value);
            }
        };

        Object[] objs = yamlReader.parse(txt, OAObject.class);
        
        return objs;
    }
    
    
    
    
/*qqqqqqqqqqqqqqqqqqqqqqqqqqqqqq
    private static void _processVersions(final Execute execute, final Object[] objs) {
        final Environment env = execute.getEnvironment();
        if (env == null) {
            execute.setProcessingOutput("invalid: no environment");
            return;
        }
        String msg = null;
        int cnt = 0;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, jsonObjectName);
            if (objectName == null || !objectName.equalsIgnoreCase("RepoVersionOutput"))  {
                continue;
            }
            
            
            if (msg == null) msg = "";
            else msg += "\n";
            
            String s = _processVersions2(oaObj, env);
            msg += (++cnt) + ") " + s;
        }
        msg += "\nTotal records read=" + cnt;
        execute.setProcessingOutput(msg);
    }
    private static String _processVersions2(final OAObject oaObj, final Environment env) {
        String buildDate = (String) OAObjectPropertyDelegate.getProperty(oaObj, "build_date");
        String error = (String) OAObjectPropertyDelegate.getProperty(oaObj, "error");
        String repoName = (String) OAObjectPropertyDelegate.getProperty(oaObj, "package");
        String version = (String) OAObjectPropertyDelegate.getProperty(oaObj, "version");
        
        String msg = String.format("buildDate=%s, repoName=%s, version=%s, error=%s", 
            buildDate, repoName, version, error
        );
        msg += "\n  >>";

        if ("null".equals(buildDate)) buildDate = null;
        if ("null".equals(repoName)) repoName = null;
        if ("null".equals(error)) error = null;
        if ("null".equals(version)) version = null;
        
        if (repoName == null)  {
            msg += "invalid: repoName is null";
            return msg;
        }
        if (error != null)  {
            msg += "invalid: has error";
            return msg;
        }
        if (version == null)  {
            msg += "invalid: version";
            return msg;
        }

        ServerType st = ServerTypeDelegate.getServerTypeUsingRepoName(repoName);
        if (st == null) {
            msg += "invalid: repoName/package, no serverType for it.";
            return msg;
        }

        ServerTypeVersion serverTypeVersion = st.getServerTypeVersions().find(ServerTypeVersion.P_Version, version);
        if (serverTypeVersion == null) {
            serverTypeVersion = new ServerTypeVersion();
            serverTypeVersion.setVersion(version);
            st.getServerTypeVersions().add(serverTypeVersion);
        }
        
        if (OAString.isInteger(buildDate))  {
            OADateTime dt = new OADateTime(Long.valueOf(buildDate));
            serverTypeVersion.setDtVersion(dt);
        }

        msg += "ok";
        return msg;
    }
qqqqqqqqqqq */    
    
}


