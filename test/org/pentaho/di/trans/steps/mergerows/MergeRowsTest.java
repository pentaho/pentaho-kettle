/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mergerows;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.junit.Test;

import static org.junit.Assert.*;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;

public class MergeRowsTest {

  String keyField = "key";
  String compareField = "compareValue";
  String extraField = "extraValue";
  String flagField = "flagField";

  // Stuff to set up row generators
  String[] fieldName = { keyField, compareField, extraField };
  String[] type = { "String", "String", "String" };
  String[] fieldFormat = { "", "", "" };
  String[] group = { "", "", "" };
  String[] decimal = { "", "", "" };
  String[] currency = { "", "", "" };
  int[] intDummies = { -1, -1, -1 };
  boolean[] setEmptystring = { false, false, false };

  public RowMetaInterface createResultRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();
    try {
      ValueMetaInterface[] valuesMeta = {
        ValueMetaFactory.createValueMeta( keyField, ValueMetaInterface.TYPE_STRING ),
        ValueMetaFactory.createValueMeta( compareField, ValueMetaInterface.TYPE_STRING ),
        ValueMetaFactory.createValueMeta( extraField, ValueMetaInterface.TYPE_STRING ),
        ValueMetaFactory.createValueMeta( flagField, ValueMetaInterface.TYPE_STRING )
      };
      for ( int i = 0; i < valuesMeta.length; i++ ) {
        rm.addValueMeta( valuesMeta[i] );
      }
    } catch ( Exception ex ) {
      return null;
    }
    return rm;
  }

  public List<RowMetaAndData> createResultData(Object[] values) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface();


    list.add( new RowMetaAndData( rm, values ) );

    return list;
  }

  /**
   *  Check the 2 lists comparing the rows in order.
   *  If they are not the same fail the test.
   */
  public void checkRows( List<RowMetaAndData> rows1, List<RowMetaAndData> rows2 ) {
    int idx = 1;
    if ( rows1.size() != rows2.size() ) {
      fail( "Number of rows is not the same: " + rows1.size() + " and " + rows2.size() );
    }
    Iterator<RowMetaAndData> it1 = rows1.iterator();
    Iterator<RowMetaAndData> it2 = rows2.iterator();

    while ( it1.hasNext() && it2.hasNext() ) {
      RowMetaAndData rm1 = it1.next();
      RowMetaAndData rm2 = it2.next();

      Object[] r1 = rm1.getData();
      Object[] r2 = rm2.getData();

      if ( rm1.size() != rm2.size() ) {
        fail( "row size of row at " + idx + " is not equal (" + rm1.size() + "," + rm2.size() + ")" );
      }
      int[] fields = new int[1];
      for ( int ydx = 0; ydx < rm1.size(); ydx++ ) {
        fields[0] = ydx;
        try {
          if ( rm1.getRowMeta().compare( r1, r2, fields ) != 0 ) {
            fail( "row nr " + idx + " is not equal at field nr "
              + ydx + "(" + rm1.toString() + ";" + rm2.toString() + ")" );
          }
        } catch ( KettleValueException e ) {
          fail( "row nr " + idx + " is not equal at field nr "
            + ydx + "(" + rm1.toString() + ";" + rm2.toString() + ")" );
        }
      }

      idx++;
    }
  }

  void createRowGenerator(
    TransMeta transMeta, PluginRegistry registry,
    String stepName, String[] values,
    StepMeta mergeRowsStep, MergeRowsMeta mergeRowsMeta,
    int index
  ) {
    RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();
    String rowGeneratorPid = registry.getPluginId( StepPluginType.class, rowGeneratorMeta );
    StepMeta rowGeneratorStep = new StepMeta( rowGeneratorPid, stepName, rowGeneratorMeta );
    transMeta.addStep( rowGeneratorStep );

    rowGeneratorMeta.setDefault();
    rowGeneratorMeta.setFieldName( fieldName );
    rowGeneratorMeta.setFieldType( type );
    rowGeneratorMeta.setFieldLength( intDummies );
    rowGeneratorMeta.setFieldPrecision( intDummies );
    rowGeneratorMeta.setRowLimit( "1" );
    rowGeneratorMeta.setFieldFormat( fieldFormat );
    rowGeneratorMeta.setGroup( group );
    rowGeneratorMeta.setDecimal( decimal );
    rowGeneratorMeta.setCurrency( currency );
    rowGeneratorMeta.setEmptyString( setEmptystring );

    rowGeneratorMeta.setValue( values );

    TransHopMeta hi1 = new TransHopMeta( rowGeneratorStep, mergeRowsStep );
    transMeta.addTransHop( hi1 );

    List<StreamInterface> infoStreams = mergeRowsMeta.getStepIOMeta().getInfoStreams();
    StreamInterface infoStream = infoStreams.get( index );
    infoStream.setStepMeta( transMeta.findStep( stepName ) );
  }

  void testOneRow(String transName, String[] referenceValues, String[] comparisonValues, Object[] goldenImageRowValues) throws Exception {
    KettleEnvironment.init();

    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( transName );
    PluginRegistry registry = PluginRegistry.getInstance();

    // Create a merge rows step
    String mergeRowsStepName = "merge rows step";
    MergeRowsMeta mergeRowsMeta = new MergeRowsMeta();

    String mergeRowsStepPid = registry.getPluginId( StepPluginType.class, mergeRowsMeta );
    StepMeta mergeRowsStep = new StepMeta( mergeRowsStepPid, mergeRowsStepName, mergeRowsMeta );
    transMeta.addStep( mergeRowsStep );

    mergeRowsMeta.setKeyFields( new String[]{ keyField } );
    mergeRowsMeta.setValueFields( new String[]{ compareField } );
    mergeRowsMeta.setFlagField( flagField );

    List<StreamInterface> infoStreams = mergeRowsMeta.getStepIOMeta().getInfoStreams();

    //
    // create a reference stream (row generator step)
    //
    createRowGenerator(
      transMeta, registry,
      "reference row generator", referenceValues,
      mergeRowsStep, mergeRowsMeta,
      0
    );

    //
    // create a comparison stream (row generator step)
    //
    createRowGenerator(
      transMeta, registry,
      "comparison row generator", comparisonValues,
      mergeRowsStep, mergeRowsMeta,
      1
    );

    // Now execute the transformation
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( mergeRowsStepName, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    trans.startThreads();
    trans.waitUntilFinished();

    // Now check whether the output is still as we expect.
    List<RowMetaAndData> goldenImageRows = createResultData( goldenImageRowValues );
    List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );
  }

  @Test
  public void testMergeRowsIdentical() throws Exception {
    //if not in backwards compatible mode, use the comparison values when values are "identical"
    System.setProperty( Const.KETTLE_COMPATIBILITY_MERGE_ROWS_USE_REFERENCE_STREAM_WHEN_IDENTICAL, "N" );
    String[] refs = new String[]{ "key", "compareValue1", "extraValue1" };
    String[] comp = new String[]{ "key", "compareValue1", "extraValue2" };
    Object[] gold = new Object[]{ "key", "compareValue1", "extraValue2", "identical" };
    testOneRow("testMergeRowsIdentical", refs, comp, gold);
  }

  @Test
  public void testMergeRowsIdenticalPDI736Compatible() throws Exception {
    //if not in backwards compatible mode, use the reference values when values are "identical"
    System.setProperty( Const.KETTLE_COMPATIBILITY_MERGE_ROWS_USE_REFERENCE_STREAM_WHEN_IDENTICAL, "Y" );
    String[] refs = new String[]{ "key", "compareValue1", "extraValue1" };
    String[] comp = new String[]{ "key", "compareValue1", "extraValue2" };
    Object[] gold = new Object[]{ "key", "compareValue1", "extraValue1", "identical" };
    testOneRow("testMergeRowsIdenticalPDI736Compatible", refs, comp, gold);
  }

  @Test
  public void testMergeRowsChanged() throws Exception {
    //regardless of backwards compatible mode, use the comparison values when values are "changed"
    System.setProperty( Const.KETTLE_COMPATIBILITY_MERGE_ROWS_USE_REFERENCE_STREAM_WHEN_IDENTICAL, "N" );
    String[] refs = new String[]{ "key", "compareValue1", "extraValue1" };
    String[] comp = new String[]{ "key", "this value changed", "extraValue2" };
    Object[] gold = new Object[]{ "key", "this value changed", "extraValue2", "changed" };
    testOneRow("testMergeRowsChanged", refs, comp, gold);
  }

  @Test
  public void testMergeRowsChangedPDI736Compatible() throws Exception {
    //regardless of backwards compatible mode, use the comparison values when values are "changed"
    System.setProperty( Const.KETTLE_COMPATIBILITY_MERGE_ROWS_USE_REFERENCE_STREAM_WHEN_IDENTICAL, "Y" );
    String[] refs = new String[]{ "key", "compareValue1", "extraValue1" };
    String[] comp = new String[]{ "key", "this value changed", "extraValue2" };
    Object[] gold = new Object[]{ "key", "this value changed", "extraValue2", "changed" };
    testOneRow("testMergeRowsChangedPDI736Compatible", refs, comp, gold);
  }

}

