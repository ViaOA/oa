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
