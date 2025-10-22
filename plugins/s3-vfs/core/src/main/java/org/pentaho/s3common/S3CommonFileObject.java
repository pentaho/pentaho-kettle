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


package org.pentaho.s3common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.pentaho.di.connections.vfs.provider.ConnectionFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.annotations.VisibleForTesting;

public abstract class S3CommonFileObject extends AbstractFileObject<S3CommonFileSystem> {

  private static final Logger logger = LoggerFactory.getLogger( S3CommonFileObject.class );
  public static final String DELIMITER = "/";

  protected S3CommonFileSystem fileSystem;
  protected String bucketName;
  protected String key;
  protected S3Object s3Object;
  protected ObjectMetadata s3ObjectMetadata;

  protected S3CommonFileObject( final AbstractFileName name, final S3CommonFileSystem fileSystem ) {
    super( name, fileSystem );
    this.fileSystem = fileSystem;
    this.bucketName = getS3BucketName();
    this.key = getBucketRelativeS3Path();
  }

  @Override
  protected long doGetContentSize() {
    return s3ObjectMetadata.getContentLength();
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    logger.debug( "Accessing content {}", getQualifiedName() );
    closeS3Object();
    S3Object streamS3Object = getS3Object();
    return new S3CommonFileInputStream( streamS3Object.getObjectContent(), streamS3Object );
  }

  @Override
  public Path getPath() {
    // default impl will only work for schemes registered with nio
    String namePath = getName().getPath();
    String[] pathElements = StringUtils.split( namePath, DELIMITER );
    return Path.of( "", pathElements );
  }

  @Override public void createFile() throws FileSystemException {
    //PDI-19598: Copied from super.createFile() but it was a way to force the file creation on S3
    synchronized ( fileSystem ) {
      try {
        // VFS-210: We do not want to trunc any existing file, checking for its existence is
        // still required
        if ( exists() && !isFile() ) {
          throw new FileSystemException( "vfs.provider/create-file.error", super.getName() );
        }

        if ( !exists() ) {
          try ( OutputStream outputStream = getOutputStream() ) {
            //Force to write an empty array to force file creation on S3 bucket
            outputStream.write( new byte[] {} );
          }
          endOutput();
        }
      } catch ( final RuntimeException re ) {
        throw re;
      } catch ( final Exception e ) {
        throw new FileSystemException( "vfs.provider/create-file.error", super.getName(), e );
      }
    }
  }

  @Override
  protected FileType doGetType() throws Exception {
    return getType();
  }

  @Override
  protected String[] doListChildren() throws Exception {
    List<String> childrenList = new ArrayList<>();

    // only listing folders or the root bucket
    if ( getType() == FileType.FOLDER || isRootBucket() ) {
      childrenList = getS3ObjectsFromVirtualFolder( key, bucketName );
    }
    String[] childrenArr = new String[ childrenList.size() ];

    return childrenList.toArray( childrenArr );
  }

  protected String getS3BucketName() {
    String bucket = getName().getPath();
    if ( bucket.indexOf( DELIMITER, 1 ) > 1 ) {
      // this file is a file, to get the bucket, remove the name from the path
      bucket = bucket.substring( 1, bucket.indexOf( DELIMITER, 1 ) );
    } else {
      // this file is a bucket
      bucket = bucket.replace( DELIMITER, "" );
    }
    return bucket;
  }

  protected List<String> getS3ObjectsFromVirtualFolder( String key, String bucketName ) {
    List<String> childrenList = new ArrayList<>();

    // fix cases where the path doesn't include the final delimiter
    String realKey = key;
    if ( !realKey.endsWith( DELIMITER ) ) {
      realKey += DELIMITER;
    }

    if ( "".equals( key ) && "".equals( bucketName ) ) {
      //Getting buckets in root folder
      List<Bucket> bucketList = fileSystem.getS3Client().listBuckets();
      for ( Bucket bucket : bucketList ) {
        childrenList.add( bucket.getName() + DELIMITER );
      }
    } else {
      getObjectsFromNonRootFolder( key, bucketName, childrenList, realKey );
    }
    return childrenList;
  }

