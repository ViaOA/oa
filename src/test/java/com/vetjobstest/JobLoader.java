package com.vetjobstest;

import java.io.*;
import java.util.*;

import java.sql.*;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.ds.jdbc.*;


public class JobLoader implements Runnable {

    String root = "D:\\projects\\java\\com\\vetjobs\\v3\\uploads\\";
    // String root = "C:\\projects\\data\\vetjobs\\batchUpload\\";
    boolean bStop;

    public JobLoader() {
        Thread t = new Thread(this);
        t.start();
    }
    
    public void stop() {
        bStop = true;
    }
    public void run() {
        System.out.println("JobLoader: Starting process to load FTP batch files");
        for (;;) {
            if (bStop) break;
            System.out.println("JobLoader: checking for FTP batch files");
            process();
            System.out.println("JobLoader: waiting 10 minutes");
            synchronized (this) {
                try {
                    wait(1000 * 60 * 10);  // wait 10 minutes
                }
                catch (Exception e) {
                }
            }
        }
    }             

    public void process() {
        File file = new File(root);

        if (file == null || !file.exists()) {
            System.out.println("JobLoader: Error: root directory not found: "+root);
            return;
        }
        
        
        String[] fnames = file.list();
        if (fnames == null) return;
        for (int i=0; i<fnames.length; i++) {
            String fn = root + fnames[i];  // "ftp" + emp.ID
            file = new File(fn);
            if (!file.isDirectory()) continue;
            
            String empId = fnames[i];
            if (!empId.startsWith("emp")) continue;
            empId = empId.substring(3);
            
            Hub hubEmp = new Hub(Employer.class);
            hubEmp.select("id = " + empId);
            if (hubEmp.getSize() != 1) continue;
            Employer emp = (Employer) hubEmp.elementAt(0);

            System.out.println("JobLoader: checking directory "+fnames[i]+" for FTP files");
            
            String[] fnames2 = file.list();
            for (int i2=0; i2<fnames2.length; i2++) {
                String fn2 = fn + "\\" + fnames2[i2];
                file = new File(fn2);
                if (file.isDirectory()) continue;
                
                process(emp, fn2, fnames2[i2]);
            }
            
        }
    }
    
    
    
    
    
    
    public void process(Employer employer, String fileName, String fnShort) {
        if (employer == null) return;
System.out.println("JobLoader START");
System.out.println("  Employer: " + employer.getId() + " " + employer.getCompany());
System.out.println("  FileName: " + fileName);
        BatchRow batchrow = null;
        Hub hubJobCounter = new Hub(Job.class);
        int newCnt,rowCnt;

        // Create Batch and add to Batch Hub
        Batch batch = new Batch();
        batch.setEmployer(employer);
        batch.setFileName(fnShort);
        batch.setLoadDate(new OADate());
        batch.setProcessDate(new OADate());
        batch.save();
System.out.println("  Batch: " + batch.getId());

        Hub hubBatchRow = new Hub(BatchRow.class);

        // Process Rows from Import File;
        try {
            DataInputStream inStream = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            int rcnt = 0;
            int rejectCnt = 0;
            for (; ;rcnt++) {
                String newRow = inStream.readLine();
                if (newRow == null) break;
                String[] stringArray = new String[19];
                int startPos = 0, nextPos = 0;
                char searchChar = (newRow.indexOf('\t') >= 0) ? '\t' : '*';
                try {
                    for (int j = 0; j < 19; j++) {
                        nextPos = newRow.indexOf(searchChar, startPos);
                        if (nextPos != -1){
                            stringArray[j] = newRow.substring(startPos,nextPos).trim();
                            startPos = nextPos + 1;
                        } 
                        else {
                            stringArray[j] = newRow.substring(startPos).trim();
                            break;
                        }
                    }

//qqqqqqqq
//System.out.println("loading row> " + rcnt+"  "+stringArray[0]);

                    batchrow = new BatchRow();
                    batchrow.setReference(stringArray[0]);
                    batchrow.setOrigCategory1(stringArray[1]);
                    batchrow.setOrigCategory2(stringArray[2]);
                    batchrow.setOrigCategory3(stringArray[3]);
                    batchrow.setOrigCategory4(stringArray[4]);
                    batchrow.setOrigCategory5(stringArray[5]);
                    batchrow.setTitle(stringArray[6]);
                    batchrow.setRateFrom(stringArray[7]);
                    batchrow.setRateTo(stringArray[8]);
                    batchrow.setHourly(stringArray[9]);
                    batchrow.setContract(stringArray[10]);
                    batchrow.setFulltime(stringArray[11]);
                    batchrow.setCity(stringArray[12]);
                    batchrow.setState(stringArray[13]);
                    batchrow.setCountry(stringArray[14]);
                    batchrow.setDescription(stringArray[15]);
                    batchrow.setBenefits(stringArray[16]);
                    batchrow.setContact(stringArray[17]);
                    batchrow.setEmail(stringArray[18]);
                    hubBatchRow.add(batchrow); 
                } 
                catch(Exception e) {
                    System.out.println("1 JobLoader exception: "+e);
                    e.printStackTrace();
                    rejectCnt++;
                }
            }
            batch.setQtyRow(rcnt);
            batch.setQtyReject(rejectCnt);
            batch.save();
            inStream.close();
System.out.println("  Total Jobs Read: " + rcnt);
        } 
        catch(Exception e) {
            System.out.println("2 JobLoader exception: "+e);
            e.printStackTrace();
            return;
        }

        
        // remove all jobs
//qqqqqvvvv
OADataSourceJDBC ds = (OADataSourceJDBC) OADataSource.getDataSource(Job.class);
String query = "DELETE FROM JOB WHERE employerId = " + employer.getId();
Statement st = ds.getStatement(query);
try {
    st.executeUpdate(query);
}
catch (Exception e) {
    System.out.println("Error deleting Jobs " + e);
}
finally {
    ds.releaseStatement(st);
}



//        Hub hubJob = employer.getJobs();
//	hubJob.clear();
//        hubJob.deleteAll();

        File file = new File(fileName);
        file.delete();
        
        for (int i=0; ;i++) {
            BatchRow batchRow = (BatchRow) hubBatchRow.elementAt(i);
            if (batchRow == null) break;
            //20090703 was: batchRow.canSave(); // updates other batchRow properties and references

            Job job = new Job();
            job.setEmployer(employer);
            job.setReference(batchRow.getReference());

            Hub hubCat = batchRow.getCategories();
            for (int i2=0; ;i2++) {
                Category cat = (Category) hubCat.elementAt(i2);
                if (cat == null) break;
                job.getCategories().addElement(cat);
            }

            if (batchRow.getLocation() != null) {
                job.getLocations().addElement(batchRow.getLocation());
            }

            job.setFolder(batchRow.getFolder());
            job.setRateFrom(batchRow.getRateFromDouble());
            job.setRateTo(batchRow.getRateToDouble());
            job.setPositionsAvailable(batchRow.getPositionsInteger());
            job.setCity(batchRow.getCity());
            job.setRegion(batchRow.getRegion());
            job.setState(batchRow.getState());
            job.setCountry(batchRow.getCountry());
            job.setTitle(batchRow.getTitle());
            job.setDescription(batchRow.getDescription());
            job.setContact(batchRow.getContact());
            job.setEmail(batchRow.getEmail());
            job.setBenefits(batchRow.getBenefits());
            job.setAutoResponse(false);

            String tempString = batchRow.getHourly();
            if (tempString != null) {
                if (tempString.equalsIgnoreCase("hourly") || tempString.equalsIgnoreCase("h")){
                    job.setHourly(true);
                }
                if (tempString.equalsIgnoreCase("salary") || tempString.equalsIgnoreCase("s")){
                    job.setHourly(false);
                }
            }

            tempString = batchRow.getContract();
            if (tempString != null) {
                if (tempString.equalsIgnoreCase("contract") || tempString.equalsIgnoreCase("c")){
                    job.setContract(true);
                }
                if (tempString.equalsIgnoreCase("permanent") || tempString.equalsIgnoreCase("p")){
                    job.setContract(false);
                }
            }

            tempString = batchRow.getFulltime();
            if (tempString != null) {
                if (tempString.equalsIgnoreCase("full-time") || tempString.equalsIgnoreCase("ft") || tempString.equalsIgnoreCase("f")){
                    job.setFulltime(true);
                }
                if (tempString.equalsIgnoreCase("part-time") || tempString.equalsIgnoreCase("pt") || tempString.equalsIgnoreCase("p")){
                    job.setFulltime(false);
                }
                
            }

//            hubJob.addElement(job);
            job.save();
 System.out.println("loading job> " + i+"  ID="+job.getId());
        }
System.out.println("JobLoader COMPLETED");
    }

    public static void main(String[] args) throws Exception {
        new DataSource();
        JobLoader jl = new JobLoader();
//        jl.process();
    }
}
