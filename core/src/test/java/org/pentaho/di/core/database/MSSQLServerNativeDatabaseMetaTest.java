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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

public class MSSQLServerNativeDatabaseMetaTest extends MSSQLServerDatabaseMetaTest {

  @Test
  public void testMSSQLOverrides() throws Exception {
    MSSQLServerNativeDatabaseMeta localNativeMeta = new MSSQLServerNativeDatabaseMeta();
    localNativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );

    assertEquals( "com.microsoft.sqlserver.jdbc.SQLServerDriver", localNativeMeta.getDriverClass() );

    assertEquals( "jdbc:sqlserver://FOO:1234;databaseName=WIBBLE;integratedSecurity=false",
        localNativeMeta.getURL( "FOO", "1234", "WIBBLE" ) );

    Properties attrs = new Properties();
    attrs.put( "MSSQLUseIntegratedSecurity", "false" );
    localNativeMeta.setAttributes( attrs );
    assertEquals( "jdbc:sqlserver://FOO:1234;databaseName=WIBBLE;integratedSecurity=false",
        localNativeMeta.getURL( "FOO", "1234", "WIBBLE" ) );
    attrs.put( "MSSQLUseIntegratedSecurity", "true" );
    assertEquals( "jdbc:sqlserver://FOO:1234;databaseName=WIBBLE;integratedSecurity=true",
        localNativeMeta.getURL( "FOO", "1234", "WIBBLE" ) );

  }

  @Test
  public void setConnectionSpecificInfoFromAttributes_setsAllAttributes() {
    MSSQLServerNativeDatabaseMeta dbMeta = new MSSQLServerNativeDatabaseMeta();
    Map<String, String> attributes = new HashMap<>();
    attributes.put( MSSQLServerNativeDatabaseMeta.ATTRIBUTE_MSSQL_DOUBLE_DECIMAL_SEPARATOR, "Y" );
    attributes.put( MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY, "Y" );

    dbMeta.setConnectionSpecificInfoFromAttributes( attributes );

    assertTrue( dbMeta.isUsingDoubleDecimalAsSchemaTableSeparator() );
    assertEquals( "Y", dbMeta.getAttributes().getProperty( MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY ) );
  }
}