  private void getObjectsFromNonRootFolder( String key, String bucketName, List<String> childrenList, String realKey ) {
    //Getting files/folders in a folder/bucket
    String prefix = key.isEmpty() || key.endsWith( DELIMITER ) ? key : key + DELIMITER;
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
      .withBucketName( bucketName )
      .withPrefix( prefix )
      .withDelimiter( DELIMITER );

    ObjectListing ol = fileSystem.getS3Client().listObjects( listObjectsRequest );

    ArrayList<S3ObjectSummary> allSummaries = new ArrayList<>( ol.getObjectSummaries() );
    ArrayList<String> allCommonPrefixes = new ArrayList<>( ol.getCommonPrefixes() );

    // get full list
    while ( ol.isTruncated() ) {
      ol = fileSystem.getS3Client().listNextBatchOfObjects( ol );
      allSummaries.addAll( ol.getObjectSummaries() );
      allCommonPrefixes.addAll( ol.getCommonPrefixes() );
    }

    for ( S3ObjectSummary s3os : allSummaries ) {
      if ( !s3os.getKey().equals( realKey ) ) {
        childrenList.add( s3os.getKey().substring( prefix.length() ) );
      }
    }

    for ( String commonPrefix : allCommonPrefixes ) {
      if ( !commonPrefix.equals( realKey ) ) {
        childrenList.add( commonPrefix.substring( prefix.length() ) );
      }
    }
  }

  protected String getBucketRelativeS3Path() {
    if ( getName().getPath().indexOf( DELIMITER, 1 ) >= 0 ) {
      return getName().getPath().substring( getName().getPath().indexOf( DELIMITER, 1 ) + 1 );
    } else {
      return "";
    }
  }

  @VisibleForTesting
  public S3Object getS3Object() {
    return getS3Object( this.key, this.bucketName );
  }

  protected S3Object getS3Object( String key, String bucket ) {
    if ( s3Object != null && s3Object.getObjectContent() != null ) {
      logger.debug( "Returning exisiting object {}", getQualifiedName() );
      return s3Object;
    } else {
      logger.debug( "Getting object {}", getQualifiedName() );
      return fileSystem.getS3Client().getObject( bucket, key );
    }
  }

  protected boolean isRootBucket() {
    return key.equals( "" );
  }

  @Override
  @SuppressWarnings( "java:S2139" ) // Logging for traceability while allowing the exception to propagate normally
  public void doAttach() throws Exception {
    logger.trace( "Attach called on {}", getQualifiedName() );
    injectType( FileType.IMAGINARY );

    if ( isRootBucket() ) {
      // cannot attach to root bucket but still need to figure out the type for exists()
      try {
        fileSystem.getS3Client().getBucketLocation( bucketName );
      } catch ( AmazonS3Exception e ) {
        if ( "NoSuchBucket".equals( e.getErrorCode() ) ) {
          injectType( FileType.IMAGINARY );
          return;
        }

        // One common case is "403 Access Denied", for bucket names which exist in the same region
        // but are not of this account. This can also happen for normal files and is being handled
        // similarly in handleAttachExceptionFallback.
        // Any other errors should also bubble up.

        // Make sure this gets printed for the user.
        logger.error( "Could not get information on {}", getQualifiedName(), e );
        throw new FileSystemException( "vfs.provider/get-type.error", e, getQualifiedName() );
      }

      injectType( FileType.FOLDER );
      return;
    }

    try {
      // 1. Is it an existing file?
      s3ObjectMetadata = fileSystem.getS3Client().getObjectMetadata( bucketName, key );
      injectType( getName().getType() ); // if this worked then the automatically detected type is right
    } catch ( AmazonS3Exception e ) { // S3 object doesn't exist
      // 2. Is it in reality a folder?
      handleAttachException( key, bucketName );
    } finally {
      closeS3Object();
    }
  }

