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


package org.pentaho.di.core.database;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_ACCESS_KEY_ID;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_CREDENTIALS;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_PROFILE_NAME;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_SECRET_ACCESS_KEY;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_SESSION_TOKEN;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.JDBC_AUTH_METHOD;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.PROFILE_CREDENTIALS;

/**
 * Unit tests for RedshiftDatabaseMeta
 */
public class RedshiftDatabaseMetaTest {

  private RedshiftDatabaseMeta dbMeta;

  @BeforeClass
  public static void beforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    dbMeta = new RedshiftDatabaseMeta();
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testExtraOption() {
    Map<String, String> opts = dbMeta.getExtraOptions();
    assertNotNull( opts );
    assertEquals( "true", opts.get( "REDSHIFT.tcpKeepAlive" ) );
  }

  @Test
  public void testGetDefaultDatabasePort() throws Exception {
    assertEquals( 5439, dbMeta.getDefaultDatabasePort() );
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    assertEquals( -1, dbMeta.getDefaultDatabasePort() );
  }

  @Test
  public void testGetDriverClass() throws Exception {
    assertEquals( "com.amazon.redshift.jdbc.Driver", dbMeta.getDriverClass() );
  }

  @Test
  public void testGetURL() throws Exception {
    assertEquals( "jdbc:redshift://:/", dbMeta.getURL( "", "", "" ) );
    assertEquals( "jdbc:redshift://rs.pentaho.com:4444/myDB",
      dbMeta.getURL( "rs.pentaho.com", "4444", "myDB" ) );
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    dbMeta.addAttribute( JDBC_AUTH_METHOD, IAM_CREDENTIALS );
    dbMeta.addAttribute( IAM_ACCESS_KEY_ID, Encr.encryptPassword( "myid" ) );
    dbMeta.addAttribute( IAM_SECRET_ACCESS_KEY, Encr.encryptPassword( "mysecretkey" ) );
    dbMeta.addAttribute( IAM_SESSION_TOKEN, Encr.encryptPassword( "mytoken" ) );
    assertEquals(
      "jdbc:redshift:iam://amazonhost:12345/foodmart",
      dbMeta.getURL( "amazonhost", "12345", "foodmart" ) );
    HashMap<String, String> optionalOptions = new HashMap<>();
    dbMeta.putOptionalOptions( optionalOptions );
    assertEquals( "myid", optionalOptions.get( "REDSHIFT.AccessKeyID" ) );
    assertEquals( "mysecretkey", optionalOptions.get( "REDSHIFT.SecretAccessKey" ) );
    assertEquals( "mytoken", optionalOptions.get( "REDSHIFT.SessionToken" ) );
    dbMeta.addAttribute( JDBC_AUTH_METHOD, PROFILE_CREDENTIALS );
    dbMeta.addAttribute( IAM_PROFILE_NAME, "super" );
    optionalOptions.clear();
    dbMeta.putOptionalOptions( optionalOptions );
    assertEquals( "super", optionalOptions.get( "REDSHIFT.Profile" ) );
  }

  @Test
  public void testGetExtraOptionsHelpText() throws Exception {
    assertEquals( "http://docs.aws.amazon.com/redshift/latest/mgmt/configure-jdbc-connection.html",
      dbMeta.getExtraOptionsHelpText() );
  }

  @Test
  public void testIsFetchSizeSupported() throws Exception {
    assertFalse( dbMeta.isFetchSizeSupported() );
  }

  @Test
  public void testSupportsSetMaxRows() throws Exception {
    assertFalse( dbMeta.supportsSetMaxRows() );
  }

  @Test
  public void testGetUsedLibraries() throws Exception {
    String[] libs = dbMeta.getUsedLibraries();
    assertNotNull( libs );
    assertEquals( 1, libs.length );
    assertEquals( "RedshiftJDBC4_1.0.10.1010.jar", libs[0] );
  }

  @Test
  public void setConnectionSpecificInfoFromAttributes_setsAllAttributes() {
    dbMeta = new RedshiftDatabaseMeta();
    Map<String, String> attributes = new HashMap<>();
    attributes.put( RedshiftDatabaseMeta.JDBC_AUTH_METHOD, "IAM Credentials" );
    attributes.put( RedshiftDatabaseMeta.IAM_ACCESS_KEY_ID, "testAccessKeyId" );
    attributes.put( RedshiftDatabaseMeta.IAM_SECRET_ACCESS_KEY, "testSecretAccessKey" );
    attributes.put( RedshiftDatabaseMeta.IAM_SESSION_TOKEN, "testSessionToken" );
    attributes.put( RedshiftDatabaseMeta.IAM_PROFILE_NAME, "testProfileName" );
    attributes.put( RedshiftDatabaseMeta.AUTHENTICATION_METHOD, "testAuthMethod" );
    attributes.put( RedshiftDatabaseMeta.IAM_ROLE, "testIamRole" );
    attributes.put( RedshiftDatabaseMeta.AWS_ACCESS_KEY, "testAwsAccessKey" );
    attributes.put( RedshiftDatabaseMeta.AWS_ACCESS_KEY_ID, "testAwsAccessKeyId" );

    dbMeta.setConnectionSpecificInfoFromAttributes( attributes );

    assertEquals( "IAM Credentials", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.JDBC_AUTH_METHOD ) );
    assertEquals( "testAccessKeyId", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.IAM_ACCESS_KEY_ID ) );
    assertEquals( "testSecretAccessKey", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.IAM_SECRET_ACCESS_KEY ) );
    assertEquals( "testSessionToken", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.IAM_SESSION_TOKEN ) );
    assertEquals( "testProfileName", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.IAM_PROFILE_NAME ) );
    assertEquals( "testAuthMethod", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.AUTHENTICATION_METHOD ) );
    assertEquals( "testIamRole", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.IAM_ROLE ) );
    assertEquals( "testAwsAccessKey", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.AWS_ACCESS_KEY ) );
    assertEquals( "testAwsAccessKeyId", dbMeta.getAttributes().getProperty( RedshiftDatabaseMeta.AWS_ACCESS_KEY_ID ) );
  }
}
