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

package org.pentaho.s3.vfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.s3common.S3CommonFileSystemConfigBuilder;
import org.pentaho.s3common.S3CommonFileSystemTestUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AbstractAmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.junit.Test;

public class S3MoveTest {

  /** Paths starting with /, as required by spec, were causing the bucket to be parsed wrong */
  @Test
  public void testBucketOkWithProperPath() throws Exception {
    S3FileName fileName = new S3FileName( "s3", "bucket", "/bucket/proper/path/innit", FileType.FILE );
    FileName root = fileName.getParent().getParent();
    try ( S3FileSystem fileSystem = new S3FileSystem( root, new FileSystemOptions() );
        FileObject file = fileSystem.createFile( fileName ) ) {
      assertEquals( "bucket", ( (S3FileObject) file ).getS3BucketName() );
    }
  }

  @Test
  public void testGetPath() throws Exception {
    TestAmazonS3 s3 = new TestAmazonS3();
    S3FileName root = new S3FileName( "s3", "somewhere", "/", FileType.FOLDER );
    try ( S3FileSystem fs = S3CommonFileSystemTestUtil.createS3FileSystem( root, new FileSystemOptions(), s3 ) ) {

      FileObject file = fs.resolveFile( "s3://somewhere/over/the/rainbow" );
      assertEquals( Path.of( "somewhere", "over", "the", "rainbow" ), file.getPath() );

      FileObject file2 = fs.resolveFile( "s3://" );
      assertEquals( Path.of( "" ), file2.getPath() );
    }
  }

  /** A couple properties were getting lost in the trip */
  @Test
  public void testConfigBuilderGetsPropsRight() throws Exception {
    final String name = "nom";
    final String accessKey = "cHFw";
    final String secretKey = "e3264f3f1fe95dbf44b45bd8a33cd17a";
    final String endpoint = "http://localhost:9000";
    final String pathStyleAccess = "false";
    final String sigVersion = "AWSS3V4SignerType";

    S3Provider provider = new S3Provider();
    S3Details s3Details = new S3Details();
    s3Details.setName( name );
    s3Details.setAccessKey( accessKey );
    s3Details.setSecretKey( secretKey );
    s3Details.setEndpoint( endpoint );
    s3Details.setPathStyleAccess( pathStyleAccess );
    s3Details.setSignatureVersion( sigVersion );

    FileSystemOptions opts = provider.getOpts( s3Details );

    S3CommonFileSystemConfigBuilder cfg = new S3CommonFileSystemConfigBuilder( opts );
    assertEquals( name, cfg.getName() );
    assertEquals( accessKey, cfg.getAccessKey() );
    assertEquals( secretKey, cfg.getSecretKey() );
    assertEquals( endpoint, cfg.getEndpoint() );
    assertEquals( pathStyleAccess, cfg.getPathStyleAccess() );
    assertEquals( sigVersion, cfg.getSignatureVersion() );
  }

  @Test
  public void testMoveFile() throws Exception {
    final String bucket = "balde";
    S3FileName root = new S3FileName( "s3", "balde", "/balde/dir", FileType.FOLDER );
    TestAmazonS3 s3 = new TestAmazonS3();
    S3FileSystem fileSystem = S3CommonFileSystemTestUtil.createS3FileSystem( root, new FileSystemOptions(), s3 );
    // dir
    // +--source
    //    +--file
    // +--target
    s3.setUpObjectListing( bucket, "dir/", "source/" );
    s3.setUpObjectListing( bucket, "dir/source/", "file" );
    s3.setUpObjectListing( bucket, "dir/", "target/" );

    FileObject file = fileSystem.resolveFile( root.createName( "/balde/dir/source/file", FileType.FILE ) );
    assertTrue( file.exists() );
    FileObject target = fileSystem.resolveFile( root.createName( "/balde/dir/target/file_moved", FileType.IMAGINARY ) );
    assertFalse( target.exists() );

    // move it
    file.moveTo( target );

    assertFalse( file.exists() );
    assertTrue( target.exists() );

    assertEquals( "#CopyObject", 1, s3.copyObjectRequests.size() );
    CopyObjectRequest coreq = s3.copyObjectRequests.get( 0 );
    assertEquals( "balde", coreq.getSourceBucketName() );
    assertEquals( "dir/source/file", coreq.getSourceKey() );
    assertEquals( "balde", coreq.getDestinationBucketName() );
    assertEquals( "dir/target/file_moved", coreq.getDestinationKey() );

    assertEquals( "#DeleteObject", 1, s3.deleteObjectRequests.size() );
    assertArrayEquals( new String[] { "balde", "dir/source/file" }, s3.deleteObjectRequests.get( 0 ) );
  }


