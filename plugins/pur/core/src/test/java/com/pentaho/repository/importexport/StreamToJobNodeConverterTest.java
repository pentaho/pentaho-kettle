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

package com.pentaho.repository.importexport;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryBowl;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.MemorySharedObjectsIO;
import org.pentaho.platform.api.repository2.unified.ConverterException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class StreamToJobNodeConverterTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setupClass() {
    DefaultBowl.getInstance().setSharedObjectsIO( new MemorySharedObjectsIO() );
  }

  @Test
  public void testConvertJobWithMissingPlugins() throws IOException, KettleException {
    RepositoryFile repositoryFile = new RepositoryFile.Builder( "test file" ).build();
    IUnifiedRepository pur = mock( IUnifiedRepository.class );
    when( pur.getFileById( "MissingEntries.ktr" ) ).thenReturn( repositoryFile );
    JobMeta jobMeta = new JobMeta();

    Repository repository = mock( Repository.class );
    when( repository.loadJob( any( StringObjectId.class ), anyString() ) ).thenReturn( jobMeta );
    when( repository.getBowl() ).thenReturn( new RepositoryBowl( repository ) );


    StreamToJobNodeConverter jobNodeConverter = new StreamToJobNodeConverter( pur );
    jobNodeConverter = spy( jobNodeConverter );
    doReturn( repository ).when( jobNodeConverter ).connectToRepository();

    try {
      jobNodeConverter.convert( getClass().getResource( "MissingEntries.kjb" ).openStream(), "UTF-8", "application/vnd.pentaho.transformation" );
    } catch ( ConverterException e ) {
      assertTrue( e.getMessage().contains( "MissingPlugin" ) );
      return;
    }
    fail();
  }

  private List<DatabaseMeta> getDummyDatabases() {
    List<DatabaseMeta> databases = new ArrayList<>(  );
    databases.add( new DatabaseMeta( "database1", "Oracle", "Native", "", "", "", "", "" ) );
    databases.add( new DatabaseMeta( "database2", "Oracle", "Native", "", "", "", "", "" ) );
    databases.add( new DatabaseMeta( "database3", "Oracle", "Native", "", "", "", "", "" ) );
    databases.add( new DatabaseMeta( "database4", "Oracle", "Native", "", "", "", "", "" ) );
    return databases;
  }

  private void setDatabases( AbstractMeta meta, List<DatabaseMeta> databases ) throws Exception {
    meta.getDatabaseManagementInterface().clear();
    for ( DatabaseMeta db : databases ) {
      meta.getDatabaseManagementInterface().add( db );
    }
  }
}
