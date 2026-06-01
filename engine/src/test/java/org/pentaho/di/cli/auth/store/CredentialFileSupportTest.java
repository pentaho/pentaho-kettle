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

package org.pentaho.di.cli.auth.store;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CredentialFileSupportTest {

  private static final String TARGET_DESCRIPTION = "encrypted credential file";
  private static final String SESSION_FILE_DESCRIPTION = "session file";
  private static final String NON_EMPTY_DIRECTORY_DESCRIPTION = "non-empty directory";
  private static final Set<PosixFilePermission> OWNER_ONLY_PERMISSIONS = EnumSet.of(
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE );

  @Rule
  public TemporaryFolder tempDir = new TemporaryFolder();

  @Test
  public void ensureParentDirectoryExistsCreatesMissingParentsAndNoopsWhenPresent() throws IOException {
    File nestedFile =
      tempDir.getRoot().toPath().resolve( "nested" ).resolve( "deeper" ).resolve( "session.properties" ).toFile();

    CredentialFileSupport.ensureParentDirectoryExists( nestedFile );
    CredentialFileSupport.ensureParentDirectoryExists( nestedFile );

    assertTrue( nestedFile.getParentFile().exists() );
  }

  @Test
  public void ensureParentDirectoryExistsThrowsWhenParentCannotBeCreated() {
    File file = mock( File.class );
    File parent = mock( File.class );

    when( file.getParentFile() ).thenReturn( parent );
    when( parent.exists() ).thenReturn( false );
    when( parent.mkdirs() ).thenReturn( false );
    when( parent.getAbsolutePath() ).thenReturn( tempDir.getRoot().toPath().resolve( "blocked" ).toString() );

    try {
      CredentialFileSupport.ensureParentDirectoryExists( file );
      fail( "Expected directory creation failure" );
    } catch ( IOException e ) {
      assertEquals(
        message( "CredentialFileSupport.CannotCreateDirectory", parent.getAbsolutePath() ),
        e.getMessage() );
    }
  }

  @Test
  public void ensureParentDirectoryExistsNoopsWhenFileHasNoParent() throws IOException {
    File file = new File( "session.properties" );

    CredentialFileSupport.ensureParentDirectoryExists( file );

    assertNull( file.getParent() );
    assertFalse( file.exists() );
  }

  @Test
  public void deleteIfExistsRemovesExistingFileAndIgnoresMissingOrUndeletableTargets() throws IOException {
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isDebug() ).thenReturn( true );

    File file = tempDir.getRoot().toPath().resolve( "session.properties" ).toFile();
    Files.writeString( file.toPath(), "data" );

    CredentialFileSupport.deleteIfExists( file, log, SESSION_FILE_DESCRIPTION );
    CredentialFileSupport.deleteIfExists( file, log, SESSION_FILE_DESCRIPTION );

    assertFalse( file.exists() );

    Path nonEmptyDirectory = Files.createDirectory( tempDir.getRoot().toPath().resolve( "non-empty" ) );
    Files.writeString( nonEmptyDirectory.resolve( "child.txt" ), "child" );

    CredentialFileSupport.deleteIfExists( nonEmptyDirectory.toFile(), log, NON_EMPTY_DIRECTORY_DESCRIPTION );
    assertTrue( Files.exists( nonEmptyDirectory ) );
    ArgumentCaptor<String> logMessage = ArgumentCaptor.forClass( String.class );
    verify( log, atLeastOnce() ).logDebug( logMessage.capture() );
    assertTrue( logMessage.getValue().startsWith(
      message( "CredentialFileSupport.CouldNotDelete", NON_EMPTY_DIRECTORY_DESCRIPTION, "" ) ) );
  }

  @Test
  public void deleteIfExistsWithoutLogSwallowsIoFailures() {
    File file = tempDir.getRoot().toPath().resolve( "missing-without-log.properties" ).toFile();
    assertFalse( file.exists() );

    try ( MockedStatic<Files> files = mockStatic( Files.class ) ) {
      files.when( () -> Files.deleteIfExists( file.toPath() ) ).thenThrow( new IOException( "boom" ) );

      CredentialFileSupport.deleteIfExists( file, null, SESSION_FILE_DESCRIPTION );

      files.verify( () -> Files.deleteIfExists( file.toPath() ) );
    }

    assertFalse( file.exists() );
  }

  @Test
  public void applyOwnerOnlyPermissionsDoesNotThrowWhenPosixUnsupported() throws IOException {
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isDebug() ).thenReturn( true );
    File file = tempDir.getRoot().toPath().resolve( "encrypted.properties" ).toFile();
    Files.writeString( file.toPath(), "data" );

    CredentialFileSupport.applyOwnerOnlyPermissions( file, log, TARGET_DESCRIPTION );
    assertTrue( file.exists() );
  }

  @Test
  public void applyOwnerOnlyPermissionsLogsWhenPosixPermissionsCannotBeSet() throws IOException {
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isDebug() ).thenReturn( true );
    File file = tempDir.getRoot().toPath().resolve( "io-failure.properties" ).toFile();
    Files.writeString( file.toPath(), "data" );

    try ( MockedStatic<Files> files = mockStatic( Files.class ) ) {
      files.when( () -> Files.setPosixFilePermissions( file.toPath(), OWNER_ONLY_PERMISSIONS ) )
        .thenThrow( new IOException( "boom" ) );

      CredentialFileSupport.applyOwnerOnlyPermissions( file, log, TARGET_DESCRIPTION );
    }

    verify( log ).logDebug( message( "CredentialFileSupport.CouldNotSetRestrictivePermissions",
      TARGET_DESCRIPTION, "boom" ) );
  }

  @Test
  public void applyOwnerOnlyPermissionsLogsFallbackOperationFailuresWhenPosixUnsupported() throws IOException {
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isDebug() ).thenReturn( true );
    File file = spy( tempDir.getRoot().toPath().resolve( "fallback.properties" ).toFile() );
    Files.writeString( file.toPath(), "data" );
    doReturn( false ).when( file ).setReadable( false, false );
    doReturn( false ).when( file ).setWritable( false, false );
    doReturn( false ).when( file ).setExecutable( false, false );
    doReturn( false ).when( file ).setReadable( true, true );
    doReturn( false ).when( file ).setWritable( true, true );

    try ( MockedStatic<Files> files = mockStatic( Files.class ) ) {
      files.when( () -> Files.setPosixFilePermissions( file.toPath(), OWNER_ONLY_PERMISSIONS ) )
        .thenThrow( new UnsupportedOperationException( "no posix" ) );

      CredentialFileSupport.applyOwnerOnlyPermissions( file, log, TARGET_DESCRIPTION );
    }

    assertTrue( file.exists() );
    verify( log, atLeast( 1 ) ).logDebug( message( "CredentialFileSupport.CouldNotApplyOperation",
      message( "CredentialFileSupport.Operation.ClearWorldReadable" ), TARGET_DESCRIPTION ) );
  }

  @Test
  public void applyOwnerOnlyPermissionsWithoutLogDoesNotThrowOnFallback() {
    File file = spy( tempDir.getRoot().toPath().resolve( "missing-without-log.properties" ).toFile() );
    doReturn( false ).when( file ).setReadable( false, false );
    doReturn( false ).when( file ).setWritable( false, false );
    doReturn( false ).when( file ).setExecutable( false, false );
    doReturn( false ).when( file ).setReadable( true, true );
    doReturn( false ).when( file ).setWritable( true, true );

    CredentialFileSupport.applyOwnerOnlyPermissions( file, TARGET_DESCRIPTION );

    assertFalse( file.exists() );
    verify( file, never() ).setReadable( true, false );
  }

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( CredentialFileSupport.class, key, tokens );
  }
}
