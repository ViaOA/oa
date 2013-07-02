/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
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
