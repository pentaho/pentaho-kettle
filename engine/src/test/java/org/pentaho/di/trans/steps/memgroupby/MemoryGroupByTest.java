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

package org.pentaho.di.trans.steps.memgroupby;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import static org.pentaho.test.util.InternalState.setInternalState;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class MemoryGroupByTest {

  private MemoryGroupBy memGroupBy;
  MemoryGroupByData memGroupByData;

  @Before
  public void setup() {
    memGroupBy = mock( MemoryGroupBy.class );
    memGroupByData = new MemoryGroupByData();
    memGroupByData.aggMeta = new RowMeta(  );
    memGroupByData.outputRowMeta = new RowMeta(  );
    setInternalState( memGroupBy, "data", memGroupByData );
  }

  @Test
  public void updateValueMetaTest() throws KettleException {
    ValueMetaString stringMetaFromOutput = new ValueMetaString( "stringMeta" );
    ValueMetaBinary binaryMetaFromOutput = new ValueMetaBinary( "binaryMeta" );
    ValueMetaBinary binaryMetaFromAgg = new ValueMetaBinary( "binaryMeta" );
    ValueMetaInteger integerMetaFromOutput = new ValueMetaInteger( "integerMeta" );
    memGroupByData.outputRowMeta.addValueMeta( stringMetaFromOutput );
    memGroupByData.outputRowMeta.addValueMeta( binaryMetaFromOutput );
    memGroupByData.outputRowMeta.addValueMeta( integerMetaFromOutput );
    memGroupByData.aggMeta.addValueMeta( binaryMetaFromAgg );

    doCallRealMethod().when( memGroupBy ).updateValueMeta();
    memGroupBy.updateValueMeta();

    assertFalse( memGroupByData.outputRowMeta.getValueMetaList().contains( binaryMetaFromOutput ) );
    assertTrue( memGroupByData.outputRowMeta.getValueMetaList().contains( binaryMetaFromAgg ) );
  }

  @Test
  public void updateValueMetaNoMatchTest() throws KettleException {
    ValueMetaString stringMetaFromOutput = new ValueMetaString( "stringMeta" );
    ValueMetaBinary binaryMetaFromOutput = new ValueMetaBinary( "binaryMeta" );
    ValueMetaBinary binaryMetaFromAgg = new ValueMetaBinary( "binaryMeta2" );
    ValueMetaInteger integerMetaFromOutput = new ValueMetaInteger( "integerMeta" );
    memGroupByData.outputRowMeta.addValueMeta( stringMetaFromOutput );
    memGroupByData.outputRowMeta.addValueMeta( binaryMetaFromOutput );
    memGroupByData.outputRowMeta.addValueMeta( integerMetaFromOutput );
    memGroupByData.aggMeta.addValueMeta( binaryMetaFromAgg );

    doCallRealMethod().when( memGroupBy ).updateValueMeta();
    memGroupBy.updateValueMeta();

    assertTrue( memGroupByData.outputRowMeta.getValueMetaList().contains( binaryMetaFromOutput ) );
    assertFalse( memGroupByData.outputRowMeta.getValueMetaList().contains( binaryMetaFromAgg ) );
  }
}
