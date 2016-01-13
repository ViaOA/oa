package com.viaoa.object;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.hifive.HifiveDataGenerator;
import test.theice.tsac3.model.oa.*;

public class OAObjectAnalyzerTest extends OAUnitTest {

    @Test
    public void test() {
        init();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData1();
        
        
        OAObjectAnalyzer oa = new OAObjectAnalyzer();
        oa.load();
        
        reset();
    }
    
}
