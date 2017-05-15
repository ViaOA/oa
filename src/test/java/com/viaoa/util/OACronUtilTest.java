package com.viaoa.util;

import org.junit.Test;

import com.viaoa.util.OACronUtil;
import com.viaoa.util.OADateTime;
import static org.junit.Assert.*;

import java.util.Calendar;

public class OACronUtilTest {
    @Test
    public void test1() {
        OACronUtil cu = new OACronUtil("1", "0", "*", "*", "0");
        assertTrue(cu.isValid());

        OADateTime dtNow = new OADateTime();

        // every sunday at 12:01am
        OADateTime dt = cu.findNext(null);

        assertNotNull(dt);

        int x = dt.getDayOfWeek();
        assertEquals(x, Calendar.SUNDAY);

        x = dt.betweenDays(dtNow);
        assertEquals(x, 8 - dtNow.getDayOfWeek());
    }

    @Test
    public void test2() {
        OACronUtil cu = new OACronUtil("1", "0", "*", "*", "0");
        assertTrue(cu.isValid());

        OADateTime dtFrom = new OADateTime(2017, Calendar.APRIL, 1, 0, 0);

        OADateTime dt = cu.findNext(dtFrom); // next sunday at 12:01
        assertNotNull(dt);

        OADateTime dtx = new OADateTime(2017, Calendar.APRIL, 2, 0, 1);
        assertEquals(dt, dtx);
    }

    @Test
    public void test3() {
        OACronUtil cu = new OACronUtil("1", "*", "*", "*", "*");
        assertTrue(cu.isValid());

        OADateTime dtFrom = new OADateTime(2017, Calendar.APRIL, 1, 0, 0, 0);

        OADateTime dt = cu.findNext(dtFrom); // every minute
        assertNotNull(dt);

        OADateTime dtx = new OADateTime(dtFrom);
        dtx.setMinute(1);
        assertEquals(dt, dtx);
    }

    @Test
    public void test4() {
        OACronUtil cu = new OACronUtil("0", "0", "5,10", "*", "*");
        assertTrue(cu.isValid());
        OADateTime dtFrom = new OADateTime();
        dtFrom.clearTime();

        System.out.println("test4, every month on 5th and 10th, dtFrom="+dtFrom);
        for (int i=0; i<1000; i++) {
            OADateTime dt = cu.findNext(dtFrom); // 5th or 10 of month
            assertNotNull(dt);
            int x = dt.getDay();
            assertTrue(x == 5 || x == 10);

            assertEquals(0, dt.get24Hour());
            assertEquals(0, dt.getMinute());

            dtFrom = dt;
        }
        System.out.println("    end, dtFrom="+dtFrom);
    }

    @Test
    public void test5() {
        OACronUtil cu = new OACronUtil("0", "0", "31", "*", "*");
        assertTrue(cu.isValid());
        OADateTime dtFrom = new OADateTime();
        dtFrom.clearTime();

        int m = dtFrom.getDay();
        int max = dtFrom.getDaysInMonth();

        System.out.println("test5, every month on the 31st, dtFrom="+dtFrom);
        for (int i=0; i<1000; i++) {
            OADateTime dt = cu.findNext(dtFrom); // 31
            assertNotNull(dt);
            int x = dt.getDay();
            assertTrue(x == 31);

            assertEquals(0, dt.get24Hour());
            assertEquals(0, dt.getMinute());

            dtFrom = dt;
        }
        System.out.println("    end, dtFrom="+dtFrom);
    }

    @Test
    public void test6() {
        OACronUtil cu = new OACronUtil("1,2,3,4,5", "*", "31", "*", "*");
        assertTrue(cu.isValid());
        OADateTime dtFrom = new OADateTime();
        dtFrom.clearTime();

        int m = dtFrom.getDay();
        int max = dtFrom.getDaysInMonth();

        System.out.println("test6, every month on the 31st every hr, 5 times, dtFrom="+dtFrom);
        for (int i=0; i<1000; i++) {
            // System.out.println(i+" "+dtFrom);
            OADateTime dt = cu.findNext(dtFrom); // 31
            assertNotNull(dt);
            int x = dt.getDay();
            assertTrue(x == 31);

            x = dt.getMinute();
            assertEquals( (i%5)+1, x);

            x = dt.get24Hour();
            assertEquals( (i / 5) % 24, x);
            dtFrom = dt;
        }
        System.out.println("    end, dtFrom="+dtFrom);
    }


