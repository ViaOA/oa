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
package com.viaoa.hub;

/**
*/
public class LinkedHub<TYPE> extends Hub<TYPE> {

    /* this one is not a good idea - since the hub is not populated when created and it could need to be linked to hubTo.AO
    public LinkedHub(Class<TYPE> clazz, Hub<?> hubTo, String toPropertyName) {
    	super(clazz);
    	setLinkHub(hubTo, toPropertyName);
    }
    */
    public LinkedHub(Class<TYPE> clazz) {
        super(clazz);
    }
}

