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

package org.pentaho.di.trans.steps.s3csvinput;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3ObjectsProviderTest {
  private static final String BUCKET1_NAME = "Bucket1";
  private static final String BUCKET2_NAME = "Bucket2";
  private static final String BUCKET3_NAME = "Bucket3";
  private static final String UNKNOWN_BUCKET = "UnknownBucket";
  private static final Bucket BUCKET2 = new Bucket( BUCKET2_NAME );
  private static final Bucket BUCKET3 = new Bucket( BUCKET3_NAME );
  private static final Bucket BUCKET1 = new Bucket( BUCKET1_NAME );
  private static final String[] TEST_USER_BUCKETS_NAMES = { BUCKET1_NAME, BUCKET2_NAME, BUCKET3_NAME };
  private static final String[] EXPECTED_BUCKETS_NAMES = TEST_USER_BUCKETS_NAMES;
  private static ObjectListing bucket2Objects, bucket3Objects;
  private static S3Object testObject;
  private static AWSCredentials testUserCredentials;
  private S3ObjectsProvider provider;
  private AmazonS3 s3ClientMock;

  @BeforeClass public static void setUpBeforeClass() throws Exception {
    testUserCredentials = new BasicAWSCredentials(  "awsAccessKey", "awsSecretAccessKey" );
  }

  @AfterClass public static void tearDownAfterClass() throws Exception {
  }

  private static List<Bucket> generateTestBuckets( String[] TEST_USER_BUCKETS_NAMES ) {
    return Arrays.stream( TEST_USER_BUCKETS_NAMES ).map( p -> new Bucket( p ) ).collect( Collectors.toList() );
  }

  private static ObjectListing generateTestS3ObjectsInBucket( Bucket bucket, boolean isEmpty ) throws Exception {
    List<S3ObjectSummary> objectSummary;
    ObjectListing mockObjectListing = mock( ObjectListing.class );
    if ( !isEmpty ) {
      objectSummary = IntStream.rangeClosed( 1, 10 ).mapToObj( i -> buildObjectSummary( bucket.getName(), "file" + i, "DataString" + i ) )
          .collect( Collectors.toList() );
    } else {
      objectSummary = new ArrayList<S3ObjectSummary>();
    }
    when( mockObjectListing.getObjectSummaries() ).thenReturn( objectSummary );
    return mockObjectListing;
  }

  private static S3ObjectSummary buildObjectSummary( String bucketName, String key, String dataString ) {
    S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
    s3ObjectSummary.setKey( key );
    s3ObjectSummary.setBucketName( bucketName );
    return s3ObjectSummary;
  }

  private static S3Object buildS3Object( Bucket bucket, String key, String dataString ) {
    S3Object s3Object = new S3Object();
    s3Object.setKey( key );
    s3Object.setBucketName( bucket.getName() );
    s3Object.setObjectContent( new ByteArrayInputStream( dataString.getBytes() ) );
    return s3Object;
  }

  private static void logArray( Object[] array ) {
    Arrays.stream( array ).forEach( item -> System.out.println( item ) );
  }

  @Before public void setUp() throws Exception {
    bucket2Objects = generateTestS3ObjectsInBucket( BUCKET2, false );
    bucket3Objects = generateTestS3ObjectsInBucket( BUCKET3, true );
    testObject = buildS3Object( BUCKET1, "tests3Object", "TestString" );
    s3ClientMock = getS3ClientMock( testUserCredentials );
    provider = new S3ObjectsProvider( s3ClientMock );
  }

  @After public void tearDown() throws Exception {
  }

  @Test public void testGetBucketsNames() throws Exception {
    String[] actual = provider.getBucketsNames();
    assertArrayEquals( EXPECTED_BUCKETS_NAMES, actual );
  }

  @Test public void testGetBucketFound() throws Exception {
    Bucket actual = provider.getBucket( BUCKET2_NAME );
    assertEquals( BUCKET2_NAME, actual.getName() );
  }

  @Test public void testGetBucketNotFound_ReturnsNull() throws Exception {
    Bucket actual = provider.getBucket( UNKNOWN_BUCKET );
    assertNull( actual );
  }

  @Test public void testGetObjectsNamesInBucketWithObjects() throws Exception {
    List<String> actual = Arrays.asList( provider.getS3ObjectsNames( BUCKET2_NAME ) );
    assertEquals( bucket2Objects.getObjectSummaries().size(), actual.size() );
    List<S3ObjectSummary> expectedList = bucket2Objects.getObjectSummaries();
    expectedList.stream().forEach( i -> assertTrue( actual.contains( i.getKey() ) ) );
  }

  @Test public void testGetObjectsNamesInEmptyBucket() throws Exception {
    String[] actual = provider.getS3ObjectsNames( BUCKET3_NAME );
    logArray( actual );
    assertEquals( 0, actual.length );
  }

  @Test public void testGetObjectsNamesNoSuchBucket_ThrowsExeption() {
    try {
      provider.getS3ObjectsNames( UNKNOWN_BUCKET );
      fail( "The Exception: Unable to find bucket '" + UNKNOWN_BUCKET + "' should be thrown but it was not." );
    } catch ( Exception e ) {
      assertTrue( e.getLocalizedMessage().contains( "Unable to find bucket '" + UNKNOWN_BUCKET + "'" ) );
    }
  }

  @Test public void testGetS3ObjectInBucket() throws Exception {
    S3Object actual = provider.getS3Object( BUCKET1, "tests3Object" );
    assertNotNull( actual );
    assertEquals( testObject, actual );
  }

  @Test public void testGetS3ObjectLenght() throws Exception {
    long actual = provider.getS3ObjectContentLenght( BUCKET1, "test3Object" );
    assertEquals( testObject.getObjectMetadata().getContentLength(), actual );
  }

  private AmazonS3 getS3ClientMock( AWSCredentials credentials ) throws Exception {
    AmazonS3 s3Client = mock( AmazonS3.class );
    when( s3Client.listBuckets() ).thenReturn( generateTestBuckets( TEST_USER_BUCKETS_NAMES ) );
    // BUCKET2 - not empty bucket
    when( s3Client.listObjects( BUCKET2.getName() ) ).thenReturn( bucket2Objects );
    // BUCKET3 - empty bucket
    when( s3Client.listObjects( BUCKET3.getName() ) ).thenReturn( bucket3Objects );

    when( s3Client.getObject( any( String.class ), any( String.class ) ) ).thenReturn( testObject );
    ObjectMetadata mockMetaData = mock( ObjectMetadata.class );
    when( s3Client.getObjectMetadata( BUCKET1.getName(), "test3Object" ) ).thenReturn( mockMetaData );

    doReturn( false ).when( s3Client ).doesBucketExistV2( UNKNOWN_BUCKET );
    doReturn( true ).when( s3Client ).doesBucketExistV2( BUCKET2_NAME );
    doReturn( true ).when( s3Client ).doesBucketExistV2( BUCKET3_NAME );

    return s3Client;
  }
}
