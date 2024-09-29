/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
    check( "TABLE_NAME_FIELD", () -> meta.getKeyFields()[0].getKeyLookup() );
    check( "COMPARATOR", () -> meta.getKeyFields()[0].getKeyCondition() );
    check( "STREAM_FIELDNAME_1", () -> meta.getKeyFields()[0].getKeyStream() );
    check( "STREAM_FIELDNAME_2", () -> meta.getKeyFields()[0].getKeyStream2() );
    skipPropertyTest( "CONNECTIONNAME" );
  }
}