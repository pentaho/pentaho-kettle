/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
