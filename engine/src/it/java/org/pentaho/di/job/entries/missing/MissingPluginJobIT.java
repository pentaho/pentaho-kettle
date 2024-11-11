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


package org.pentaho.di.job.entries.missing;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertFalse;

public class MissingPluginJobIT {

  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
  }

  /**
   * Given a job having an entry which's plugin is missing in current Kettle installation.
   * When this job is executed, then execution should fail.
   */
  @Test
  public void testForPluginMissingStep() throws Exception {
    InputStream is = new FileInputStream(
      new File( this.getClass().getResource( "missing_plugin_job.kjb" ).getFile() ) );

    JobMeta meta = new JobMeta( is, null, null );
    Job job = new Job( null, meta );

    Result result = new Result();
    job.execute( 0, result );
    assertFalse( result.getResult() );
  }
}
