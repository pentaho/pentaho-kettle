/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.groupby;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowHandler;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class GroupByTest  {

  private StepMockHelper<GroupByMeta, GroupByData> mockHelper;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Mock private StepMeta mockStepMeta;
  @Mock private GroupByData mockStepDataInterface;
  @Mock private TransMeta mockTransMeta;
  @Mock private Trans mockTrans;
  @Mock private RowHandler mockRowHandler;

  private int currentInputRow;

  @BeforeClass
  public static void setUpBeforeClass() throws KettlePluginException {
    ValueMetaPluginType.getInstance().searchPlugins();
  }

  @Before
  public void setUp() throws Exception {
    mockHelper =
      new StepMockHelper<>( "Group By", GroupByMeta.class, GroupByData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() throws Exception {
    mockHelper.cleanUp();
  }

  @Test
  public void testProcessRow() throws KettleException {
    GroupByMeta groupByMeta = mock( GroupByMeta.class );
    GroupByData groupByData = mock( GroupByData.class );

    GroupBy groupBySpy = Mockito.spy( new GroupBy( mockHelper.stepMeta, mockHelper.stepDataInterface, 0,
      mockHelper.transMeta, mockHelper.trans ) );
    doReturn( null ).when( groupBySpy ).getRow();
    doReturn( null ).when( groupBySpy ).getInputRowMeta();

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaInteger( "ROWNR" ) );

    List<RowSet> outputRowSets = new ArrayList<RowSet>();
    BlockingRowSet rowSet = new BlockingRowSet( 1 );
    rowSet.putRow( rowMeta, new Object[] { new Long( 0 ) } );
    outputRowSets.add( rowSet );
    groupBySpy.setOutputRowSets( outputRowSets );

    final String[] sub = { "b" };
    doReturn( sub ).when( groupByMeta ).getSubjectField();

    final String[] groupField = { "a" };
    doReturn( groupField ).when( groupByMeta ).getGroupField();

    final String[] aggFields = { "b_g" };
    doReturn( aggFields ).when( groupByMeta ).getAggregateField();

    final int[] aggType = { GroupByMeta.TYPE_GROUP_CONCAT_COMMA };
    doReturn( aggType ).when( groupByMeta ).getAggregateType();

    when( mockHelper.transMeta.getPrevStepFields( mockHelper.stepMeta ) ).thenReturn( new RowMeta() );
    groupBySpy.processRow( groupByMeta, groupByData );

    assertTrue( groupBySpy.getOutputRowSets().get( 0 ).isDone() );
  }

  @Test
  public void testGetFields() {
    RowMeta outputFields = new RowMeta();
    outputFields.addValueMeta( new ValueMetaString( "group_by_field" ) );
    outputFields.addValueMeta( new ValueMetaInteger( "raw_integer" ) );
    outputFields.addValueMeta( new ValueMetaString( "raw_string" ) );

    GroupByMeta meta = new GroupByMeta();
    meta.allocate( 1, 8 );
    meta.setGroupField( new String[]{ "group_by_field" } );
    meta.setAggregateField( new String[]{
      "perc_field", "stddev_field", "median_field", "count_distinct_field",
      "count_any_field", "count_all_field", "concat_comma_field", "concat_custom_field" } );
    meta.setSubjectField( new String[]{
      "raw_integer", "raw_integer", "raw_integer", "raw_integer",
      "raw_integer", "raw_integer", "raw_string", "raw_string" } );
    meta.setAggregateType( new int[] {
      GroupByMeta.TYPE_GROUP_PERCENTILE,
      GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION,
      GroupByMeta.TYPE_GROUP_MEDIAN,
      GroupByMeta.TYPE_GROUP_COUNT_DISTINCT,
      GroupByMeta.TYPE_GROUP_COUNT_ANY,
      GroupByMeta.TYPE_GROUP_COUNT_ALL,
      GroupByMeta.TYPE_GROUP_CONCAT_COMMA,
      GroupByMeta.TYPE_GROUP_CONCAT_STRING } );

    meta.getFields( outputFields, "Group By Step", (RowMetaInterface[]) null, (StepMeta) null,
      (Variables) null, (Repository) null, (IMetaStore) null );

    assertEquals( outputFields.getValueMetaList().size(), 9 );
    assertTrue( outputFields.getValueMeta( 0 ).getType() == ValueMetaInterface.TYPE_STRING );
    assertTrue( outputFields.getValueMeta( 0 ).getName().equals( "group_by_field" ) );
    assertTrue( outputFields.getValueMeta( 1 ).getType() == ValueMetaInterface.TYPE_NUMBER );
    assertTrue( outputFields.getValueMeta( 1 ).getName().equals( "perc_field" ) );
    assertTrue( outputFields.getValueMeta( 2 ).getType() == ValueMetaInterface.TYPE_NUMBER );
    assertTrue( outputFields.getValueMeta( 2 ).getName().equals( "stddev_field" ) );
    assertTrue( outputFields.getValueMeta( 3 ).getType() == ValueMetaInterface.TYPE_NUMBER );
    assertTrue( outputFields.getValueMeta( 3 ).getName().equals( "median_field" ) );
    assertTrue( outputFields.getValueMeta( 4 ).getType() == ValueMetaInterface.TYPE_INTEGER );
    assertTrue( outputFields.getValueMeta( 4 ).getName().equals( "count_distinct_field" ) );
    assertTrue( outputFields.getValueMeta( 5 ).getType() == ValueMetaInterface.TYPE_INTEGER );
    assertTrue( outputFields.getValueMeta( 5 ).getName().equals( "count_any_field" ) );
    assertTrue( outputFields.getValueMeta( 6 ).getType() == ValueMetaInterface.TYPE_INTEGER );
    assertTrue( outputFields.getValueMeta( 6 ).getName().equals( "count_all_field" ) );
    assertTrue( outputFields.getValueMeta( 7 ).getType() == ValueMetaInterface.TYPE_STRING );
    assertTrue( outputFields.getValueMeta( 7 ).getName().equals( "concat_comma_field" ) );
    assertTrue( outputFields.getValueMeta( 8 ).getType() == ValueMetaInterface.TYPE_STRING );
    assertTrue( outputFields.getValueMeta( 8 ).getName().equals( "concat_custom_field" ) );
  }


  @Test
  public void testTempFileIsDeleted_AfterCallingDisposeMethod() throws Exception {
    GroupByData groupByData = new GroupByData();
    groupByData.tempFile = File.createTempFile( "test", ".txt" );

    // emulate connections to file are opened
    groupByData.fosToTempFile = new FileOutputStream( groupByData.tempFile );
    groupByData.fisToTmpFile = new FileInputStream( groupByData.tempFile );

    GroupBy groupBySpy = Mockito.spy( new GroupBy( mockHelper.stepMeta, groupByData, 0,
      mockHelper.transMeta, mockHelper.trans ) );

    assertTrue( groupByData.tempFile.exists() );
    groupBySpy.dispose( mock( StepMetaInterface.class ), groupByData );
    // check file is deleted
    assertFalse( groupByData.tempFile.exists() );

  }

  @Test
  public void testAddToBuffer() throws KettleException, FileSystemException {
    GroupByData groupByData = new GroupByData();
    ArrayList listMock = mock( ArrayList.class );
    when( listMock.size() ).thenReturn( 5001 );
    groupByData.bufferList = listMock;
    groupByData.rowsOnFile = 0;
    RowMetaInterface inputRowMetaMock = mock( RowMetaInterface.class );
    groupByData.inputRowMeta = inputRowMetaMock;

    GroupBy groupBySpy = Mockito.spy(
        new GroupBy( mockHelper.stepMeta, groupByData, 0, mockHelper.transMeta, mockHelper.trans ) );

    GroupByMeta groupByMetaMock = mock( GroupByMeta.class );
    when( groupByMetaMock.getPrefix() ).thenReturn( "group-by-test-temp-file-" );
    when( groupBySpy.getMeta() ).thenReturn( groupByMetaMock );

    String userDir = System.getProperty( "user.dir" );
    String vfsFilePath = "file:///" + userDir;
    when( groupBySpy.environmentSubstitute( anyString() ) ).thenReturn( vfsFilePath );

    Object[] row = { "abc" };
    // tested method itself
    groupBySpy.addToBuffer( row );

    // check if file is created
    assertTrue( groupByData.tempFile.exists() );
    groupBySpy.dispose( groupByMetaMock, groupByData );
    // check file is deleted
    assertFalse( groupByData.tempFile.exists() );

    // since path started with "file:///"
    verify( groupBySpy, times( 1 ) ).retrieveVfsPath( anyString() );
  }

  @Test
  public void testCalculations() throws Exception {
    final RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( new ValueMetaString( "NAME" ) );
    inputRowMeta.addValueMeta( new ValueMetaNumber( "VALUE" ) );

    final RowMetaAndData[] inputRows = new RowMetaAndData[] {
      new RowMetaAndData( inputRowMeta, "Foo", 2255.84 ),
      new RowMetaAndData( inputRowMeta, "Foo", 7381.16 ),
      new RowMetaAndData( inputRowMeta, "Foo", 4516.22 )
    };

    GroupByMeta groupByMeta = new GroupByMeta();
    groupByMeta.setValueField( new String[] { "10", "10", "10" } ); // 10th percentile, a value for each row
    groupByMeta.setSubjectField( new String[] {
      "VALUE", "VALUE", "VALUE", "NAME", "NAME", "VALUE", "NAME", "VALUE", "VALUE" } );
    groupByMeta.setGroupField( new String[] { "NAME" } );
    groupByMeta.setAggregateField( new String[] { "Sum", "Average (Mean)", "Percentile", "Number of Distinct Values",
      "Number of Values", "Standard deviation", "Concatenated", "Minimum", "Maximum" } );
    groupByMeta.setAggregateType( new int[] { GroupByMeta.TYPE_GROUP_SUM, GroupByMeta.TYPE_GROUP_AVERAGE,
      GroupByMeta.TYPE_GROUP_PERCENTILE, GroupByMeta.TYPE_GROUP_COUNT_DISTINCT, GroupByMeta.TYPE_GROUP_COUNT_ALL,
      GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION, GroupByMeta.TYPE_GROUP_CONCAT_COMMA, GroupByMeta.TYPE_GROUP_MIN,
      GroupByMeta.TYPE_GROUP_MAX } );
    groupByMeta.setParentStepMeta( mockStepMeta );

    final String stepName = "Group by";
    when( mockStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    when( mockStepMeta.getName() ).thenReturn( stepName );
    when( mockTransMeta.findStep( stepName ) ).thenReturn( mockStepMeta );
    try {
      when( mockRowHandler.getRow() ).thenAnswer( ( InvocationOnMock invocation ) -> {
        Object[] result = null;
        if ( currentInputRow < inputRows.length ) {
          result = inputRows[ currentInputRow ].getData().clone();
          currentInputRow++;
        }
        return result;
      } );
    } catch ( KettleException e ) {
      fail( e.getMessage() );
    }

    final GroupBy groupBy = new GroupBy( mockStepMeta, mockStepDataInterface, 0, mockTransMeta, mockTrans );
    groupBy.setRowHandler( mockRowHandler );
    groupBy.setInputRowMeta( inputRowMeta );
    groupBy.setLogLevel( LogLevel.ERROR );

    int rowsProcessed = -1;
    boolean result = true;
    while ( result ) {
      rowsProcessed++;
      result = groupBy.processRow( groupByMeta, mockStepDataInterface );
    }

    assertEquals( inputRows.length, rowsProcessed );

    final Object[] results = groupBy.getAggregateResult();
    assertNotNull( results );
    assertEquals( groupByMeta.getSubjectField().length, results.length );

    final NumberFormat formatter = new DecimalFormat( "#.##" );
    assertEquals( "14153.22", formatter.format( results[ 0 ] ) ); // sum
    assertEquals( "4717.74", formatter.format( results[ 1 ] ) ); // average
    assertEquals( "2255.84", formatter.format( results[ 2 ] ) ); // 10th percentile (nearest rank)
    assertEquals( 1L, results[ 3 ] ); // distinct count
    assertEquals( 3L, results[ 4 ] ); // total count
    assertEquals( "2568.6", formatter.format( results[ 5 ] ) ); // standard deviation
    assertEquals( "Foo, Foo, Foo", results[ 6 ] ); // concat, comma
    assertEquals( "2255.84", formatter.format( results[ 7 ] ) ); // min
    assertEquals( "7381.16", formatter.format( results[ 8 ] ) ); // max
  }
}
