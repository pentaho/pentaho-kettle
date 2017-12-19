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

package org.pentaho.di.trans.steps.unique;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;
import org.pentaho.di.trans.steps.sort.SortRowsMeta;
import org.pentaho.di.trans.steps.uniquerows.UniqueRowsMeta;

import junit.framework.TestCase;

/**
 * Test class for the Unique step.
 *
 * These tests only cover the case (in)sensitive comparison of a single key field, and to ensure the first row is not
 * treated as both duplicate and unique.
 *
 * @author Daniel Einspanjer
 */
public class UniqueRowsIT extends TestCase {
  public static int MAX_COUNT = 1000;

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaString( "KEY" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  public List<RowMetaAndData> createData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "abc" };
    Object[] r2 = new Object[] { "ABC" };
    Object[] r3 = new Object[] { "abc" };
    Object[] r4 = new Object[] { "ABC" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );

    return list;
  }

  public List<RowMetaAndData> createDataAllUnique() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "A" };
    Object[] r2 = new Object[] { "B" };
    Object[] r3 = new Object[] { "C" };
    Object[] r4 = new Object[] { "D" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );

    return list;
  }

  public List<RowMetaAndData> createResultDataAllUnique() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "A" };
    Object[] r2 = new Object[] { "B" };
    Object[] r3 = new Object[] { "C" };
    Object[] r4 = new Object[] { "D" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );

    return list;
  }

  public List<RowMetaAndData> createResultDataCaseSensitiveNoPreviousSort() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "abc" };
    Object[] r2 = new Object[] { "ABC" };
    Object[] r3 = new Object[] { "abc" };
    Object[] r4 = new Object[] { "ABC" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );

    return list;
  }

  public List<RowMetaAndData> createResultDataCaseInsensitiveNoPreviousSort() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "abc" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  public List<RowMetaAndData> createResultDataSortCaseSensitiveUniqueCaseSensitive() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "ABC" };
    Object[] r2 = new Object[] { "abc" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );

    return list;
  }

  public List<RowMetaAndData> createResultDataSortCaseSensitiveUniqueCaseInsensitive() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "ABC" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  public List<RowMetaAndData> createResultDataSortCaseInsensitiveUniqueCaseSensitive() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "abc" };
    Object[] r2 = new Object[] { "ABC" };
    Object[] r3 = new Object[] { "abc" };
    Object[] r4 = new Object[] { "ABC" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );

    return list;
  }

  public List<RowMetaAndData> createResultDataSortCaseInsensitiveUniqueCaseInsensitive() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { "abc" };

    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  /**
   * Check the 2 lists comparing the rows in order. If they are not the same fail the test.
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
        fail( "row nr " + idx + " is not equal" );
      }
      int[] fields = new int[r1.length];
      for ( int ydx = 0; ydx < r1.length; ydx++ ) {
        fields[ydx] = ydx;
      }
      try {
        if ( rm1.getRowMeta().compare( r1, r2, fields ) != 0 ) {
          fail( "row nr " + idx + " is not equal" );
        }
      } catch ( KettleValueException e ) {
        fail( "row nr " + idx + " is not equal" );
      }

      idx++;
    }
  }

  public void testCaseSensitiveNoPreviousSort() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "uniquerowstest" );

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
    // Create a unique rows step
    //
    String uniqueRowsStepname = "unique rows step";
    UniqueRowsMeta urm = new UniqueRowsMeta();
    urm.setCompareFields( new String[] { "KEY" } );
    urm.setCaseInsensitive( new boolean[] { false } );

    String uniqueRowsStepPid = registry.getPluginId( StepPluginType.class, urm );
    StepMeta uniqueRowsStep = new StepMeta( uniqueRowsStepPid, uniqueRowsStepname, urm );
    transMeta.addStep( uniqueRowsStep );

    transMeta.addTransHop( new TransHopMeta( injectorStep, uniqueRowsStep ) );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    transMeta.addTransHop( new TransHopMeta( uniqueRowsStep, dummyStep ) );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
    checkRows( createResultDataCaseSensitiveNoPreviousSort(), resultRows );
  }

  public void testCaseInsensitiveNoPreviousSort() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "uniquerowstest" );

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
    // Create a unique rows step
    //
    String uniqueRowsStepname = "unique rows step";
    UniqueRowsMeta urm = new UniqueRowsMeta();
    urm.setCompareFields( new String[] { "KEY" } );
    urm.setCaseInsensitive( new boolean[] { true } );

    String uniqueRowsStepPid = registry.getPluginId( StepPluginType.class, urm );
    StepMeta uniqueRowsStep = new StepMeta( uniqueRowsStepPid, uniqueRowsStepname, urm );
    transMeta.addStep( uniqueRowsStep );

    transMeta.addTransHop( new TransHopMeta( injectorStep, uniqueRowsStep ) );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    transMeta.addTransHop( new TransHopMeta( uniqueRowsStep, dummyStep ) );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
    checkRows( createResultDataCaseInsensitiveNoPreviousSort(), resultRows );
  }

  public void testSortCaseSensitiveUniqueCaseSensitive() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "uniquerowstest" );

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
    // Create a sort rows step
    //
    String sortRowsStepname = "sort rows step";
    SortRowsMeta srm = new SortRowsMeta();
    srm.setFieldName( new String[] { "KEY" } );
    srm.setAscending( new boolean[] { true } );
    srm.setCaseSensitive( new boolean[] { true } );
    srm.setPreSortedField( new boolean[] { false } );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    String sortRowsStepPid = registry.getPluginId( StepPluginType.class, srm );
    StepMeta sortRowsStep = new StepMeta( sortRowsStepPid, sortRowsStepname, srm );
    transMeta.addStep( sortRowsStep );

    transMeta.addTransHop( new TransHopMeta( injectorStep, sortRowsStep ) );

    //
    // Create a unique rows step
    //
    String uniqueRowsStepname = "unique rows step";
    UniqueRowsMeta urm = new UniqueRowsMeta();
    urm.setCompareFields( new String[] { "KEY" } );
    urm.setCaseInsensitive( new boolean[] { false } );

    String uniqueRowsStepPid = registry.getPluginId( StepPluginType.class, urm );
    StepMeta uniqueRowsStep = new StepMeta( uniqueRowsStepPid, uniqueRowsStepname, urm );
    transMeta.addStep( uniqueRowsStep );

    transMeta.addTransHop( new TransHopMeta( sortRowsStep, uniqueRowsStep ) );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    transMeta.addTransHop( new TransHopMeta( uniqueRowsStep, dummyStep ) );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
    checkRows( createResultDataSortCaseSensitiveUniqueCaseSensitive(), resultRows );
  }

  public void testSortCaseSensitiveUniqueCaseInsensitive() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "uniquerowstest" );

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
    // Create a sort rows step
    //
    String sortRowsStepname = "sort rows step";
    SortRowsMeta srm = new SortRowsMeta();
    srm.setFieldName( new String[] { "KEY" } );
    srm.setAscending( new boolean[] { true } );
    srm.setCaseSensitive( new boolean[] { true } );
    srm.setPreSortedField( new boolean[] { false } );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    String sortRowsStepPid = registry.getPluginId( StepPluginType.class, srm );
    StepMeta sortRowsStep = new StepMeta( sortRowsStepPid, sortRowsStepname, srm );
    transMeta.addStep( sortRowsStep );

    transMeta.addTransHop( new TransHopMeta( injectorStep, sortRowsStep ) );

    //
    // Create a unique rows step
    //
    String uniqueRowsStepname = "unique rows step";
    UniqueRowsMeta urm = new UniqueRowsMeta();
    urm.setCompareFields( new String[] { "KEY" } );
    urm.setCaseInsensitive( new boolean[] { true } );

    String uniqueRowsStepPid = registry.getPluginId( StepPluginType.class, urm );
    StepMeta uniqueRowsStep = new StepMeta( uniqueRowsStepPid, uniqueRowsStepname, urm );
    transMeta.addStep( uniqueRowsStep );

    transMeta.addTransHop( new TransHopMeta( sortRowsStep, uniqueRowsStep ) );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    transMeta.addTransHop( new TransHopMeta( uniqueRowsStep, dummyStep ) );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
    checkRows( createResultDataSortCaseSensitiveUniqueCaseInsensitive(), resultRows );
  }

  public void testSortCaseInsensitiveUniqueCaseSensitive() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "uniquerowstest" );

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
    // Create a sort rows step
    //
    String sortRowsStepname = "sort rows step";
    SortRowsMeta srm = new SortRowsMeta();
    srm.setFieldName( new String[] { "KEY" } );
    srm.setAscending( new boolean[] { true } );
    srm.setCaseSensitive( new boolean[] { false } );
    srm.setPreSortedField( new boolean[] { false } );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    String sortRowsStepPid = registry.getPluginId( StepPluginType.class, srm );
    StepMeta sortRowsStep = new StepMeta( sortRowsStepPid, sortRowsStepname, srm );
    transMeta.addStep( sortRowsStep );

    transMeta.addTransHop( new TransHopMeta( injectorStep, sortRowsStep ) );

    //
    // Create a unique rows step
    //
    String uniqueRowsStepname = "unique rows step";
    UniqueRowsMeta urm = new UniqueRowsMeta();
    urm.setCompareFields( new String[] { "KEY" } );
    urm.setCaseInsensitive( new boolean[] { false } );

    String uniqueRowsStepPid = registry.getPluginId( StepPluginType.class, urm );
    StepMeta uniqueRowsStep = new StepMeta( uniqueRowsStepPid, uniqueRowsStepname, urm );
    transMeta.addStep( uniqueRowsStep );

    transMeta.addTransHop( new TransHopMeta( sortRowsStep, uniqueRowsStep ) );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    transMeta.addTransHop( new TransHopMeta( uniqueRowsStep, dummyStep ) );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
    checkRows( createResultDataSortCaseInsensitiveUniqueCaseSensitive(), resultRows );
  }

  public void testSortCaseInsensitiveUniqueCaseInsensitive() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "uniquerowstest" );

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
    // Create a sort rows step
    //
    String sortRowsStepname = "sort rows step";
    SortRowsMeta srm = new SortRowsMeta();
    srm.setFieldName( new String[] { "KEY" } );
    srm.setAscending( new boolean[] { true } );
    srm.setCaseSensitive( new boolean[] { false } );
    srm.setPreSortedField( new boolean[] { false } );
    srm.setPrefix( "SortRowsTest" );
    srm.setDirectory( "." );

    String sortRowsStepPid = registry.getPluginId( StepPluginType.class, srm );
    StepMeta sortRowsStep = new StepMeta( sortRowsStepPid, sortRowsStepname, srm );
    transMeta.addStep( sortRowsStep );

    transMeta.addTransHop( new TransHopMeta( injectorStep, sortRowsStep ) );

    //
    // Create a unique rows step
    //
    String uniqueRowsStepname = "unique rows step";
    UniqueRowsMeta urm = new UniqueRowsMeta();
    urm.setCompareFields( new String[] { "KEY" } );
    urm.setCaseInsensitive( new boolean[] { true } );

    String uniqueRowsStepPid = registry.getPluginId( StepPluginType.class, urm );
    StepMeta uniqueRowsStep = new StepMeta( uniqueRowsStepPid, uniqueRowsStepname, urm );
    transMeta.addStep( uniqueRowsStep );

    transMeta.addTransHop( new TransHopMeta( sortRowsStep, uniqueRowsStep ) );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    transMeta.addTransHop( new TransHopMeta( uniqueRowsStep, dummyStep ) );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
    checkRows( createResultDataSortCaseInsensitiveUniqueCaseInsensitive(), resultRows );
  }

  @Test
  public void testAllUnique() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "uniquerowstest" );

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
    // Create a unique rows step
    //
    String uniqueRowsStepname = "unique rows step";
    UniqueRowsMeta urm = new UniqueRowsMeta();
    urm.setCompareFields( new String[] { "KEY" } );
    urm.setCaseInsensitive( new boolean[] { true } );
    urm.setRejectDuplicateRow( true );

    String uniqueRowsStepPid = registry.getPluginId( StepPluginType.class, urm );
    StepMeta uniqueRowsStep = new StepMeta( uniqueRowsStepPid, uniqueRowsStepname, urm );
    uniqueRowsStep.setDistributes( false );
    transMeta.addStep( uniqueRowsStep );

    transMeta.addTransHop( new TransHopMeta( injectorStep, uniqueRowsStep ) );

    //
    // Create a dummy step to receive the unique rows
    //
    String dummyStepname1 = "dummy step";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    transMeta.addTransHop( new TransHopMeta( uniqueRowsStep, dummyStep1 ) );

    //
    // Create a dummy step to receive the duplicate rows (errors)
    //
    String dummyStepname2 = "dummy step2";
    DummyTransMeta dm2 = new DummyTransMeta();

    String dummyPid2 = registry.getPluginId( StepPluginType.class, dm2 );
    StepMeta dummyStep2 = new StepMeta( dummyPid2, dummyStepname2, dm2 );
    transMeta.addStep( dummyStep2 );

    // Set up error (aka duplicates) handling info
    StepErrorMeta stepErrorMeta = new StepErrorMeta( new Variables(), uniqueRowsStep );
    stepErrorMeta.setTargetStep( dummyStep2 );
    stepErrorMeta.setEnabled( true );
    stepErrorMeta.setNrErrorsValuename( "numErrors" );
    stepErrorMeta.setErrorDescriptionsValuename( "duplicates" );
    stepErrorMeta.setErrorFieldsValuename( "KEY" );
    stepErrorMeta.setErrorCodesValuename( "errorCodes" );
    stepErrorMeta.setMaxErrors( "9999" );
    stepErrorMeta.setMaxPercentErrors( "" );
    stepErrorMeta.setMinPercentRows( "" );
    uniqueRowsStep.setStepErrorMeta( stepErrorMeta );
    transMeta.addTransHop( new TransHopMeta( uniqueRowsStep, dummyStep2 ) );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector dummyRc1 = new RowStepCollector();
    si.addRowListener( dummyRc1 );

    StepInterface si2 = trans.getStepInterface( dummyStepname2, 0 );
    RowStepCollector dummyRc2 = new RowStepCollector();
    si2.addRowListener( dummyRc2 );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createDataAllUnique();
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> resultRows = dummyRc1.getRowsWritten();
    checkRows( createResultDataAllUnique(), resultRows );

    List<RowMetaAndData> errorRows = dummyRc2.getRowsWritten();
    assertEquals( errorRows.size(), 0 ); // There should be no duplicates for this test
  }
}
