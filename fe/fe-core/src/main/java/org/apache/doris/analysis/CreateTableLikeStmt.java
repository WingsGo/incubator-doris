// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.analysis;


import org.apache.doris.catalog.Catalog;
import org.apache.doris.common.ErrorCode;
import org.apache.doris.common.ErrorReport;
import org.apache.doris.common.FeNameFormat;
import org.apache.doris.common.UserException;
import org.apache.doris.mysql.privilege.PrivPredicate;
import org.apache.doris.qe.ConnectContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateTableLikeStmt extends DdlStmt {
    private static final Logger LOG = LogManager.getLogger(CreateTableLikeStmt.class);

    private boolean isExternal;
    private boolean ifNotExists;
    private TableName tableName;
    private TableName existedTableName;

    public CreateTableLikeStmt(boolean ifNotExists, boolean isExternal, TableName tableName, TableName existedTableName) {
        this.ifNotExists = ifNotExists;
        this.isExternal = isExternal;
        this.tableName = tableName;
        this.existedTableName = existedTableName;
    }

    public boolean isSetIfNotExists() {
        return ifNotExists;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public String getDbName() {
        return tableName.getDb();
    }

    public String getTableName() {
        return tableName.getTbl();
    }

    public String getExistedDbName() {
        return existedTableName.getDb();
    }

    public String getExistedTableName() {
        return existedTableName.getTbl();
    }

    @Override
    public void analyze(Analyzer analyzer) throws UserException {
        super.analyze(analyzer);
        existedTableName.analyze(analyzer);
        if (!Catalog.getCurrentCatalog().getAuth().checkTblPriv(ConnectContext.get(), existedTableName.getDb(),
                existedTableName.getTbl(), PrivPredicate.SELECT)) {
            ErrorReport.reportAnalysisException(ErrorCode.ERR_SPECIFIC_ACCESS_DENIED_ERROR, "SELECT");
        }

        tableName.analyze(analyzer);
        FeNameFormat.checkTableName(getTableName());
        if (!Catalog.getCurrentCatalog().getAuth().checkTblPriv(ConnectContext.get(), tableName.getDb(),
                tableName.getTbl(), PrivPredicate.CREATE)) {
            ErrorReport.reportAnalysisException(ErrorCode.ERR_SPECIFIC_ACCESS_DENIED_ERROR, "CREATE");
        }
    }

    @Override
    public String toSql() {
        return String.format("CREATE TABLE %s LIKE %s", tableName.toSql(), existedTableName.toSql());
    }

    @Override
    public String toString() {
        return toSql();
    }
}
