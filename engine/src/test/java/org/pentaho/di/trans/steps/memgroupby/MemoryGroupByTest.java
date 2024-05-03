/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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
