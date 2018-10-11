/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S3ObjectsProviderTest {
  private static final String BUCKET1_NAME = "Bucket1";
  private static final String BUCKET2_NAME = "Bucket2";
  private static final String BUCKET3_NAME = "Bucket3";
  private static final S3Bucket BUCKET2 = new S3Bucket( BUCKET2_NAME );
  private static final S3Bucket BUCKET3 = new S3Bucket( BUCKET3_NAME );
  private static final S3Bucket BUCKET1 = new S3Bucket( BUCKET1_NAME );
  private static final String[] TEST_USER_BUCKETS_NAMES = { BUCKET1_NAME, BUCKET2_NAME, BUCKET3_NAME };
  private static final String[] EXPECTED_BUCKETS_NAMES = TEST_USER_BUCKETS_NAMES;
  private static S3Object[] bucket2Objects, bucket3Objects;
  private static S3Object testObject;
  private static AWSCredentials testUserCredentials;
  private S3ObjectsProvider provider;
  private S3Service s3serviceMock;

  @BeforeClass public static void setUpBeforeClass() throws Exception {
    testUserCredentials = new AWSCredentials( "awsAccessKey", "awsSecretAccessKey" );
  }

  @AfterClass public static void tearDownAfterClass() throws Exception {
  }

  private static S3Bucket[] generateTestBuckets( String[] TEST_USER_BUCKETS_NAMES ) {
    return Arrays.stream( TEST_USER_BUCKETS_NAMES ).map( p -> new S3Bucket( p ) ).toArray( S3Bucket[]::new );
  }

  private static S3Object[] generateTestS3ObjectsInBucket( S3Bucket bucket, boolean isEmpty ) throws Exception {
    if ( !isEmpty ) {
      return IntStream.rangeClosed( 1, 10 ).mapToObj( i -> buildS3Object( bucket, "file" + i, "DataString" + i ) )
          .toArray( S3Object[]::new );
    }
    return new S3Object[] {};
  }

  private static S3Object buildS3Object( S3Bucket bucket, String key, String dataString ) {
    try {
      return new S3Object( bucket, key, dataString );
    } catch ( NoSuchAlgorithmException | IOException e ) {
      // do nothing
    }
    return null;
  }

  private static void logArray( Object[] array ) {
    Arrays.stream( array ).forEach( item -> System.out.println( item ) );
  }

  @Before public void setUp() throws Exception {
    bucket2Objects = generateTestS3ObjectsInBucket( BUCKET2, false );
    bucket3Objects = generateTestS3ObjectsInBucket( BUCKET3, true );
    testObject = new S3Object( BUCKET1, "tests3Object", "TestString" );
    s3serviceMock = getS3ServiceMock( testUserCredentials );
    provider = new S3ObjectsProvider( s3serviceMock );
  }

  @After public void tearDown() throws Exception {
  }

  @Test public void testGetBucketsNames() throws Exception {
    String[] actual = provider.getBucketsNames();
    assertArrayEquals( EXPECTED_BUCKETS_NAMES, actual );
  }

  @Test public void testGetBucketFound() throws Exception {
    S3Bucket actual = provider.getBucket( BUCKET2_NAME );
    assertEquals( BUCKET2_NAME, actual.getName() );
  }

  @Test public void testGetBucketNotFound_ReturnsNull() throws Exception {
    S3Bucket actual = provider.getBucket( "UnknownBucket" );
    assertNull( actual );
  }

  @Test public void testGetObjectsNamesInBucketWithObjects() throws Exception {
    String[] actual = provider.getS3ObjectsNames( BUCKET2_NAME );
    assertEquals( bucket2Objects.length, actual.length );
    IntStream.rangeClosed( 0, actual.length - 1 )
        .forEachOrdered( i -> assertEquals( bucket2Objects[i].getName(), actual[i] ) );
    assertEquals( bucket2Objects.length, actual.length );
  }

  @Test public void testGetObjectsNamesInEmptyBucket() throws Exception {
    String[] actual = provider.getS3ObjectsNames( BUCKET3_NAME );
    logArray( actual );
    assertEquals( 0, actual.length );
  }

  @Test public void testGetObjectsNamesNoSuchBucket_ThrowsExeption() {
    try {
      provider.getS3ObjectsNames( "UnknownBucket" );
      fail( "The Exception: Unable to find bucket 'UnknownBucket' should be thrown but it was not." );
    } catch ( Exception e ) {
      assertTrue( e.getLocalizedMessage().contains( "Unable to find bucket 'UnknownBucket'" ) );
    }
  }

  @Test public void testGetS3ObjectInBucket() throws Exception {
    S3Object actual = provider.getS3Object( BUCKET1, "tests3Object" );
    assertNotNull( actual );
    assertEquals( testObject, actual );
  }

  @Test public void testGetS3ObjectLenght() throws Exception {
    long actual = provider.getS3ObjectContentLenght( BUCKET1, "tests3Object" );
    assertEquals( testObject.getContentLength(), actual );
  }

  private S3Service getS3ServiceMock( AWSCredentials credentials ) throws Exception {
    S3Service service = mock( S3Service.class );
    when( service.listAllBuckets() ).thenReturn( generateTestBuckets( TEST_USER_BUCKETS_NAMES ) );
    // BUCKET2 - not empty bucket
    when( service.listObjects( (S3Bucket) argThat( this.new S3BucketArgumentMatcher( BUCKET2 ) ) ) )
        .thenReturn( bucket2Objects );
    // BUCKET3 - empty bucket
    when( service.listObjects( (S3Bucket) argThat( this.new S3BucketArgumentMatcher( BUCKET3 ) ) ) )
        .thenReturn( bucket3Objects );
    when( service.getObject( any( S3Bucket.class ), any( String.class ), any(), any(), any(), any(), any(), any() ) )
        .thenReturn( testObject );
    when( service.getObjectDetails( any( S3Bucket.class ), any( String.class ), any(), any(), any(), any() ) )
        .thenReturn( testObject );
    return service;
  }

  class S3BucketArgumentMatcher extends ArgumentMatcher {
    private S3Bucket expected;

    S3BucketArgumentMatcher( S3Bucket expected ) {
      super();
      this.expected = expected;
    }

    public boolean matches( Object o ) {
      if ( o instanceof S3Bucket ) {
        S3Bucket bucket = (S3Bucket) o;
        if ( ( bucket != null ) && bucket.getName().equals( this.expected.getName() ) ) {
          return true;
        }
      }
      return false;
    }
  }

}
