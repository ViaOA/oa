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
    public static final int SEPERATORBEGIN = 4;
    public static final int SEPERATOREND = 5;
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
    public static final int NULL = 18;
    public static final int STRINGSQ = 19; // single quote
    public static final int STRINGDQ = 20; // double quote
    public static final int STRINGESC = 21; // escape bracket "{"
    public static final int TRUE = 22;
    public static final int FALSE = 23;
    public static final int PASSTHRU = 24;  // PASS[xxx]THRU
    public static final int QUESTION = 25;  // question mark "?"
    public static final int FUNCTIONBEGIN = 26;  // the '(' for a sql function, ex: lower(lastName)
    public static final int FUNCTIONEND = 27;    // the ')' for a sql function, ex: lower(lastName)

}
