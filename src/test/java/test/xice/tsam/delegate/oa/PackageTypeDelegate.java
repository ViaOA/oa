package test.xice.tsam.delegate.oa;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.logging.Logger;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAString;
import com.viaoa.util.OAYamlReader;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.delegate.oa.PackageTypeDelegate;
import test.xice.tsam.model.oa.*;

public class PackageTypeDelegate {
    private static Logger LOG = Logger.getLogger(PackageTypeDelegate.class.getName());
    
    public static PackageType getPackageTypeUsingCode(String code, boolean bAutoCreate) {
        if (OAString.isEmpty(code)) return null;

        PackageType pt = ModelDelegate.getPackageTypes().find(PackageType.P_Code, code);
        
        if (pt == null && bAutoCreate) {
            pt = new PackageType();
            pt.setCode(code);
            ModelDelegate.getPackageTypes().add(pt);
        }
        return pt;
    }
    
    public static PackageType getPackageTypeUsingPackageName(String packageName, boolean bAutoCreate) {
        if (OAString.isEmpty(packageName)) return null;

        PackageType pt = ModelDelegate.getPackageTypes().find(PackageType.P_PackageName, packageName);
        
        if (pt == null && bAutoCreate) {
            pt = new PackageType();
            pt.setPackageName(packageName);
            ModelDelegate.getPackageTypes().add(pt);
        }
        return pt;
    }

    // from RC project (Shane's team)
    public static void importYaml() throws Exception {
        LOG.fine("checking for packages.yaml");
        File file = new File("packages.yaml");
        if (!file.exists()) return;
        String data = OAFile.readTextFile(file, 16000);
        LOG.fine("loading packages.yaml, data="+data);
        data = cleanupYaml(data);
        
        Object[] objs = getYamlObjects(data);
        if (objs == null) return;
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;

            String code = (String) OAObjectPropertyDelegate.getProperty(oaObj, "name1");
            String packageName = (String) OAObjectPropertyDelegate.getProperty(oaObj, "name");
            String repoDir = (String) OAObjectPropertyDelegate.getProperty(oaObj, "repo_dir");
            
            PackageType pt = PackageTypeDelegate.getPackageTypeUsingCode(code, true);
            
            if (OAString.isEmpty(pt.getCode())) pt.setCode(code);
            if (OAString.isEmpty(pt.getPackageName())) pt.setPackageName(packageName);
            // if (OAString.isEmpty(pt.getRepoDirectory())) pt.setRepoDirectory(repoDir);
        }
    }

    private static String cleanupYaml(String output) {
        if (output == null) return "";
        BufferedReader br = new BufferedReader(new StringReader(output));
        StringBuilder sb = new StringBuilder(output.length());
        
        try {
            boolean bSkip = true;
            for (int i=0;; ) {
                String line = br.readLine();
                if (line == null) break;
                if (bSkip) {
                    if (line.indexOf("packages:") >= 0) { 
                        bSkip = false;
                    }
                    continue;
                }
                if (i++ > 0) sb.append("\n");

                String s = OAString.trim(line);
                String s2 = OAString.field(s, ':', 2);
                if (s2 == null || s2.length() == 0) {
                    String s1 = OAString.field(s, ':', 1);
                    if (!"name".equals(s1) && !"extension".equals(s1) && !"repo_dir".equals(s1)) {
                        line = OAString.trim(line);
                    }
                }
                sb.append(line);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("error parsing output", e);
        }
        return new String(sb);
    }

    private static Object[] getYamlObjects(final String txt) {
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

}
