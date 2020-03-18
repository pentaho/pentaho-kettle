/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.delete;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class DeleteMetadataInjectionTest extends BaseMetadataInjectionTest<DeleteMeta> {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new DeleteMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "TARGET_SCHEMA", () -> meta.getSchemaName() );
    check( "TARGET_TABLE", () -> meta.getTableName() );
    check( "COMMIT_SIZE", () -> meta.getCommitSizeVar() );
    check( "TABLE_NAME_FIELD", () -> meta.getKeyLookup()[0] );
    check( "COMPARATOR", () -> meta.getKeyCondition()[0] );
    check( "STREAM_FIELDNAME_1", () -> meta.getKeyStream()[0] );
    check( "STREAM_FIELDNAME_2", () -> meta.getKeyStream2()[0] );
    skipPropertyTest( "CONNECTIONNAME" );
  }
}
