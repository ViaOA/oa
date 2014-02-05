package com.viaoa.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import com.viaoa.util.OAArray;


/**
 * Read a text file that has delimited rows.
 * @author vincevia
 */
public abstract class LoadDelimitedFile {
    
    /**
     * Read text file, where each line uses "\\r\\n", and parse based on delimiter.
     * @param file
     * @param sep
     * @param bQuoted if column data could have quotes around it
     * @throws Exception
     */
    public void read(File file, String sep, boolean bQuoted) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (int i=1;;i++) {
            String line = reader.readLine();
            if (line == null) break;
            String[] ss = parse(line, sep, bQuoted, i);
            if (ss != null) process(ss, i);
        }
        reader.close();
    }
    
    // parse line into columns
    public String[] parse(String line, String sep, boolean bQuoted, int lineNumber) {
        int pos = 0;
        String[] flds = new String[0];
        StringTokenizer tok = new StringTokenizer(line, sep, true);
        for ( ;tok.hasMoreTokens(); ) {
            String word = tok.nextToken();
            if (word.equals(sep)) {
                pos++;
            }
            else {
                int x = word.length();
                if (bQuoted && x > 2) {
                    if ( word.charAt(0) == '"' && word.charAt(x-1) == '"') {
                        word = word.substring(1, x-1);
                    }
                }
                flds = Arrays.copyOf(flds, pos+1);
                flds[pos] = word;
            }
        }
        return flds;
    }
        
    public abstract void process(String[] columns, int lineNumber);

    
    
    public static void main(String[] args) throws Exception {
        final ArrayList<String> alTable = new ArrayList<String>();
        alTable.add("ZITBAL");
        
        

        String s = "COLUMN_NAME,TABLE_NAME,TABLE_OWNER,ORDINAL_POSITION,DATA_TYPE,LENGTH,NUMERIC_SCALE,IS_NULLABLE,IS_UPDATABLE,LONG_COMMENT,"+
        "HAS_DEFAULT,COLUMN_HEADING,STORAGE,NUMERIC_PRECISION,CCSID,TABLE_SCHEMA,COLUMN_DEFAULT,CHARACTER_MAXIMUM_LENGTH,"+
        "CHARACTER_OCTET_LENGTH,NUMERIC_PRECISION_RADIX,DATETIME_PRECISION,COLUMN_TEXT,SYSTEM_COLUMN_NAME,"+
        "SYSTEM_TABLE_NAME,SYSTEM_TABLE_SCHEMA,USER_DEFINED_TYPE_SCHEMA,USER_DEFINED_TYPE_NAME,IS_IDENTITY,IDENTITY_GENERATION,"+
        "IDENTITY_START,IDENTITY_INCREMENT,IDENTITY_MINIMUM,IDENTITY_MAXIMUM,IDENTITY_CYCLE,IDENTITY_CACHE,IDENTITY_ORDER,"+
        "COLUMN_EXPRESSION,HIDDEN";

        /*                
        [0] column name
        [1] table name
        [4] dataType
        [5] length
        7-8 could be for key?
        [9] long comment
        [11] COLUMN_HEADING
        */                
        
        
        final ArrayList<String> alHeading = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(s, ",", false);
        for ( ;tok.hasMoreTokens(); ) {
            String word = tok.nextToken();
            alHeading.add(word);
        }
        
        
        LoadDelimitedFile ldf = new LoadDelimitedFile() {
            int cnt;
            @Override
            public void process(String[] columns, int lineNumber) {
                if (columns == null) return;
                int x = columns.length;
                if (x < 13) return;
                
                String s = columns[1];
                if (!alTable.contains(s)) return;
                
                System.out.println(lineNumber+"> "+(++cnt)+") "+columns[1]+", "+columns[0]+", "+columns[4]+", "+columns[5]+", "+columns[9]);
                /*
                int x2 = alHeading.size();
                for (int i=0; i<x2; i++) {
                    System.out.println("  "+i+") "+alHeading.get(i) + ") " + columns[i]);
                }
                */
            }
        };
        File file = new File("c:\\temp\\M3TABLES_Columns.csv"); 
        ldf.read(file, ",", false);
    }
}

/*
OOHEAD - TF: Customer order
OCUSMA - MF: Customer
ZOHEAD - ?? not found
OCUSAD - MF: Customer address
OSYTXL - MF: Text, line
OOLINE - TF: Customer order lines
MWOHED - TF: Work order head
MITLOC - MF: Locations
MITBAL - MF: Warehouse/itemnumber stock and plan.valu2/(MB)
ZITBAL - ?? not found
MITFAC - MF: Facility/item balance and plan.value
MPDWCT - MF: Planning groups (workcenters)
MITMAS - MF: Items 
*/

/* 
108412 OCUSMA, OKCONO
  0) COLUMN_NAME) OKCONO
  1) TABLE_NAME) OCUSMA
  4) DATA_TYPE) DECIMAL
  5) LENGTH) 3
  6) NUMERIC_SCALE) 0
  7) IS_NULLABLE) N
  8) IS_UPDATABLE) Y
  9) LONG_COMMENT) Company
  21) COLUMN_TEXT) Company
*/


/* Sample
108412 OCUSMA, OKCONO
  0) COLUMN_NAME) OKCONO
  1) TABLE_NAME) OCUSMA
  2) TABLE_OWNER) M3SRVADM
  3) ORDINAL_POSITION) 1
  4) DATA_TYPE) DECIMAL
  5) LENGTH) 3
  6) NUMERIC_SCALE) 0
  7) IS_NULLABLE) N
  8) IS_UPDATABLE) Y
  9) LONG_COMMENT) Company
  10) HAS_DEFAULT) N
  11) COLUMN_HEADING) Cmp
  12) STORAGE) 2
  13) NUMERIC_PRECISION) 3
  14) CCSID) null
  15) TABLE_SCHEMA) M3DJDPRE
  16) COLUMN_DEFAULT) null
  17) CHARACTER_MAXIMUM_LENGTH) null
  18) CHARACTER_OCTET_LENGTH) null
  19) NUMERIC_PRECISION_RADIX) 10
  20) DATETIME_PRECISION) null
  21) COLUMN_TEXT) Company
  22) SYSTEM_COLUMN_NAME) OKCONO    
  23) SYSTEM_TABLE_NAME) OCUSMA    
  24) SYSTEM_TABLE_SCHEMA) M3DJDPRE  
  25) USER_DEFINED_TYPE_SCHEMA) null
  26) USER_DEFINED_TYPE_NAME) null
  27) IS_IDENTITY) NO
  28) IDENTITY_GENERATION) null
  29) IDENTITY_START) null
  30) IDENTITY_INCREMENT) null
  31) IDENTITY_MINIMUM) null
  32) IDENTITY_MAXIMUM) null
  33) IDENTITY_CYCLE) null
  34) IDENTITY_CACHE) null
  35) IDENTITY_ORDER) null
  36) COLUMN_EXPRESSION) null
  37) HIDDEN) N
*/


