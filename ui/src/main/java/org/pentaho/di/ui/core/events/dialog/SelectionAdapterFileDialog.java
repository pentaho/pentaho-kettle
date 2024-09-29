/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.core.events.dialog;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.vfs.provider.ConnectionFileProvider;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.events.dialog.extension.ExtensionPointWrapper;
import org.pentaho.di.ui.core.events.dialog.extension.SpoonOpenExtensionPointWrapper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This adapter class opens up the VFS file dialog, where the primary goal is to select a file.
 *
 * This adapter class provides default implementations for the methods described by the SelectionListener interface.
 *
 * Classes that wish to deal with SelectionEvents can extend this class and override only the methods which they are
 * interested in.
 *
 * example use:
 *
 *  Dialog.java
 *
 * wbFilename.addSelectionListener(  new SelectionAdapterFileDialogTextVar( log, wFilename, transMeta, new SelectionAdapterOptions(
 * SelectionOperation.FILE, new FilterType[] { FilterType.TXT, FilterType.CSV, FilterType.ALL }, FilterType.TXT ) ) );
 *
 *
 * Side effect:
 *  wFilename#setText() will be called with chosen file.
 *
 */
public abstract class SelectionAdapterFileDialog<T> extends SelectionAdapter {
  private final LogChannelInterface log;
  private final T textWidget;
  private final AbstractMeta meta;
  private final RepositoryUtility repositoryUtility;
  private final ExtensionPointWrapper extensionPointWrapper;
  private SelectionAdapterOptions options;

  public SelectionAdapterFileDialog( LogChannelInterface log, T textWidget, AbstractMeta meta,
                                     SelectionAdapterOptions options, RepositoryUtility repositoryUtility,
                                     ExtensionPointWrapper extensionPointWrapper  ) {
    this.log = log;
    this.textWidget = textWidget;
    this.meta = meta;
    this.options = options;
    this.repositoryUtility = repositoryUtility;
    this.extensionPointWrapper = extensionPointWrapper;
  }

  public SelectionAdapterFileDialog( LogChannelInterface log, T textWidget, AbstractMeta meta,
                                     SelectionAdapterOptions options ) {
    this( log, textWidget, meta, options, new RepositoryUtility(), new SpoonOpenExtensionPointWrapper() );
  }

  @Override
  public void widgetSelected( SelectionEvent selectionEvent ) {
    super.widgetSelected( selectionEvent );
    widgetSelectedHelper( );
  }

  /**
   * Get underlying widget that will get text assigned.
   * @return
   */
  public T getTextWidget() {
    return this.textWidget;
  }

  /**
   * Get text of widget.
   * @return text from widget.
   */
  protected abstract String getText();

  /**
   * Set text for widget.
   * @param text text to be set
   */
  protected abstract void setText( String text );

  /**
   * Setter for SelectionAdapterOptions
   * @param options
   */
  void setSelectionOptions( SelectionAdapterOptions options ) {
    this.options = options;
  }

  /**
   * Getter for SelectionAdapterOptions
   * @return
   */
  public SelectionAdapterOptions getSelectionOptions() {
    return options;
  }


  private void widgetSelectedHelper( ) {
    FileDialogOperation fileDialogOperation;
    String initialFilePath = "";
    FileObject initialFile = null;

    String path = "";
    if ( getText() != null ) {

      try {
        // Attempt to set up initial conditions, if fails browse will fallback to default path.
        initialFilePath = resolveFile( meta, getText() );
        initialFile = KettleVFS.getFileObject( initialFilePath );

        // In cases where filePath is empty set it to the default path returned by kettleVFS.
        if ( Utils.isEmpty( initialFilePath ) && initialFile != null ) {
          initialFilePath = initialFile.getName().getPath();
        }
      } catch ( Exception e ) {
        log.logError( "Error in widgetSelectedHelper", e );
        initialFile = null;
      }

      try {
        fileDialogOperation = constructFileDialogOperation( options.getSelectionOperation(), initialFilePath,
          initialFile, options.getFilters(), options.getProviderFilters(), options.getConnectionFilters() );

        // open dialog
        extensionPointWrapper.callExtensionPoint( log, KettleExtensionPoint.SpoonOpenSaveNew.id, fileDialogOperation );

        // grab path
        path = constructPath( fileDialogOperation );

      } catch ( KettleException ke ) {
        log.logError( "Error in widgetSelectedHelper", ke );
      }
    }

    if ( !Utils.isEmpty( path ) ) {
      setText( path );
    }
  }

