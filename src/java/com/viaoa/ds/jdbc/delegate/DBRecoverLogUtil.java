package com.viaoa.ds.jdbc.delegate;

import java.io.*;

import com.viaoa.util.OAArray;


/**
 * Recovers items that were written by DBLogDelegate.
 * @author vvia
 */
public class DBRecoverLogUtil {

    public void recover(InputStream is) throws IOException {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        
        byte[] bs = new byte[8096];
        String[] params = null;
        String command = null;
        boolean bParam = false;
        
        for (int i=0 ;; i++) {
            String line = br.readLine();
            if (line == null) break;
            
            if (!line.startsWith("FINE: ")) continue;
            line = line.substring(5);

            
            if (line.startsWith("PARAM: [[BEGIN[")) {
                line = line.substring(15);
                bParam = true;
            }
            else if (line.startsWith("INSERT: [[BEGIN[")) {
                line = line.substring(16);
            }
            else if (line.startsWith("UPDATE: [[BEGIN[")) {
                line = line.substring(16);
                
            }
            else if (line.startsWith("DELETE: [[BEGIN[")) {
                line = line.substring(16);
            }
            else if (line.startsWith("DDL: [[BEGIN[")) {
                line = line.substring(13);
            }
            

            if (line.endsWith("]END]]")) {
                line = line.substring(0, line.length()-6);
                
                if (bParam) {
                    params = (String[]) OAArray.add(String.class, params, line);
                    bParam = false;
                }
                else {
                    // execute command
//qqqqqqqqqqqqqqqqq                    
                }
            }
            
            
        }
        
/*        
  
Apr 5, 2010 2:37:35 PM com.viaoa.ds.jdbc.delegate.DBLogDelegate logInsert
FINE: INSERT: [[BEGIN[INSERT INTO PRODUCTIONDATE (WORKING, ID, TYPE, DATEVALUE) VALUES (NULL, 491, 2, {d '2010-03-28'})]END]]

        
*/        
        
    }
    
}
