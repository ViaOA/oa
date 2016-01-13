package test.theice.tsac;

import com.viaoa.OAUnitTest;
import test.theice.tsac.delegate.ModelDelegate;
import test.theice.tsac.model.Model;
import test.theice.tsac.model.oa.cs.ServerRoot;

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

