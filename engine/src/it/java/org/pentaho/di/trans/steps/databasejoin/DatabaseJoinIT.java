/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.databasejoin;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DatabaseJoinIT {
  @Test
  public void databaseJoinPossibleDeadlockTest() throws KettleException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    KettleEnvironment.init();
    String path = getClass().getResource( "databasejoinmultithread.ktr" ).getPath();
    Variables variables = new Variables();
    TransMeta transMeta = new TransMeta( path, variables );
    Trans trans = new Trans( transMeta );
    executor.submit( new Runnable() {
      @Override public void run() {
        try {
          trans.prepareExecution( null );
          trans.startThreads();
          trans.waitUntilFinished();
        } catch ( KettleException e ) {
          //should not occur
          fail( "Something went wrong" );
        }
      }
    } );
    try {
      executor.shutdown();
      if ( !executor.awaitTermination( 30, TimeUnit.SECONDS ) ) {
        fail( "Deadlock detected" );
      }
      // Transformation reach the end. No Deadlock!
      // (It ends with errors because there is a step that fails on purpose)
      assertEquals( trans.getStatus(), "Finished (with errors)" );
    } catch ( InterruptedException e ) {
      //should not occur
      fail( "Something went wrong" );
    } finally {
      //Clean db files created by running the transformation
      File db = new File( "TESTING.h2.db" );
      File dbTrace = new File( "TESTING.trace.db" );
      db.delete();
      dbTrace.delete();
    }
  }
}
