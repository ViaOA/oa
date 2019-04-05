package test.xice.tsac;

import com.viaoa.OAUnitTest;
import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.Model;
import test.xice.tsac.model.oa.cs.ServerRoot;

public class TsacUnitTest extends OAUnitTest {

    protected Model model;
    protected DataGenerator dataGenerator;
    
    public void reset(boolean bDataGen) {
        super.reset();

        model = new Model();
        ServerRoot sr = new ServerRoot();
        ModelDelegate.initialize(sr, null);
        
        if (bDataGen) {
            getDataGenerator().createSampleData1();
        }
    }
    
    public DataGenerator getDataGenerator() {
        if (dataGenerator == null) {
            dataGenerator = new DataGenerator(model);
        }
        return dataGenerator;
    }
}

