package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.theicetest.tsactest.model.oa.*;
import com.viaoa.HifiveDataGenerator;
import com.viaoa.OAUnitTest;

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
