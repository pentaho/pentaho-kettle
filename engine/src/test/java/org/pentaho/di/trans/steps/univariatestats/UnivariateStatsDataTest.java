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

package org.pentaho.di.trans.steps.univariatestats;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.test.util.GetterSetterTester;
import org.pentaho.test.util.ObjectTesterBuilder;

public class UnivariateStatsDataTest {
  @Test
  public void testGettersAndSetters() {
    GetterSetterTester<UnivariateStatsData> getterSetterTester =
        new GetterSetterTester<UnivariateStatsData>( UnivariateStatsData.class );

    getterSetterTester.addObjectTester( "fieldIndexes", new ObjectTesterBuilder<FieldIndex[]>().addObject( null )
        .addObject( new FieldIndex[] {} ).useEqualsEquals().build() );
    RowMetaInterface mockRowMetaInterface = mock( RowMetaInterface.class );

    getterSetterTester.addObjectTester( "inputRowMeta", new ObjectTesterBuilder<RowMetaInterface>().addObject( null )
        .addObject( mockRowMetaInterface ).useEqualsEquals().build() );
    mockRowMetaInterface = mock( RowMetaInterface.class );

    getterSetterTester.addObjectTester( "outputRowMeta", new ObjectTesterBuilder<RowMetaInterface>().addObject( null )
        .addObject( mockRowMetaInterface ).useEqualsEquals().build() );
    getterSetterTester.test( new UnivariateStatsData() );
  }
}
