/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository.filerep;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

public class KettleFileRepositoryTest extends KettleFileRepositoryTestBase {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testCurrentDirJob() throws Exception {
    final String dirName = "dirName";
    final String jobName = "job";
    JobMeta setupJobMeta = new JobMeta();
    setupJobMeta.setName( jobName );
    RepositoryDirectoryInterface repoDir = repository.createRepositoryDirectory( new RepositoryDirectory(), dirName );
    setupJobMeta.setRepositoryDirectory( repoDir );
    repository.save( setupJobMeta, "" );

    JobMeta jobMeta = repository.loadJob( jobName, repoDir, null, "" );
    assertEquals( repository, jobMeta.getRepository() );
    assertEquals( repoDir.getPath(), jobMeta.getRepositoryDirectory().getPath() );

    jobMeta.setInternalKettleVariables();
    String currentDir = jobMeta.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY );
    assertEquals( repoDir.getPath(), currentDir );
  }

}
