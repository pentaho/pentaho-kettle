/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.utils.VFSConnectionTestOptions;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static org.pentaho.di.connections.vfs.provider.ConnectionFileObject.DELIMITER;

public abstract class BaseVFSConnectionProvider<T extends VFSConnectionDetails> implements VFSConnectionProvider<T> {

  private Supplier<ConnectionManager> connectionManagerSupplier = ConnectionManager::getInstance;

  private static final Logger LOGGER = LoggerFactory.getLogger( BaseVFSConnectionProvider.class );

  @Override
  public List<String> getNames() {
    return connectionManagerSupplier.get().getNamesByType( getClass() );
  }

  @Override
  public List<T> getConnectionDetails() {
    return getConnectionDetails( connectionManagerSupplier.get() );
  }

  @Override
  public List<String> getNames( ConnectionManager connectionManager ) {
    return connectionManager.getNamesByType( getClass() );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public List<T> getConnectionDetails( ConnectionManager connectionManager ) {
    return (List<T>) connectionManager.getConnectionDetailsByScheme( getKey() );
  }

  @Override public T prepare( T connectionDetails ) throws KettleException {
    return connectionDetails;
  }

  @Override public String sanitizeName( String string ) {
    return string;
  }

  // Utility method to perform variable substitution on values
  protected String getVar( String value, VariableSpace variableSpace ) {
    if ( variableSpace != null ) {
      return variableSpace.environmentSubstitute( value );
    }
    return value;
  }

  // Utility method to derive a boolean checkbox setting that may use variables instead
  protected static boolean getBooleanValueOfVariable( VariableSpace space, String variableName, String defaultValue ) {
    if ( !Utils.isEmpty( variableName ) ) {
      String value = space.environmentSubstitute( variableName );
      if ( !Utils.isEmpty( value ) ) {
        Boolean b = ValueMetaBase.convertStringToBoolean( value );
        return b != null && b;
      }
    }
    return Objects.equals( Boolean.TRUE, ValueMetaBase.convertStringToBoolean( defaultValue ) );
  }

  protected VariableSpace getSpace( ConnectionDetails connectionDetails ) {
    return connectionDetails.getSpace() == null ? Variables.getADefaultVariableSpace() : connectionDetails.getSpace();
  }

  @Override
  public boolean test( @NonNull T connectionDetails, @NonNull VFSConnectionTestOptions connectionTestOptions ) throws KettleException {
    boolean valid = test( connectionDetails );
    if ( !valid ) {
      return false;
    }

    if ( !connectionDetails.isSupportsRootPath()  || connectionTestOptions.isIgnoreRootPath() ) {
      return true;
    }

    String resolvedRootPath = getResolvedRootPath( connectionDetails );
    if ( StringUtils.isEmpty( resolvedRootPath ) ) {
      return !connectionDetails.isRootPathRequired();
    }

    String internalUrl = buildUrl( connectionDetails, resolvedRootPath );
    FileObject fileObject = KettleVFS.getFileObject( internalUrl, new Variables(), getOpts( connectionDetails ) );

    try {
      return fileObject.exists() && this.isFolder( fileObject );
    } catch ( FileSystemException fileSystemException ) {
      LOGGER.error( fileSystemException.getMessage() );
      return false;
    }
  }

  @Override
  public String getResolvedRootPath( @NonNull T connectionDetails ) {
    if ( StringUtils.isNotEmpty( connectionDetails.getRootPath() ) ) {
      VariableSpace space = getSpace( connectionDetails );
      String resolvedRootPath = getVar( connectionDetails.getRootPath(), space );
      if ( StringUtils.isNotBlank( resolvedRootPath ) ) {
        return normalizeRootPath( resolvedRootPath );
      }
    }

    return StringUtils.EMPTY;
  }

  private String normalizeRootPath( String rootPath ) {
    if ( StringUtils.isNotEmpty( rootPath ) ) {
      if ( !rootPath.startsWith( DELIMITER ) ) {
        rootPath = DELIMITER + rootPath;
      }
      if (rootPath.endsWith( DELIMITER ) ) {
        rootPath = rootPath.substring( 0, rootPath.length() - 1 );
      }
    }
    return rootPath;
  }

  private String buildUrl( VFSConnectionDetails connectionDetails, String rootPath ) {
    String domain = connectionDetails.getDomain();
    if ( !domain.isEmpty() ) {
      domain = DELIMITER + domain;
    }
    return connectionDetails.getType() + ":/" + domain + rootPath;
  }

  private boolean isFolder( @NonNull FileObject fileObject ) {
    try {

      return fileObject.getType() != null && fileObject.getType().equals( FileType.FOLDER );
    } catch ( FileSystemException e ) {
      return false;
    }
  }
}

