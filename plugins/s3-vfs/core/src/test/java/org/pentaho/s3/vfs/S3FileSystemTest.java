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

package org.pentaho.s3.vfs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.s3common.TestCleanupUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for S3FileSystem
 */
@RunWith( MockitoJUnitRunner.class )
public class S3FileSystemTest {

  S3FileSystem fileSystem;
  S3FileName fileName;

  @BeforeClass
  public static void setClassUp() throws KettleException {
    KettleEnvironment.init( false );
  }

  @AfterClass
  public static void tearDownClass() {
    KettleEnvironment.shutdown();
    TestCleanupUtil.cleanUpLogsDir();
  }

  @Before
  public void setUp() {
    fileName = new S3FileName(
      S3FileNameTest.SCHEME,
      "/",
      "",
      FileType.FOLDER );
    fileSystem = new S3FileSystem( fileName, new FileSystemOptions() );
  }

  @Test
  public void testCreateFile() {
    assertNotNull( fileSystem.createFile( new S3FileName( "s3", "bucketName", "/bucketName/key", FileType.FILE ) ) );
  }

  @Test
  public void testGetS3Service() {
    assertNotNull( fileSystem.getS3Client() );

    FileSystemOptions options = new FileSystemOptions();
    UserAuthenticator authenticator = mock( UserAuthenticator.class );
    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator( options, authenticator );
    // test is still slow for little gain, but the region check was the slowest part
    fileSystem = new S3FileSystem( fileName, options ) {
      @Override
      protected boolean isRegionSet() {
        return false;
      }
    };
    assertNotNull( fileSystem.getS3Client() );
  }

  public S3FileSystem getTestInstance() {
    // Use a real S3FileName with dummy but non-null values to avoid NPE in S3Util.getKeysFromURI
    S3FileName rootName = new S3FileName( "s3", "bucket", "/bucket/key", FileType.FOLDER );
    FileSystemOptions fileSystemOptions = new FileSystemOptions();
    return new S3FileSystem( rootName, fileSystemOptions );
  }

  @Test
  public void getS3ClientWithDefaultRegion() {
    FileSystemOptions options = new FileSystemOptions();
    try ( MockedStatic<Regions> regionsMockedStatic = Mockito.mockStatic( Regions.class ) ) {
      regionsMockedStatic.when( Regions::getCurrentRegion ).thenReturn( null );
      //Not under an EC2 instance - getCurrentRegion returns null

      fileSystem = new S3FileSystem( fileName, options );

      AmazonS3Client s3Client = (AmazonS3Client) fileSystem.getS3Client();
      assertEquals( "No Region was configured - client must have default region",
        Regions.DEFAULT_REGION.getName(), s3Client.getRegionName() );
    }
  }

}
