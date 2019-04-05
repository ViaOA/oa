/*  Copyright 1999-2017 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.util;

public class OADouble {
    private double x;
    
    public OADouble() {
    }
    public OADouble(double x) {
        this.x = x;
    }
    
    public void set(double x) {
        this.x = x;
    }
    public double get() {
        return x;
    }
    
    public double add(double x) {
        this.x += x;
        return this.x;
    }
    public double add() {
        return this.add(1);
    }
    public double subtract(int x) {
        this.x -= x;
        return this.x;
    }
    public double subtract() {
        return this.subtract(1);
    }


}
