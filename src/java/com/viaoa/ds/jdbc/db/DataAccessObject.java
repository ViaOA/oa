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
package com.viaoa.ds.jdbc.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.viaoa.object.OAObject;

/**
 * Used for populated OAObject properties with resultSet data.
 * @author vvia
 *
 */
public interface DataAccessObject {

    public class ResultSetInfo {
        ResultSet rs;
        boolean foundInCache;
        public void reset(ResultSet rs) {
            this.rs = rs;
            foundInCache = false;
        }
        public boolean getFoundInCache() {
            return this.foundInCache;
        }
        public void setFoundInCache(boolean b) {
            this.foundInCache = b;
        }
        public ResultSet getResultSet() {
            return rs;
        }
    }
    
    public OAObject getObject(ResultSetInfo rsi) throws SQLException;
    public String getPkeySelectColumns();
    public String getSelectColumns();
    
}
