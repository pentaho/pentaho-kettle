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

package org.pentaho.di.trans.steps.stringcut;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransformationIT;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

/**
 * Test class for the StringCut step.
 *
 * @author Matt Burgess
 */
public class StringCutIT extends TransformationIT {

  public StringCutIT() throws KettleException {
    super();
  }

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaString( "field1" ), new ValueMetaString( "field2" ),

    };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public RowMetaInterface createResultRowMetaInterface1() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    {
      new ValueMetaString( "field1" ), new ValueMetaString( "field2" ),
      new ValueMetaString( "outf3" ),

    };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public RowMetaInterface createResultRowMetaInterface2() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    {
      new ValueMetaString( "field1" ), new ValueMetaString( "field2" ),
      new ValueMetaString( "outf3" ), new ValueMetaString( "outf4" ),

    };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  /**
   * Create data rows.
   *
   * @return List of row and meta data
   */
  public List<RowMetaAndData> createData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "abcdef", "ghijkl" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list.subList( 0, 1 );
  }

  public List<RowMetaAndData> createResultData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface1();

    Object[] r1 = new Object[] { "abcdef", "ghijkl", "hi" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list.subList( 0, 1 );
  }

  public List<RowMetaAndData> createResultData2() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createResultRowMetaInterface2();

    Object[] r1 = new Object[] { "abcdef", "ghijkl", "a", "hi" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list.subList( 0, 1 );
  }

  /**
   * This is a test for PDI-8042, where the first row of meta has no output field but the second does.
   */
  @Test
  public void testStringCut1() throws KettleException {

    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "stringcuttest1" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi = new TransHopMeta( injectorStep, dummyStep1 );
    transMeta.addTransHop( hi );

    //
    // Create a String Cut step
    //
    String stringcutStepname = "string cut step";
    StringCutMeta scm = new StringCutMeta();
    scm.setFieldInStream( new String[] { "field1", "field2" } );
    scm.setFieldOutStream( new String[] { null, "outf3" } );
    scm.setCutFrom( new String[] { null, "1" } );
    scm.setCutTo( new String[] { null, "3" } );

    String stringCutStepPid = registry.getPluginId( StepPluginType.class, scm );
    StepMeta stringCutStep = new StepMeta( stringCutStepPid, stringcutStepname, scm );
    transMeta.addStep( stringCutStep );

    TransHopMeta hi2 = new TransHopMeta( dummyStep1, stringCutStep );
    transMeta.addTransHop( hi2 );

    //
    // Create a dummy step 2
    //
    String dummyStepname2 = "dummy step 2";
    DummyTransMeta dm2 = new DummyTransMeta();

    String dummyPid2 = registry.getPluginId( StepPluginType.class, dm2 );
    StepMeta dummyStep2 = new StepMeta( dummyPid2, dummyStepname2, dm2 );
    transMeta.addStep( dummyStep2 );

    TransHopMeta hi3 = new TransHopMeta( stringCutStep, dummyStep2 );
    transMeta.addTransHop( hi3 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector dummyRc1 = new RowStepCollector();
    si.addRowListener( dummyRc1 );

    si = trans.getStepInterface( stringcutStepname, 0 );
    RowStepCollector stringCutRc = new RowStepCollector();
    si.addRowListener( stringCutRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData1();
    Iterator<RowMetaAndData> it = inputList.iterator();
    while ( it.hasNext() ) {
      RowMetaAndData rm = it.next();
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows1 = dummyRc1.getRowsRead();
    checkRows( resultRows1, inputList );

    List<RowMetaAndData> goldRows = createResultData1();
    List<RowMetaAndData> resultRows2 = stringCutRc.getRowsWritten();
    checkRows( resultRows2, goldRows );
  }

  /**
   * This is a generic test using two input fields and different cuts for each
   */
  @Test
  public void testStringCut2() throws KettleException {

    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "stringcuttest1" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi = new TransHopMeta( injectorStep, dummyStep1 );
    transMeta.addTransHop( hi );

    //
    // Create a String Cut step
    //
    String stringcutStepname = "string cut step";
    StringCutMeta scm = new StringCutMeta();
    scm.setFieldInStream( new String[] { "field1", "field2" } );
    scm.setFieldOutStream( new String[] { "outf3", "outf4" } );
    scm.setCutFrom( new String[] { "0", "1" } );
    scm.setCutTo( new String[] { "1", "3" } );

    String stringCutStepPid = registry.getPluginId( StepPluginType.class, scm );
    StepMeta stringCutStep = new StepMeta( stringCutStepPid, stringcutStepname, scm );
    transMeta.addStep( stringCutStep );

    TransHopMeta hi2 = new TransHopMeta( dummyStep1, stringCutStep );
    transMeta.addTransHop( hi2 );

    //
    // Create a dummy step 2
    //
    String dummyStepname2 = "dummy step 2";
    DummyTransMeta dm2 = new DummyTransMeta();

    String dummyPid2 = registry.getPluginId( StepPluginType.class, dm2 );
    StepMeta dummyStep2 = new StepMeta( dummyPid2, dummyStepname2, dm2 );
    transMeta.addStep( dummyStep2 );

    TransHopMeta hi3 = new TransHopMeta( stringCutStep, dummyStep2 );
    transMeta.addTransHop( hi3 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector dummyRc1 = new RowStepCollector();
    si.addRowListener( dummyRc1 );

    si = trans.getStepInterface( stringcutStepname, 0 );
    RowStepCollector stringCutRc = new RowStepCollector();
    si.addRowListener( stringCutRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData1();
    Iterator<RowMetaAndData> it = inputList.iterator();
    while ( it.hasNext() ) {
      RowMetaAndData rm = it.next();
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows1 = dummyRc1.getRowsRead();
    checkRows( resultRows1, inputList );

    List<RowMetaAndData> goldRows = createResultData2();
    List<RowMetaAndData> resultRows2 = stringCutRc.getRowsWritten();
    checkRows( resultRows2, goldRows );
  }

  /**
   * This is a generic test using two input fields and different cuts for each
   */
  public void testPDI11236() throws KettleException {

    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "stringcuttest3" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi = new TransHopMeta( injectorStep, dummyStep1 );
    transMeta.addTransHop( hi );

    //
    // Create a String Cut step
    //
    String stringcutStepname = "string cut step";
    StringCutMeta scm = new StringCutMeta();
    scm.setFieldInStream( new String[] { "field1", "field1" } );
    scm.setFieldOutStream( new String[] { "", "field1" } );
    scm.setCutFrom( new String[] { "1", "0" } );
    scm.setCutTo( new String[] { "3", "1" } );

    String stringCutStepPid = registry.getPluginId( StepPluginType.class, scm );
    StepMeta stringCutStep = new StepMeta( stringCutStepPid, stringcutStepname, scm );
    transMeta.addStep( stringCutStep );

    TransHopMeta hi2 = new TransHopMeta( dummyStep1, stringCutStep );
    transMeta.addTransHop( hi2 );

    //
    // Create a dummy step 2
    //
    String dummyStepname2 = "dummy step 2";
    DummyTransMeta dm2 = new DummyTransMeta();

    String dummyPid2 = registry.getPluginId( StepPluginType.class, dm2 );
    StepMeta dummyStep2 = new StepMeta( dummyPid2, dummyStepname2, dm2 );
    transMeta.addStep( dummyStep2 );

    TransHopMeta hi3 = new TransHopMeta( stringCutStep, dummyStep2 );
    transMeta.addTransHop( hi3 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector dummyRc1 = new RowStepCollector();
    si.addRowListener( dummyRc1 );

    si = trans.getStepInterface( stringcutStepname, 0 );
    RowStepCollector stringCutRc = new RowStepCollector();
    si.addRowListener( stringCutRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createDataBinary();
    Iterator<RowMetaAndData> it = inputList.iterator();
    while ( it.hasNext() ) {
      RowMetaAndData rm = it.next();
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows1 = dummyRc1.getRowsRead();
    checkRows( resultRows1, inputList );

    List<RowMetaAndData> goldRows = createBinaryResultData();
    List<RowMetaAndData> resultRows2 = stringCutRc.getRowsWritten();
    checkRows( resultRows2, goldRows );
  }

  private List<RowMetaAndData> createDataBinary() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createBinaryRowMetaInterface();

    Object[] r1 = new Object[] { "abcdef".getBytes(), "ghijkl".getBytes() };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list.subList( 0, 1 );
  }

  private RowMetaInterface createBinaryRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaString( "field1" ), new ValueMetaString( "field2" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      valuesMeta[i].setStorageMetadata( createRowMetaInterface().getValueMeta( i ) );
      valuesMeta[i].setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  private RowMetaInterface createBinaryResultRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaString( "field1" ), new ValueMetaString( "field2" ),
      new ValueMetaString( "field1_1" ), };

    valuesMeta[1].setStorageMetadata( createRowMetaInterface().getValueMeta( 1 ) );
    valuesMeta[1].setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    for ( ValueMetaInterface aValuesMeta : valuesMeta ) {
      rm.addValueMeta( aValuesMeta );
    }

    return rm;
  }

  public List<RowMetaAndData> createBinaryResultData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createBinaryResultRowMetaInterface();

    Object[] r1 = new Object[] { "bc", "ghijkl".getBytes(), "a" };

    // Now not used row but it seems to be such result in
    // conversion work with ReplaceString and StringOperations
    // Object[] r2 = new Object[] { "bc", "ghijkl".getBytes(), "b" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list.subList( 0, 1 );
  }
}
