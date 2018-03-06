/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.sync.remote;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;
import com.viaoa.remote.multiplexer.annotation.OARemoteMethod;

@OARemoteInterface()
public interface RemoteClientCallbackInterface {
    
    @OARemoteMethod(noReturnValue=true, timeoutSeconds=2, dontUseQueue=true)
    void stop(String title, String msg);
    
    @OARemoteMethod(dontUseQueue=true)
    String ping(String msg);
    
    @OARemoteMethod(dontUseQueue=true)
    public String performThreadDump(String msg);

}