  private FileDialogOperation constructFileDialogOperation( SelectionOperation selectionOperation, String initialFilePath,
                                                              FileObject initialFile, String[] filter,
                                                              String[] providerFilters, List<ConnectionFilterType> connectionFilterTypes ) {
    FileDialogOperation fileDialogOperation = createFileDialogOperation( selectionOperation );

    setProviderFilters( fileDialogOperation, providerFilters );
    setProvider( fileDialogOperation, initialFile );

    String connectionFilter = connectionFilterTypes.stream()
      .map( Enum::toString ).collect( Collectors.joining( "," ) );
    fileDialogOperation.setConnectionTypeFilter( connectionFilter );

    try {
      if ( initialFile != null && initialFile.exists() ) {
        // Attempt to set path and dir based on File object
        setPath( fileDialogOperation, initialFile, initialFilePath );
        setFilename( fileDialogOperation, initialFile );
        setStartDir( fileDialogOperation, initialFile, initialFilePath );
      }
    } catch ( Exception e  ) {
      fileDialogOperation.setPath( null );
      fileDialogOperation.setFilename( null );
      fileDialogOperation.setStartDir( null );
    }

    if ( fileDialogOperation.getPath() == null && fileDialogOperation.getStartDir() == null ) {
      // Otherwise fallback to using original path
      fileDialogOperation.setPath( initialFilePath );
    }

    setFilter( fileDialogOperation, filter );
    fileDialogOperation.setDefaultFilter( options.getDefaultFilter() );
    fileDialogOperation.setUseSchemaPath( options.getUseSchemaPath() );

    return fileDialogOperation;
  }

  protected  String resolveFile( AbstractMeta abstractMeta, String unresolvedPath ) {
    return abstractMeta.environmentSubstitute( unresolvedPath );
  }

  FileDialogOperation createFileDialogOperation( SelectionOperation selectionOperation ) {

    switch ( selectionOperation ) {
      case FILE:
        return new FileDialogOperation( FileDialogOperation.SELECT_FILE, FileDialogOperation.ORIGIN_SPOON );
      case FOLDER:
        return new FileDialogOperation( FileDialogOperation.SELECT_FOLDER, FileDialogOperation.ORIGIN_SPOON );
      case FILE_OR_FOLDER:
        return new FileDialogOperation( FileDialogOperation.SELECT_FILE_FOLDER, FileDialogOperation.ORIGIN_SPOON );
      case SAVE:
        return new FileDialogOperation( FileDialogOperation.SAVE, FileDialogOperation.ORIGIN_SPOON );
      case SAVE_TO:
        return new FileDialogOperation( FileDialogOperation.SAVE_TO, FileDialogOperation.ORIGIN_SPOON );
      case SAVE_TO_FILE_FOLDER:
        return new FileDialogOperation( FileDialogOperation.SAVE_TO_FILE_FOLDER, FileDialogOperation.ORIGIN_SPOON );
      case OPEN:
        return new FileDialogOperation( FileDialogOperation.OPEN, FileDialogOperation.ORIGIN_SPOON );
      default:
        throw new IllegalArgumentException( "Unexpected SelectionOperation: " + selectionOperation );
    }

  }

  void setPath( FileDialogOperation fileDialogOperation, FileObject fileObject, String filePath ) throws KettleException {
    try {
      fileDialogOperation.setPath( fileObject.isFile() ? filePath : null  );
    } catch ( FileSystemException fse ) {
      throw new KettleException( "failed to check isFile in setPath()", fse );
    }
  }

