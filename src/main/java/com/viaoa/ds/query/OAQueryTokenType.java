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
 * Types of tokens used by tokenizer, and token manager.
 * @author vvia
 *
 */
public interface OAQueryTokenType {

	public static final int EOF = 1;
    public static final int NUMBER = 2;
    public static final int OPERATOR = 3;
    public static final int SEPERATORBEGIN = 4;  // "("  block
    public static final int SEPERATOREND = 5;    // ")"  block
    public static final int VARIABLE = 7;
    public static final int GT = 8;
    public static final int GE = 9;
    public static final int LT = 10;
    public static final int LE = 11;
    public static final int EQUAL = 12;
    public static final int NOTEQUAL = 13;
    public static final int AND = 14;
    public static final int OR = 15;
    public static final int LIKE = 17;
    public static final int NOTLIKE = 18;
    public static final int NULL = 19;
    public static final int STRINGSQ = 20; // single quote
    public static final int STRINGDQ = 21; // double quote
    public static final int STRINGESC = 22; // escape bracket "{"
    public static final int TRUE = 23;
    public static final int FALSE = 24;
    public static final int PASSTHRU = 25;  // PASS[xxx]THRU
    public static final int QUESTION = 26;  // question mark "?"
    public static final int FUNCTIONBEGIN = 27;  // the '(' for a sql function, ex: lower(lastName)
    public static final int FUNCTIONEND = 28;    // the ')' for a sql function, ex: lower(lastName)
    public static final int IN = 29; // operator IN(..,..)
    public static final int COMMA = 30; // separator

}
