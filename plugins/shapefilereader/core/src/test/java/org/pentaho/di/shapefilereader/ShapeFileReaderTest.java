/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.shapefilereader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.gis.shapefiles.Shape;
import org.pentaho.gis.shapefiles.ShapeFile;
import org.pentaho.gis.shapefiles.ShapeFileHeader;
import org.pentaho.gis.shapefiles.ShapePoint;
import org.pentaho.gis.shapefiles.ShapePolyLine;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith( MockitoJUnitRunner.class )
public class ShapeFileReaderTest {

  private RowMeta rowMeta;
  private ShapeFile shapeFile;
  private ShapeFileHeader shapeFileHeader;
  private ShapeFileReader shapeFileReader;
  private ShapeFileReaderData shapeFileReaderData;
  private ShapePoint shapePoint;
  private ShapePolyLine shapePolyLine;
  private StepMockHelper<ShapeFileReaderMeta, StepDataInterface> stepMockHelper;

  @Before
  public void setUp() {
    stepMockHelper = StepMockUtil.getStepMockHelper( ShapeFileReaderMeta.class, "ShapeFileReader" );
    rowMeta = new RowMeta();
    // Adding a dummy field to rowMeta to ensure it has at least one column
    rowMeta.addValueMeta( new ValueMetaString( "dummy" ) );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void processRowPolyLineCloneRowTest() throws KettleException {
    shapePoint = mock( ShapePoint.class );
    shapePoint.x = 0;
    shapePoint.y = 0;

    shapePolyLine = mock( ShapePolyLine.class );
    shapePolyLine.nrparts = 0;
    shapePolyLine.nrpoints = 1;
    shapePolyLine.point = new ShapePoint[] { shapePoint };
    when( shapePolyLine.getType() ).thenReturn( Shape.SHAPE_TYPE_POLYLINE );
    when( shapePolyLine.getDbfData() ).thenReturn( new Object[] { new Object() } );
    when( shapePolyLine.getDbfMeta() ).thenReturn( rowMeta );

    shapeFileHeader = mock( ShapeFileHeader.class );
    when( shapeFileHeader.getShapeTypeDesc() ).thenReturn( "ShapeFileHeader Test" );

    shapeFile = mock( ShapeFile.class );
    when( shapeFile.getNrShapes() ).thenReturn( 1 );
    when( shapeFile.getShape( anyInt() ) ).thenReturn( shapePolyLine );
    when( shapeFile.getFileHeader() ).thenReturn( shapeFileHeader );

    shapeFileReaderData = new ShapeFileReaderData();
    shapeFileReaderData.outputRowMeta = rowMeta;
    shapeFileReaderData.shapeFile = shapeFile;
    shapeFileReaderData.shapeNr = 0;

    shapeFileReader = spy( createShapeFileReader() );
    shapeFileReader.first = false;

    Object[] outputRow = new Object[ RowDataUtil.allocateRowData( shapeFileReaderData.outputRowMeta.size() ).length ];
    try (
      MockedStatic<RowDataUtil> rowDataUtilMockedStatic = mockStatic( RowDataUtil.class, withSettings().lenient() ) ) {
      rowDataUtilMockedStatic.when( () -> RowDataUtil.allocateRowData( anyInt() ) ).thenReturn( outputRow );

      shapeFileReader.processRow( stepMockHelper.initStepMetaInterface, shapeFileReaderData );
      verify( shapeFileReader, times( 1 ) ).putRow( eq( shapeFileReaderData.outputRowMeta ), any( Object[].class ) );
      // Changing the original outputRow in order to test if the outputRow was cloned
      outputRow[ 0 ] = "outputRow Clone Test";
      verify( shapeFileReader, times( 0 ) ).putRow( shapeFileReaderData.outputRowMeta, outputRow );
    }
  }

  private ShapeFileReader createShapeFileReader() {
    return new ShapeFileReader(
      stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
      stepMockHelper.trans );
  }
}
