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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

@Deprecated
public class RemedyActionRequestSystemDatabaseMetaTest {
  RemedyActionRequestSystemDatabaseMeta odbcMeta;

  @Before
  public void setupBefore() {
    odbcMeta = new RemedyActionRequestSystemDatabaseMeta();
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_JNDI },
        odbcMeta.getAccessTypeList() );
    assertEquals( 1, odbcMeta.getNotFoundTK( true ) );
    assertEquals( 0, odbcMeta.getNotFoundTK( false ) );
    assertEquals( "sun.jdbc.odbc.JdbcOdbcDriver", odbcMeta.getDriverClass() );
    assertEquals( "jdbc:odbc:WIBBLE", odbcMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertFalse( odbcMeta.isFetchSizeSupported() );
    assertFalse( odbcMeta.supportsBitmapIndex() );
    assertFalse( odbcMeta.isRequiringTransactionsOnQueries() );
    assertFalse( odbcMeta.supportsViews() );
  }

}
