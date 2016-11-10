package com.viaoa.util;


import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

public class OADateTimeTest extends OAUnitTest {

    @Test
    public void dateTimeTest() {
        String s = "04/28/2015 18:59:32";
        OADateTime dt = new OADateTime(s, "MM/dd/yyyy HH:mm:ss");

        String s2 = dt.toString("MM/dd/yyyy HH:mm:ss");
        
        assertEquals(s, s2);
    }

    @Test
    public void compareTest() {
        OADateTime dt = new OADateTime();
        OADate d = new OADate(dt);
        OATime t = new OATime(dt);

        assertEquals(dt, d);
        assertEquals(dt, t);
        assertNotEquals(t, d);
        
        assertTrue(dt.compareTo(d) == 0);
        assertTrue(dt.compareTo(t) == 0);
        
        OADateTime dt2 = new OADateTime(dt);
        dt2 = dt2.addMilliSeconds(1);
        int x = dt2.compareTo(d);
        assertTrue(x == 0);
        x = dt2.compareTo(dt);
        assertTrue(x > 0);
    }
    
    @Test
    public void betweenTest() {
        OADateTime dt1 = new OADateTime(2015, 9, 1, 0, 0, 0);
        OADateTime dt2 = new OADateTime(2015, 9, 1, 6, 0, 0); // 6am
        int hours = dt1.betweenHours(dt2);
        assertEquals(6, hours);

        // before dst ends Nov 1, 2015
        dt1 = new OADateTime(2015, 10, 1, 0, 0, 0);
        // after dst
        dt2 = new OADateTime(2015, 10, 1, 6, 0, 0); // 6am in another timezone
        hours = dt1.betweenHours(dt2);
        assertEquals(7, hours);

        
        long ts = dt2.getTime();
        
    }
    @Test
    public void timezoneTest() {
        OADateTime dt1 = new OADateTime();
        TimeZone tz1 = dt1.getTimeZone();
        
        OADateTime dt2 = new OADateTime();
        TimeZone tz2 = TimeZone.getTimeZone("Etc/GMT+0");
        dt2.setTimeZone(tz2);
        
        assertEquals(dt1.getHour(), dt2.getHour());
    }

    @Test
    public void timezoneTest2() {
        TimeZone tz1 = TimeZone.getTimeZone("Etc/GMT+0");
        OADateTime dt1 = new OADateTime();
        dt1.setTimeZone(tz1);
        
        TimeZone tz2 = TimeZone.getTimeZone("Etc/GMT+8");
        OADateTime dt2 = dt1.convertTo(tz2);

        dt2 = dt2.convertTo(tz1);
        
        assertEquals(dt1.getHour(), dt2.getHour());
    }

    @Test
    public void dstTest() {
        // dst  2015/11/1 @ 2:am is fall back (lose 1 hour)
        OADateTime dtNow = new OADateTime(2015, 10, 1, 0, 0, 0);  // midnight, 1:am is change
        OADateTime d =     new OADateTime(2015, 10, 1, 6, 0, 0); // 6:am in another timezone
        int hours = dtNow.betweenHours(d);
        assertEquals(7, hours);
    

        // without a change to dst
        dtNow = new OADateTime(2015, 5, 1, 0, 0, 0);  // midnight
        d = new OADateTime(2015, 5, 1, 6, 0, 0); // 6:am in another timezone
        hours = dtNow.betweenHours(d);
        assertEquals(6, hours);
    

        // DST Mar8, Nov1 2015
        
        // before dst and after end dst
        dtNow = new OADateTime(2015, 2, 1, 0, 0, 0); 
        d = new OADateTime(2015, 10, 2, 0, 0, 0);
        hours = dtNow.betweenHours(d);
        assertEquals(5904, hours);
    
        
        // after dst and before end dst
        dtNow = new OADateTime(2015, 2, 2, 0, 0, 0); 
        d = new OADateTime(2015, 10, 1, 0, 0, 0);
        hours = dtNow.betweenHours(d);
        assertEquals(5855, hours);
    }

    @Test
    public void dstTest2() {
        OADateTime.setGlobalOutputFormat("MM/dd/yyyy hh:mm:ss.Sa z");
        // dst  2015/11/1 @ 2:am is fall back (lose 1 hour)
        OADateTime dt1 = new OADateTime(2015, 10, 1, 0, 59, 59);  // EDT
        long ts1 = dt1.getTime();
        
        OADateTime dt2 = dt1.addSeconds(1);  // EDT
        long ts2 = dt2.getTime();
        
        OADateTime dt3 = dt2.addMinutes(59);  
        dt3 = dt3.addSeconds(59);  // EDT  1:59:59am
        long ts3 = dt3.getTime();
        
        // this will hit the daylight change time to "fall back" one hour, and make it 1:00am again
        dt3 = dt3.addSeconds(1);
        long ts4 = dt3.getTime();  // EST 1:00:00am
        assertEquals(1, dt3.getHour());
    }

    @Test
    public void dateTest() {
        
        OADate d1 = new OADate(2016, Calendar.SEPTEMBER, 27);
        OADate d1x = new OADate(2016, Calendar.SEPTEMBER, 27);
        
        OADate d2 = new OADate(d1);
        OADate d3 = new OADate();
    }
    
    
    
//qqqqqqqqqqqqqqqqqqqq    

    // this verifies that betweenDays works correctly with leap years and DLS (daylight savings) DLS will have a 23hr day and a 25hr day
    public static void mainX(String[] args) {
        OADate d1 = new OADate(1201, Calendar.SEPTEMBER, 27);
        OADate d2 = new OADate(d1);
        for (int i=0; i<500000; i++) {
            int x = d1.betweenDays(d2);
            if (i != x) {
                x = d1.betweenDays(d2);
                System.out.println("Error: "+d2);
                break;
            }
            d2 = (OADate) d2.addDays(1);
        }
        System.out.println("Done => "+d2);
    }

