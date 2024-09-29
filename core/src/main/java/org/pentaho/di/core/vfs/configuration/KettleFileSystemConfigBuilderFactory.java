/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
