/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

public class SimpleEvalNullFieldTest {
  private static String jobPath = "PDI-13387.kjb";
  private static String PKG = "org/pentaho/di/job/";

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();   
  }

  @Test
  public void testNullField() throws KettleXMLException, IOException, URISyntaxException {
    JobMeta jm = new JobMeta( new File( SimultaneousJobsAppenderTest.class.getClassLoader().getResource( PKG + jobPath ).toURI() ).getCanonicalPath(), null );      
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