  protected void handleAttachException( String key, String bucket ) throws IOException {
    String keyWithDelimiter = key + DELIMITER;
    try {
      s3ObjectMetadata = fileSystem.getS3Client().getObjectMetadata( bucketName, key );
      injectType( FileType.FOLDER );
      this.key = keyWithDelimiter;
    } catch ( AmazonS3Exception e1 ) {
      String errorCode = e1.getErrorCode();
      try {
        //S3 Object does not exist (could be the process of creating a new file. Lets fallback to the old
        // behavior. (getting the s3 object)
        if ( errorCode.equals( "404 Not Found" ) ) {
          s3Object = getS3Object( keyWithDelimiter, bucket );
          s3ObjectMetadata = s3Object.getObjectMetadata();
          injectType( FileType.FOLDER );
          this.key = keyWithDelimiter;
        } else {
          //The exception was not related with not finding the file
          handleAttachExceptionFallback( bucket, keyWithDelimiter, e1 );
        }
      } catch ( AmazonS3Exception e2 ) {
        //something went wrong getting the s3 object
        handleAttachExceptionFallback( bucket, keyWithDelimiter, e2 );
      }
    } finally {
      closeS3Object();
    }
  }

  private void handleAttachExceptionFallback( String bucket, String keyWithDelimiter, AmazonS3Exception exception )
    throws FileSystemException {
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
      .withBucketName( bucket )
      .withPrefix( keyWithDelimiter )
      .withDelimiter( DELIMITER );
    ObjectListing ol = fileSystem.getS3Client().listObjects( listObjectsRequest );

    if ( !( ol.getCommonPrefixes().isEmpty() && ol.getObjectSummaries().isEmpty() ) ) {
      injectType( FileType.FOLDER );
    } else {
      //Folders don't really exist - they will generate a "NoSuchKey" exception
      // confirms key doesn't exist but connection okay
      String errorCode = exception.getErrorCode();
      if ( !errorCode.equals( "NoSuchKey" ) ) {
        // bubbling up other connection errors
        logger.error( "Could not get information on " + getQualifiedName(),
          exception ); // make sure this gets printed for the user
        throw new FileSystemException( "vfs.provider/get-type.error", getQualifiedName(), exception );
      }
    }
  }

  private void closeS3Object() throws IOException {
    if ( s3Object != null ) {
      s3Object.close();
      s3Object = null;
    }
  }

  @Override
  public void doDetach() throws Exception {
    logger.trace( "detaching {}", getQualifiedName() );
    closeS3Object();
  }

  @Override
  protected void doDelete() throws FileSystemException {
    doDelete( this.key, this.bucketName );
  }

  protected void doDelete( String key, String bucketName ) throws FileSystemException {
    // can only delete folder if empty
    if ( getType() == FileType.FOLDER ) {
      // AbstractFileObject.delete is recursive, by the time it gets here the folder must be empty
      key = StringUtils.appendIfMissing( key, DELIMITER );
    }
    logger.debug( "deleteObject([{}], [{}])", bucketName, key );
    fileSystem.getS3Client().deleteObject( bucketName, key );
  }

  @Override
  protected OutputStream doGetOutputStream( boolean bAppend ) throws Exception {
    int partSize = (int) Long.min( Integer.MAX_VALUE, this.fileSystem.getPartSize() );
    return new S3CommonPipedOutputStream( this.fileSystem, bucketName, key, partSize );
  }

  /**
   * Attempts to extract an S3FileObject from a FileObject, including wrappers like ResolvedConnectionFileObject (via
   * reflection for getResolvedFileObject()).
   */
  private S3CommonFileObject extractDelegateS3FileObject( FileObject file ) {
    if ( file instanceof S3CommonFileObject ) {
      return (S3CommonFileObject) file;
    }
    if ( file instanceof ConnectionFileObject ) {
      // If it's a ResolvedConnectionFileObject, we can try to get the underlying S3FileObject
      ConnectionFileObject resolved = (ConnectionFileObject) file;
      FileObject delegate = resolved.getResolvedFileObject();
      if ( delegate instanceof S3CommonFileObject ) {
        return (S3CommonFileObject) delegate;
      }
    }
    return null;
  }

