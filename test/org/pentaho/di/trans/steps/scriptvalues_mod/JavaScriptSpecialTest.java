/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.scriptvalues_mod;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

import junit.framework.TestCase;

/**
 * Test class for the Modified Javascript step for the special functions. Things tested: LuhnCheck().
 *
 * @author Sven Boden
 */
public class JavaScriptSpecialTest extends TestCase {
  public RowMetaInterface createRowMetaInterface1() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaString( "string" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[ i ] );
    }

    return rm;
  }

  public List<RowMetaAndData> createData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface1();

    Object[] r1 = new Object[] { "446-667-651" };
    Object[] r2 = new Object[] { "446667651" };
    Object[] r3 = new Object[] { "4444333322221111" };
    Object[] r4 = new Object[] { "4444 3333 2222 1111" };
    Object[] r5 = new Object[] { "444433332aa2221111" };
    Object[] r6 = new Object[] { "4444333322221111aa" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );
    list.add( new RowMetaAndData( rm, r5 ) );
    list.add( new RowMetaAndData( rm, r6 ) );

    return list;
  }

  /**
   * Create the meta data for the results (ltrim/rtrim/trim).
   */
  public RowMetaInterface createRowMetaInterfaceResult1() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
      { new ValueMetaString( "string" ), new ValueMetaBoolean( "bool" ) };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[ i ] );
    }

    return rm;
  }

  /**
   * Create result data for test case 1.
   */
  public List<RowMetaAndData> createResultData1() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterfaceResult1();

    Object[] r1 = new Object[] { "446-667-651", Boolean.FALSE };
    Object[] r2 = new Object[] { "446667651", Boolean.TRUE };
    Object[] r3 = new Object[] { "4444333322221111", Boolean.TRUE };
    Object[] r4 = new Object[] { "4444 3333 2222 1111", Boolean.FALSE };
    Object[] r5 = new Object[] { "444433332aa2221111", Boolean.FALSE };
    Object[] r6 = new Object[] { "4444333322221111aa", Boolean.FALSE };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );
    list.add( new RowMetaAndData( rm, r5 ) );
    list.add( new RowMetaAndData( rm, r6 ) );

    return list;
  }

  public List<RowMetaAndData> createResultData2() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface1();

    Object[] r1 = new Object[] { "446-667-651" };
    Object[] r2 = new Object[] { "446667651" };
    Object[] r3 = new Object[] { "4444333322221111" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );

    return list;
  }

  public RowMetaInterface createRowMetaInterface3() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
      {
        new ValueMetaInteger( "int_in" ),
        new ValueMetaNumber( "number_in" ),
        new ValueMetaString( "string_in" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[ i ] );
    }

    return rm;
  }

  public List<RowMetaAndData> createData3() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface3();

    Object[] r1 = new Object[] { new Long( 1 ), new Double( 1.0D ), "1" };
    Object[] r2 = new Object[] { new Long( 2 ), new Double( 2.0D ), "2" };
    Object[] r3 = new Object[] { new Long( 3 ), new Double( 3.0D ), "3" };
    Object[] r4 = new Object[] { new Long( 4 ), new Double( 4.0D ), "4" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );

    return list;
  }

  public RowMetaInterface createRowMetaInterface4() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
      {
        new ValueMetaInteger( "int_in" ),
        new ValueMetaNumber( "number_in" ),
        new ValueMetaString( "string_in" ), new ValueMetaInteger( "long1" ),
        new ValueMetaNumber( "number1" ), new ValueMetaString( "string1" ),
        new ValueMetaInteger( "long2" ), new ValueMetaNumber( "number2" ),
        new ValueMetaString( "string2" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[ i ] );
    }

    return rm;
  }

  public List<RowMetaAndData> createResultData3() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    RowMetaInterface rm = createRowMetaInterface4();

    Object[] r1 =
      new Object[] {
        new Long( 1 ), new Double( 1.0D ), "1", new Long( 2 ), new Double( 2.0D ), "2", new Long( 2 ),
        new Double( 2.0D ), "2" };
    Object[] r2 =
      new Object[] {
        new Long( 2 ), new Double( 2.0D ), "2", new Long( 3 ), new Double( 3.0D ), "3", new Long( 3 ),
        new Double( 3.0D ), "3" };
    Object[] r3 =
      new Object[] {
        new Long( 3 ), new Double( 3.0D ), "3", new Long( 4 ), new Double( 4.0D ), "4", new Long( 4 ),
        new Double( 4.0D ), "4" };
    Object[] r4 =
      new Object[] {
        new Long( 4 ), new Double( 4.0D ), "4", new Long( 5 ), new Double( 5.0D ), "5", new Long( 5 ),
        new Double( 5.0D ), "5" };

    list.add( new RowMetaAndData( rm, r1 ) );
    list.add( new RowMetaAndData( rm, r2 ) );
    list.add( new RowMetaAndData( rm, r3 ) );
    list.add( new RowMetaAndData( rm, r4 ) );

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
      int[] fields = new int[ rm1.size() ];
      for ( int ydx = 0; ydx < rm1.size(); ydx++ ) {
        fields[ ydx ] = ydx;
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

  /**
   * Test case for javascript functionality: ltrim(), rtrim(), trim().
   */
  public void testLuhnCheck() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "test javascript LuhnCheck" );

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
    // Create a javascript step
    //
    String javaScriptStepname = "javascript step";
    ScriptValuesMetaMod svm = new ScriptValuesMetaMod();

    ScriptValuesScript[] js =
      new ScriptValuesScript[] { new ScriptValuesScript(
        ScriptValuesScript.TRANSFORM_SCRIPT, "script", "var str = string;\n" + "var bool = LuhnCheck(str);" ) };
    svm.setJSScripts( js );
    svm.setFieldname( new String[] { "bool" } );
    svm.setRename( new String[] { "" } );
    svm.setType( new int[] { ValueMetaInterface.TYPE_BOOLEAN } );
    svm.setLength( new int[] { -1 } );
    svm.setPrecision( new int[] { -1 } );
    svm.setReplace( new boolean[] { false } );
    svm.setCompatible( false );

    String javaScriptStepPid = registry.getPluginId( StepPluginType.class, svm );
    StepMeta javaScriptStep = new StepMeta( javaScriptStepPid, javaScriptStepname, svm );
    transMeta.addStep( javaScriptStep );

    TransHopMeta hi1 = new TransHopMeta( injectorStep, javaScriptStep );
    transMeta.addTransHop( hi1 );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    TransHopMeta hi2 = new TransHopMeta( javaScriptStep, dummyStep );
    transMeta.addTransHop( hi2 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si;

    si = trans.getStepInterface( javaScriptStepname, 0 );
    RowStepCollector javaScriptRc = new RowStepCollector();
    si.addRowListener( javaScriptRc );

    si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

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

    List<RowMetaAndData> goldenImageRows = createResultData1();
    List<RowMetaAndData> resultRows1 = javaScriptRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );

    List<RowMetaAndData> resultRows2 = dummyRc.getRowsRead();
    checkRows( resultRows2, goldenImageRows );
  }

  /**
   * Test case for javascript functionality: trans_Status and SKIP_TRANSFORMATION. Regression test case for JIRA defect
   * PDI-364.
   */
  public void testTransStatus() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "test javascript trans_Status" );

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
    // Create a javascript step
    //
    String javaScriptStepname = "javascript step";
    ScriptValuesMetaMod svm = new ScriptValuesMetaMod();

    // process 3 rows and skip the rest.
    ScriptValuesScript[] js =
      new ScriptValuesScript[] { new ScriptValuesScript(
        ScriptValuesScript.TRANSFORM_SCRIPT, "script", "trans_Status = CONTINUE_TRANSFORMATION;\n"
        + "if (getProcessCount(\"r\") > 3) {\n" + " \ttrans_Status = SKIP_TRANSFORMATION;\n" + "}" ) };
    svm.setJSScripts( js );
    svm.setFieldname( new String[] {} );
    svm.setRename( new String[] {} );
    svm.setType( new int[] {} );
    svm.setLength( new int[] {} );
    svm.setPrecision( new int[] {} );
    svm.setCompatible( false );

    String javaScriptStepPid = registry.getPluginId( StepPluginType.class, svm );
    StepMeta javaScriptStep = new StepMeta( javaScriptStepPid, javaScriptStepname, svm );
    transMeta.addStep( javaScriptStep );

    TransHopMeta hi1 = new TransHopMeta( injectorStep, javaScriptStep );
    transMeta.addTransHop( hi1 );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    TransHopMeta hi2 = new TransHopMeta( javaScriptStep, dummyStep );
    transMeta.addTransHop( hi2 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si;

    si = trans.getStepInterface( javaScriptStepname, 0 );
    RowStepCollector javaScriptRc = new RowStepCollector();
    si.addRowListener( javaScriptRc );

    si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

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

    List<RowMetaAndData> goldenImageRows = createResultData2();
    List<RowMetaAndData> resultRows1 = javaScriptRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );

    List<RowMetaAndData> resultRows2 = dummyRc.getRowsRead();
    checkRows( resultRows2, goldenImageRows );
  }

  /**
   * Test case for JavaScript/Java/JavaScript interfacing.
   */
  public void disabledTestJavaInterface() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "test javascript interface" );

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
    // Create a javascript step
    //
    String javaScriptStepname = "javascript step";
    ScriptValuesMetaMod svm = new ScriptValuesMetaMod();

    // process 3 rows and skip the rest.
    ScriptValuesScript[] js =
      new ScriptValuesScript[] { new ScriptValuesScript(
        ScriptValuesScript.TRANSFORM_SCRIPT,
        "script1",
        "java;\n\n"
          + "var obj     = new Packages.org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptTest();\n"
          + "var long1   = obj.add1ToLong(getInputRowMeta().getInteger(row, 0));\n"
          + "var number1 = obj.add1ToNumber(getInputRowMeta().getNumber(row, 1));\n"
          + "var string1 = obj.add1ToString(getInputRowMeta().getString(row, 2));\n"
          + "var long2   = Packages.org.pentaho.di.trans.steps.scriptvalues_mod."
          + "JavaScriptTest.add1ToLongStatic(getInputRowMeta().getInteger(row, 0));\n"
          + "var number2 = Packages.org.pentaho.di.trans.steps.scriptvalues_mod."
          + "JavaScriptTest.add1ToNumberStatic(getInputRowMeta().getNumber(row, 1));\n"
          + "var string2 = Packages.org.pentaho.di.trans.steps.scriptvalues_mod."
          + "JavaScriptTest.add1ToStringStatic(getInputRowMeta().getString(row, 2));\n" ) };
    svm.setJSScripts( js );
    svm.setFieldname( new String[] { "long1", "number1", "string1", "long2", "number2", "string2" } );
    svm.setRename( new String[] { "long1", "number1", "string1", "long2", "number2", "string2" } );
    svm.setType( new int[] {
      ValueMetaInterface.TYPE_INTEGER, ValueMetaInterface.TYPE_NUMBER, ValueMetaInterface.TYPE_STRING,
      ValueMetaInterface.TYPE_INTEGER, ValueMetaInterface.TYPE_NUMBER, ValueMetaInterface.TYPE_STRING, } );
    svm.setLength( new int[] { -1, -1, -1, -1, -1, -1, -1 } );
    svm.setPrecision( new int[] { -1, -1, -1, -1, -1, -1, -1 } );
    svm.setReplace( new boolean[] { false, false, false, false, false, false, } );
    svm.setCompatible( false );

    String javaScriptStepPid = registry.getPluginId( StepPluginType.class, svm );
    StepMeta javaScriptStep = new StepMeta( javaScriptStepPid, javaScriptStepname, svm );
    transMeta.addStep( javaScriptStep );

    TransHopMeta hi1 = new TransHopMeta( injectorStep, javaScriptStep );
    transMeta.addTransHop( hi1 );

    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm = new DummyTransMeta();

    String dummyPid = registry.getPluginId( StepPluginType.class, dm );
    StepMeta dummyStep = new StepMeta( dummyPid, dummyStepname, dm );
    transMeta.addStep( dummyStep );

    TransHopMeta hi2 = new TransHopMeta( javaScriptStep, dummyStep );
    transMeta.addTransHop( hi2 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si;

    si = trans.getStepInterface( javaScriptStepname, 0 );
    RowStepCollector javaScriptRc = new RowStepCollector();
    si.addRowListener( javaScriptRc );

    si = trans.getStepInterface( dummyStepname, 0 );
    RowStepCollector dummyRc = new RowStepCollector();
    si.addRowListener( dummyRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData3();
    Iterator<RowMetaAndData> it = inputList.iterator();
    while ( it.hasNext() ) {
      RowMetaAndData rm = it.next();
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> goldenImageRows = createResultData3();
    List<RowMetaAndData> resultRows1 = javaScriptRc.getRowsWritten();
    checkRows( resultRows1, goldenImageRows );

    List<RowMetaAndData> resultRows2 = dummyRc.getRowsRead();
    checkRows( resultRows2, goldenImageRows );
  }

  public List<RowMetaAndData> createDateAddData() throws ParseException {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    //Create RowMeta
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaDate( "input" ) );

    //Populate Row
    DateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    list.add( new RowMetaAndData( rm, new Object[] { format.parse( "2014-01-01 00:00:00" ) } ) );

    return list;
  }

  public List<RowMetaAndData> createDateAddResultData() throws ParseException {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

    //Create RowMeta
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaDate( "input" ) );
    rm.addValueMeta( new ValueMetaDate( "new_weekday" ) );
    rm.addValueMeta( new ValueMetaDate( "new_year" ) );
    rm.addValueMeta( new ValueMetaDate( "new_month" ) );
    rm.addValueMeta( new ValueMetaDate( "new_week" ) );
    rm.addValueMeta( new ValueMetaDate( "new_day" ) );
    rm.addValueMeta( new ValueMetaDate( "new_hour" ) );
    rm.addValueMeta( new ValueMetaDate( "new_minute" ) );
    rm.addValueMeta( new ValueMetaDate( "new_second" ) );

    DateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    Object[] r1 = new Object[] {
      format.parse( "2014-01-01 00:00:00" ), //input
      format.parse( "2014-01-06 00:00:00" ), //weekday
      format.parse( "2019-01-01 00:00:00" ), //year
      format.parse( "2014-06-01 00:00:00" ), //month
      format.parse( "2014-02-05 00:00:00" ), //week
      format.parse( "2014-01-06 00:00:00" ), //day
      format.parse( "2014-01-01 05:00:00" ), //hour
      format.parse( "2014-01-01 00:05:00" ), //minute
      format.parse( "2014-01-01 00:00:05" ), //second
    };
    list.add( new RowMetaAndData( rm, r1 ) );

    return list;
  }

  public void testDateAdd() throws Exception {
    KettleEnvironment.init();

    //
    // Create a javascript step
    //
    String javaScriptStepName = "javascript step";
    ScriptValuesMetaMod svm = new ScriptValuesMetaMod();

    ScriptValuesScript[] js =
      new ScriptValuesScript[] { new ScriptValuesScript(
        ScriptValuesScript.TRANSFORM_SCRIPT, "script",
        "var new_weekday = dateAdd( input, 'wd', 3 );\n" // PDI-13486
          + "var new_year = dateAdd( input, 'y', 5 );\n"
          + "var new_month = dateAdd( input, 'm', 5 );\n"
          + "var new_week = dateAdd( input, 'w', 5 );\n"
          + "var new_day = dateAdd( input, 'd', 5 );\n"
          + "var new_hour = dateAdd( input, 'hh', 5 );\n"
          + "var new_minute = dateAdd( input, 'mi', 5 );\n"
          + "var new_second = dateAdd( input, 'ss', 5 );\n" )
      };
    svm.setJSScripts( js );
    svm.setFieldname( new String[] {
      "new_weekday", "new_year", "new_month", "new_week", "new_day", "new_hour", "new_minute", "new_second" } );
    svm.setType( new int[] {
      ValueMetaInterface.TYPE_DATE, ValueMetaInterface.TYPE_DATE, ValueMetaInterface.TYPE_DATE,
      ValueMetaInterface.TYPE_DATE, ValueMetaInterface.TYPE_DATE, ValueMetaInterface.TYPE_DATE,
      ValueMetaInterface.TYPE_DATE, ValueMetaInterface.TYPE_DATE } );
    svm.setRename( new String[] { null, null, null, null, null, null, null, null } );
    svm.setLength( new int[] { -1, -1, -1, -1, -1, -1, -1, -1 } );
    svm.setPrecision( new int[] { -1, -1, -1, -1, -1, -1, -1, -1 } );
    svm.setReplace( new boolean[] { false, false, false, false, false, false, false, false } );
    svm.setCompatible( false );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( new Variables(), svm, javaScriptStepName );
    List<RowMetaAndData> result =
      TransTestFactory.executeTestTransformation(
        transMeta, TransTestFactory.INJECTOR_STEPNAME, javaScriptStepName, TransTestFactory.DUMMY_STEPNAME,
        createDateAddData() );

    checkRows( result, createDateAddResultData() );
  }
}
