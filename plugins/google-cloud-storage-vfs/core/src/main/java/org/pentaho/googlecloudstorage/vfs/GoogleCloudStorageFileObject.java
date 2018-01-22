/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.googlecloudstorage.vfs;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.URLFileName;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The FileObject definition for a file/folder stored in Google Cloud Storage
 * <p>
 * Created by bmorrise on 8/28/17.
 */
public class GoogleCloudStorageFileObject extends AbstractFileObject<GoogleCloudStorageFileSystem> {

  public static final String DELIMITER = "/";
  private Storage storage;

  public GoogleCloudStorageFileObject( AbstractFileName name, GoogleCloudStorageFileSystem fs, Storage storage ) {
    super( name, fs );
    this.storage = storage;
  }

  /**
   * Gets the size of the file content
   *
   * @return The file size
   * @throws Exception
   */
  @Override protected long doGetContentSize() throws Exception {
    Blob blob = getBlob();
    return blob != null ? blob.getSize() : 0;
  }

  /**
   * Gets the file type
   *
   * @return The file type
   * @throws Exception
   */
  @Override protected FileType doGetType() throws Exception {
    if ( getName().getPath().equals( DELIMITER ) || getName().getPath().endsWith( DELIMITER ) || getName().getPath()
      .equals( "" ) ) {
      return FileType.FOLDER;
    }

    Blob blob = getBlob();
    if ( blob != null ) {
      if ( blob.getName().endsWith( DELIMITER ) ) {
        return FileType.FOLDER;
      } else {
        return FileType.FILE;
      }
    }

    return FileType.IMAGINARY;
  }

  /**
   * Gets the list of child files of the current directory
   *
   * @return A list of file/folders
   * @throws Exception
   */
  @Override protected String[] doListChildren() throws Exception {
    List<String> items = new ArrayList<>();
    Map<String, List<String>> folders = new HashMap<>();
    Bucket bucket = storage.get( getBucketName() );
    if ( bucket != null && bucket.exists() ) {
      folders.put( "", new ArrayList<>() );
      for ( Blob blob : bucket.list().iterateAll() ) {
        String path = blob.getName();
        boolean isDirectory = path.endsWith( DELIMITER );
        String parent = "";
        if ( path.contains( DELIMITER ) ) {
          parent = path.substring( 0, path.lastIndexOf( DELIMITER ) );
        }
        String[] parts = path.split( DELIMITER );
        String name = parts[ parts.length - 1 ];
        if ( isDirectory ) {
          folders.put( fixSlashes( path, false ), new ArrayList<>() );
          name = name.concat( DELIMITER );
          parent = path.substring( 0, path.lastIndexOf( DELIMITER ) );
          if ( parent.contains( DELIMITER ) ) {
            parent = parent.substring( 0, parent.lastIndexOf( DELIMITER ) );
          } else {
            parent = "";
          }
        }
        List<String> folderList = folders.getOrDefault( fixSlashes( parent, true ), new ArrayList<>() );
        folderList.add( name );
        folders.put( fixSlashes( parent, true ), folderList );
      }
      items = folders.getOrDefault( fixSlashes( getName().getPath(), true ), Collections.emptyList() );
    }

    return items.toArray( new String[ items.size() ] );
  }

  /**
   * Adds a delimeter to the beginning of a file name and delimeter to the end of a folder name
   *
   * @param value       - The file/folder name
   * @param isDirectory - Whether or not the file is a directory
   * @return The update file/folder name
   */
  private String fixSlashes( String value, boolean isDirectory ) {
    if ( !value.startsWith( DELIMITER ) ) {
      value = DELIMITER.concat( value );
    }
    if ( isDirectory && !value.endsWith( DELIMITER ) ) {
      value = value.concat( DELIMITER );
    }
    return value;
  }

