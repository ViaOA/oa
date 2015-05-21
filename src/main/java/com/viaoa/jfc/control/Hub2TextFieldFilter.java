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
package com.viaoa.jfc.control;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;

/** 
    Used for binding a JTextField component to perform filter on a Hub.
*/
public class Hub2TextFieldFilter extends TextFieldController {
    
    public Hub2TextFieldFilter(Hub hubMaster, Hub hubFiltered, JTextField tf, String propertyPath) {
        super(hubMaster, tf, propertyPath);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
//qqqqqqqqqqqqqqqq
        
        
    }
}
