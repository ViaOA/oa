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
package com.viaoa.jfc.console;

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAProperty;
import com.viaoa.object.OAObject;
import com.viaoa.util.OADateTime;

@OAClass(initialize=false, localOnly=true, useDataSource=false)
public class Console extends OAObject {

    public static final String P_DateTime = "DateTime";
    public static final String P_Text = "Text";
    
    protected OADateTime dateTime;
    protected String text;

    public Console() {
        if (!isLoading()) {
            setDateTime(new OADateTime());
        }
    }
    
    @OAProperty()
    public OADateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(OADateTime newValue) {
        fireBeforePropertyChange(P_DateTime, this.dateTime, newValue);
        OADateTime old = dateTime;
        this.dateTime = newValue;
        firePropertyChange(P_DateTime, old, this.dateTime);
    }
    
    @OAProperty()
    public String getText() {
        return text;
    }
    public void setText(String newValue) {
        fireBeforePropertyChange(P_Text, this.text, newValue);
        String old = text;
        this.text = newValue;
        firePropertyChange(P_Text, old, this.text);
    }
}
