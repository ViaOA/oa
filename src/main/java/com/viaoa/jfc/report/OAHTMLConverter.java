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
package com.viaoa.jfc.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

/*
 *    ************* REPLACED with com.viaoa.util.OATemplate ***************
 *    ************* REPLACED with com.viaoa.util.OATemplate ***************
 *    ************* REPLACED with com.viaoa.util.OATemplate ***************
 *    ************* REPLACED with com.viaoa.util.OATemplate ***************
 */
public class OAHTMLConverter extends OATemplate {

    public OAHTMLConverter() {
    }
    public OAHTMLConverter(String htmlTemplate) {
        super(htmlTemplate);
    }

    public void setHtmlTemplate(String temp) {
        super.setTemplate(temp);
    }
    public String getHtmlTemplate() {
        return super.getTemplate();
    }

    public String getHtml(OAObject objRoot1, Hub hubRoot, OAProperties props) {
        return super.process(objRoot1, null, hubRoot, props);
    }
    public void stopHtml() {
        super.stopProcessing();
    }
}
