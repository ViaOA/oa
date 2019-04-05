package com.viaoa.comm.io;

import com.viaoa.annotation.OAClass;
import com.viaoa.object.OAObject;

/**
 * Dummy class that is used as a holder when reading an objectInputStream that has classes
 * that no longer exist.
 * @author vvia
 *
 */
@OAClass (addToCache = false, initialize = false, localOnly = true, useDataSource = false)
public class IODummy extends OAObject {
    private static final long serialVersionUID = 1L; // internally used by Java Serialization to identify this version of OAObject.

    
}
