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
