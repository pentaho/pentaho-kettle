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

import java.util.Arrays;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

public class S3ObjectsProvider {
  private S3Service service;

  public S3ObjectsProvider( S3Service service ) {
    super();
    this.service = service;
  }

  /**
   * Returns the buckets belonging to the service user
   *
   * @return the list of buckets owned by the service user.
   * @throws S3ServiceException
   */
  private S3Bucket[] getBuckets() throws S3ServiceException {
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
      return service.listAllBuckets();
    } finally {
      Thread.currentThread().setContextClassLoader( currentClassLoader );
    }
  }

  /**
   * Returns the names of buckets belonging to the service user
   *
   * @return the list of buckets names owned by the service user.
   * @throws S3ServiceException
   */
  public String[] getBucketsNames() throws S3ServiceException {
    return Arrays.stream( getBuckets() ).map( b -> b.getName() ).toArray( String[]::new );
  }

  /**
   * Returns the named bucket.
   *
   * @param bucketName
   *          the name of the bucket to find.
   * @return the bucket, or null if no the named bucket has found.
   * @throws S3ServiceException
   */
  public S3Bucket getBucket( String bucketName ) throws S3ServiceException {
    return Arrays.stream( getBuckets() ).filter( x -> bucketName.equals( x.getName() ) ).findFirst().orElse( null );
  }

  /**
   * Returns the objects in a bucket. The objects returned by this method contain only minimal information.
   *
   * @param bucket
   *          the bucket whose contents will be listed.
   * @return the set of objects contained in a bucket.
   * @throws S3ServiceException
   */
  private S3Object[] getS3Objects( S3Bucket bucket ) throws S3ServiceException {
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
      return service.listObjects( bucket );
    } finally {
      Thread.currentThread().setContextClassLoader( currentClassLoader );
    }
  }

  /**
   * Returns the objects names in a bucket.
   *
   * @param bucketName
   *          the bucket whose contents will be listed.
   * @return the set of names of objects contained in a bucket.
   * @throws Exception
   */
  public String[] getS3ObjectsNames( String bucketName ) throws Exception {
    S3Bucket bucket = getBucket( bucketName );
    if ( bucket == null ) {
      throw new Exception( Messages.getString( "S3DefaultService.Exception.UnableToFindBucket.Message", bucketName ) );
    }
    return Arrays.stream( getS3Objects( bucket ) ).map( b -> b.getKey() ).toArray( String[]::new );
  }

  /**
   * Returns an object representing the details and data of an item in S3.
   *
   * @param bucket
   *          the bucket containing the object.
   * @param objectKey
   *          the key identifying the object.
   * @param byteRangeStart
   *          include only a portion of the object's data - starting at this point
   * @param byteRangeEnd
   *          include only a portion of the object's data - ending at this point
   * @return the object with the given key in S3, including details and data
   * @throws S3ServiceException
   */
  public S3Object getS3Object( S3Bucket bucket, String objectKey, Long byteRangeStart, Long byteRangeEnd ) throws S3ServiceException {
    return service.getObject( bucket, objectKey, null, null, null, null, byteRangeStart, byteRangeEnd );
  }

  /**
   * Returns an object representing the details and data of an item in S3.
   *
   * @param bucket
   *          the bucket containing the object.
   * @param objectKey
   *          the key identifying the object.
   * @return the object with the given key in S3, including details and data
   * @throws S3ServiceException
   */
  public S3Object getS3Object( S3Bucket bucket, String objectKey ) throws S3ServiceException {
    return getS3Object( bucket, objectKey, null, null );
  }

  /**
   * Returns an object representing the details of an item in S3. The object is returned without the object's data.
   *
   * @param bucket
   *          the bucket containing the object.
   * @param objectKey
   *          the key identifying the object.
   * @return the object with the given key in S3, including only general details and metadata (not the data input
   *         stream)
   * @throws S3ServiceException
   */
  private StorageObject getS3ObjectDetails( S3Bucket bucket, String objectKey ) throws S3ServiceException {
    return service.getObjectDetails( bucket, objectKey, null, null, null, null );
  }

  /**
   * Returns the content length, or size, of this object's data, or 0 if it is unknown.
   *
   * @param bucket
   *          the bucket containing the object.
   * @param objectKey
   *          the key identifying the object.
   * @return the content length, or size, of this object's data, or 0 if it is unknown
   * @throws S3ServiceException
   */
  public long getS3ObjectContentLenght( S3Bucket bucket, String objectKey ) throws S3ServiceException {
    return getS3ObjectDetails( bucket, objectKey ).getContentLength();
  }

}
