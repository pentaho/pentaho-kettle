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
