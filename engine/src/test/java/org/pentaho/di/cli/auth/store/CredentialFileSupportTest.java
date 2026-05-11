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
import org.pentaho.di.core.logging.LogChannelInterface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CredentialFileSupportTest {

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
  public void deleteIfExistsRemovesExistingFileAndIgnoresMissingOrUndeletableTargets() throws IOException {
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isDebug() ).thenReturn( true );

    File file = tempDir.getRoot().toPath().resolve( "session.properties" ).toFile();
    Files.writeString( file.toPath(), "data" );

    CredentialFileSupport.deleteIfExists( file, log, "session file" );
    CredentialFileSupport.deleteIfExists( file, log, "session file" );

    assertFalse( file.exists() );

    Path nonEmptyDirectory = Files.createDirectory( tempDir.getRoot().toPath().resolve( "non-empty" ) );
    Files.writeString( nonEmptyDirectory.resolve( "child.txt" ), "child" );

    CredentialFileSupport.deleteIfExists( nonEmptyDirectory.toFile(), log, "non-empty directory" );
    assertTrue( Files.exists( nonEmptyDirectory ) );
    verify( log, atLeastOnce() ).logDebug( contains( "CliFileSupport.CouldNotDelete" ) );
  }

  @Test
  public void applyOwnerOnlyPermissionsDoesNotThrowWhenPosixUnsupported() throws IOException {
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isDebug() ).thenReturn( true );
    File file = tempDir.getRoot().toPath().resolve( "encrypted.properties" ).toFile();
    Files.writeString( file.toPath(), "data" );

    CredentialFileSupport.applyOwnerOnlyPermissions( file, log, "encrypted credential file" );
    assertTrue( file.exists() );
  }

  @Test
  public void applyOwnerOnlyPermissionsLogsWhenPosixWriteFails() {
    LogChannelInterface log = mock( LogChannelInterface.class );
    when( log.isDebug() ).thenReturn( true );
    File file = new PermissionRejectingFile( tempDir.getRoot().toPath().resolve( "missing.properties" ) );

    CredentialFileSupport.applyOwnerOnlyPermissions( file, log, "encrypted credential file" );

    assertFalse( file.exists() );
    verify( log, atLeastOnce() ).logDebug( contains( "CliFileSupport.CouldNotApplyOperation" ) );
  }

  private static final class PermissionRejectingFile extends File {
    private final Path path;

    PermissionRejectingFile( Path path ) {
      super( path.toString() );
      this.path = path;
    }

    @Override
    public Path toPath() {
      return path;
    }

    @Override
    public boolean setReadable( boolean readable, boolean ownerOnly ) {
      return false;
    }

    @Override
    public boolean setWritable( boolean writable, boolean ownerOnly ) {
      return false;
    }

    @Override
    public boolean setExecutable( boolean executable, boolean ownerOnly ) {
      return false;
    }
  }
}