    @Test
    public void test7() {
        OACronUtil cu = new OACronUtil("1", "1", "1", "1", "1");
        assertTrue(cu.isValid());
        OADateTime dtFrom = new OADateTime();
        dtFrom.clearTime();

        int m = dtFrom.getDay();
        int max = dtFrom.getDaysInMonth();

        System.out.println("test7, any year with Sunday Jan 1 1:01am , dtFrom="+dtFrom);
        for (int i=0; i<10; i++) {
            // System.out.println(i+" "+dtFrom);
            OADateTime dt = cu.findNext(dtFrom);

            assertEquals(dt.get24Hour(), 1);
            assertEquals(dt.getMinute(), 1);
            assertEquals(dt.getDay(), 1);
            assertEquals(dt.getMonth(), Calendar.JANUARY);
            assertEquals(dt.getDayOfWeek(), Calendar.MONDAY);

            dtFrom = dt;
        }
        System.out.println("    end, dtFrom="+dtFrom);
    }

    @Test
    public void test8() {
        OACronUtil cu = new OACronUtil("0, 30", "12-23", "*", "*", "1-5");
        assertTrue(cu.isValid());
        assertFalse(cu.getIncludeLastDayOfMonth());
        OADateTime dtFrom = new OADateTime();
        dtFrom.clearTime();

        int[] is = cu.getHours();
        assertNotNull(is);
        assertEquals(12, is.length);

        for (int i=0; i<12; i++) {
            assertEquals(i+12, is[i]);
        }



        is = cu.getDaysOfWeek();
        assertNotNull(is);
        assertEquals(5, is.length);
        for (int i=0; i<5; i++) {
            assertEquals(i+1, is[i]);
        }


        int m = dtFrom.getDay();
        int max = dtFrom.getDaysInMonth();

        System.out.println("test8, every weekday, top&bottom of hour, from noon until midnight , dtFrom="+dtFrom);
        for (int i=0; i<150; i++) {
            // System.out.println(i+" "+dtFrom);
            OADateTime dt = cu.findNext(dtFrom);

            int x = dt.getMinute();
            assertTrue(x == 0 || x == 30);

            x = dt.get24Hour();
            assertTrue(x > 11 && x < 24);

            x = dt.getDayOfWeek();
            assertTrue(x >= Calendar.MONDAY && x <= Calendar.FRIDAY);

            dtFrom = dt;
        }
        System.out.println("    end, dtFrom="+dtFrom);
    }

    @Test
    public void test9() {
        OACronUtil cu = new OACronUtil("3", "0", "*", "*", "0");
        assertTrue(cu.isValid());
        OADateTime dtFrom = new OADateTime();
        dtFrom.clearTime();

        int m = dtFrom.getDay();
        int max = dtFrom.getDaysInMonth();

        System.out.println("test9, every Sunday @ 12:03am, dtFrom="+dtFrom);
        for (int i=0; i<100; i++) {
            // System.out.println(i+" "+dtFrom);
            OADateTime dt = cu.findNext(dtFrom);

            int x = dt.getMinute();
            assertEquals(3, x);

            x = dt.getDayOfWeek();
            assertEquals(x, Calendar.SUNDAY);

            if (i > 0) {
                assertEquals(7, dt.betweenDays(dtFrom));
            }

            dtFrom = dt;
        }
        System.out.println("    end, dtFrom="+dtFrom);
    }

    @Test
    public void test10() {
        OACronUtil cu = new OACronUtil("3,99", "0", "*", "*", "0");
        assertFalse(cu.isValid());
    }

