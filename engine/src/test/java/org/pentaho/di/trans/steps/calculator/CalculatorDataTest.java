/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.calculator;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.ValueMetaInterface;

import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public class CalculatorDataTest {

  @Test
  public void dataReturnsCachedValues() throws Exception {
    KettleEnvironment.init( false );

    CalculatorData data = new CalculatorData();
    ValueMetaInterface valueMeta = data.getValueMetaFor( ValueMetaInterface.TYPE_INTEGER, null );
    ValueMetaInterface shouldBeTheSame = data.getValueMetaFor( ValueMetaInterface.TYPE_INTEGER, null );
    assertTrue( "CalculatorData should cache loaded value meta instances", valueMeta == shouldBeTheSame );
  }
}