  /**
   * Copies the content of the specified file to this file
   * Uses server-side multipart copy on large files, if both files are S3FileObjects.
   * If the source is not an S3FileObject or is in a different region, it uses TransferManager to upload the content.
   * Supports both files and folders as source and destination. Empty source folders are not valid for S3 destination and will be ignored.
   *
   * @param file     The FileObject to copy.
   * @param selector The FileSelector.
   * @throws FileSystemException If an error occurs during the copy operation.
   */
  @Override
  public void copyFrom( final FileObject file, final FileSelector selector ) throws FileSystemException {
    if ( !FileObjectUtils.exists( file ) ) {
      throw new FileSystemException( "vfs.provider/copy-missing-file.error", file );
    }

    // Locate the files to copy across
    final ArrayList<FileObject> files = new ArrayList<>();
    file.findFiles( selector, false, files );

    // Copy everything across
    for ( final FileObject srcFile : files ) {
      // Skip folders - they will be created automatically when their files are copied, and empty folders do not exist in S3
      if ( srcFile.getType() == FileType.FOLDER ) {
        continue;
      }

      // Calculate the destination key preserving folder structure
      final S3CommonFileObject dstFile = calculateDestination( file, srcFile );
      
      // Copy the individual file
      copySingleFileFrom( srcFile, dstFile );
    }
  }

  /**
   * Calculates the destination S3CommonFileObject for a source file being copied.
   * Preserves the folder structure by calculating the relative path from the source base to the source file.
   *
   * @param srcBase The base source folder/file
   * @param srcFile The specific source file being copied
   * @return The destination S3CommonFileObject with the correct key
   * @throws FileSystemException if destination calculation fails
   */
  private S3CommonFileObject calculateDestination( final FileObject srcBase, final FileObject srcFile )
    throws FileSystemException {
    // If destination (this) is a file, use it as-is
    if ( getType() == FileType.FILE ) {
      return this;
    }

    // Destination is a folder - calculate the relative path to preserve structure
    try {
      String relativePath = srcBase.getName().getRelativeName( srcFile.getName() );
      if ( relativePath == null || relativePath.isEmpty() ) {
        relativePath = srcFile.getName().getBaseName();
      }

      // Build the destination key: current key + relative path
      String dstKey = this.key;
      if ( !dstKey.isEmpty() && !dstKey.endsWith( DELIMITER ) ) {
        dstKey += DELIMITER;
      }
      dstKey += relativePath;

      // Create a new S3CommonFileObject with the calculated key
      return (S3CommonFileObject) fileSystem.resolveFile( getName().getRoot() + DELIMITER + bucketName + DELIMITER + dstKey );
    } catch ( Exception e ) {
      throw new FileSystemException( "vfs.provider/copy-file.error", srcFile, this, e );
    }
  }

  /**
   * Copies a single file (folders are not supported as both src and dst) from the specified source
   * to the specified destination S3CommonFileObject.
   * Uses S3 server-side copy if both source and destination are S3CommonFileObject.
   * Falls back to S3 upload if server-side copy fails or if the source is not an S3CommonFileObject.
   *
   * @param src The source FileObject to copy from
   * @param dst The destination S3CommonFileObject to copy to
   * @throws FileSystemException If an error occurs during the copy operation.
   */
  private void copySingleFileFrom( final FileObject src, final S3CommonFileObject dst ) throws FileSystemException {
    S3CommonFileObject s3Src = extractDelegateS3FileObject( src );
    if ( s3Src != null ) {
      // S3 to S3 copy
      try {
        logger.info( "Attempting S3->S3 copy from {} to {}",
          s3Src.getQualifiedName(), this.getQualifiedName() );
        fileSystem.copy( s3Src, dst );
        return;
      } catch ( FileSystemException e ) {
        logger.warn( "S3->S3 copy failed, falling back to S3 upload: {}", e.getMessage(), e );
        // fallback to TransferManager upload below
      }
    }
    // For non-S3FileObject or fallback, use S3 upload
    try {
      logger.info( "Uploading to S3 from {} to {}", src.getName(), this.getName() );
      fileSystem.upload( src, dst );
    } catch ( Exception e ) {
      logger.error( "Upload failed: {}", e.getMessage(), e );
      throw new FileSystemException( "vfs.provider.s3/transfer.upload-failed",
        src.getName().getURI(), this.getQualifiedName(), e );
    }
  }

