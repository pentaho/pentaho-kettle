/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.vfs;

import org.apache.commons.vfs2.FileName;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * An interface to a bowl-specific KettleVFS interface. Most, but not all methods from KettleVFS have been moved to this
 * interface. Methods that remain are truly static or use state that must remain global. Use
 * KettleVFS.getInstance( Bowl ) to get an instance.
 *
 */
public interface IKettleVFS {

  FileObject getFileObject( String vfsFilename ) throws KettleFileException;

  FileObject getFileObject( String vfsFilename, VariableSpace space ) throws KettleFileException;

  FileObject getFileObject( String vfsFilename, FileSystemOptions fsOptions ) throws KettleFileException;

  FileObject getFileObject( String vfsFilename, VariableSpace space, FileSystemOptions fsOptions )
    throws KettleFileException;

  /**
   * Resolves the given URI to a file name.
   *
   * @param uri The URI to resolve.
   * @return A {@link FileName} that matches the URI; never {@code null}.
   * @throws KettleFileException if this is not possible.
   */
  FileName resolveURI( String uri ) throws KettleFileException;

  // warning, was not public
  FileSystemOptions getFileSystemOptions( String scheme, String vfsFilename, VariableSpace space,
                                          FileSystemOptions fileSystemOptions )
    throws IOException;

  String getFriendlyURI( String filename );

  String getFriendlyURI( String filename, VariableSpace space );

  /**
   * Read a text file (like an XML document). WARNING DO NOT USE FOR DATA FILES.
   *
   * @param vfsFilename the filename or URL to read from
   * @param charSetName the character set of the string (UTF-8, ISO8859-1, etc)
   * @return The content of the file as a String
   * @deprecated use getInstance( Bowl )
   * @throws IOException
   */
  String getTextFileContent( String vfsFilename, String charSetName ) throws KettleFileException;

  String getTextFileContent( String vfsFilename, VariableSpace space, String charSetName )
    throws KettleFileException;

  boolean fileExists( String vfsFilename ) throws KettleFileException;

  boolean fileExists( String vfsFilename, VariableSpace space ) throws KettleFileException;

  InputStream getInputStream( String vfsFilename ) throws KettleFileException;

  InputStream getInputStream( String vfsFilename, VariableSpace space ) throws KettleFileException;

  OutputStream getOutputStream( FileObject fileObject, boolean append ) throws IOException;

  OutputStream getOutputStream( String vfsFilename, boolean append ) throws KettleFileException;

  OutputStream getOutputStream( String vfsFilename, VariableSpace space, boolean append )
    throws KettleFileException;

  OutputStream getOutputStream( String vfsFilename, VariableSpace space,
                                FileSystemOptions fsOptions, boolean append ) throws KettleFileException;


  /**
   * Creates a file using "java.io.tmpdir" directory
   *
   * @param prefix - file name
   * @param prefix - file extension
   * @return FileObject
   * @throws KettleFileException
   */
  FileObject createTempFile( String prefix, KettleVFS.Suffix suffix ) throws KettleFileException;

  /**
   * Creates a file using "java.io.tmpdir" directory
   *
   * @param prefix        - file name
   * @param suffix        - file extension
   * @param variableSpace is used to get system variables
   * @return FileObject
   * @throws KettleFileException
   */
  FileObject createTempFile( String prefix, KettleVFS.Suffix suffix, VariableSpace variableSpace )
    throws KettleFileException;

  /**
   * @param prefix    - file name
   * @param suffix    - file extension
   * @param directory - directory where file will be created
   * @return FileObject
   * @throws KettleFileException
   */
  FileObject createTempFile( String prefix, KettleVFS.Suffix suffix, String directory ) throws KettleFileException;

  FileObject createTempFile( String prefix, String suffix, String directory ) throws KettleFileException;

  /**
   * @param prefix    - file name
   * @param directory path to directory where file will be created
   * @param space     is used to get system variables
   * @return FileObject
   * @throws KettleFileException
   */
  FileObject createTempFile( String prefix, KettleVFS.Suffix suffix, String directory, VariableSpace space )
    throws KettleFileException;

  FileObject createTempFile( String prefix, String suffix, String directory, VariableSpace space )
    throws KettleFileException;

  /**
   * resets the VariableSpace
   *
   */
  void reset();

}