  void setFilename( FileDialogOperation fileDialogOperation, FileObject fileObject ) throws KettleException {
    try {
      fileDialogOperation.setFilename( fileObject.isFile() ? fileObject.getName().getBaseName() : null );
    } catch ( FileSystemException fse ) {
      throw new KettleException( "failed to check isFile in setFilename()", fse );
    }
  }

  void setStartDir( FileDialogOperation fileDialogOperation, FileObject fileObject, String filePath ) throws KettleException {
    try {
      fileDialogOperation.setStartDir( fileObject.isFolder() ? filePath : null );
    } catch ( FileSystemException fse ) {
      throw new KettleException( "failed to check isFile in setStartDir()", fse );
    }
  }

  void setProvider( FileDialogOperation op, FileObject initalFile ) {
    if ( op.getProviderFilter() == null ) {
      if ( isConnectedToRepository() ) {
        op.setProvider( ProviderFilterType.REPOSITORY.toString() );
      } else if ( isConnectionFile( initalFile ) ) {
        op.setProvider( ProviderFilterType.VFS.toString() );
      } else if ( "hc".equalsIgnoreCase( initalFile.getURI().getScheme() ) ) {
        op.setProvider( ProviderFilterType.CLUSTERS.toString() );
      } else {
        op.setProvider( ProviderFilterType.LOCAL.toString() );
      }
    } else if ( op.getProviderFilter().equalsIgnoreCase( ProviderFilterType.DEFAULT.toString() ) ) {
      if ( isConnectionFile( initalFile ) ) {
        op.setProvider( ProviderFilterType.VFS.toString() );
      } else if ( "hc".equalsIgnoreCase( initalFile.getURI().getScheme() ) ) {
        op.setProvider( ProviderFilterType.CLUSTERS.toString() );
      } else {
        op.setProvider( ProviderFilterType.LOCAL.toString() );
      }
    }
  }

  /**
   * Determines if a <code>fileObject</code> is of type {@value ConnectionFileProvider#SCHEME}
   * @param fileObject
   * @return if connection file type, false otherwise.
   */
  protected boolean isConnectionFile( FileObject fileObject ) {
    return fileObject != null && fileObject.getURI() != null
        && ConnectionFileProvider.SCHEME.equalsIgnoreCase( fileObject.getURI().getScheme() );
  }

  /**
   * Helper function for {@link FileDialogOperation#setFilter} . Blank entries in <code>filters</code> will be removed.
   * If an "blank" array is entered, the less restrictive filters option {FilterType.ALL} will be applied.
   * @param fileDialogOperation
   * @param filters
   */
  protected void setFilter( FileDialogOperation fileDialogOperation, String[] filters ) {
    String[] cleanedFilters = cleanFilters( filters );
    String filterString = ArrayUtils.isEmpty( cleanedFilters )
        ? FilterType.ALL.toString() // least restrictive option
        : String.join( ",", cleanedFilters );

    fileDialogOperation.setFilter( filterString );
  }

  /**
   * Helper function for {@link FileDialogOperation#setProviderFilter} . Blank entries in <code>setProviderFilter</code> will be removed.
   * If a "blank" array is entered, the less restrictive filters option {ProviderFilterType.ALL_PROVIDERS} will be applied.
   * @param fileDialogOperation - FileDialogOperation
   * @param providerFilters - Array of providerFilters
   */
  private void setProviderFilters( FileDialogOperation fileDialogOperation, String[] providerFilters ) {
    String[] cleanedFilters = cleanFilters( providerFilters );
    String providerFilterString = ArrayUtils.isEmpty( cleanedFilters )
      ? ProviderFilterType.DEFAULT.toString()
      : String.join( ",", cleanedFilters );

    fileDialogOperation.setProviderFilter( providerFilterString );
  }

  /**
   * Remove "blank" items such as empty, null or whitespace items in <code>filters</code>
   * @param filters
   * @return non "blank" array.
   */
   String[] cleanFilters( String[] filters ) {
    return !ArrayUtils.isEmpty( filters )
        ? Arrays.asList( filters ).stream().filter( f -> !StringUtils.isBlank( f ) ).toArray( String[]::new )
        : null;
  }

