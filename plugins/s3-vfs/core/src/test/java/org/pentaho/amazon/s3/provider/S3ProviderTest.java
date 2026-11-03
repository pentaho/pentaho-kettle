/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.amazon.s3.provider;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.s3.vfs.S3FileProvider;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class S3ProviderTest {

  private static final String ACCESS_KEY = "myAccessKey";
  private static final String SECRET_KEY = "mySecretKey";
  private static final String SAVED = "saved";
  private static final String OTHER = "other";

  private S3Provider s3Provider;
  private ConnectionManager connectionManager;

  @BeforeClass
  public static void setUpClass() throws Exception {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    s3Provider = spy( new S3Provider() );
    connectionManager = mock( ConnectionManager.class );
  }

  @Test
  public void testDoesNotChangeOtherConnections() throws Exception {
    doReturn( mock( AmazonS3.class ) ).when( s3Provider ).getS3Client( any() );

    try ( MockedStatic<ConnectionManager> connectionManagerStatic = mockStatic( ConnectionManager.class ) ) {
      connectionManagerStatic.when( ConnectionManager::getInstance ).thenReturn( connectionManager );

      assertTrue( s3Provider.test( createDetails( SAVED, "true" ) ) );
    }

    verifyNoInteractions( connectionManager );
  }

  @Test
  public void testAfterSaveOfDefaultConnectionRemovesDefaultFromOthers() {
    S3Details other = createDetails( OTHER, "true" );
    other.setAccessKey( Encr.encryptPasswordIfNotUsingVariables( ACCESS_KEY ) );
    doReturn( Arrays.asList( createDetails( SAVED, "true" ), other ) )
      .when( connectionManager ).getConnectionDetailsByScheme( S3FileProvider.SCHEME );

    s3Provider.afterSave( connectionManager, createDetails( SAVED, "true" ) );

    S3Details saved = captureSingleSave();
    assertEquals( OTHER, saved.getName() );
    assertEquals( "false", saved.getDefaultS3Config() );
    assertNull( saved.getDefaultS3ConfigVariable() );
    assertEquals( ACCESS_KEY, saved.getAccessKey() );
    assertEquals( SECRET_KEY, saved.getSecretKey() );
  }

  @Test
  public void testAfterSaveRemovesDefaultSetByVariable() {
    S3Details other = createDetails( OTHER, "false" );
    other.setDefaultS3ConfigVariable( "${IS_DEFAULT}" );
    doReturn( Arrays.asList( other ) ).when( connectionManager ).getConnectionDetailsByScheme( S3FileProvider.SCHEME );

    s3Provider.afterSave( connectionManager, createDetails( SAVED, "true" ) );

    S3Details saved = captureSingleSave();
    assertEquals( OTHER, saved.getName() );
    assertEquals( "false", saved.getDefaultS3Config() );
    assertNull( saved.getDefaultS3ConfigVariable() );
  }

  @Test
  public void testAfterSaveUsesTheDefaultVariableOfTheSavedConnection() {
    S3Details details = createDetails( SAVED, "false" );
    details.setDefaultS3ConfigVariable( "${IS_DEFAULT}" );
    details.getSpace().setVariable( "IS_DEFAULT", "Y" );
    doReturn( Arrays.asList( createDetails( OTHER, "true" ) ) )
      .when( connectionManager ).getConnectionDetailsByScheme( S3FileProvider.SCHEME );

    s3Provider.afterSave( connectionManager, details );

    assertEquals( OTHER, captureSingleSave().getName() );
  }

  @Test
  public void testAfterSaveSkipsConnectionsThatAreNotDefault() {
    doReturn( Arrays.asList( createDetails( OTHER, "false" ) ) )
      .when( connectionManager ).getConnectionDetailsByScheme( S3FileProvider.SCHEME );

    s3Provider.afterSave( connectionManager, createDetails( SAVED, "true" ) );

    verify( connectionManager, never() ).save( any() );
  }

  @Test
  public void testAfterSaveOfNonDefaultConnectionDoesNothing() {
    s3Provider.afterSave( connectionManager, createDetails( SAVED, "false" ) );

    verifyNoInteractions( connectionManager );
  }

  private S3Details captureSingleSave() {
    ArgumentCaptor<S3Details> captor = ArgumentCaptor.forClass( S3Details.class );
    verify( connectionManager, times( 1 ) ).save( captor.capture() );
    return captor.getValue();
  }

  private static S3Details createDetails( String name, String defaultS3Config ) {
    S3Details details = new S3Details();
    details.setName( name );
    details.setAuthType( S3Provider.ACCESS_KEY_SECRET_KEY );
    details.setAccessKey( ACCESS_KEY );
    details.setSecretKey( SECRET_KEY );
    details.setDefaultS3Config( defaultS3Config );
    details.setSpace( new Variables() );
    return details;
  }
}
