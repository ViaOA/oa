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
package com.viaoa.jfc;

import javax.swing.JLabel;

import com.viaoa.jfc.control.OAJfcController;

public interface OAJfcComponent {
    public OAJfcController getController();
    public void initialize();
    
    default public void customizeRenderer(JLabel lbl, Object object, Object value, boolean isSelected, boolean hasFocus, int row, boolean wasChanged, boolean wasMouseOver) {
    }

    default public String getToolTipText(Object object, int row, String defaultValue) {
        return defaultValue;
    }

}
