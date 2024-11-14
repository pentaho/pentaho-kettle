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


package org.pentaho.di.trans.steps.stringoperations;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class StringOperationsMetadataInjectionTest extends BaseMetadataInjectionTest<StringOperationsMeta> {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new StringOperationsMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_IN_STREAM", () -> meta.getFieldInStream()[0] );
    check( "FIELD_OUT_STREAM", () -> meta.getFieldOutStream()[0] );
    check( "TRIM_TYPE", () -> meta.getTrimType()[0] );
    check( "LOWER_UPPER", () -> meta.getLowerUpper()[0] );
    check( "INIT_CAP", () -> meta.getInitCap()[0] );
    check( "ESCAPE", () -> meta.getMaskXML()[0] );
    check( "DIGITS", () -> meta.getDigits()[0] );
    check( "REMOVE_SPECIAL_CHARACTERS", () -> meta.getRemoveSpecialCharacters()[0] );
    check( "PADDING_TYPE", () -> meta.getPaddingType()[0] );
    check( "PADDING_LENGTH", () -> meta.getPadLen()[0] );
    check( "PADDING_CHAR", () -> meta.getPadChar()[0] );
  }
}
