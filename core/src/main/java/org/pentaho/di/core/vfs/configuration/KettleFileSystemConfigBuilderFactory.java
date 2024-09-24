/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.vfs.configuration;

import java.io.IOException;
import java.lang.reflect.Method;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

/**
 * This class supports overriding of config builders by supplying a VariableSpace containing a variable in the format of
 * vfs.[scheme].config.parser where [scheme] is one of the VFS schemes (file, http, sftp, etc...)
 *
 * @author cboyden
 */
public class KettleFileSystemConfigBuilderFactory {

  private static Class<?> PKG = KettleVFS.class; // for i18n purposes, needed by Translator2!!

  /**
   * This factory returns a FileSystemConfigBuilder. Custom FileSystemConfigBuilders can be created by implementing the
   * {@link IKettleFileSystemConfigBuilder} or overriding the {@link KettleGenericFileSystemConfigBuilder}
   *
   * @see org.apache.commons.vfs.FileSystemConfigBuilder
   *
   * @param varSpace
   *          A Kettle variable space for resolving VFS config parameters
   * @param scheme
   *          The VFS scheme (FILE, HTTP, SFTP, etc...)
   * @return A FileSystemConfigBuilder that can translate Kettle variables into VFS config parameters
   * @throws IOException
   */
  public static IKettleFileSystemConfigBuilder getConfigBuilder( VariableSpace varSpace, String scheme ) throws IOException {
    IKettleFileSystemConfigBuilder result = null;

    // Attempt to load the Config Builder from a variable: vfs.config.parser = class
    String parserClass = varSpace.getVariable( "vfs." + scheme + ".config.parser" );

    if ( parserClass != null ) {
      try {
        Class<?> configBuilderClass =
          KettleFileSystemConfigBuilderFactory.class.getClassLoader().loadClass( parserClass );
        Method mGetInstance = configBuilderClass.getMethod( "getInstance" );
        if ( ( mGetInstance != null )
          && ( IKettleFileSystemConfigBuilder.class.isAssignableFrom( mGetInstance.getReturnType() ) ) ) {
          result = (IKettleFileSystemConfigBuilder) mGetInstance.invoke( null );
        } else {
          result = (IKettleFileSystemConfigBuilder) configBuilderClass.newInstance();
        }
      } catch ( Exception e ) {
        // Failed to load custom parser. Throw exception.
        throw new IOException( BaseMessages.getString( PKG, "CustomVfsSettingsParser.Log.FailedToLoad" ) );
      }
    } else {
      // No custom parser requested, load default
      if ( scheme.equalsIgnoreCase( "sftp" ) ) {
        result = KettleSftpFileSystemConfigBuilder.getInstance();
      } else {
        result = KettleGenericFileSystemConfigBuilder.getInstance();
      }
    }

    return result;
  }

}
