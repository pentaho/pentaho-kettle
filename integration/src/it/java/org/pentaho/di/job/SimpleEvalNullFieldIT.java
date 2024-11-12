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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;

public class SimpleEvalNullFieldIT {
  private static String jobPath = "PDI-13387.kjb";
  private static String PKG = "org/pentaho/di/job/";

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();   
  }

  @Test
  public void testNullField() throws KettleXMLException, IOException, URISyntaxException {
    JobMeta jm = new JobMeta( new File( SimultaneousJobsAppenderIT.class.getClassLoader().getResource( PKG + jobPath ).toURI() ).getCanonicalPath(), null );
    Job job = new Job( null, jm );    
    job.start();
    job.waitUntilFinished();
    Result result = job.getResult();

    Assert.assertTrue( result.getResult() );
    if ( result.getNrErrors() != 0 ) {
      Assert.fail( result.getLogText() );
    }
  }
}