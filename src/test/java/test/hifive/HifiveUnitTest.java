package test.hifive;

import com.viaoa.OAUnitTest;


public class HifiveUnitTest extends OAUnitTest {

    protected HifiveDataGenerator dataGenerator;
    
    public void reset(boolean bDataGen) {
        super.reset();

        if (bDataGen) {
            getDataGenerator().createSampleData();
        }
    }
    
    public HifiveDataGenerator getDataGenerator() {
        if (dataGenerator == null) {
            dataGenerator = new HifiveDataGenerator();
        }
        return dataGenerator;
    }
}

