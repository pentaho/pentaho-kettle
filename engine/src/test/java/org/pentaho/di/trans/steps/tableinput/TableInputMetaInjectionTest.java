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


package org.pentaho.di.trans.steps.tableinput;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class TableInputMetaInjectionTest extends BaseMetadataInjectionTest<TableInputMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new TableInputMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SQL", new StringGetter() {
      public String get() {
        return meta.getSQL();
      }
    } );
    check( "LIMIT", new StringGetter() {
      public String get() {
        return meta.getRowLimit();
      }
    } );
    check( "EXECUTE_FOR_EACH_ROW", new BooleanGetter() {
      public boolean get() {
        return meta.isExecuteEachInputRow();
      }
    } );
    check( "REPLACE_VARIABLES", new BooleanGetter() {
      public boolean get() {
        return meta.isVariableReplacementActive();
      }
    } );
    check( "LAZY_CONVERSION", new BooleanGetter() {
      public boolean get() {
        return meta.isLazyConversionActive();
      }
    } );
    check( "CACHED_ROW_META", new BooleanGetter() {
      public boolean get() {
        return meta.isCachedRowMetaActive();
      }
    } );
    skipPropertyTest( "CONNECTIONNAME" );
  }
}
