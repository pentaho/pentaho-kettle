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

package org.pentaho.di.core.fileinput;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class FileInputList {
  private List<FileObject> files = new ArrayList<>();
  private List<FileObject> nonExistantFiles = new ArrayList<>( 1 );
  private List<FileObject> nonAccessibleFiles = new ArrayList<>( 1 );

  private static LogChannelInterface log = new LogChannel( "FileInputList" );

  public enum FileTypeFilter {
    FILES_AND_FOLDERS( "all_files", FileType.FILE, FileType.FOLDER ), ONLY_FILES( "only_files", FileType.FILE ),
    ONLY_FOLDERS( "only_folders", FileType.FOLDER );

    private String name;
    private final Collection<FileType> allowedFileTypes;

    private FileTypeFilter( String name, FileType... allowedFileTypes ) {
      this.name = name;
      this.allowedFileTypes = Collections.unmodifiableCollection( Arrays.asList( allowedFileTypes ) );
    }

    public boolean isFileTypeAllowed( FileType fileType ) {
      return allowedFileTypes.contains( fileType );
    }

    @Override
    public String toString() {
      return name;
    }

    public static FileTypeFilter getByOrdinal( int ordinal ) {
      for ( FileTypeFilter filter : FileTypeFilter.values() ) {
        if ( filter.ordinal() == ordinal ) {
          return filter;
        }
      }
      return ONLY_FILES;
    }

    public static FileTypeFilter getByName( String name ) {
      for ( FileTypeFilter filter : FileTypeFilter.values() ) {
        if ( filter.name.equals( name ) ) {
          return filter;
        }
      }
      return ONLY_FILES;
    }
  }

  private static final String YES = "Y";

  public static String getRequiredFilesDescription( List<FileObject> nonExistantFiles ) {
    StringBuilder buffer = new StringBuilder();
    for ( Iterator<FileObject> iter = nonExistantFiles.iterator(); iter.hasNext(); ) {
      FileObject file = iter.next();
      buffer.append( Const.optionallyDecodeUriString( file.getName().getURI() ) );
      buffer.append( Const.CR );
    }
    return buffer.toString();
  }


  private static boolean[] includeSubdirsFalse( int iLength ) {
    boolean[] includeSubdirs = new boolean[ iLength ];
    for ( int i = 0; i < iLength; i++ ) {
      includeSubdirs[ i ] = false;
    }
    return includeSubdirs;
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static String[] createFilePathList( VariableSpace space, String[] fileName, String[] fileMask,
                                             String[] excludeFileMask, String[] fileRequired ) {
    return createFilePathList( DefaultBowl.getInstance(), space, fileName, fileMask, excludeFileMask, fileRequired );
  }

  public static String[] createFilePathList( Bowl bowl, VariableSpace space, String[] fileName, String[] fileMask,
                                             String[] excludeFileMask, String[] fileRequired ) {
    boolean[] includeSubdirs = includeSubdirsFalse( fileName.length );
    return createFilePathList( bowl, space, fileName, fileMask, excludeFileMask, fileRequired, null );
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static String[] createFilePathList( VariableSpace space, String[] fileName, String[] fileMask,
                                             String[] excludeFileMask, String[] fileRequired,
                                             boolean[] includeSubdirs ) {
    return createFilePathList( DefaultBowl.getInstance(), space, fileName, fileMask, excludeFileMask, fileRequired,
                               includeSubdirs, null );
  }

  public static String[] createFilePathList( Bowl bowl, VariableSpace space, String[] fileName, String[] fileMask,
                                             String[] excludeFileMask, String[] fileRequired,
                                             boolean[] includeSubdirs ) {
    return createFilePathList( bowl, space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, null );
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static String[] createFilePathList( VariableSpace space, String[] fileName, String[] fileMask,
                                             String[] excludeFileMask, String[] fileRequired, boolean[] includeSubdirs,
                                             FileTypeFilter[] filters ) {
    return createFilePathList( DefaultBowl.getInstance(), space, fileName, fileMask, excludeFileMask, fileRequired,
                               includeSubdirs, filters );
  }

  public static String[] createFilePathList( Bowl bowl, VariableSpace space, String[] fileName, String[] fileMask,
                                             String[] excludeFileMask, String[] fileRequired, boolean[] includeSubdirs,
                                             FileTypeFilter[] filters ) {
    List<FileObject> fileList =
      createFileList( bowl, space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, filters )
        .getFiles();
    String[] filePaths = new String[ fileList.size() ];
    for ( int i = 0; i < filePaths.length; i++ ) {
      filePaths[ i ] = Const.optionallyDecodeUriString( fileList.get( i ).getName().getURI() );
    }
    return filePaths;
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static FileInputList createFileList( VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] excludeFileMask, String[] fileRequired ) {
    return createFileList( DefaultBowl.getInstance(), space, fileName, fileMask, excludeFileMask, fileRequired );
  }

  public static FileInputList createFileList( Bowl bowl, VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] excludeFileMask, String[] fileRequired ) {
    boolean[] includeSubdirs = includeSubdirsFalse( fileName.length );
    return createFileList( bowl, space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, null );
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static FileInputList createFileList( VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] excludeFileMask, String[] fileRequired,
                                              boolean[] includeSubdirs ) {
    return createFileList( DefaultBowl.getInstance(), space, fileName, fileMask, excludeFileMask, fileRequired,
                           includeSubdirs );
  }

  public static FileInputList createFileList( Bowl bowl, VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] excludeFileMask, String[] fileRequired,
                                              boolean[] includeSubdirs ) {
    return createFileList( bowl, space, fileName, fileMask, excludeFileMask, fileRequired, includeSubdirs, null );
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static FileInputList createFileList( VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] excludeFileMask, String[] fileRequired, boolean[] includeSubdirs,
                                              FileTypeFilter[] fileTypeFilters ) {
    return createFileList( DefaultBowl.getInstance(), space, fileName, fileMask, excludeFileMask, fileRequired,
                           includeSubdirs, fileTypeFilters );
  }

  public static FileInputList createFileList( Bowl bowl, VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] excludeFileMask, String[] fileRequired, boolean[] includeSubdirs,
                                              FileTypeFilter[] fileTypeFilters ) {
    FileInputList fileInputList = new FileInputList();

    // Replace possible environment variables...
    final String[] realfile = space.environmentSubstitute( fileName );
    final String[] realmask = space.environmentSubstitute( fileMask );
    final String[] realExcludeMask = space.environmentSubstitute( excludeFileMask );

    for ( int i = 0; i < realfile.length; i++ ) {
      final String onefile = realfile[ i ];
      final String onemask = realmask[ i ];
      final String excludeonemask = realExcludeMask[ i ];
      final boolean onerequired = YES.equalsIgnoreCase( fileRequired[ i ] );
      final boolean subdirs = includeSubdirs[ i ];
      final FileTypeFilter filter =
        ( ( fileTypeFilters == null || fileTypeFilters[ i ] == null )
          ? FileTypeFilter.ONLY_FILES : fileTypeFilters[ i ] );

      if ( Utils.isEmpty( onefile ) ) {
        continue;
      }

      try {
        FileObject directoryFileObject = KettleVFS.getInstance( bowl ).getFileObject( onefile, space );
        boolean processFolder = true;
        if ( onerequired ) {
          if ( !directoryFileObject.exists() ) {
            // if we don't find folder..no need to continue
            fileInputList.addNonExistantFile( directoryFileObject );
            processFolder = false;
          } else {
            if ( !directoryFileObject.isReadable() ) {
              fileInputList.addNonAccessibleFile( directoryFileObject );
              processFolder = false;
            }
          }
        }

        // Find all file names that match the wildcard in this directory
        //
        if ( processFolder ) {
          if ( directoryFileObject != null && directoryFileObject.getType() == FileType.FOLDER ) { // it's a directory
            FileObject[] fileObjects = directoryFileObject.findFiles( new AllFileSelector() {
              @Override
              public boolean traverseDescendents( FileSelectInfo info ) {
                return info.getDepth() == 0 || subdirs;
              }

              @Override
              public boolean includeFile( FileSelectInfo info ) {
                // Never return the parent directory of a file list.
                if ( info.getDepth() == 0 ) {
                  return false;
                }

                FileObject fileObject = info.getFile();
                try {
                  if ( fileObject != null && filter.isFileTypeAllowed( fileObject.getType() ) ) {
                    String name = info.getFile().getName().getBaseName();
                    boolean matches = true;
                    if ( !Utils.isEmpty( onemask ) ) {
                      matches = Pattern.matches( onemask, name );
                    }
                    boolean excludematches = false;
                    if ( !Utils.isEmpty( excludeonemask ) ) {
                      excludematches = Pattern.matches( excludeonemask, name );
                    }
                    return ( matches && !excludematches );
                  }
                  return false;
                } catch ( IOException ex ) {
                  // Upon error don't process the file.
                  return false;
                }
              }
            } );
            if ( fileObjects != null ) {
              for ( int j = 0; j < fileObjects.length; j++ ) {
                FileObject fileObject = fileObjects[ j ];
                if ( fileObject.exists() ) {
                  fileInputList.addFile( fileObject );
                }
              }
            }
            if ( Utils.isEmpty( fileObjects ) ) {
              if ( onerequired ) {
                fileInputList.addNonAccessibleFile( directoryFileObject );
              }
            }

            // Sort the list: quicksort, only for regular files
            fileInputList.sortFiles();
          } else if ( directoryFileObject instanceof CompressedFileFileObject ) {
            FileObject[] children = directoryFileObject.getChildren();
            for ( int j = 0; j < children.length; j++ ) {
              // See if the wildcard (regexp) matches...
              String name = children[ j ].getName().getBaseName();
              boolean matches = true;
              if ( !Utils.isEmpty( onemask ) ) {
                matches = Pattern.matches( onemask, name );
              }
              boolean excludematches = false;
              if ( !Utils.isEmpty( excludeonemask ) ) {
                excludematches = Pattern.matches( excludeonemask, name );
              }
              if ( matches && !excludematches ) {
                fileInputList.addFile( children[ j ] );
              }

            }
            // We don't sort here, keep the order of the files in the archive.
          } else {
            FileObject fileObject = KettleVFS.getInstance( bowl ).getFileObject( onefile, space );
            if ( fileObject.exists() ) {
              if ( fileObject.isReadable() ) {
                fileInputList.addFile( fileObject );
              } else {
                if ( onerequired ) {
                  fileInputList.addNonAccessibleFile( fileObject );
                }
              }
            } else {
              if ( onerequired ) {
                fileInputList.addNonExistantFile( fileObject );
              }
            }
          }
        }
      } catch ( Exception e ) {
        if ( onerequired ) {
          fileInputList.addNonAccessibleFile( new NonAccessibleFileObject( onefile ) );
        }
        log.logError( Const.getStackTracker( e ) );
      }
    }

    return fileInputList;
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static FileInputList createFolderList( VariableSpace space, String[] folderName,
                                                String[] folderRequired ) {
    return createFolderList( DefaultBowl.getInstance(), space, folderName, folderRequired );
  }

  public static FileInputList createFolderList( Bowl bowl, VariableSpace space, String[] folderName,
                                                String[] folderRequired ) {
    FileInputList fileInputList = new FileInputList();

    // Replace possible environment variables...
    final String[] realfolder = space.environmentSubstitute( folderName );

    for ( int i = 0; i < realfolder.length; i++ ) {
      final String onefile = realfolder[ i ];
      final boolean onerequired = YES.equalsIgnoreCase( folderRequired[ i ] );
      final boolean subdirs = true;
      final FileTypeFilter filter = FileTypeFilter.ONLY_FOLDERS;

      if ( Utils.isEmpty( onefile ) ) {
        continue;
      }
      FileObject directoryFileObject = null;

      try {
        // Find all folder names in this directory
        //
        directoryFileObject = KettleVFS.getInstance( bowl ).getFileObject( onefile, space );
        if ( directoryFileObject != null && directoryFileObject.getType() == FileType.FOLDER ) { // it's a directory
          FileObject[] fileObjects = directoryFileObject.findFiles( new AllFileSelector() {
            @Override
            public boolean traverseDescendents( FileSelectInfo info ) {
              return ( info.getDepth() == 0 || subdirs )
                // Check if one has permission to list this folder
                && hasAccess( info.getFile() );
            }

            private boolean hasAccess( FileObject fileObject ) {
              try {
                if ( fileObject instanceof LocalFile ) {
                  // fileObject.isReadable wrongly returns true in windows file system even if not readable
                  return Files.isReadable( Paths.get( ( new File( fileObject.getPath().toString() ) ).toURI() ) );
                }
                return fileObject.isReadable();
              } catch ( FileSystemException e ) {
                // Something went wrong... well, let's assume "no access"!
                return false;
              }
            }

            @Override
            public boolean includeFile( FileSelectInfo info ) {
              // Never return the parent directory of a file list.
              if ( info.getDepth() == 0 ) {
                return false;
              }

              FileObject fileObject = info.getFile();
              try {
                return ( fileObject != null
                  // Is this an allowed type?
                  && filter.isFileTypeAllowed( fileObject.getType() )
                  // Check if one has permission to access it
                  && hasAccess( fileObject ) );
              } catch ( IOException ex ) {
                // Upon error don't process the file.
                return false;
              }
            }
          } );
          if ( fileObjects != null ) {
            for ( int j = 0; j < fileObjects.length; j++ ) {
              if ( fileObjects[ j ].exists() ) {
                fileInputList.addFile( fileObjects[ j ] );
              }
            }
          }
          if ( Utils.isEmpty( fileObjects ) ) {
            if ( onerequired ) {
              fileInputList.addNonAccessibleFile( directoryFileObject );
            }
          }

          // Sort the list: quicksort, only for regular files
          fileInputList.sortFiles();
        } else {
          if ( onerequired && !directoryFileObject.exists() ) {
            fileInputList.addNonExistantFile( directoryFileObject );
          }
        }
      } catch ( Exception e ) {
        log.logError( Const.getStackTracker( e ) );
      } finally {
        try {
          if ( directoryFileObject != null ) {
            directoryFileObject.close();
          }
          directoryFileObject = null;
        } catch ( Exception e ) {
          // Ignore
        }
      }
    }

    return fileInputList;
  }

  public List<FileObject> getFiles() {
    return files;
  }

  public String[] getFileStrings() {
    String[] fileStrings = new String[ files.size() ];
    for ( int i = 0; i < fileStrings.length; i++ ) {
      fileStrings[ i ] = KettleVFS.getFilename( files.get( i ) );
    }
    return fileStrings;
  }

  public String[] getUrlStrings() {
    String[] fileStrings = new String[ files.size() ];
    for ( int i = 0; i < fileStrings.length; i++ ) {
      fileStrings[ i ] = Const.optionallyDecodeUriString( files.get( i ).getPublicURIString() );
    }
    return fileStrings;
  }

  public List<FileObject> getNonAccessibleFiles() {
    return nonAccessibleFiles;
  }

  public List<FileObject> getNonExistantFiles() {
    return nonExistantFiles;
  }

  public void addFile( FileObject file ) {
    files.add( file );
  }

  public void addNonAccessibleFile( FileObject file ) {
    nonAccessibleFiles.add( file );
  }

  public void addNonExistantFile( FileObject file ) {
    nonExistantFiles.add( file );
  }

  public void sortFiles() {
    Collections.sort( files, KettleVFS.getComparator() );
    Collections.sort( nonAccessibleFiles, KettleVFS.getComparator() );
    Collections.sort( nonExistantFiles, KettleVFS.getComparator() );
  }

  public FileObject getFile( int i ) {
    return files.get( i );
  }

  public int nrOfFiles() {
    return files.size();
  }

  public int nrOfMissingFiles() {
    return nonAccessibleFiles.size() + nonExistantFiles.size();
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static FileInputList createFileList( VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] fileRequired, boolean[] includeSubdirs ) {
    return createFileList( DefaultBowl.getInstance(), space, fileName, fileMask, fileRequired, includeSubdirs );
  }

  public static FileInputList createFileList( Bowl bowl, VariableSpace space, String[] fileName, String[] fileMask,
                                              String[] fileRequired, boolean[] includeSubdirs ) {
    return createFileList(
      bowl, space, fileName, fileMask, new String[ fileName.length ], fileRequired, includeSubdirs, null );
  }

  /**
   * @deprecated, use the version with the Bowl
   */
  @Deprecated
  public static String[] createFilePathList( VariableSpace space, String[] fileName, String[] fileMask,
                                             String[] fileRequired ) {
    return createFilePathList( DefaultBowl.getInstance(), space, fileName, fileMask, fileRequired );
  }

  public static String[] createFilePathList( Bowl bowl, VariableSpace space, String[] fileName, String[] fileMask,
                                             String[] fileRequired ) {
    boolean[] includeSubdirs = includeSubdirsFalse( fileName.length );
    return createFilePathList(
      bowl, space, fileName, fileMask, new String[ fileName.length ], fileRequired, includeSubdirs, null );
  }
}
