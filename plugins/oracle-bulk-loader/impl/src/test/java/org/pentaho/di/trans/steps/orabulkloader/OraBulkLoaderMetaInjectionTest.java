/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.orabulkloader;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class OraBulkLoaderMetaInjectionTest extends BaseMetadataInjectionTest<OraBulkLoaderMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new OraBulkLoaderMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SCHEMA_NAME", ()-> meta.getSchemaName() );
    check( "TABLE_NAME", ()-> meta.getTableName() );
    check( "SQLLDR_PATH", ()-> meta.getSqlldr() );
    check( "CONTROL_FILE", ()-> meta.getControlFile() );
    check( "DATA_FILE", ()-> meta.getDataFile() );
    check( "LOG_FILE",()-> meta.getLogFile() );
    check( "BAD_FILE",()-> meta.getBadFile());
    check( "DISCARD_FILE", ()-> meta.getDiscardFile());
    check( "FIELD_TABLE", ()-> meta.getFieldTable()[0] );
    check( "FIELD_STREAM", ()-> meta.getFieldStream()[0] );
    check( "FIELD_DATEMASK", ()-> meta.getDateMask()[0] );
    check( "COMMIT_SIZE", ()-> meta.getCommitSize() );
    check( "BIND_SIZE", ()-> meta.getBindSize() );
    check( "READ_SIZE", ()-> meta.getReadSize() );
    check( "MAX_ERRORS", ()-> meta.getMaxErrors() );
    check( "LOAD_METHOD",()-> meta.getLoadMethod() );
    check( "LOAD_ACTION",()-> meta.getLoadAction() );
    check( "ENCODING", ()-> meta.getEncoding() );
    check( "ORACLE_CHARSET_NAME", ()-> meta.getCharacterSetName() );
    check( "DIRECT_PATH", ()-> meta.isDirectPath() );
    check( "ERASE_FILES", ()-> meta.isEraseFiles() );
    check( "DB_NAME_OVERRIDE", ()-> meta.getDbNameOverride() );
    check( "FAIL_ON_WARNING",()-> meta.isFailOnWarning() );
    check( "FAIL_ON_ERROR", ()-> meta.isFailOnError() );
    check( "PARALLEL", ()-> meta.isParallel() );
    check( "RECORD_TERMINATOR", ()-> meta.getAltRecordTerm() );
    check( "CONNECTION_NAME", ()-> "My Connection", "My Connection" );
  }
}
