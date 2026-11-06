/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.s3.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.pentaho.di.connections.vfs.provider.ConnectionFileObject;
import org.pentaho.s3.vfs.client.S3TypeFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import com.google.common.annotations.VisibleForTesting;

public class S3FileObject extends AbstractFileObject<S3FileSystem> {
  public static final String DELIMITER = "/";

  private static final Logger logger = LoggerFactory.getLogger( S3FileObject.class );

  private final S3FileSystem fileSystem;
  private final S3TypeFetcher typeFetcher;
  protected final String bucketName;
  protected final String key;

  private ResponseInputStream<GetObjectResponse> s3Object;
  private HeadObjectResponse s3ObjectMetadata;

  protected S3FileObject( final AbstractFileName name, final S3FileSystem fileSystem ) {
    super( name, fileSystem );
    this.fileSystem = fileSystem;
    this.typeFetcher = new S3TypeFetcher( fileSystem::getS3Client, name );
    this.bucketName = getS3BucketName();
    this.key = getBucketRelativeS3Path();
  }

  @Override
  protected long doGetContentSize() {
    return s3ObjectMetadata.contentLength();
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    logger.debug( "Accessing content {}", getQualifiedName() );
    closeS3Object();
    return new S3FileInputStream( getS3Object() );
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
      childrenList = listS3Objects( key, bucketName );
    }
    String[] childrenArr = new String[ childrenList.size() ];

