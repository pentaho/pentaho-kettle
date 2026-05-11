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

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

/**
 * Small helper for CLI-owned credential/config files.
 * Keeps restrictive permission and delete handling consistent across Pan
 * config and token stores without pulling those classes into a bigger utility.
 */
public final class CredentialFileSupport {

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( CredentialFileSupport.class, key, tokens );
  }

  private CredentialFileSupport() {
  }

  public static void ensureParentDirectoryExists( File file ) throws IOException {
    File parent = file.getParentFile();
    if ( parent != null && !parent.exists() && !parent.mkdirs() ) {
      throw new IOException( message( "CliFileSupport.CannotCreateDirectory", parent.getAbsolutePath() ) );
    }
  }

  /**
   * Applies restrictive file permissions so only the owning OS user can read or
   * write the credentials file.
   *
   * <ul>
   * <li>POSIX (Linux / macOS): {@code chmod 600} (owner r/w only)</li>
   * <li>Windows: uses {@link File#setReadable}/{@link File#setWritable} which
   * maps to the "Deny" ACE for other users on NTFS</li>
   * </ul>
   */
  public static void applyOwnerOnlyPermissions( File file, LogChannelInterface log, String targetDescription ) {
    try {
      Set<PosixFilePermission> permissions = EnumSet.of(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE );
      Files.setPosixFilePermissions( file.toPath(), permissions );
    } catch ( UnsupportedOperationException e ) {
      logIfPermissionChangeFailed( file.setReadable( false, false ), log,
        message( "CliFileSupport.Operation.ClearWorldReadable" ), targetDescription );
      logIfPermissionChangeFailed( file.setWritable( false, false ), log,
        message( "CliFileSupport.Operation.ClearWorldWritable" ), targetDescription );
      logIfPermissionChangeFailed( file.setExecutable( false, false ), log,
        message( "CliFileSupport.Operation.ClearExecutable" ), targetDescription );
      logIfPermissionChangeFailed( file.setReadable( true, true ), log,
        message( "CliFileSupport.Operation.SetOwnerReadable" ), targetDescription );
      logIfPermissionChangeFailed( file.setWritable( true, true ), log,
        message( "CliFileSupport.Operation.SetOwnerWritable" ), targetDescription );
    } catch ( IOException e ) {
      logDebug( log, message( "CliFileSupport.CouldNotSetRestrictivePermissions",
        targetDescription, e.getMessage() ) );
    }
  }

  public static void applyOwnerOnlyPermissions( File file, String targetDescription ) {
    applyOwnerOnlyPermissions( file, null, targetDescription );
  }

  public static void deleteIfExists( File file, LogChannelInterface log, String targetDescription ) {
    try {
      Files.deleteIfExists( file.toPath() );
    } catch ( IOException e ) {
      logDebug( log, message( "CliFileSupport.CouldNotDelete",
        targetDescription, e.getMessage() ) );
    }
  }

  private static void logIfPermissionChangeFailed( boolean changed, LogChannelInterface log,
                                                   String operation, String targetDescription ) {
    if ( !changed ) {
      logDebug( log, message( "CliFileSupport.CouldNotApplyOperation", operation, targetDescription ) );
    }
  }

  private static void logDebug( LogChannelInterface log, String message ) {
    if ( log != null && log.isDebug() ) {
      log.logDebug( message );
    }
  }
}
