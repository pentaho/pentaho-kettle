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

package org.pentaho.di.trans.steps.systemdata;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;

public class SystemDataMetaInjectionTest extends BaseMetadataInjectionTest<SystemDataMeta> {

  @Before
  public void setup() {
    setup( new SystemDataMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_NAME", new StringGetter() {
      @Override
      public String get() {
        return meta.getFieldName()[ 0 ];
      }
    } );
    check( "FIELD_TYPE", new EnumGetter() {
      @Override
      public Enum<?> get() {
        return meta.getFieldType()[ 0 ];
      }
    }, SystemDataTypes.class );
  }
}
