/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.di.purge;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;

/**
 * Created by tkafalas 7/14/14.
 */
@RunWith( org.mockito.junit.MockitoJUnitRunner.class )
public class UnifiedRepositoryPurgeServiceTest {

  private static final String[][] versionData = new String[][] {
    { "100", "1", "01/01/2000", "Bugs Bunny", "original", "1.0" },
    { "101", "1", "01/01/2002", "Bugs Bunny", "1st change", "1.1" },
    { "102", "1", "01/01/2004", "Micky Mouse", "2nd change", "1.2" },
    { "103", "1", "01/01/2006", "Micky Mouse", "3rd change", "1.3" },
    { "104", "1", "01/01/2008", "Micky Mouse", "4th change", "1.4" },
    { "105", "1", "01/01/2010", "Donald Duck", "5th change", "1.5" },
    { "200", "2", "01/01/2001", "Donald Duck", "original", "1.0" },
    { "201", "2", "01/01/2003", "Fred Flintstone", "1st change", "1.1" },
    { "202", "2", "01/01/2005", "Fred Flintstone", "2nd change", "1.2" },
    { "203", "2", "01/01/2013", "Barny Rubble", "3rd change", "1.3" }, };

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "MM/dd/yyyy" );

  private static final String treeResponse =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
          + "<repositoryFileTreeDto><children><children><file><folder>false</folder><hidden>false</hidden><id>1</id><locked>false</locked><name>file1.ktr</name><ownerType>-1</ownerType><path>/home/joe/file1.ktr</path><versionId>1.5</versionId><versioned>true</versioned></file></children>"
          + "<children><children><file><folder>false</folder><hidden>false</hidden><id>2</id><locked>false</locked><name>file2.ktr</name><ownerType>-1</ownerType><path>/home/joe/newdirTest/file2.ktr</path><versionId>1.3</versionId><versioned>true</versioned></file></children>"
          + "</children><file><folder>true</folder><hidden>false</hidden><id>homejoessn</id><locked>false</locked><name>joe</name><ownerType>-1</ownerType><path>/home/joe</path><versioned>false</versioned></file></children>"
          + "<file><folder>true</folder><hidden>false</hidden><id>homessn</id><locked>false</locked><name>home</name><ownerType>-1</ownerType><path>/home</path><versioned>false</versioned></file>"
          + "</repositoryFileTreeDto>";

  private static RepositoryElementInterface element1;

  static {
    // Setup a mocked RepositoryElementInterface so alternate methods can be called for maximum code coverage
    element1 = mock( RepositoryElementInterface.class );
    ObjectId mockObjectId1 = mock( ObjectId.class );
    when( mockObjectId1.getId() ).thenReturn( "1" );
    when( element1.getObjectId() ).thenReturn( mockObjectId1 );
  }

  private HashMap<String, List<VersionSummary>> processVersionMap( IUnifiedRepository mockRepo ) {
    // Build versionListMap
    final HashMap<String, List<VersionSummary>> versionListMap = new HashMap<String, List<VersionSummary>>();
    List<VersionSummary> mockVersionList = new ArrayList<VersionSummary>();
    Date d = null;
    String fileId = null;
    for ( String[] values : versionData ) {
      d = getDate( values[2] );
      if ( !values[1].equals( fileId ) ) {
        if ( fileId != null ) {
          versionListMap.put( fileId, mockVersionList );
        }
        mockVersionList = new ArrayList<VersionSummary>();
      }

      fileId = values[1];
      VersionSummary versionSummary =
          new VersionSummary( values[0], fileId, false, d, values[3], values[4], Arrays
              .asList( new String[] { values[5] } ) );
      mockVersionList.add( versionSummary );

    }
    versionListMap.put( fileId, mockVersionList );

    final ArgumentCaptor<String> fileIdArgument = ArgumentCaptor.forClass( String.class );
    when( mockRepo.getVersionSummaries( fileIdArgument.capture() ) ).thenAnswer( new Answer<List<VersionSummary>>() {
      public List<VersionSummary> answer( InvocationOnMock invocation ) throws Throwable {
        return versionListMap.get( fileIdArgument.getValue() );
      }
    } );

    return versionListMap;
  }

  @Test
  public void deleteAllVersionsTest() throws KettleException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );

    UnifiedRepositoryPurgeService purgeService = new UnifiedRepositoryPurgeService( mockRepo );
    String fileId = "1";
    purgeService.deleteAllVersions( element1 );

    verifyAllVersionsDeleted( versionListMap, mockRepo, "1" );
    verify( mockRepo, never() ).deleteFileAtVersion( eq( "2" ), anyString() );
  }

  @Test
  public void deleteVersionTest() throws KettleException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );

    UnifiedRepositoryPurgeService purgeService = new UnifiedRepositoryPurgeService( mockRepo );
    String fileId = "1";
    String versionId = "103";
    purgeService.deleteVersion( element1, versionId );

    verify( mockRepo, times( 1 ) ).deleteFileAtVersion( fileId, versionId );
    verify( mockRepo, never() ).deleteFileAtVersion( eq( "2" ), anyString() );
  }

  @Test
  public void keepNumberOfVersions0Test() throws KettleException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );

    UnifiedRepositoryPurgeService purgeService = new UnifiedRepositoryPurgeService( mockRepo );
    String fileId = "1";
    int versionCount = 0;

    purgeService.keepNumberOfVersions( element1, versionCount );

    verifyVersionCountDeletion( versionListMap, mockRepo, fileId, versionCount );
    verify( mockRepo, never() ).deleteFileAtVersion( eq( "2" ), anyString() );
  }

  @Test
  public void keepNumberOfVersionsTest() throws KettleException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );

    UnifiedRepositoryPurgeService purgeService = new UnifiedRepositoryPurgeService( mockRepo );
    String fileId = "1";
    int versionCount = 2;

    purgeService.keepNumberOfVersions( element1, versionCount );

    verifyVersionCountDeletion( versionListMap, mockRepo, fileId, versionCount );
    verify( mockRepo, never() ).deleteFileAtVersion( eq( "2" ), anyString() );
  }

  @Test
  public void deleteVersionsBeforeDate() throws KettleException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );

    UnifiedRepositoryPurgeService purgeService = new UnifiedRepositoryPurgeService( mockRepo );
    String fileId = "1";
    Date beforeDate = getDate( "01/02/2006" );

    purgeService.deleteVersionsBeforeDate( element1, beforeDate );

    verifyDateBeforeDeletion( versionListMap, mockRepo, fileId, beforeDate );
    verify( mockRepo, never() ).deleteFileAtVersion( eq( "2" ), anyString() );
  }

  @Test
  public void doPurgeUtilPurgeFileTest() throws PurgeDeletionException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );
    UnifiedRepositoryPurgeService purgeService = getPurgeService( mockRepo );

    PurgeUtilitySpecification spec = new PurgeUtilitySpecification();
    spec.purgeFiles = true;
    spec.setPath( "/" );
    purgeService.doDeleteRevisions( spec );

    verifyAllVersionsDeleted( versionListMap, mockRepo, "1" );
    verifyAllVersionsDeleted( versionListMap, mockRepo, "2" );
    verify( UnifiedRepositoryPurgeService.getRepoWs(), times( 1 ) ).deleteFileWithPermanentFlag( eq( "1" ), eq( true ),
        anyString() );
    verify( UnifiedRepositoryPurgeService.getRepoWs(), times( 1 ) ).deleteFileWithPermanentFlag( eq( "2" ), eq( true ),
        anyString() );
  }

  @Test
  public void doPurgeUtilVersionCountTest() throws PurgeDeletionException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );
    UnifiedRepositoryPurgeService purgeService = getPurgeService( mockRepo );

    PurgeUtilitySpecification spec = new PurgeUtilitySpecification();
    spec.setVersionCount( 3 );
    spec.setPath( "/" );
    purgeService.doDeleteRevisions( spec );

    verifyVersionCountDeletion( versionListMap, mockRepo, "1", spec.getVersionCount() );
    verifyVersionCountDeletion( versionListMap, mockRepo, "2", spec.getVersionCount() );
  }

  @Test
  public void doPurgeUtilDateBeforeTest() throws PurgeDeletionException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );
    UnifiedRepositoryPurgeService purgeService = getPurgeService( mockRepo );

    PurgeUtilitySpecification spec = new PurgeUtilitySpecification();
    spec.setBeforeDate( getDate( "01/02/2006" ) );
    spec.setPath( "/" );
    purgeService.doDeleteRevisions( spec );
    verifyDateBeforeDeletion( versionListMap, mockRepo, "1", spec.getBeforeDate() );
    verifyDateBeforeDeletion( versionListMap, mockRepo, "2", spec.getBeforeDate() );
  }

  @Test
  public void doPurgeUtilSharedObjectsTest() throws PurgeDeletionException {
    IUnifiedRepository mockRepo = mock( IUnifiedRepository.class );
    final HashMap<String, List<VersionSummary>> versionListMap = processVersionMap( mockRepo );
    UnifiedRepositoryPurgeService purgeService = getPurgeService( mockRepo );

    PurgeUtilitySpecification spec = new PurgeUtilitySpecification();
    spec.purgeFiles = true;
    spec.setSharedObjects( true );
    purgeService.doDeleteRevisions( spec );

    // Since each tree call delivers the same mock tree, we expect the files to get deleted once per folder.
    String fileId = "1";
    String fileLastRevision = "105";
    List<VersionSummary> list = versionListMap.get( fileId );
    for ( VersionSummary sum : list ) {
      final int expectedTimes;
      if ( !fileLastRevision.equals( sum.getId() ) ) {
        expectedTimes = 4;
      } else {
        expectedTimes = 0;
      }
      verify( mockRepo, times( expectedTimes ) ).deleteFileAtVersion( fileId, sum.getId() );
      verify( UnifiedRepositoryPurgeService.getRepoWs(), times( 4 ) ).deleteFileWithPermanentFlag( eq( fileId ), eq( true ), anyString() );
    }
  }

  // create the necessary mocks for running a full Purge Utility job
  private static UnifiedRepositoryPurgeService getPurgeService( IUnifiedRepository mockRepo ) {

    UnifiedRepositoryPurgeService purgeService = new UnifiedRepositoryPurgeService( mockRepo );
    DefaultUnifiedRepositoryWebService mockRepoWs = mock( DefaultUnifiedRepositoryWebService.class );
    UnifiedRepositoryPurgeService.repoWs = mockRepoWs;

    // Create a mocked tree to be returned
    JAXBContext jc;
    RepositoryFileTreeDto tree = null;
    try {
      jc = JAXBContext.newInstance( RepositoryFileTreeDto.class );
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      ByteArrayInputStream xml = new ByteArrayInputStream( treeResponse.getBytes() );
      tree = (RepositoryFileTreeDto) unmarshaller.unmarshal( xml );
    } catch ( JAXBException e ) {
      e.printStackTrace();
      fail( "Test class has invalid xml representation of tree" );
    }

    when( mockRepoWs.getTreeFromRequest( any( RepositoryRequest.class ) ) ).thenReturn( tree );
    return purgeService;
  }

  private static void verifyAllVersionsDeleted( HashMap<String, List<VersionSummary>> versionListMap,
      IUnifiedRepository mockRepo, String fileId ) {
    List<VersionSummary> list = versionListMap.get( fileId );
    int i = 1;
    for ( VersionSummary sum : list ) {
      if ( i < list.size() ) {
        verify( mockRepo, times( 1 ) ).deleteFileAtVersion( fileId, sum.getId() );
      }
      i++;
    }
  }

  private static void verifyVersionCountDeletion( HashMap<String, List<VersionSummary>> versionListMap,
      IUnifiedRepository mockRepo, String fileId, int versionCount ) {
    List<VersionSummary> list = versionListMap.get( fileId );
    int i = 1;
    for ( VersionSummary sum : list ) {
      if ( i <= list.size() - versionCount ) {
        verify( mockRepo, times( 1 ) ).deleteFileAtVersion( fileId, sum.getId() );
      }
      i++;
    }
  }

  private static void verifyDateBeforeDeletion( HashMap<String, List<VersionSummary>> versionListMap,
      IUnifiedRepository mockRepo, String fileId, Date beforeDate ) {
    int i = 0;
    List<VersionSummary> list = versionListMap.get( fileId );
    for ( VersionSummary sum : list ) {
      if ( beforeDate.after( sum.getDate() ) && !sum.getId().equals( list.get( list.size() - 1 ).getId() ) ) {
        verify( mockRepo, times( 1 ) ).deleteFileAtVersion( fileId, sum.getId() );
      } else {
        verify( mockRepo, never() ).deleteFileAtVersion( fileId, sum.getId() );
      }
    }
  }

  private static Date getDate( String dateString ) {
    Date date = null;
    try {
      date = DATE_FORMAT.parse( dateString );
    } catch ( ParseException e ) {
      fail( "Bad Date format in test class" );
    }
    return date;
  }
}
