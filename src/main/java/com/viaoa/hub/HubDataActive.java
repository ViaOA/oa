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
	Internally used by Hub
	to know the current active object.
	A shared Hub can also use this same object.
*/
class HubDataActive implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization
	
	/**
	    Current object in Hub that the active object.
	    @see Hub#setActiveObject
	    @see Hub#getActiveObject
	*/
	protected transient Object activeObject;
	
	/**
	    Used by Hub.updateDetail() when calling setSharedHub, for Hubs that
	    do not shared same active object, so that active object is set to null.
	*/
	public void clear(boolean eof) {
	    activeObject = null;
	}
}

