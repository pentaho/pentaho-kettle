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
package org.pentaho.di.trans.steps.simplemapping;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

/**
 * Created by moliveira on 11/04/18.
 */
public class SimpleMappingMetaInjectionTest extends BaseMetadataInjectionTest<SimpleMappingMeta> {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new SimpleMappingMeta() );
  }

  @Test
  public void test() throws Exception {

    check( "NAME", () ->  meta.getMappingParameters().getVariable()[0] );
    check( "VALUE", () ->  meta.getMappingParameters().getInputField()[0] );
    check( "INHERIT_VARIABLES", () ->  meta.getMappingParameters().isInheritingAllVariables() );

    check( "FIELD_NAME_FROM_SOURCE_STEP", () ->  meta.getInputMapping().getValueRenames().get( 0 ).getSourceValueName() );
    check( "FIELD_NAME_TO_MAPPING_INPUT_STEP", () ->  meta.getInputMapping().getValueRenames().get( 0 ).getTargetValueName() );
    check( "UPDATE_MAPPED_FIELD_NAMES_DOWNSTREAM", () ->  meta.getInputMapping().isRenamingOnOutput() );
  }
}
