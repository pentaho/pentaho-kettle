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

package org.pentaho.di.trans.steps.setvaluefield;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class SetValueFieldMetaInjectionTest extends BaseMetadataInjectionTest<SetValueFieldMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Before
  public void setup() {
    setup( new SetValueFieldMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.getFieldName()[ 0 ];
      }
    } );
    check( "REPLACE_BY_FIELD_VALUE", new StringGetter() {
      public String get() {
        return meta.getReplaceByFieldValue()[ 0 ];
      }
    } );
  }
}
