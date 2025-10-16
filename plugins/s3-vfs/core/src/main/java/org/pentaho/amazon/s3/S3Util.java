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

package org.pentaho.amazon.s3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class S3Util {

  /** System property name for the AWS Endpoint (This is for Minio) */
  public static final String ENDPOINT_SYSTEM_PROPERTY = "aws.endpoint";

  /** System property name for the AWS Signature version (This is for Minio) */
  public static final String SIGNATURE_VERSION_SYSTEM_PROPERTY = "aws.endpoint";

  /** System property name for the AWS access key ID */
  public static final String ACCESS_KEY_SYSTEM_PROPERTY = "aws.accessKeyId";

  /** System property name for the AWS secret key */
  public static final String SECRET_KEY_SYSTEM_PROPERTY = "aws.secretKey";

  /** Environment variable for the AWS region */
  public static final String AWS_REGION = "AWS_REGION";

  /** Environment variable for the specific location of the AWS config file */
  public static final String AWS_CONFIG_FILE = "AWS_CONFIG_FILE";

  /** AWS configuration folder */
  public static final String AWS_FOLDER = ".aws";

  /** Configuration file name */
  public static final String CONFIG_FILE = "config";

  /** Regex for detecting S3 credentials in URI */
  private static final String URI_AWS_CREDENTIALS_REGEX = "(s3[an]?:\\/)?\\/(?<fullkeys>(?<keys>.*:.*)@)?s3[an]?\\/?((?<bucket>[^\\/]+)(?<path>.*))?";

  /** to be used with getKeysFromURI to get the FULL KEYS GROUP **/
  private static final String URI_AWS_FULL_KEYS_GROUP = "fullkeys";

  /** to be used with getKeysFromURI to get the KEYS GROUP **/
  private static final String URI_AWS_KEYS_GROUP = "keys";

  /** to be used with getKeysFromURI to get the BUCKET GROUP **/
  private static final String URI_AWS_BUCKET_GROUP = "bucket";

  /** to be used with getKeysFromURI to get the PATH GROUP **/
  private static final String URI_AWS_PATH_GROUP = "path";

  public static boolean hasChanged( String previousValue, String currentValue ) {
    if ( !isEmpty( previousValue ) && isEmpty( currentValue ) ) {
      return true;
    }
    if ( isEmpty( previousValue ) && !isEmpty( currentValue ) ) {
      return true;
    }
    return !isEmpty( previousValue ) && !isEmpty( currentValue ) && !currentValue.equals( previousValue );
  }

  public static boolean isEmpty( String value ) {
    return value == null || value.length() == 0;
  }

  /**
   * Extracts from a S3 URI the credentials including the S3 protocol.
   * Uses URI_AWS_CREDENTIALS_REGEX and the group URI_AWS_FULL_KEYS_GROUP
   * @param uri string representing the URI
   * @return the full keys extracted from the URI representing "<accesskey>:<secretkey>@s3[na]"
   */
  public static String getFullKeysFromURI( String uri ) {
    Matcher match = Pattern.compile( URI_AWS_CREDENTIALS_REGEX ).matcher( uri );
    return match.find() ? match.group( URI_AWS_FULL_KEYS_GROUP ) : null;
  }

  /**
   * Extracts from a S3 URI the credentials.
   * Uses URI_AWS_CREDENTIALS_REGEX and the group URI_AWS_KEYS_GROUP
   * @param uri string representing the URI
   * @return the full keys extracted from the URI representing "<accesskey>:<secretkey>"
   */
  public static S3Keys getKeysFromURI( String uri ) {
    Matcher match = Pattern.compile( URI_AWS_CREDENTIALS_REGEX ).matcher( uri );
    return match.find() && match.group( URI_AWS_KEYS_GROUP ) != null ? new S3Keys( match.group( URI_AWS_KEYS_GROUP ) ) : null;
  }

  /**
   * S3Keys object returned by @see getKeysFromURI
   * contains accessKey and secretKey
   */
  public static class S3Keys {

    private String accessKey;
    private String secretKey;

    private S3Keys( String keys ) {
      String[] splitKeys = keys.split( ":" );
      accessKey = splitKeys[0];
      secretKey = splitKeys[1];
    }

    public String getAccessKey() {
      return accessKey;
    }

    public String getSecretKey() {
      return secretKey;
    }
  }

  private S3Util() { }
}
