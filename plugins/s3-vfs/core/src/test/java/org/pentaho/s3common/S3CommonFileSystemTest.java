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


package org.pentaho.s3common;

import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FilesCache;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileObject;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileSystem;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.transfer.TransferManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.s3common.DummyS3CommonObjects.getDummyInstance;

public class S3CommonFileSystemTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleClientEnvironment.init();
  }

  @Test
  public void getPartSize() {
    DummyS3FileSystem s3FileSystem = getDummyInstance();
    s3FileSystem.storageUnitConverter = new StorageUnitConverter();
    S3KettleProperty s3KettleProperty = mock( S3KettleProperty.class );
    when( s3KettleProperty.getPartSize() ).thenReturn( "6MB" );
    s3FileSystem.s3KettleProperty = s3KettleProperty;

    //TEST 1: Below max
    assertEquals( 6 * 1024 * 1024, s3FileSystem.getPartSize() );

    // TEst 2: above max
    when( s3KettleProperty.getPartSize() ).thenReturn( "600GB" );
    assertEquals( 600L * 1024 * 1024 * 1024, s3FileSystem.getPartSize() );
  }

  @Test
  public void testParsePartSize() {
    DummyS3FileSystem s3FileSystem = getDummyInstance();
    s3FileSystem.storageUnitConverter = new StorageUnitConverter();
    long _5MBLong = 5L * 1024L * 1024L;
    long _124MBLong = 124L * 1024L * 1024L;
    long _5GBLong = 5L * 1024L * 1024L * 1024L;
    long _12GBLong = 12L * 1024L * 1024L * 1024L;
    long minimumPartSize = _5MBLong;
    long maximumPartSize = _5GBLong;


    // TEST 1: below minimum
    assertEquals( minimumPartSize, s3FileSystem.parsePartSize( "1MB" ) );

    // TEST 2: at minimum
    assertEquals( minimumPartSize, s3FileSystem.parsePartSize( "5MB" ) );

    // TEST 3: between minimum and maximum
    assertEquals( _124MBLong, s3FileSystem.parsePartSize( "124MB" ) );

    // TEST 4: at maximum
    assertEquals( maximumPartSize, s3FileSystem.parsePartSize( "5GB" ) );

    // TEST 5: above maximum
    assertEquals( _12GBLong, s3FileSystem.parsePartSize( "12GB" ) );
  }

  @Test
  public void testCopy_DelegatesToTransferManager() throws Exception {
    DummyS3FileObject src = mock( DummyS3FileObject.class );
    DummyS3FileObject dest = mock( DummyS3FileObject.class );
    S3TransferManager transferManager = mock( S3TransferManager.class );
    DummyS3FileSystem fs = Mockito.spy( getDummyInstance() );
    doReturn( transferManager ).when( fs ).getS3TransferManager();
    fs.copy( src, dest );
    verify( transferManager, times( 1 ) ).copy( src, dest );
  }

  @Test
  public void testGetS3TransferManager_CreatesWithTransferManager() {
    DummyS3FileSystem fs = Mockito.spy( getDummyInstance() );
    S3TransferManager transferManager = mock( S3TransferManager.class );
    doReturn( transferManager ).when( fs ).getS3TransferManager();
    S3TransferManager result = fs.getS3TransferManager();
    assertNotNull( result );
  }

  @Test
  public void testBuildTransferManager_CreatesWithS3Client() {
    DummyS3FileSystem fs = Mockito.spy( getDummyInstance() );
    com.amazonaws.services.s3.AmazonS3 s3Client = mock( com.amazonaws.services.s3.AmazonS3.class );
    doReturn( s3Client ).when( fs ).getS3Client();
    TransferManager tm = fs.buildTransferManager();
    assertNotNull( tm );
  }

  @Test
  public void testGetS3Client_DefaultConnectionWithPlainKeys_UsesKeysAsIs() {
    // ConnectionManager already decrypts @Encrypted fields on load, so the default connection holds plain keys
    assertDefaultConnectionCredentials( "AKIAPLAINACCESSKEY", "plain/Secret+Key",
      "AKIAPLAINACCESSKEY", "plain/Secret+Key" );
  }

  @Test
  public void testGetS3Client_DefaultConnectionWithEncryptedKeys_DecryptsKeys() {
    assertDefaultConnectionCredentials(
      Encr.PASSWORD_ENCRYPTED_PREFIX + Encr.encryptPassword( "AKIAENCRYPTEDKEY" ),
      Encr.PASSWORD_ENCRYPTED_PREFIX + Encr.encryptPassword( "encrypted/Secret+Key" ),
      "AKIAENCRYPTEDKEY", "encrypted/Secret+Key" );
  }

  private void assertDefaultConnectionCredentials( String storedAccessKey, String storedSecretKey,
                                                   String expectedAccessKey, String expectedSecretKey ) {
    S3Details defaultConnection = new S3Details();
    defaultConnection.setName( "defaultS3" );
    defaultConnection.setAccessKey( storedAccessKey );
    defaultConnection.setSecretKey( storedSecretKey );
    defaultConnection.setDefaultS3Config( "true" );

    ConnectionManager connectionManager = mock( ConnectionManager.class );
    doReturn( Collections.singletonList( defaultConnection ) )
      .when( connectionManager ).getConnectionDetailsByScheme( "s3" );

    FileSystemManager fileSystemManager = mock( FileSystemManager.class );
    when( fileSystemManager.getFilesCache() ).thenReturn( mock( FilesCache.class ) );

    // No pvfs connection name in the options, i.e. a raw s3:// URL that falls back to the default connection
    DummyS3FileSystem fs = Mockito.spy( getDummyInstance() );
    doReturn( fileSystemManager ).when( fs ).getFileSystemManager();
    doReturn( false ).when( fs ).isRegionSet();

    List<List<?>> credentialsArgs = new ArrayList<>();
    try ( MockedStatic<ConnectionManager> connectionManagerStatic = mockStatic( ConnectionManager.class );
          MockedConstruction<BasicAWSCredentials> ignored = mockConstruction( BasicAWSCredentials.class,
            ( mock, context ) -> credentialsArgs.add( context.arguments() ) ) ) {
      connectionManagerStatic.when( ConnectionManager::getInstance ).thenReturn( connectionManager );

      assertNotNull( fs.getS3Client() );
    }

    assertEquals( 1, credentialsArgs.size() );
    assertEquals( Arrays.asList( expectedAccessKey, expectedSecretKey ), credentialsArgs.get( 0 ) );
  }

}
