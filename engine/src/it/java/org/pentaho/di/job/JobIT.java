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

package org.pentaho.di.job;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;

public class JobIT {

  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
  }

  @After
  public void tearDown() throws KettleException {
    KettleEnvironment.shutdown();
  }

  @Test
  public void testJobStatusContainsOnlyOneFinished() throws Exception {
    AtomicBoolean jobStarted = new AtomicBoolean( true );
    int countOfCheckers = 100;
    HashMap<Integer, Future<StringBuilder>> checkers = new HashMap<>();

    String testKJBFullPath = this.getClass().getResource( "testJob.kjb" ).getFile();
    JobMeta jm = new JobMeta( testKJBFullPath, null );
    final Job job = new Job( null, jm );

    ExecutorService execServ = Executors.newFixedThreadPool( countOfCheckers );
    for ( int i = 0; i < countOfCheckers; i++ ) {
      Future<StringBuilder> future = execServ.submit( new Callable<StringBuilder>() {

        private StringBuilder statusCollector = new StringBuilder();

        @Override
        public StringBuilder call() throws Exception {
          while ( jobStarted.get() ) {
            statusCollector.append( job.getStatus() );
            //add the sleep to reduce the count of logged status and prevent OOM
            Thread.sleep( 10 );
          }
          return statusCollector;
        }
      } );
      checkers.put( i, future );
    }
    job.start();
    job.waitUntilFinished();
    jobStarted.set( false );

    for ( Future<StringBuilder> checker : checkers.values() ) {
      assertTrue( checkOrderStatus( checker.get().toString() ) );
    }
    execServ.shutdown();
  }

  public boolean checkOrderStatus( String status ) {
    String[] tokens = { "Waiting", "Running", "Finished" };
    int offset = 0;
    for ( String t : tokens ) {
      while ( status.startsWith( t, offset ) )
        offset += t.length();
    }
    return offset == status.length();
  }
}
