/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.core.database;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaString;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.IS_ALWAYS_ENCRYPTION_ENABLED;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.CLIENT_ID;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.CLIENT_SECRET_KEY;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.JDBC_AUTH_METHOD;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.SQL_AUTHENTICATION;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.ACTIVE_DIRECTORY_MFA;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.ACTIVE_DIRECTORY_PASSWORD;

public class AzureSqlDataBaseMetaTest {

  private AzureSqlDataBaseMeta dbMeta;

  @Before
  public void setUp() throws Exception {
    dbMeta = new AzureSqlDataBaseMeta();
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testGetAccessTypeList() {
    int[] accessTypeList = dbMeta.getAccessTypeList();
    assertEquals( 0, accessTypeList[0] );
    assertEquals( 4, accessTypeList[1] );
  }

  @Test
  public void testGetDriverClass() throws Exception {
    assertEquals( "com.microsoft.sqlserver.jdbc.SQLServerDriver", dbMeta.getDriverClass() );
  }

  @Test
  public void testAlwaysEncryptionParameterIncludedInUrl() {
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    dbMeta.addAttribute( IS_ALWAYS_ENCRYPTION_ENABLED, "true" );
    dbMeta.addAttribute( CLIENT_ID, "dummy" );
    dbMeta.addAttribute( CLIENT_SECRET_KEY, "xxxxx" );
    String expectedUrl = "jdbc:sqlserver://abc.database.windows.net:1433;database=AzureDB;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;columnEncryptionSetting=Enabled;keyVaultProviderClientId=dummy;keyVaultProviderClientKey=xxxxx;";
    String actualUrl = dbMeta.getURL( "abc.database.windows.net", "1433", "AzureDB" );
    assertEquals( expectedUrl, actualUrl );
  }

  @Test
  public void testGetUrlWithSqlAuth(){
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    dbMeta.addAttribute( IS_ALWAYS_ENCRYPTION_ENABLED, "false" );
    dbMeta.addAttribute( JDBC_AUTH_METHOD, SQL_AUTHENTICATION );
    String expectedUrl = "jdbc:sqlserver://abc.database.windows.net:1433;database=AzureDB;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    String actualUrl = dbMeta.getURL( "abc.database.windows.net", "1433", "AzureDB" );
    assertEquals( expectedUrl, actualUrl );
  }

  @Test
  public void testGetUrlWithAadPasswordAuth(){
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    dbMeta.addAttribute( IS_ALWAYS_ENCRYPTION_ENABLED, "false" );
    dbMeta.addAttribute( JDBC_AUTH_METHOD, ACTIVE_DIRECTORY_PASSWORD );
    String expectedUrl = "jdbc:sqlserver://abc.database.windows.net:1433;database=AzureDB;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;authentication=ActiveDirectoryPassword;";
    String actualUrl = dbMeta.getURL( "abc.database.windows.net", "1433", "AzureDB" );
    assertEquals( expectedUrl, actualUrl );
  }

  @Test
  public void testGetUrlWithAadMfaAuth(){
    dbMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    dbMeta.addAttribute( IS_ALWAYS_ENCRYPTION_ENABLED, "false" );
    dbMeta.addAttribute( JDBC_AUTH_METHOD, ACTIVE_DIRECTORY_MFA );
    String expectedUrl = "jdbc:sqlserver://abc.database.windows.net:1433;database=AzureDB;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;authentication=ActiveDirectoryInteractive;";
    String actualUrl = dbMeta.getURL( "abc.database.windows.net", "1433", "AzureDB" );
    assertEquals( expectedUrl, actualUrl );
  }

  @Test
  public void testGetValueFromResultSet() throws SQLException, KettleDatabaseException {
    ResultSet rs = mock( ResultSet.class );
    //Binary Data
    Mockito.when( rs.getString( 1 ) ).thenReturn( "HODBACXXXXAAA" );
    ValueMetaBinary tb = new ValueMetaBinary( "HODBACXXXXAAA" );
    assertEquals( "HODBACXXXXAAA", dbMeta.getValueFromResultSet( rs,tb,0 ) );

   //Super class function calling
    Mockito.when( rs.getString( 2 ) ).thenReturn( "AzureDB" );
    ValueMetaString ts = new ValueMetaString( "AzureDB" );
    assertEquals( "AzureDB", dbMeta.getValueFromResultSet( rs,ts,1 ) );

    //ResultSet was null
    Mockito.when( rs.wasNull() ).thenReturn( true );
    assertNull( dbMeta.getValueFromResultSet( rs,tb,2 ) );

  }

  @Test(expected = KettleDatabaseException.class)
  public void testGetValueFromResultSetWhenExceptionIsComing() throws SQLException, KettleDatabaseException {
    ResultSet rs = mock( ResultSet.class );
    Mockito.when( rs.getString( 3 ) ).thenThrow( SQLException.class );
    ValueMetaString ts = new ValueMetaString( "AzureDB" );
    dbMeta.getValueFromResultSet( rs,ts,2 );
  }

}