    @Test
    public void test11() {
        OACronUtil cu = new OACronUtil("1", "1", "1,last", "*", "*");
        assertTrue(cu.isValid());
        assertTrue(cu.getIncludeLastDayOfMonth());
        OADateTime dtFrom = new OADateTime();
        dtFrom.clearTime();


        System.out.println("test11, every month on the 1st and last, dtFrom="+dtFrom);
        for (int i=0; i<100; i++) {
            // System.out.println(i+" "+dtFrom);
            OADateTime dt = cu.findNext(dtFrom); // 31
            assertNotNull(dt);
            int x = dt.getDay();

            int max = dtFrom.getDaysInMonth();
            assertTrue(x==1 || x == max);

            x = dt.getMinute();
            assertEquals(1, x);

            x = dt.get24Hour();
            assertEquals(1, x);
            dtFrom = dt;
        }
        System.out.println("    end, dtFrom="+dtFrom);
    }

    @Test
    public void test12() {
        OACronUtil cu = new OACronUtil("1", null, null, null, null);
        assertTrue(cu.isValid());
        assertFalse(cu.getIncludeLastDayOfMonth());
        OADateTime dtFrom = new OADateTime();

        OADateTime dt = cu.findNext(dtFrom);
        assertEquals(0, dt.getSecond());
    }


    @Test
    public void testx() {
        OACronUtil cu = new OACronUtil("1", "0", "*", "*", "0");
        assertTrue(cu.isValid());

        OADateTime dt;

        OADateTime dtFrom = new OADateTime(2017, Calendar.APRIL, 1);   // Saturday
        OADateTime dtx = new OADateTime(2017, Calendar.APRIL, 2, 0, 1);   // Sun
        for (int hr=0; hr<24;hr++) {
            dtFrom.set24Hour(hr);
            dt = cu.findNext(dtFrom);
            assertEquals(dt, dtx);
        }
        dtFrom = new OADateTime(2017, Calendar.APRIL, 2, 0, 0);   // sun

        dt = cu.findNext(dtFrom);
        assertEquals(dt, dtx);

        dtFrom.setMinute(2);
        dtx = new OADateTime(2017, Calendar.APRIL, 9, 0, 1);   // Saturday
        dt = cu.findNext(dtFrom);
        assertEquals(dt, dtx);

        // Test #2 every 5 seconds
        dtFrom = new OADateTime();
        cu = new OACronUtil("0,5,10,15,20,25,30,35,40,45,50,55", "*", "*", "*", "*");
        assertTrue(cu.isValid());

        int[] is = cu.getMinutes();
        assertNotNull(is);
        assertEquals(12, is.length);


        for (int i=0; i<1000; i++) {
            dt = cu.findNext(dtFrom);
            int mins = dt.getMinute();
            assertEquals(0, mins % 5);
            dtFrom = dt;
        }

        // Test #3 every minute
        dtFrom = new OADateTime();
        dtFrom.setSecond(0);
        dtFrom.setMilliSecond(0);
        long ts = dtFrom.getTime();
        cu = new OACronUtil("*", "*", "*", "*", "*");
        assertTrue(cu.isValid());
        for (int i=0; i<1000; i++) {
            dt = cu.findNext(dtFrom);
            long diff = dt.getTime() - ts;
            assertEquals(diff, (60*1000));
            ts += (60 * 1000);
            dtFrom = dt;
        }


        dtFrom = new OADateTime(2017, Calendar.APRIL, 24);
        dtFrom = new OADateTime();
        cu = new OACronUtil("0,5,10,15,21", "1,2,3,4,5,23,22,21,20", "25", "*", "0,1,3,5");
        assertTrue(cu.isValid());
        for (int i=0; i<1; i++) {
            dt = cu.findNext(dtFrom);  // sunday at 12:01am
            // System.out.println(i+") from="+dtFrom+" => "+dt.toString("EEE MM/dd/yyyy HH:mm"));

            dt = dt.addMinutes(1);
            dtFrom = dt;
        }
    }

}
