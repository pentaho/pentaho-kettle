/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.operations.FileOperations;

public class NonAccessibleFileObject implements FileObject {
  private final String fullyQualifiedName;

  public NonAccessibleFileObject( String fullyQualifiedName ) {
    this.fullyQualifiedName = fullyQualifiedName;
  }

  @Override
  public boolean canRenameTo( FileObject arg0 ) {
    return false;
  }

  @Override
  public void close() throws FileSystemException {

  }

  @Override
  public void copyFrom( FileObject arg0, FileSelector arg1 ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public void createFile() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public void createFolder() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public boolean delete() throws FileSystemException {
    return false;
  }

  @Override
  public int delete( FileSelector arg0 ) throws FileSystemException {
    return 0;
  }

  @Override
  public boolean exists() throws FileSystemException {
    return false;
  }

  @Override
  public FileObject[] findFiles( FileSelector arg0 ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public void findFiles( FileSelector arg0, boolean arg1, @SuppressWarnings( "rawtypes" ) List arg2 ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileObject getChild( String arg0 ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileObject[] getChildren() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileContent getContent() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileOperations getFileOperations() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileSystem getFileSystem() {
    throw new NotImplementedException();
  }

  @Override
  public FileName getName() {
    throw new NotImplementedException();
  }

  @Override
  public FileObject getParent() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileType getType() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public URL getURL() throws FileSystemException {
    try {
      return new URL( fullyQualifiedName );
    } catch ( MalformedURLException e ) {
      throw new FileSystemException( e );
    }
  }

  @Override
  public boolean isAttached() {
    return false;
  }

  @Override
  public boolean isContentOpen() {
    return false;
  }

  @Override
  public boolean isHidden() throws FileSystemException {
    return false;
  }

  @Override
  public boolean isReadable() throws FileSystemException {
    return false;
  }

  @Override
  public boolean isWriteable() throws FileSystemException {
    return false;
  }

  @Override
  public void moveTo( FileObject arg0 ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public void refresh() throws FileSystemException {

  }

  @Override
  public FileObject resolveFile( String arg0 ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileObject resolveFile( String arg0, NameScope arg1 ) throws FileSystemException {
    throw new NotImplementedException();
  }

}