    public static void mainZ(String[] args) {
        OADateTime dt = new OADateTime(1965, Calendar.MAY, 4, 12, 0, 0);
        int x = dt.getCalendar().get(Calendar.ZONE_OFFSET);
        System.out.println("=>"+dt.toString("MMddyyy HH:mm:ss a"));
        System.out.println("=>"+x);

        dt.getCalendar().add(Calendar.HOUR, 3);
        
        dt.cal.add(Calendar.ZONE_OFFSET, 5);
        x = dt.getCalendar().get(Calendar.ZONE_OFFSET);
        
        System.out.println("=>"+dt.toString("MMddyyy HH:mm:ss a"));
        System.out.println("=>"+x);
    }
    
    
    public static void mainA(String[] args) {
        OADateTime dt = new OADateTime(2015, 6, 1);
        TimeZone tz = dt.getTimeZone();
        long t1 = dt.getTime();
        int h1 = dt.getHour();
        int offset = tz.getOffset(dt.getTime());

        System.out.println("=>"+dt.toString("MM/dd/yyyy HH:mm:ss a z")+", offset="+offset);
        
        String[] stzs = TimeZone.getAvailableIDs();
        for (String stz : stzs) {
            TimeZone tz2 = TimeZone.getTimeZone(stz);

            OADateTime dt2 = new OADateTime(dt);
            dt2.setTimeZone(tz2);
            long t2 = dt2.getTime();
            int h2 = dt2.getHour();
            System.out.println("   => "+dt2.toString("MM/dd/yyyy HH:mm:ss a z")+"  "+tz2.getDisplayName());
            
            OADateTime dt3 = dt.convertTo(tz2);
            long t3 = dt2.getTime();
            int h3 = dt2.getHour();
            
            System.out.println("   => "+dt3.toString("MM/dd/yyyy HH:mm:ss a z"));
            int xx = 4;
            xx++;
        }
    }
    
    public static void mainB(String[] args) {
        OADateTime dtLocal = new OADateTime();
        
        TimeZone tzLocal = dtLocal.getTimeZone();
        long timeLocale = dtLocal.getTime();
        int offsetLocale = tzLocal.getOffset(dtLocal.getTime());
        offsetLocale /= 1000*60*60;
        

        System.out.println("LOCAL =>"+dtLocal.toString("MM/dd/yyyy HH:mm:ss a z")+", offset="+offsetLocale);
        
        String[] stzs = TimeZone.getAvailableIDs();
        for (String stz : stzs) {
            TimeZone tz = TimeZone.getTimeZone(stz);
            
            OADateTime dt = dtLocal.convertTo(tz);
            int offset = tz.getOffset(dt.getTime());
            offset /= 1000*60*60;
            
            System.out.println("  =>"+dt.toString("MM/dd/yyyy HH:mm:ss a z")+", offset="+offset);
            
            
            dt.setTimeZone(tzLocal);
            
            int secs = dt.betweenSeconds(dtLocal);
            
            System.out.println("  between seconds="+secs);
            int xx = 4;
            xx++;
        }
    }
    
    // DST check
    public static void mainC(String[] args) {
        OADateTime dt1 = new OADateTime(2015, 10, 1, 0, 0, 0);
        System.out.println("  =>"+dt1.toString("MM/dd/yyyy HH:mm:ss a z"));
        
        OADateTime dt = new OADateTime(2015, 10, 1, 0, 59, 59);
        System.out.println("x  =>"+dt.toString("MM/dd/yyyy HH:mm:ss a z"));

        
        OADateTime dt2 = new OADateTime(2015, 10, 1, 1, 0, 0);
        System.out.println("  =>"+dt2.toString("MM/dd/yyyy HH:mm:ss a z"));
        OADateTime dt3 = new OADateTime(2015, 10, 1, 1, 0, 0);
        System.out.println("  =>"+dt3.toString("MM/dd/yyyy HH:mm:ss a z"));
        int hrs = dt1.betweenHours(dt2);
        int xx = 4;
        xx++;
    }
    
    // DST check2
    public static void mainD(String[] args) {
        // before dst ends
        OADateTime dtHere = new OADateTime(2015, 10, 1, 0, 0, 0);
        System.out.println("  =>"+dtHere.toString("MM/dd/yyyy HH:mm:ss a z"));

        OADateTime d = new OADateTime(2015, 10, 1, 6, 0, 0); // 6am in another timezone
        // TimeZone tz = TimeZone.getTimeZone("EST");
        TimeZone tz = TimeZone.getTimeZone("Etc/GMT+0");
        d.setTimeZone(tz);
        System.out.println("  =>"+d.toString("MM/dd/yyyy HH:mm:ss a z"));
        
        tz = dtHere.getTimeZone();
        OADateTime d2 = d.convertTo(tz);
        
        int hours = dtHere.betweenHours(d);
        int xx = 4;
        xx++;
    }
    
    public static void main(String[] args) throws Exception {
        
        OATime t = new OATime(8, 49, 0);
        OADateTime dtNow = new OADateTime();

        OATime tNow = new OATime(dtNow);
        
        OADateTime dt1 = new OADateTime(t.getTime());
        OADateTime dt2 = new OADateTime(tNow.getTime());
        
        
        long diff = Math.abs(t.getTime() - tNow.getTime());
        
        int xx = 4;
        xx++;
    }
    
}