  /**
   * Determine if connected to repository.
   * @return true if connected, false otherwise.
   */
  boolean isConnectedToRepository() {
    return this.repositoryUtility.isConnectedToRepository();
  }

  private String constructPath( FileDialogOperation fileDialogOperation ) {
    if ( Utils.isEmpty( fileDialogOperation.getProvider() ) ) {
      return null;
    }
    try {
      String result;

      if ( fileDialogOperation.isProviderRepository() ) {
        result = getRepositoryFilePath( fileDialogOperation );
      } else {
        if ( fileDialogOperation.isSaveCommand() && !Utils.isEmpty( fileDialogOperation.getFilename() ) ) {
          String sep = isUrl( fileDialogOperation.getPath() ) ? "/" : File.separator;
          result = fileDialogOperation.getPath() + sep + fileDialogOperation.getFilename();
        } else {
          result = fileDialogOperation.getPath();
        }
      }

      return applyRelativePathEnvVar( result );
    } catch ( Exception e ) {
      return null;
    }

  }

  /**
   * Helper method which applies the relative environment var of the current dir if applies to a given path.
   * @param path string path to be updated
   * @return updated path
   */
  String applyRelativePathEnvVar( String path ) {
    String parentFolder = null;

    if ( !Utils.isEmpty( path ) ) {

      // Based on the working environment determine the parent path to compare too.
      if ( isConnectedToRepository() ) {
        String parentPath =
          meta.getRepositoryDirectory().getParent() == null ? "" : meta.getRepositoryDirectory().getPath();
        return replaceCurrentDir( path.replace( '\\', '/' ), parentPath );
      }

      // Attempt to match using current file as is
      String currentFile = meta.environmentSubstitute( meta.getFilename() );
      try {
        parentFolder = ( new File( currentFile ) ).getParent();
        parentFolder = ( path.startsWith( parentFolder ) ) ? parentFolder : null;
      } catch ( Exception e ) {
        // Ignore
      }

      // If no match, attempt to compare to kettleVFS object
      if ( Utils.isEmpty( parentFolder ) ) {
        try {
          FileObject parentFileObj = KettleVFS.getFileObject( currentFile ).getParent();

          // Non-local files should include the schema i.e. pvfs://
          parentFolder = ( parentFileObj instanceof LocalFile ) ? parentFileObj.getName().getPath()
            : parentFileObj.toString();

        } catch ( Exception e ) {
          // Ignore
        }
      }
    }
    return replaceCurrentDir( path, parentFolder );
  }

  private static String replaceCurrentDir( String path, String parentPath ) {
    if ( Utils.isEmpty( path ) || Utils.isEmpty( parentPath ) || !path.startsWith( parentPath ) ) {
      return path;
    }

    // Ensure the path is uniform for windows. Path's using the internal variable need to use forward slash
    if ( Const.isWindows() ) {
      path = path.replace( '\\', '/' );
      if ( parentPath != null ) {
        parentPath = parentPath.replace( '\\', '/' );
      } 
    }

    return path.replace( parentPath, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
  }

  String getRepositoryFilePath( FileDialogOperation fileDialogOperation ) {
    return getRepositoryFilePath( (RepositoryElementMetaInterface) fileDialogOperation.getRepositoryObject() );
  }

  /**
   * construct <code>repositoryElementMeta</code> path. Similar to java.io.File method 'getPath()'
   * @param repositoryElementMeta
   * @return
   */
  private String getRepositoryFilePath( RepositoryElementMetaInterface repositoryElementMeta ) {
    return concat( repositoryElementMeta.getRepositoryDirectory().getPath(), repositoryElementMeta.getName() );
  }

  protected boolean isUrl( String path ) {
    return path.matches( "^[\\w]+://.*" );
  }

  protected String concat( String path, String name ) {
    return FilenameUtils.concat( path, name );
  }

}
