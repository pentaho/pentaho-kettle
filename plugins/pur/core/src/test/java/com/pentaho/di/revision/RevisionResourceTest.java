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

package com.pentaho.di.revision;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;

import org.pentaho.di.core.util.Assert;
import org.pentaho.di.repository.pur.PurObjectRevision;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.repository2.unified.webservices.FileVersioningConfiguration;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;

public class RevisionResourceTest {

  RevisionResource revisionResource;

  RepositoryFile mockRepositoryFile;

  private static final String MOCK_FILE_PATH = ":mock:file:path";
  private static final String MOCK_FILE_ID = "0123456789";

  private static final String MOCK_VERSION_ID_1 = "0123456789";
  private static final String MOCK_VERSION_AUTHOR_1 = "Admin";
  private static final String MOCK_VERSION_MESSAGE_1 = "Version message 1";

  /**
   * 
   * @throws Exception
   */

  @org.junit.Before
  public void setUp() throws Exception {
    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    RevisionResource.setRepositoryVersionManager( mockRepositoryVersionManager );

    mockRepositoryFile = mock( RepositoryFile.class );
    when( mockRepositoryFile.getId() ).thenReturn( MOCK_FILE_ID );
    IUnifiedRepository mockRepository = mock( IUnifiedRepository.class );

    when( mockRepository.getFile( FileUtils.idToPath( MOCK_FILE_PATH ) ) ).thenReturn( mockRepositoryFile );
    when( mockRepository.getVersionSummaries( MOCK_FILE_ID ) ).thenReturn( getMockVersionSummaries() );

    revisionResource = new RevisionResource( mockRepository );
  }

  @org.junit.Test
  public void testDummy() {

  }

  /**
   * 
   * @throws Exception
   */
  @org.junit.Ignore
  @org.junit.Test
  public void testDoGetVersions() throws Exception {
    Response response = revisionResource.doGetVersions( MOCK_FILE_PATH );
    Object entity = response.getEntity();

    // Yeah this gets weird: List, wrapped in a Response, wrapped in GenericEnttiy
    List<PurObjectRevision> revisionList = (List<PurObjectRevision>) ( (GenericEntity) entity ).getEntity();

    Assert.assertTrue( revisionList.size() == 1 );
    Assert.assertTrue( revisionList.get( 0 ).getLogin().equals( MOCK_VERSION_AUTHOR_1 ) );
  }

  /**
   * 
   * @throws Exception
   */
  @org.junit.Test
  public void testGetVersioningEnabled() throws Exception {
    FileVersioningConfiguration conf = revisionResource.doVersioningConfiguration( "foo.ktr" );
    Assert.assertTrue( conf.isVersioningEnabled() );
    Assert.assertFalse( conf.isVersionCommentEnabled() );
  }

  /**
   * Return mock list of version summaries
   * 
   * @return
   */
  private List<VersionSummary> getMockVersionSummaries() {
    List<VersionSummary> versionSummaries = new ArrayList<VersionSummary>();

    VersionSummary versionSummary1 =
        new VersionSummary( MOCK_VERSION_ID_1, MOCK_FILE_ID, false, new Date(), MOCK_VERSION_AUTHOR_1,
            MOCK_VERSION_MESSAGE_1, new ArrayList<String>() );

    versionSummaries.add( versionSummary1 );

    return versionSummaries;
  }
}
