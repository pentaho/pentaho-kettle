/*
 * Copyright 2017-2024 Hitachi Vantara. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */

package org.pentaho.di.ui.core;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryObjectInterface;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by bmorrise on 8/17/17.
 */
public class FileDialogOperation {


  /** Will be called whenever files are loaded by this dialog */
  public interface FileLoadListener {
    void onFileLoaded( FileLookupInfo file );
  }

  /** File information and lookup */
  public interface FileLookupInfo {
    /** The full path of the file */
    String getPath();
    String getName();

    boolean isFolder();

    /** Lookup children for a folder */
    boolean hasChildFile( String folderPath );
  }

  /** A provider for additional images, to be applied based on path */
  public interface CustomImageProvider {
    /** identifier -> custom image */
    Map<String, CustomImage> getCustomImages();

    /** @return identifier of custom image to use for that file */
    Optional<String> getImage( String path );
  }

  /** An image location. If a classloader is supplied, paths will be resolved within it. */
  public interface CustomImage {
    String getPath();
    Optional<ClassLoader> getClassLoader();
  }

  public static final String SELECT_FOLDER = "selectFolder";
  public static final String SELECT_FILE = "selectFile";
  public static final String SELECT_FILE_FOLDER = "selectFileFolder";
  public static final String OPEN = "open";
  public static final String SAVE = "save";
  public static final String SAVE_AS = "saveAs";
  public static final String SAVE_TO = "saveTo";
  public static final String SAVE_TO_FILE_FOLDER = "saveToFileFolder";

  public static final String EXPORT = "export";

  public static final String EXPORT_ALL = "exportAll";

  public static final String IMPORT = "import";
  public static final String ORIGIN_SPOON = "spoon";
  public static final String ORIGIN_OTHER = "other";
  public static final String TRANSFORMATION = "transformation";
  public static final String JOB = "job";
  public static final String PROVIDER_REPO = "repository";

  private Repository repository;
  private String command;
  private String filter;
  private String defaultFilter;
  private String origin;
  private RepositoryObjectInterface repositoryObject;
  private String startDir;
  private String title;
  private String filename;
  private String fileType;
  private String path;
  private String connection;
  private String provider;
  private String providerFilter;
  private String connectionTypeFilter;
  private boolean useSchemaPath;

  private Optional<CustomImageProvider> customImageProvider = Optional.empty();
  private Optional<FileLoadListener> fileLoadListener = Optional.empty();
  private boolean showOnlyFolders = false;
  private Predicate<String> openCondition = any -> true;

  public FileDialogOperation( String command ) {
    this.command = command;
  }

  public FileDialogOperation( String command, String origin ) {
    this.command = command;
    this.origin = origin;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand( String command ) {
    this.command = command;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter( String filter ) {
    this.filter = filter;
  }

  public String getDefaultFilter() {
    return defaultFilter;
  }

  public void setDefaultFilter( String defaultFilter ) {
    this.defaultFilter = defaultFilter;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin( String origin ) {
    this.origin = origin;
  }

  public RepositoryObjectInterface getRepositoryObject() {
    return repositoryObject;
  }

  public void setRepositoryObject( RepositoryObjectInterface repositoryObject ) {
    this.repositoryObject = repositoryObject;
  }

  public String getStartDir() {
    return startDir;
  }

  public void setStartDir( String startDir ) {
    this.startDir = startDir;
  }

  public Repository getRepository() {
    return repository;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType( String fileType ) {
    this.fileType = fileType;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public String getConnection() {
    return connection;
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public void setConnection( String connection ) {
    this.connection = connection;
  }

  public boolean getUseSchemaPath() {
    return useSchemaPath;
  }

  public void setUseSchemaPath( boolean useSchemaPath ) {
    this.useSchemaPath = useSchemaPath;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider( String provider ) {
    this.provider = provider;
  }

  public String getProviderFilter() {
    return providerFilter;
  }

  public void setProviderFilter( String providerFilter ) {
    this.providerFilter = providerFilter;
  }

  public String getConnectionTypeFilter() {
    return connectionTypeFilter;
  }

  public void setConnectionTypeFilter( String connectionTypeFilter ) {
    this.connectionTypeFilter = connectionTypeFilter;
  }

  public boolean isProviderRepository() {
    return provider.equalsIgnoreCase( PROVIDER_REPO );
  }

  public boolean isSaveCommand() {
    return ( command.equalsIgnoreCase( SAVE ) || command.equalsIgnoreCase( SAVE_TO )
      || command.equalsIgnoreCase( SAVE_AS ) || command.equalsIgnoreCase( SAVE_TO_FILE_FOLDER )
            || command.equalsIgnoreCase( EXPORT) || command.equalsIgnoreCase( EXPORT_ALL) );
  }

  /** To load additional images and define what files they apply to */
  public void setCustomImageProvider( CustomImageProvider provider ) {
    this.customImageProvider = Optional.of( provider );
  }

  public Optional<CustomImageProvider> getCustomImageProvider() {
    return customImageProvider;
  }

  public Optional<FileLoadListener> getFileLoadListener() {
    return fileLoadListener;
  }

  /** Add a listener to inspect files as they are loaded */
  public void setFileLoadListener( FileLoadListener fileLoadListener ) {
    this.fileLoadListener = Optional.of( fileLoadListener );
  }

  public boolean isShowOnlyFolders() {
    return showOnlyFolders;
  }

  /** Omit any files and show only folders in the dialog */
  public void setShowOnlyFolders( boolean value ) {
    showOnlyFolders = value;
  }

  public Predicate<String> getOpenCondition() {
    return openCondition;
  }

  /** Additional condition for opening a file, based on its path */
  public void setOpenCondition( Predicate<String> value ) {
    openCondition = value;
  }
}
