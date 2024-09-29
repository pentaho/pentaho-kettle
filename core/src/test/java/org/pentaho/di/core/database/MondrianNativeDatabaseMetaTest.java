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
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MondrianNativeDatabaseMetaTest {

  @Test
  public void testSettings() throws Exception {
    MondrianNativeDatabaseMeta nativeMeta = new MondrianNativeDatabaseMeta();
    assertNull( nativeMeta.getUsedLibraries() );
    assertEquals( "mondrian.olap4j.MondrianOlap4jDriver", nativeMeta.getDriverClass() );
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_JNDI }, nativeMeta.getAccessTypeList() );
    assertNull( nativeMeta.getModifyColumnStatement( "", null, "", false, "", true ) );
    assertNull( nativeMeta.getAddColumnStatement( "", null, "", false, "", true ) );
    assertNull( nativeMeta.getFieldDefinition( null, "", "", true, false, true ) );
    assertEquals( "jdbc:mondrian:Datasource=jdbc/WIBBLE;Catalog=FOO", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
  }

}
