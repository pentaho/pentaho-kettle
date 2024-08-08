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

import static org.junit.Assert.assertEquals;

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

}
