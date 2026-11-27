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

package org.pentaho.amazon.s3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class S3Util {

  /** System property name for the AWS access key ID */
  public static final String ACCESS_KEY_SYSTEM_PROPERTY = "aws.accessKeyId";

  /** System property name for the AWS secret key */
  public static final String SECRET_KEY_SYSTEM_PROPERTY = "aws.secretAccessKey";

  /** System property name for the AWS session token */
  public static final String SESSION_TOKEN_SYSTEM_PROPERTY = "aws.sessionToken";

  /** Only available signature version as of aws sdk v2 */
  public static final String SIG_VERSION = "AWSS3V4SignerType";

  /** Regex for detecting S3 credentials in URI */
  private static final String URI_AWS_CREDENTIALS_REGEX = "(s3[an]?:\\/)?\\/(?<fullkeys>(?<keys>.*:.*)@)?s3[an]?\\/?((?<bucket>[^\\/]+)(?<path>.*))?";

  /** to be used with getKeysFromURI to get the FULL KEYS GROUP **/
  private static final String URI_AWS_FULL_KEYS_GROUP = "fullkeys";

  /** to be used with getKeysFromURI to get the KEYS GROUP **/
  private static final String URI_AWS_KEYS_GROUP = "keys";

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
