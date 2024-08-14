/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.database;

import org.pentaho.di.core.encryption.Encr;

import java.util.Arrays;
import java.util.Map;

import static org.pentaho.di.core.util.Utils.isEmpty;

/**
 * @author mbatchelor
 *
 */
public class RedshiftDatabaseMeta extends PostgreSQLDatabaseMeta {

  public static final String IAM_ROLE = "iamRole";
  public static final String AWS_ACCESS_KEY_ID = "awsAccessKeyId";
  public static final String AWS_ACCESS_KEY = "awsAccessKey";
  public static final String AUTHENTICATION_METHOD = "awsAuthenticationMethod";
  public static final String STANDARD_CREDENTIALS = "Standard";
  public static final String IAM_CREDENTIALS = "IAM Credentials";
  public static final String PROFILE_CREDENTIALS = "Profile";

  public static final String JDBC_AUTH_METHOD = "jdbcAuthMethod";
  public static final String IAM_ACCESS_KEY_ID = "iamAccessKeyId";
  public static final String IAM_SECRET_ACCESS_KEY = "iamSecretAccessKey";
  public static final String IAM_SESSION_TOKEN = "iamSessionToken";
  public static final String IAM_PROFILE_NAME = "iamProfileName";

  public RedshiftDatabaseMeta() {
    addExtraOption( "REDSHIFT", "tcpKeepAlive", "true" );
    addExtraOption( "REDSHIFT", "loginTimeout", "10" );
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 5439;
    }
    return -1;
  }

  @Override
  public String getDriverClass() {
    return "com.amazon.redshift.jdbc.Driver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    if ( Arrays.asList( PROFILE_CREDENTIALS, IAM_CREDENTIALS ).contains( getAttribute( JDBC_AUTH_METHOD, "" ) ) ) {
      return "jdbc:redshift:iam://" + hostname + ":" + port + "/" + databaseName;
    } else {
      return "jdbc:redshift://" + hostname + ":" + port + "/" + databaseName;
    }
  }

  @Override public void putOptionalOptions( Map<String, String> extraOptions ) {
    if ( IAM_CREDENTIALS.equals( getAttribute( JDBC_AUTH_METHOD, "" ) ) ) {
      extraOptions.put( "REDSHIFT.AccessKeyID", Encr.decryptPassword( getAttribute( IAM_ACCESS_KEY_ID, "" ) ) );
      extraOptions.put( "REDSHIFT.SecretAccessKey", Encr.decryptPassword( getAttribute( IAM_SECRET_ACCESS_KEY, "" ) ) );
      extraOptions.put( "REDSHIFT.SessionToken", Encr.decryptPassword( getAttribute( IAM_SESSION_TOKEN, "" ) ) );
    } else if ( PROFILE_CREDENTIALS.equals( getAttribute( JDBC_AUTH_METHOD, "" ) ) ) {
      extraOptions.put( "REDSHIFT.Profile", getAttribute( IAM_PROFILE_NAME, "" ) );
    }
  }

  @Override
  public String getExtraOptionsHelpText() {
    return "http://docs.aws.amazon.com/redshift/latest/mgmt/configure-jdbc-connection.html";
  }

  /**
   * The superclass method checks whether or not the command setFetchSize() is supported by the driver. In the case of
   * Redshift, setFetchSize() is supported, but in the case of LIMIT, the Redshift driver will enforce that the value
   * for fetch size is less than or equal to the value specified in the LIMIT clause.
   *
   * To avoid these problems, this method (and supportsSetMaxRows()) returns false
   *
   * @return false
   */
  @Override
  public boolean isFetchSizeSupported() {
    return false;
  }

  /**
   * Redshift does not recognize the JDBC "setMaxRows" parameter
   *
   * @return false
   */
  @Override
  public boolean supportsSetMaxRows() {
    return false;
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "RedshiftJDBC4_1.0.10.1010.jar" };
  }

  public String getIamRole() {
    return getParamIfSet( IAM_ROLE, getAttributes().getProperty( IAM_ROLE ) );
  }

  public String getAwsAccessKeyId() {
    return getParamIfSet( AWS_ACCESS_KEY_ID, getAttributes().getProperty( AWS_ACCESS_KEY_ID ) );
  }

  public String getAwsAccessKey() {
    return getParamIfSet( AWS_ACCESS_KEY, getAttributes().getProperty( AWS_ACCESS_KEY ) );
  }

  public String getAwsAuthenticationMethod() {
    return getParamIfSet( AUTHENTICATION_METHOD, getAttributes().getProperty( AUTHENTICATION_METHOD ) );
  }

  @Override
  public String getXulOverlayFile() {
    return "redshift";
  }

  private String getParamIfSet( String param, String val ) {
    if ( !isEmpty( val ) ) {
      return "&" + param + "=" + val;
    }
    return "";
  }
}
