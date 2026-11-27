package org.pentaho.s3.vfs.client;

import java.util.Optional;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.pentaho.s3.vfs.S3FileObject;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Determines the VFS type of an S3 location and returns metadata if available.
 * It can do 3 types of lookup
 * <ul>
 *   <li>HeadObject for 'key' (file)</li>
 *   <li>HeadObject for 'key/' (folder)</li>
 *   <li>ListObjectsV2 for 'key/' (folder without metadata)</li>
 * </ul>
 * It will fallback to the other type chck on failure. The order by which they are tried is determined by:
 * <ul>
 *   <li>the last successful operation, if the object is being re-attached, or</li>
 *   <li>try folder first if resolved from a name with a trailing /, file first otherwise</li>
 * </ul>
 * ListObjectsV2 consistently distinguishes "folders" from files with a trailing slash,
 * so this significantly improves performance when browsing or reattaching.
 **/
public class S3TypeFetcher {
  private static final Logger log = LoggerFactory.getLogger( S3TypeFetcher.class );

  @FunctionalInterface
  public interface S3ClientSupplier {
    public S3Client get() throws FileSystemException;
  }

  public record Result( FileType type, Optional<HeadObjectResponse> metadata ) {
  }

  /** Lookup to start with */
  private enum ResolveHint {
    HEAD_FILE,
    HEAD_FOLDER,
    FOLDER_PREFIX
  }

  @FunctionalInterface
  private interface KeyLookup {
    public Result lookup( String bucket, String key ) throws FileSystemException;
  }

  private final S3ClientSupplier client;
  private final AbstractFileName fileName;

  private ResolveHint hint;

  public S3TypeFetcher( S3ClientSupplier client, AbstractFileName fileName ) {
    this.client = client;
    this.fileName = fileName;
    this.hint = hintFromName( fileName );
  }

  private static final ResolveHint hintFromName( AbstractFileName name ) {
    return switch ( name.getType() ) {
      case FILE, FILE_OR_FOLDER, IMAGINARY -> ResolveHint.HEAD_FILE;
      case FOLDER -> ResolveHint.HEAD_FOLDER;
    };
  }

  /**
   * Fetches type from server, along with metadata if available
   * @throws FileSystemException client errors
   * @throws S3Exception (unchecked) unexpected S3 errors
   **/
  public Result fetchType( String bucket, String key ) throws FileSystemException {
    if ( key.isEmpty() ) {
      try {
        client.get().headBucket( HeadBucketRequest.builder().bucket( bucket ).build() );
        return noMeta( FileType.FOLDER );
      } catch ( NoSuchBucketException e ) {
        return notFound();
      }
    }

    return fetchNonRootType( bucket, key );
  }

  private Result fetchNonRootType( String bucket, String key ) throws FileSystemException {
    if ( log.isTraceEnabled() ) {
      log.trace( "Sync type for {} ({})", fileName.getPath(), fileName.getType() );
    }
    return switch ( hint ) {
      case HEAD_FILE -> fallbackOnImaginary( bucket, key, this::fetchFileHead, this::fetchFolderHead, this::fetchFolderPrefix );
      case HEAD_FOLDER -> fallbackOnImaginary( bucket, key, this::fetchFolderHead, this::fetchFolderPrefix, this::fetchFileHead );
      // here we only do 2 cases as by prefix always succeeds if folder exists
      case FOLDER_PREFIX -> fallbackOnImaginary( bucket, key, this::fetchFolderPrefix, this::fetchFileHead );
    };
  }

  private Result fallbackOnImaginary( String bucket, String key, KeyLookup... lookups ) throws FileSystemException {
    for ( KeyLookup lookup : lookups ) {
      var res = lookup.lookup( bucket, key );
      if ( res.type != FileType.IMAGINARY ) {
        return res;
      }
    }
    return notFound();
  }

  private static Result notFound() {
    return new Result( FileType.IMAGINARY, Optional.empty() );
  };

  private Result fetchFileHead( String bucket, String key ) throws FileSystemException {
    try {
      HeadObjectResponse metadata = client.get().headObject( HeadObjectRequest.builder()
        .bucket( bucket ).key( key ).build() );
      this.hint = ResolveHint.HEAD_FILE;
      return new Result( FileType.FILE, Optional.of( metadata ) );
    } catch ( NoSuchKeyException e ) {
      log.debug( "NoSuchKey HeadObject for {}/{}", bucket, key );
      return notFound();
    }
  }

  private Result fetchFolderHead( String bucket, String key ) throws FileSystemException {
    try {
      String keyWithDelimiter = key + S3FileObject.DELIMITER;
      HeadObjectResponse metadata = client.get().headObject( HeadObjectRequest.builder()
        .bucket( bucket ).key( keyWithDelimiter ).build() );
      this.hint = ResolveHint.HEAD_FOLDER;
      return new Result( FileType.FOLDER, Optional.of( metadata ) );
    } catch ( NoSuchKeyException e ) {
      log.debug( "NoSuchKey HeadObject for {}/{}/", bucket, key );
      return notFound();
    }
  }

  private Result fetchFolderPrefix( String bucket, String key ) throws FileSystemException {
    String keyWithDelimiter = key + S3FileObject.DELIMITER;
    ListObjectsV2Response objects = client.get().listObjectsV2( ListObjectsV2Request.builder()
      .bucket( bucket )
      .prefix( keyWithDelimiter )
      .delimiter( S3FileObject.DELIMITER )
      .maxKeys( 1 )
      .build() );
    if ( objects.commonPrefixes().isEmpty() && objects.contents().isEmpty() ) {
      return notFound();
    } else {
      this.hint = ResolveHint.FOLDER_PREFIX;
      return noMeta( FileType.FOLDER );
    }
  }

  private Result noMeta( FileType type ) {
    return new Result( type, Optional.empty() );
  }

}