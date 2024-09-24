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

package org.pentaho.di.trans.steps.splitfieldtorows;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class SplitFieldsToRowsMetadataInjectionTest extends BaseMetadataInjectionTest<SplitFieldToRowsMeta> {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new SplitFieldToRowsMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "FIELD_TO_SPLIT", () -> meta.getSplitField() );
    check( "DELIMITER", () -> meta.getDelimiter() );
    check( "NEW_FIELD_NAME", () -> meta.getNewFieldname() );
    check( "INCLUDE_ROWNUM", () -> meta.includeRowNumber() );
    check( "ROWNUM_FIELD_NAME", () -> meta.getRowNumberField() );
    check( "RESET_ROWNUM", () -> meta.resetRowNumber() );
    check( "DELIMITER_IS_REGEX", () -> meta.isDelimiterRegex() );
  }
}
