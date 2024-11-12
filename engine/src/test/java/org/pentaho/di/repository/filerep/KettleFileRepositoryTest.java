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
