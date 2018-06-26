/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.adapters;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dataflow Engine Adapter
 * <p>
 * Current TransGraph class calls into Trans to run a transformation.  This class overrides some of that classic
 * functionality so we can run the transformation on Dataflow instead.  It is the control layer between the Spoon
 * and the Dataflow Engine.
 */
public class DataflowEngineAdapter extends Trans {

  private static final Logger LOG = LoggerFactory.getLogger( DataflowEngineAdapter.class );

  private CountDownLatch transFinishedSignal = new CountDownLatch( 1 );

  public DataflowEngineAdapter( TransMeta transMeta ) {
    this.transMeta = transMeta;
  }

  @Override
  public void prepareExecution( String[] arguments ) throws KettleException {
    LOG.trace( "prepareExectuion(arguments: {})", arguments );
    super.prepareExecution( arguments );
  }

  @Override
  public void startThreads() throws KettleException {
    LOG.trace( "startThreads()" );
    try {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit( () -> {

        String runner = transMeta.getVariable( "engine.runner" );
        File applicaitonJar = new File( transMeta.getVariable( "engine.application.jar" ) );
        DataflowRunner dataflowRunner = new DataflowRunner( log, runner, applicaitonJar );
        dataflowRunner.run( transMeta.getFilename() );

        finishProcess( true );
      } );
    } catch ( Exception e ) {
      throw new RuntimeException( "Unexpected error connecting to daemon.", e );
    }
  }

  @Override
  public void waitUntilFinished() {
    LOG.trace( "waitUntilFinished()" );
    super.waitUntilFinished();
  }

  @Override
  public void stopAll() {
    LOG.trace( "stopAll()" );
    super.stopAll();
  }

  public static void main( String[] args ) {
    try {
      LOG.info( "------------------------- START -------------------------" );
      LOG.info( "------------------------- STOP  -------------------------" );
    } catch ( Exception e ) {
      LOG.error( "------------------------- ERROR -------------------------", e );
    }
  }

  private void finishProcess( boolean emitToAllSteps ) {
    setFinished( true );
    if ( emitToAllSteps ) {
      // emit error on all steps
      getSteps().stream().map( stepMetaDataCombi -> stepMetaDataCombi.step ).forEach( step -> {
        step.setStopped( true );
        step.setRunning( false );
      } );
    }
    getTransListeners().forEach( l -> {
      try {
        l.transFinished( DataflowEngineAdapter.this );
      } catch ( KettleException e1 ) {
        getLogChannel().logError( "Error notifying trans listener", e1 );
      }
    } );
  }

}
