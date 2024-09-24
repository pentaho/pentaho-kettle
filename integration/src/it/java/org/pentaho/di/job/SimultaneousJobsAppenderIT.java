/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.job;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingBuffer;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.parameters.UnknownParamException;

public class SimultaneousJobsAppenderIT {

  private int howMany = 3; // number of simultaneously running jobs
  private int prevJobBuffer = 0;
  private static String jobPath = "one hundred lines.kjb";
  private static String PKG = "org/pentaho/di/job/";

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
    KettleLogStore.init();
  }

  @Test
  public void testAppendersBuffer() throws KettleXMLException, IOException, URISyntaxException, UnknownParamException {
    Job[] jobs = new Job[howMany];
    for ( int i = 0; i < jobs.length; i++ ) {
      JobMeta jm = new JobMeta( new File( SimultaneousJobsAppenderIT.class.getClassLoader().getResource( PKG + jobPath ).toURI() ).getCanonicalPath(), null );
      jm.setName( "Job number " + i );
      Job job = new Job( null, jm );
      // adjust the log level
      job.setLogLevel( LogLevel.BASIC );
      jobs[i] = job;
    }

    for (Job job : jobs) {
      job.start();
    }

    for (Job job : jobs) {
      job.waitUntilFinished();
    }

    LoggingBuffer appender = KettleLogStore.getAppender();

    for (int i = 0; i < jobs.length; i++) {
      if ( prevJobBuffer != 0 ) {
        Assert.assertEquals( "Uncorrect buffer size, job: " + i, prevJobBuffer, appender.getBuffer( jobs[i].getLogChannelId(), false ).length() );
      }
      prevJobBuffer = appender.getBuffer( jobs[i].getLogChannelId(), false ).length();
    }
  }
}