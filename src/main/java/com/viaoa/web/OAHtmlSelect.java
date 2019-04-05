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
package com.viaoa.web;

import com.viaoa.hub.Hub;

/**
 * Used for an HTML select.
 *
 */
public class OAHtmlSelect extends OACombo {

    public OAHtmlSelect(String id, Hub hub, String propertyPath, int columns) {
        super(id, hub, propertyPath, columns);
    }
    public OAHtmlSelect(String id, Hub hub, String propertyPath, int columns, int rows) {
        super(id, hub, propertyPath, columns, rows);
    }
}
