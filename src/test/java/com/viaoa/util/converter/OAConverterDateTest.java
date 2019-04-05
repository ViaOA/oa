package com.viaoa.util.converter;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;

import com.viaoa.OAUnitTest;
import com.viaoa.util.OAConverter;
import com.viaoa.util.OADateTime;

import test.xice.tsac3.model.oa.*;

public class OAConverterDateTest extends OAUnitTest {

    @Test
    public void test() {

        Object obj = OAConverter.convert(OADateTime.class, "Nov 10, 2016 6:34:00 am");
        Object objx = OAConverter.convert(Calendar.class, "Nov 10, 2016 6:34:00 am");
        
        int xx = 4;
        xx++;
        
    }
    
}
