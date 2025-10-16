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

package org.pentaho.s3n.vfs;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.s3.vfs.S3FileName;
import org.pentaho.s3.vfs.S3FileNameTest;
import org.pentaho.s3common.S3CommonFileSystemTestUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


/**
 * Unit tests for S3FileSystem
 */
@RunWith( MockitoJUnitRunner.class )
public class S3NFileSystemTest {

  S3NFileSystem fileSystem;
  S3NFileName fileName;

  @Before
  public void setUp() throws Exception {
    fileName = new S3NFileName(
      S3FileNameTest.SCHEME,
      "/",
      "",
      FileType.FOLDER );
    fileSystem = new S3NFileSystem( fileName, new FileSystemOptions() );
  }


  @Test
  public void testCreateFile() throws Exception {
    assertNotNull( fileSystem.createFile( new S3FileName( "s3n", "bucketName", "/bucketName/key", FileType.FILE ) ) );
  }

  @Test
  public void testGetS3Service() throws Exception {
    assertNotNull( fileSystem.getS3Client() );

    FileSystemOptions options = new FileSystemOptions();
    UserAuthenticator authenticator = mock( UserAuthenticator.class );
    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator( options, authenticator );

    fileSystem = new S3NFileSystem( fileName, options ) {
      @Override
      protected boolean isRegionSet() {
        return false;
      }
    };
    assertNotNull( fileSystem.getS3Client() );
  }

  @Test
  public void getS3ClientWithDefaultRegion() {
    FileSystemOptions options = new FileSystemOptions();
    try ( MockedStatic<Regions> regionsMockedStatic = Mockito.mockStatic( Regions.class ) ) {
      regionsMockedStatic.when( Regions::getCurrentRegion ).thenReturn( null );
      //Not under an EC2 instance - getCurrentRegion returns null
      fileSystem = new S3NFileSystem( fileName, options );
      fileSystem = (S3NFileSystem) S3CommonFileSystemTestUtil.stubRegionUnSet( fileSystem );
      AmazonS3Client s3Client = (AmazonS3Client) fileSystem.getS3Client();
      assertEquals( "No Region was configured - client must have default region",
        Regions.DEFAULT_REGION.getName(), s3Client.getRegionName() );
    }
  }
}