  @Test
  public void testMoveFolder() throws Exception {
    final String bucket = "balde";
    S3FileName root = new S3FileName( "s3", "balde", "/balde/dir", FileType.FOLDER );
    TestAmazonS3 s3 = new TestAmazonS3();
    S3FileSystem fileSystem = S3CommonFileSystemTestUtil.createS3FileSystem( root, new FileSystemOptions(), s3 );

    // dir
    // +--orig
    //    +--file1
    //    +--folder/
    //       +--file2
    // +--target
    s3.setUpObjectListing( bucket, "dir/", "orig/" );
    s3.setUpObjectListing( bucket, "dir/orig/", "file1", "folder/" );
    s3.setUpObjectListing( bucket, "dir/orig/folder/", "file2" );

    FileObject orig = fileSystem.resolveFile( root.createName( "/balde/dir/orig", FileType.FOLDER ) );
    FileObject target = fileSystem.resolveFile( root.createName( "/balde/dir/target", FileType.IMAGINARY ) );

    // move dir/orig/ --> dir/target/
    orig.moveTo( target );

    assertTrue( target.exists() );
    assertFalse( orig.exists() );

    fileSystem.close();

    s3.copyObjectRequests.forEach( req -> {
      assertEquals( bucket, req.getSourceBucketName() );
      assertEquals( bucket, req.getDestinationBucketName() );
    } );
    assertEquals( "# objects copied", 2, s3.copyObjectRequests.size() );
    Set<List<String>> srcDest =
        s3.copyObjectRequests.stream().map( req -> Arrays.asList(
          req.getSourceKey(), req.getDestinationKey() ) ).collect( Collectors.toSet() );
    assertEquals( 2, srcDest.size() );
    assertTrue( "file1 copied", srcDest.contains( Arrays.asList( "dir/orig/file1", "dir/target/file1" ) ) );
    assertTrue( "file2 copied", srcDest.contains( Arrays.asList( "dir/orig/folder/file2", "dir/target/folder/file2" ) ) );

    assertEquals( 2, s3.deleteObjectRequests.size() );
    Set<String> deleted = s3.deleteObjectRequests.stream().map( arr -> arr[1] ).collect(Collectors.toSet());
    assertTrue( "file1 deleted", deleted.contains( "dir/orig/file1" ) );
    assertTrue( "file2 deleted", deleted.contains( "dir/orig/folder/file2" ) );
  }

  private static class TestAmazonS3 extends AbstractAmazonS3 {
    private Map<String, BucketInfo> buckets = new HashMap<>();

    private static class BucketInfo {
      public Map<String, ObjectListing> objListings = new HashMap<>();
      public Set<String> objects = new HashSet<>();
    }

    List<PutObjectRequest> putObjectRequests = new ArrayList<>();
    List<CopyObjectRequest> copyObjectRequests = new ArrayList<>();
    List<String[]> deleteObjectRequests = new ArrayList<>();

    void setUpObjectListing( String bucket, String prefix, String... children ) {
      // an object listing (v2) returns an ObjectSummary for self and each child FILE
      // and a common prefix String for each child "folder"
      BucketInfo buc = buckets.computeIfAbsent( bucket, bname -> new BucketInfo() );
      ObjectListing list = new ObjectListing();
      List<String> commonPrefixes = new ArrayList<>();
      list.setTruncated( false );
      list.getObjectSummaries().add( getObjectSummary( StringUtils.appendIfMissing( prefix, "/" ) ) );
      list.setPrefix( StringUtils.removeEnd( prefix, "/" ) );
      for ( String child : children ) {
        child = StringUtils.appendIfMissing( prefix, "/" ) + child;
        if ( child.endsWith("/") ) {
          commonPrefixes.add( child );
        } else {
          buc.objects.add( child );
          list.getObjectSummaries().add( getObjectSummary( child ) );
        }
      }
      list.setCommonPrefixes( commonPrefixes );
      buc.objListings.put( prefix, list );
    }

    private static S3ObjectSummary getObjectSummary( String key ) {
      S3ObjectSummary sum = new S3ObjectSummary();
      sum.setKey( key );
      return sum;
    }

    @Override
    public boolean doesBucketExistV2( String bucketName ) throws SdkClientException, AmazonServiceException {
      return buckets.containsKey( bucketName );
    }

    @Override
    public ObjectListing listObjects( ListObjectsRequest listObjectsRequest )
      throws SdkClientException, AmazonServiceException {
      String bucket = listObjectsRequest.getBucketName();
      String prefix = listObjectsRequest.getPrefix();
      return Optional.ofNullable( buckets.get( bucket ) ).map( b -> b.objListings.get( prefix ) )
          .orElseGet( ObjectListing::new );
    }

    @Override
    public ObjectMetadata getObjectMetadata( GetObjectMetadataRequest objectMetadataRequest )
      throws SdkClientException, AmazonServiceException {
      String bucket = objectMetadataRequest.getBucketName();
      String key = objectMetadataRequest.getKey();
      return getObjectMetadata( bucket, key );
    }

    @Override
    public ObjectMetadata getObjectMetadata( String bucket, String key )
      throws SdkClientException, AmazonServiceException {
      Set<String> objects = buckets.get( bucket ).objects;

      if ( objects.contains( key ) || objects.contains( key + '/' ) ) {
        // actual metadata barely used
        return new ObjectMetadata();
      } else {
        AmazonS3Exception e = new AmazonS3Exception( "404 not found and stuff" );
        // code is checking for this error
        e.setErrorCode( "NoSuchKey" );
        throw e;
      }
    }

    @Override
    public PutObjectResult putObject( PutObjectRequest putObjectRequest )
      throws SdkClientException, AmazonServiceException {
      putObjectRequests.add( putObjectRequest );
      buckets.get( putObjectRequest.getBucketName() ).objects.add( putObjectRequest.getKey() );
      // not used
      return new PutObjectResult();
    }

    @Override
    public S3Object getObject( String bucket, String key ) throws SdkClientException, AmazonServiceException {
      S3Object obj = new S3Object();
      obj.setBucketName(bucket);
      obj.setKey(key);
      obj.setObjectMetadata( getObjectMetadata(bucket, key) );
      return obj;
    }

    @Override
    public CopyObjectResult copyObject( CopyObjectRequest copyObjectRequest )
      throws SdkClientException, AmazonServiceException {
      copyObjectRequests.add( copyObjectRequest );
      buckets.get( copyObjectRequest.getDestinationBucketName() ).objects.add( copyObjectRequest.getDestinationKey() );
      return new CopyObjectResult();
    }

    @Override
    public void deleteObject( String bucketName, String key ) throws SdkClientException, AmazonServiceException {
      deleteObjectRequests.add( new String[] {
        bucketName, key } );
      buckets.get( bucketName ).objects.remove( key );
    }
  }

}
