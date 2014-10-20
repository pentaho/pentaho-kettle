/*! ****************************************************************************
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

package org.pentaho.di.trans.steps.rest;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.testing.ServletTester;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.*;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Regression test case for PDI-13072
 *
 * @author vladimir.dolzhenko@gmail.com
 */
public class RestInputTest {

  private static ServletTester tester;

  @BeforeClass
  public static void init() throws Exception {
    KettleEnvironment.init();

    tester = new ServletTester();
    tester.setContextPath("/context");
    tester.setResourceBase( RestInputTest.class.getResource( "/" ).getFile() );
    final ServletHolder servletHolder = tester.addServlet( ServletContainer.class, "/*" );
    servletHolder.setInitParameter( "com.sun.jersey.config.property.classpath", "/" );
    servletHolder.setInitParameter( "jersey.config.server.provider.classnames", SimpleRestService.class.getName() );
    tester.start();
  }

  @AfterClass
  public static void destroy() throws Exception {
    tester.stop();
  }

  @Before
  public void setUp(){
    SimpleRestService.counter = 0;
  }


  protected Trans createAndTestTrans( PluginRegistry registry, TransMeta transMeta,
                                      StepMeta inputStep, RowStepCollector rowStepCollector,
                                      String name, int limit ) throws Exception {
    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi3 = new TransHopMeta( inputStep, dummyStep1 );
    transMeta.addTransHop( hi3 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    si.addRowListener( rowStepCollector );

    RowProducer rp = trans.addRowProducer( inputStep.getName(), 0 );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMeta( "url", ValueMeta.TYPE_STRING ) );
    rowMeta.addValueMeta( new ValueMeta( "pageSize", ValueMeta.TYPE_STRING ) );
    rowMeta.addValueMeta( new ValueMeta( "name", ValueMeta.TYPE_STRING ) );
    rp.putRow( rowMeta, new Object[] { tester.createSocketConnector( true ) + "/context/simple/join" , Integer.valueOf( limit ), name } );

    rp.finished();
    return trans;
  }

  public StepMeta createRestInputStep( TransMeta transMeta, PluginRegistry registry ) throws Exception {
    String inputName = "rest input step";
    RestMeta meta = new RestMeta();

    String inputPid = registry.getPluginId( StepPluginType.class, meta );
    StepMeta inputStep = new StepMeta( inputPid, inputName, meta );
    transMeta.addStep( inputStep );

    meta.setDefault();
    meta.setUrlField( "url" );
    meta.setUrlInField( true );
    meta.setMatrixParameterField( new String[] { "pageSize" } );
    meta.setMatrixParameterName( new String[] { "limit" } );

    meta.setParameterField( new String[] { "name" } );
    meta.setParameterName( new String[] { "name" } );

    meta.setApplicationType( RestMeta.APPLICATION_TYPE_TEXT_PLAIN );
    meta.setFieldName( "result" );

    return inputStep;
  }

  @Test
  public void testRESTInputMatrixParameters() throws Exception {
    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "restinput" );

    PluginRegistry registry = PluginRegistry.getInstance();

    StepMeta inputStep = createRestInputStep( transMeta, registry );

    RowStepCollector rowStepCollector = new RowStepCollector();

    Trans trans = createAndTestTrans( registry, transMeta, inputStep, rowStepCollector, "limit", 5 );

    assertEquals( "no any interaction so far", 0, SimpleRestService.counter );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( "the only interaction is made", 1, SimpleRestService.counter );

    // Compare the results
    List<RowMetaAndData> resultRows = rowStepCollector.getRowsWritten();

    assertTrue( rowStepCollector.getRowsError().isEmpty() );
    assertEquals(1, rowStepCollector.getRowsWritten().size());

    RowMetaAndData rowMetaAndData = resultRows.get( 0 );
    RowMetaInterface rowMeta = rowMetaAndData.getRowMeta();

    String[] fieldNames = rowMeta.getFieldNames();
    Object[] data = rowMetaAndData.getData();

    assertEquals( "url", fieldNames[0] );
    assertEquals( "pageSize", fieldNames[1] );
    assertEquals( "name", fieldNames[2] );
    assertEquals( "result", fieldNames[3] );

    assertEquals( Integer.valueOf( 5 ), data[1] );
    assertEquals( "limit", data[2] );
    assertEquals( "limit:5", data[3] );
  }

  @Test
  public void testRESTInputWORowInjection() throws Exception {
    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "restinput" );

    PluginRegistry registry = PluginRegistry.getInstance();

    String inputName = "rest input step";
    RestMeta restMeta = new RestMeta();

    String inputPid = registry.getPluginId( StepPluginType.class, restMeta );
    StepMeta inputStep = new StepMeta( inputPid, inputName, restMeta );
    transMeta.addStep( inputStep );

    restMeta.setDefault();
    restMeta.setUrl( tester.createSocketConnector( true ) + "/context/simple/join" );
    restMeta.setApplicationType( RestMeta.APPLICATION_TYPE_TEXT_PLAIN );
    restMeta.setFieldName( "result" );

    RowStepCollector rowStepCollector = new RowStepCollector();
    //final Trans trans = createAndTestTrans( registry, transMeta, inputStep, rowStepCollector, "limit", 5 );
    //
    // Create a dummy step
    //
    String dummyStepname = "dummy step";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi3 = new TransHopMeta( inputStep, dummyStep1 );
    transMeta.addTransHop( hi3 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname, 0 );
    si.addRowListener( rowStepCollector );

    assertEquals( "no any interaction so far", 0, SimpleRestService.counter );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( "the only interaction is made", 1, SimpleRestService.counter );

    // Compare the results
    List<RowMetaAndData> resultRows = rowStepCollector.getRowsWritten();

    assertTrue( rowStepCollector.getRowsError().isEmpty() );
    assertEquals(1, rowStepCollector.getRowsWritten().size());

    RowMetaAndData rowMetaAndData = resultRows.get( 0 );
    RowMetaInterface rowMeta = rowMetaAndData.getRowMeta();

    String[] fieldNames = rowMeta.getFieldNames();
    Object[] data = rowMetaAndData.getData();

    assertEquals( "result", fieldNames[0] );

    assertEquals( "null:null", data[0] );
  }

}
