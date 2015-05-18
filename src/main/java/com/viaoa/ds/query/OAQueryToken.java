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
package com.viaoa.ds.query;

/** 
    Internally used by OADataSources to parse object queries so that they can be converted
    into DataSource specific native queries.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAQueryToken implements OAQueryTokenType {
    public int type, subtype;
    public String value;

    public boolean isOperator() {
        return (type == OAQueryToken.OPERATOR || type == OAQueryToken.GT || type == OAQueryToken.GE || type == OAQueryToken.LT || type == OAQueryToken.LE || type == OAQueryToken.EQUAL || type == OAQueryToken.NOTEQUAL || type == OAQueryToken.LIKE);
    }
}