    return childrenList.toArray( childrenArr );
  }

  private String getS3BucketName() {
    String s3BucketName = getName().getPath();
    if ( s3BucketName.indexOf( DELIMITER, 1 ) > 1 ) {
      // this file is a file, to get the bucket, remove the name from the path
      //aws-sdk-version 1.12.460 or later requires bucket name without special character "/" -> https://github.com/aws/aws-sdk-java/discussions/2976
      s3BucketName = s3BucketName.substring( 1, s3BucketName.indexOf( DELIMITER, 1 ) );
    } else {
      // this file is a bucket
      s3BucketName = s3BucketName.replaceAll( DELIMITER, "" );
    }
    return s3BucketName;
  }

  private List<String> listS3Objects( String key, String bucketName ) throws FileSystemException {
    if ( isRootBucket() && "".equals( bucketName ) ) {
      List<String> childrenList = new ArrayList<>();
      List<Bucket> bucketList = fileSystem.getS3Client().listBuckets().buckets();
      for ( Bucket bucket : bucketList ) {
        childrenList.add( bucket.name() + DELIMITER );
      }
      return childrenList;
    }
    if ( fileSystem.isLegacyS3UrisEnabled() ) {
      return fileSystem.getLegacyUriSupport().execute( bucketName, key, this::getObjectsFromNonRootFolder );
    }
    return getObjectsFromNonRootFolder( bucketName, key );
  }

  private List<String> getObjectsFromNonRootFolder( String bucketName, String key ) throws FileSystemException {
    List<String> childrenList = new ArrayList<>();

    // fix cases where the path doesn't include the final delimiter
    String realKey = key;
    if ( !realKey.endsWith( DELIMITER ) ) {
      realKey += DELIMITER;
    }

    // Getting files/folders in a folder/bucket
    String prefix = key.isEmpty() || key.endsWith( DELIMITER ) ? key : key + DELIMITER;
    ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
      .bucket( bucketName )
      .prefix( prefix )
      .delimiter( DELIMITER )
      .build();

    List<S3Object> allSummaries = new ArrayList<>();
    List<String> allCommonPrefixes = new ArrayList<>();

    // the paginator walks the continuation tokens for us
    for ( ListObjectsV2Response page : fileSystem.getS3Client().listObjectsV2Paginator( listObjectsRequest ) ) {
      allSummaries.addAll( page.contents() );
      page.commonPrefixes().forEach( commonPrefix -> allCommonPrefixes.add( commonPrefix.prefix() ) );
    }

    for ( S3Object s3os : allSummaries ) {
      if ( !s3os.key().equals( realKey ) ) {
        childrenList.add( s3os.key().substring( prefix.length() ) );
      }
    }

    for ( String commonPrefix : allCommonPrefixes ) {
      if ( !commonPrefix.equals( realKey ) ) {
        childrenList.add( commonPrefix.substring( prefix.length() ) );
      }
    }
    return childrenList;
  }

  private String getBucketRelativeS3Path() {
    if ( getName().getPath().indexOf( DELIMITER, 1 ) >= 0 ) {
      return getName().getPath().substring( getName().getPath().indexOf( DELIMITER, 1 ) + 1 );
    } else {
      return "";
    }
  }

  @VisibleForTesting
  public ResponseInputStream<GetObjectResponse> getS3Object() throws FileSystemException {
    return getS3Object( this.key, this.bucketName );
  }

  private ResponseInputStream<GetObjectResponse> getS3Object( String key, String bucket ) throws FileSystemException {
    if ( s3Object != null ) {
      logger.debug( "Returning existing object {}", getQualifiedName() );
      return s3Object;
    } else {
      logger.debug( "Getting object {}", getQualifiedName() );
      if ( fileSystem.isLegacyS3UrisEnabled() ) {
        return fileSystem.getLegacyUriSupport().execute( bucket, key, this::getObject );
      }
      return getObject( bucket, key );
    }
  }

  private ResponseInputStream<GetObjectResponse> getObject( String bucket, String key ) throws FileSystemException {
    return fileSystem.getS3Client().getObject( GetObjectRequest.builder()
      .bucket( bucket )
      .key( key )
      .build() );
  }

  private boolean isRootBucket() {
    return key.equals( "" );
  }

  @Override
  public void doAttach() throws Exception {
    logger.trace( "Attach called on {}", getQualifiedName() );

    try {
      var result = fileSystem.isLegacyS3UrisEnabled()
        ? fileSystem.getLegacyUriSupport().execute( bucketName, key, typeFetcher::fetchType )
        : typeFetcher.fetchType( bucketName, key );
      this.s3ObjectMetadata = result.metadata().orElse( null );
      injectType( result.type() );
    } catch ( S3Exception e ) {
      throw new FileSystemException( "vfs.provider/get-type.error", getQualifiedName(), e );
    } finally {
      closeS3Object();
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
    if ( fileSystem.isLegacyS3UrisEnabled() ) {
      fileSystem.getLegacyUriSupport().execute( bucketName, key, this::doDelete );
    } else {
      doDelete( bucketName, key );
    }
  }

  private DeleteObjectResponse doDelete( String bucketName, String key ) throws FileSystemException {
    // can only delete folder if empty
    if ( getType() == FileType.FOLDER ) {
      // AbstractFileObject.delete is recursive, by the time it gets here the folder must be empty
      key = Strings.CS.appendIfMissing( key, DELIMITER );
    }
    logger.debug( "deleteObject([{}], [{}])", bucketName, key );
    return fileSystem.getS3Client().deleteObject( DeleteObjectRequest.builder()
      .bucket( bucketName ).key( key ).build() );
  }

  @Override
  protected OutputStream doGetOutputStream( boolean bAppend ) throws Exception {
    if ( fileSystem.isLegacyS3UrisEnabled() ) {
      var destination = fileSystem.getLegacyUriSupport().forWrite( bucketName, key );
      return new S3SdkOutputStream( this.fileSystem, destination.bucket(), destination.key() );
    }
    return new S3SdkOutputStream( this.fileSystem, bucketName, key );
  }

  boolean isLegacyS3UrisEnabled() {
    return fileSystem.isLegacyS3UrisEnabled();
  }

  /**
   * Attempts to extract an S3FileObject from a FileObject, including wrappers like ResolvedConnectionFileObject (via
   * reflection for getResolvedFileObject()).
   */
  private S3FileObject extractDelegateS3FileObject( FileObject file ) {
    if ( file instanceof S3FileObject ) {
      return (S3FileObject) file;
    }
    if ( file instanceof ConnectionFileObject ) {
      // If it's a ResolvedConnectionFileObject, we can try to get the underlying S3FileObject
      ConnectionFileObject resolved = (ConnectionFileObject) file;
      FileObject delegate = resolved.getResolvedFileObject();
      if ( delegate instanceof S3FileObject ) {
        return (S3FileObject) delegate;
      }
    }
    return null;
  }

  /**
   * Copies the content of the specified file to this file
    * Uses server-side copy for S3 sources, including supported connection wrappers, with multipart copy for large files.
    * Otherwise, or if server-side copy fails, streams the source using the async client.
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
    ArrayList<FileObject> files = new ArrayList<>();
    file.findFiles( selector, false, files );

    // Copy everything across
    for ( FileObject srcFile : files ) {
      // Skip folders - they will be created automatically when their files are copied, and empty folders do not exist in S3
      if ( srcFile.getType() == FileType.FOLDER ) {
        continue;
      }

      // Calculate the destination key preserving folder structure
      S3FileObject dstFile = calculateDestination( file, srcFile );

      // Copy the individual file
      copySingleFileFrom( srcFile, dstFile );
    }
  }

  /**
   * Calculates the destination S3FileObject for a source file being copied.
   * Preserves the folder structure by calculating the relative path from the source base to the source file.
   *
   * @param srcBase The base source folder/file
   * @param srcFile The specific source file being copied
   * @return The destination S3FileObject with the correct key
   * @throws FileSystemException if destination calculation fails
   */
  private S3FileObject calculateDestination( final FileObject srcBase, final FileObject srcFile )
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

      // Create a new S3FileObject with the calculated key
      return (S3FileObject) fileSystem.resolveFile( getName().getRoot() + DELIMITER + bucketName + DELIMITER + dstKey );
    } catch ( Exception e ) {
      throw new FileSystemException( "vfs.provider/copy-file.error", srcFile, this, e );
    }
  }

  /**
   * Copies a single file (folders are not supported as both src and dst) from the specified source
   * to the specified destination S3FileObject.
   * Uses S3 server-side copy if both source and destination are S3FileObject.
   * Falls back to S3 upload if server-side copy fails or if the source is not an S3FileObject.
   *
   * @param src The source FileObject to copy from
   * @param dst The destination S3FileObject to copy to
   * @throws FileSystemException If an error occurs during the copy operation.
   */
  private void copySingleFileFrom( final FileObject src, final S3FileObject dst ) throws FileSystemException {
    S3FileObject s3Src = extractDelegateS3FileObject( src );
    FileSystemException copyException = null;
    if ( s3Src != null ) {
      // S3 to S3 copy
      try {
        logger.info( "Attempting S3->S3 copy from {} to {}",
          s3Src.getQualifiedName(), this.getQualifiedName() );
        fileSystem.copy( s3Src, dst );
        return;
      } catch ( FileSystemException e ) {
        copyException = e; // This way we can preserve the original exception if upload also fails
        logger.warn( "S3->S3 copy failed, falling back to S3 upload: {}", e.getMessage(), e );
      }
    }
    // For non-S3FileObject or fallback, use S3 upload
    try {
      logger.info( "Uploading to S3 from {} to {}", src.getName(), this.getName() );
      fileSystem.upload( src, dst );
    } catch ( FileSystemException e ) {
      if ( copyException != null ) {
        // Both copy and upload failed - add copy exception as suppressed to preserve both stack traces
        e.addSuppressed( copyException );
      }
      throw new FileSystemException( "vfs.provider/copy-file.error", e, src, this );
    }
  }

  @Override
  public long doGetLastModifiedTime() {
    if ( s3ObjectMetadata != null && s3ObjectMetadata.lastModified() != null ) {
      return s3ObjectMetadata.lastModified().toEpochMilli();
    } else {
      // In some case s3 system might not return modified time.
      logger.trace( "No last modified date is available for this object" );
      return 0L;
    }
  }

  @Override
  protected void doCreateFolder() throws Exception {
    if ( fileSystem.isLegacyS3UrisEnabled() ) {
      fileSystem.getLegacyUriSupport().execute( bucketName, key, this::createFolder );
    } else {
      createFolder( bucketName, key );
    }
  }

  private PutObjectResponse createFolder( String bucketName, String key ) throws FileSystemException {
    if ( key.isEmpty() ) {
      throw new FileSystemException( "vfs.provider/create-folder-not-supported.error" );
    }
    // a folder is an empty object whose key is suffixed by /
    PutObjectRequest putObjectRequest = createPutObjectRequest( bucketName, key + DELIMITER );

    // send request to S3 to create folder
    try {
      return fileSystem.getS3Client().putObject( putObjectRequest, RequestBody.empty() );
    } catch ( S3Exception e ) {
      throw new FileSystemException( "vfs.provider.local/create-folder.error", this, e );
    }
  }

  private PutObjectRequest createPutObjectRequest( String bucketName, String key ) {
    return PutObjectRequest.builder()
      .bucket( bucketName )
      .key( key )
      .contentType( "binary/octet-stream" )
      .build();
  }

  @Override
  protected void doRename( FileObject newFile ) throws Exception {
    // no folder renames on S3
    if ( getType().equals( FileType.FOLDER ) ) {
      logger.debug( "recursively moving folder [{}] -> [{}]", this.getPublicURIString(), newFile.getPublicURIString() );
      doFolderMove( this, newFile );
      return;
    }

    s3ObjectMetadata = fileSystem.isLegacyS3UrisEnabled()
      ? fileSystem.getLegacyUriSupport().execute( bucketName, key, this::headObject )
      : headObject( bucketName, key );

    if ( s3ObjectMetadata == null ) {
      // object doesn't exist
      throw new FileSystemException( "vfs.provider/rename.error", this, newFile );
    }

    S3FileObject dest = (S3FileObject) newFile;

    // 1. copy the file
    logger.debug( "copyObject ([{}], [{}]) -> ([{}], [{}])", bucketName, key, dest.bucketName, dest.key );
    if ( fileSystem.isLegacyS3UrisEnabled() && dest.isLegacyS3UrisEnabled() ) {
      fileSystem.getLegacyUriSupport().executeCopy( bucketName, key, dest.bucketName, dest.key, this::copyObject );
    } else {
      copyObject( bucketName, key, dest.bucketName, dest.key );
    }

    // 2. delete self
    delete();
  }

  private HeadObjectResponse headObject( String bucketName, String key ) throws FileSystemException {
    return fileSystem.getS3Client().headObject( HeadObjectRequest.builder()
      .bucket( bucketName ).key( key ).build() );
  }

  private CopyObjectResponse copyObject( String sourceBucket, String sourceKey, String destinationBucket,
                                        String destinationKey ) throws FileSystemException {
    return fileSystem.getS3Client().copyObject( createCopyObjectRequest(
      sourceBucket, sourceKey, destinationBucket, destinationKey ) );
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

  private CopyObjectRequest createCopyObjectRequest( String sourceBucket, String sourceKey, String destBucket,
                                                       String destKey ) {
    return CopyObjectRequest.builder()
      .sourceBucket( sourceBucket ).sourceKey( sourceKey )
      .destinationBucket( destBucket ).destinationKey( destKey )
      .build();
  }

  protected String getQualifiedName() {
    return getQualifiedName( this );
  }

  private String getQualifiedName( S3FileObject s3FileObject ) {
    return s3FileObject.bucketName + "/" + s3FileObject.key;
  }

}
