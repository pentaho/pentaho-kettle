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


package org.pentaho.di.trans.steps.valuemapper;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class ValueMapperMetaInjectionTest extends BaseMetadataInjectionTest<ValueMapperMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new ValueMapperMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getFieldToUse();
      }
    } );
    check( "TARGET_FIELDNAME", new StringGetter() {
      public String get() {
        return meta.getTargetField();
      }
    } );
    check( "NON_MATCH_DEFAULT", new StringGetter() {
      public String get() {
        return meta.getNonMatchDefault();
      }
    } );
    check( "SOURCE", new StringGetter() {
      public String get() {
        return meta.getSourceValue()[0];
      }
    } );
    check( "TARGET", new StringGetter() {
      public String get() {
        return meta.getTargetValue()[0];
      }
    } );
  }
}
