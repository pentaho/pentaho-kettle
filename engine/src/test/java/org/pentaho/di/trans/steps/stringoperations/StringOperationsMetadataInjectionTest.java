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
