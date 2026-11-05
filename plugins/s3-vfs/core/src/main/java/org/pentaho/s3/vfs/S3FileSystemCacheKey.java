package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.FileSystemOptions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Uses VFS option comparison, with actual S3 values as a tie-breaker for hash collisions. */
final class S3FileSystemCacheKey implements Comparable<S3FileSystemCacheKey> {
  private static final Comparator<String> VALUES = Comparator.nullsFirst( Comparator.naturalOrder() );

  private final Comparable<?> root;
  private final FileSystemOptions options;

  S3FileSystemCacheKey( Comparable<?> root, FileSystemOptions options ) {
    this.root = root;
    this.options = options == null ? new FileSystemOptions() : (FileSystemOptions) options.clone();
  }

  @Override
  public int compareTo( S3FileSystemCacheKey other ) {
    int comparison = compareRoots( other.root );
    if ( comparison == 0 ) {
      comparison = options.compareTo( other.options );
    }
    if ( comparison == 0 ) {
      List<String> values = s3Values( options );
      List<String> otherValues = s3Values( other.options );
      for ( int i = 0; comparison == 0 && i < values.size(); i++ ) {
        comparison = VALUES.compare( values.get( i ), otherValues.get( i ) );
      }
    }
    return comparison;
  }

  @Override
  public boolean equals( Object other ) {
    return this == other || other instanceof S3FileSystemCacheKey key && compareTo( key ) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash( root, options, s3Values( options ) );
  }

  private static List<String> s3Values( FileSystemOptions options ) {
    var builder = new S3FileSystemConfigBuilder( options );
    // Keep aligned with the S3 options exposed by the builder.
    return Arrays.asList( builder.getName(), builder.getAccessKey(), builder.getSecretKey(), builder.getSessionToken(),
      builder.getRegion(), builder.getCredentialsFile(), builder.getProfileName(), builder.getEndpoint(),
      builder.getSignatureVersion(), builder.getPathStyleAccess(), builder.getDefaultS3Config(),
      builder.getConnectionType(), builder.getTrustStoreFilePath(), builder.getTrustStorePassword(),
      builder.getTrustAll(), builder.getKeyStoreFilePath(), builder.getKeyStorePassword(),
      Boolean.toString( builder.useDefaults() ) );
  }

  @SuppressWarnings( "unchecked" )
  private int compareRoots( Comparable<?> other ) {
    return ( (Comparable<Object>) root ).compareTo( other );
  }

  @Override
  public String toString() {
    return "S3FileSystemCacheKey[redacted]";
  }
}