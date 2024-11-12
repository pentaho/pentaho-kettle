/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.setvalueconstant;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

/**
 * Created by bmorrise on 5/18/18.
 */


public class SetValueConstantMetaInjectionTest extends BaseMetadataInjectionTest<SetValueConstantMeta> {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new SetValueConstantMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_NAME", () -> meta.getField( 0 ).getFieldName() );
    check( "REPLACE_VALUE", () -> meta.getField( 0 ).getReplaceValue() );
    check( "REPLACE_MASK", () -> meta.getField( 0 ).getReplaceMask() );
    check( "EMPTY_STRING", () -> meta.getField( 0 ).isEmptyString() );
    check( "USE_VARIABLE", () -> meta.isUseVars() );
  }
}