  /**
   * Gets the time when the file/folder was last modified
   *
   * @return The last modified time
   * @throws Exception
   */
  @Override protected long doGetLastModifiedTime() throws Exception {
    return getBlob().getUpdateTime();
  }

  /**
   * Gets the bucket name, which is the host part of the URL
   *
   * @return The bucket name
   */
  public String getBucketName() {
    return ( (URLFileName) getName() ).getHostName();
  }

  /**
   * Gets the blob from the Google Cloud Storage api for the file/folder
   *
   * @return The Blob that represents the file/folder
   */
  public Blob getBlob() {
    URLFileName urlFileName = (URLFileName) this.getName();
    Bucket bucket = storage.get( urlFileName.getHostName() );
    if ( urlFileName.getPath().equals( DELIMITER ) ) {
      return null;
    }
    String stripped = urlFileName.getPath().substring( 1, urlFileName.getPath().length() );
    Blob blob = bucket.get( stripped );
    if ( blob == null ) {
      blob = bucket.get( stripped.concat( DELIMITER ) );
    }
    return blob;
  }

  /**
   * Creates new BlobInfo for a new folder
   *
   * @param name
   * @return BlobInfo of the new folder
   */
  public BlobInfo getBlobInfo( String name ) {
    return BlobInfo.newBuilder( getBucketName(), name ).build();
  }

  @Override protected OutputStream doGetOutputStream( boolean bAppend ) throws FileSystemException {
    BlobInfo blobInfo = BlobInfo.newBuilder( getBucketName(), getPath() ).build();
    Blob blob = storage.create( blobInfo );
    return Channels.newOutputStream( blob.writer() );
  }

  /**
   * Gets the input stream of the file
   *
   * @return The InputStream of the file
   * @throws Exception
   */
  @Override protected InputStream doGetInputStream() throws Exception {
    return Channels.newInputStream( getBlob().reader() );
  }

  /**
   * Deletes the current file/folder
   *
   * @throws Exception
   */
  @Override protected void doDelete() throws Exception {
    getBlob().delete();
  }

  /**
   * Renames the file/folder
   *
   * @param newFile - The file object of the renamed destination
   * @throws Exception
   */
  @Override protected void doRename( FileObject newFile ) throws Exception {
    BlobInfo blobInfo =
      BlobInfo.newBuilder( ( (GoogleCloudStorageFileObject) newFile ).getBucketName(),
        ( (GoogleCloudStorageFileObject) newFile ).getPath() )
        .build();
    Blob newBlob = storage.create( blobInfo );
    getBlob().copyTo( newBlob.getBlobId() );
    getBlob().delete();
  }

  /**
   * Gets whether or not the file exists
   *
   * @return A boolean as to whether or not the file/folder exits
   * @throws FileSystemException
   */
  @Override public boolean exists() throws FileSystemException {
    return getBlob() != null || getName().getPath().equals( DELIMITER );
  }

  /**
   * Gets the path of the file/folder
   *
   * @return The path of the file/folder
   */
  private String getPath() {
    return getName().getPath().substring( 1, getName().getPath().length() );
  }

  /**
   * Creates a new folder
   *
   * @throws Exception
   */
  @Override protected void doCreateFolder() throws Exception {
    BlobInfo blobInfo = getBlobInfo( getPath().concat( DELIMITER ) );
    storage.create( blobInfo );
  }

  /**
   * Resolves the file based on the path
   *
   * @param path - The path of the file to resolve
   * @return The FileObject of the resolve file
   * @throws FileSystemException
   */
  @Override public FileObject resolveFile( String path ) throws FileSystemException {
    return super.resolveFile( path );
  }

  /**
   * Resolves the file based on the path and namescope
   *
   * @param path  - The path of the file to resolve
   * @param scope - the scope of the file to resolve
   * @return The FileObject of the resolve file
   * @throws FileSystemException
   */
  @Override public FileObject resolveFile( String name, NameScope scope ) throws FileSystemException {
    return super.resolveFile( name, scope );
  }


}
