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
package com.viaoa.object;


public class OAEditMessage {
    static final long serialVersionUID = 1L;
    private String msg;
    private Throwable throwable;
    
    public OAEditMessage() {
    }
    public OAEditMessage(String msg) {
        this.msg = msg;
    }
    public void setMessage(String msg) {
        this.msg = msg;
    }
    public String getMessage() {
        return this.msg;
    }
    public Throwable getThrowable() {
        return throwable;
    }
    public void setThrowable(Throwable t) {
        this.throwable = t;
    }
}    
	
