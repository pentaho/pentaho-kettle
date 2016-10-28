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

package org.pentaho.di.job.entries.missing;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;

public class MissingPluginJobIT extends TestCase {
  @Test( expected = Exception.class )
  public void testForPluginMissingStep() throws Exception {
    KettleEnvironment.init();
    Job job;
    JobMeta meta = new JobMeta(
          "test/org/pentaho/di/job/entries/missing/missing_plugin_job.kjb",
          null );
    job = new Job( null, meta );

    Result result = new Result();
    job.execute( 0, result );
    assertFalse( result.getResult() );
  }
}