  @Override
  public long doGetLastModifiedTime() {
    if ( s3ObjectMetadata != null && s3ObjectMetadata.getLastModified() != null ) {
      return s3ObjectMetadata.getLastModified().getTime();
    } else {
      // In some case s3 system might not return modified time.
      logger.trace( "No last modified date is available for this object" );
      return 0L;
    }
  }

  @Override
  protected void doCreateFolder() throws Exception {
    if ( !isRootBucket() ) {
      // create meta-data for your folder and set content-length to 0
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength( 0 );
      metadata.setContentType( "binary/octet-stream" );

      // create empty content
      InputStream emptyContent = new ByteArrayInputStream( new byte[ 0 ] );

      // create a PutObjectRequest passing the folder name suffixed by /
      PutObjectRequest putObjectRequest = createPutObjectRequest( bucketName, key + DELIMITER, emptyContent, metadata );

      // send request to S3 to create folder
      try {
        fileSystem.getS3Client().putObject( putObjectRequest );
      } catch ( AmazonS3Exception e ) {
        throw new FileSystemException( "vfs.provider.local/create-folder.error", this, e );
      }
    } else {
      throw new FileSystemException( "vfs.provider/create-folder-not-supported.error" );
    }
  }


  protected PutObjectRequest createPutObjectRequest( String bucketName, String key, InputStream inputStream,
                                                     ObjectMetadata objectMetadata ) {
    return new PutObjectRequest( bucketName, key, inputStream, objectMetadata );
  }

  @Override
  protected void doRename( FileObject newFile ) throws Exception {
    // no folder renames on S3
    if ( getType().equals( FileType.FOLDER ) ) {
      logger.debug( "recursively moving folder [{}] -> [{}]", this.getPublicURIString(), newFile.getPublicURIString() );
      doFolderMove( this, newFile );
      return;
    }

    s3ObjectMetadata = fileSystem.getS3Client().getObjectMetadata( bucketName, key );

    if ( s3ObjectMetadata == null ) {
      // object doesn't exist
      throw new FileSystemException( "vfs.provider/rename.error", this, newFile );
    }

    S3CommonFileObject dest = (S3CommonFileObject) newFile;

    // 1. copy the file
    CopyObjectRequest copyObjRequest = createCopyObjectRequest( bucketName, key, dest.bucketName, dest.key );
    logger.debug( "copyObject ([{}], [{}]) -> ([{}], [{}])", bucketName, key, dest.bucketName, dest.key );
    fileSystem.getS3Client().copyObject( copyObjRequest );

    // 2. delete self
    delete();
  }

  private void doFolderMove( FileObject sourceFolder, FileObject targetFolder ) throws FileSystemException {
    logger.debug( "creating folder [{}]", targetFolder.getPublicURIString() );
    FileObject[] children = sourceFolder.getChildren();
    targetFolder.createFolder();
    for ( FileObject child : children ) {
      FileObject targetChild = targetFolder.resolveFile( child.getName().getBaseName() );
      if ( child.isFolder() ) {
        doFolderMove( child, targetChild );
      } else if ( child.isFile() ) {
        logger.debug( "moving file [{}] -> [{}]", child.getPublicURIString(), targetChild.getPublicURIString() );
        child.moveTo( targetChild );
      }
    }
    sourceFolder.delete();
  }

  protected CopyObjectRequest createCopyObjectRequest( String sourceBucket, String sourceKey, String destBucket,
                                                       String destKey ) {
    return new CopyObjectRequest( sourceBucket, sourceKey, destBucket, destKey );
  }

  protected String getQualifiedName() {
    return getQualifiedName( this );
  }

  protected String getQualifiedName( S3CommonFileObject s3nFileObject ) {
    return s3nFileObject.bucketName + "/" + s3nFileObject.key;
  }

}
