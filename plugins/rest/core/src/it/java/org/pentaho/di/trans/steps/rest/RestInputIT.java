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


package org.pentaho.di.trans.steps.rest;

import org.glassfish.jersey.servlet.ServletContainer;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

import org.glassfish.jersey.servlet.ServletContainer;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Regression test case for PDI-13072
 *
 * @author vladimir.dolzhenko@gmail.com
 */
public class RestInputIT {

  private static ServletTester tester;

  @BeforeClass
  public static void setUp() throws Exception {
    KettleEnvironment.init();

    tester = new ServletTester();
    tester.setContextPath( "/context" );
    tester.setResourceBase( RestInputIT.class.getResource( "/" ).toExternalForm() );
    final ServletHolder servletHolder = tester.addServlet( ServletContainer.class, "/*" );
    servletHolder.setInitParameter( "javax.ws.rs.Application", "org.glassfish.jersey.server.ResourceConfig" );
    servletHolder.setInitParameter( "jersey.config.server.provider.classnames", SimpleRestService.class.getName() );
    tester.start();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    if ( tester != null ) {
      tester.stop();
    }
  }

  @Test
  public void testRESTInput() throws Exception {
    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "restinput" );

    PluginRegistry registry = PluginRegistry.getInstance();

    StepMeta inputStep = createRestInputStep( transMeta, registry );

    RowStepCollector rowStepCollector = new RowStepCollector();
    final Trans trans = createAndTestTrans( registry, transMeta, inputStep, rowStepCollector, "limit", 5 );
    trans.startThreads();

    trans.waitUntilFinished();

    // Compare the results
    List<RowMetaAndData> resultRows = rowStepCollector.getRowsWritten();

    assertTrue( rowStepCollector.getRowsError().isEmpty() );
    assertEquals( 1, rowStepCollector.getRowsWritten().size() );

    final RowMetaAndData rowMetaAndData = resultRows.get( 0 );
    final RowMetaInterface rowMeta = rowMetaAndData.getRowMeta();

    final String[] fieldNames = rowMeta.getFieldNames();
    final Object[] data = rowMetaAndData.getData();

    assertEquals( "pageSize", fieldNames[0] );
    assertEquals( "name", fieldNames[1] );
    assertEquals( "result", fieldNames[2] );

    assertEquals( 5, data[0] );
    assertEquals( "limit", data[1] );
  }

  protected Trans createAndTestTrans( PluginRegistry registry, TransMeta transMeta, StepMeta inputStep,
                                      RowStepCollector rowStepCollector, String name, int limit ) throws KettleException {
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
    rowMeta.addValueMeta( new ValueMetaString( "pageSize" ) );
    rowMeta.addValueMeta( new ValueMetaString( "name" ) );
    rp.putRow( rowMeta, new Object[] { limit, name } );

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
    meta.setUrl( tester.createConnector( true ) + "/context/simple/join" );
    meta.setMethod( "POST" );
    meta.setMatrixParameterField( new String[] { "pageSize" } );
    meta.setMatrixParameterName( new String[] { "limit" } );

    meta.setParameterField( new String[] { "name" } );
    meta.setParameterName( new String[] { "name" } );

    meta.setApplicationType( RestMeta.APPLICATION_TYPE_TEXT_PLAIN );
    meta.setFieldName( "result" );

    return inputStep;
  }
}
