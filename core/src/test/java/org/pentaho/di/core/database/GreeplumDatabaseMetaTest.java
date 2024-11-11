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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class GreeplumDatabaseMetaTest extends PostgreSQLDatabaseMetaTest {

  @Test
  public void testPostgresqlOverrides() throws Exception {
    PostgreSQLDatabaseMeta meta1 = new PostgreSQLDatabaseMeta();
    GreenplumDatabaseMeta meta2 = new GreenplumDatabaseMeta();
    String[] meta1Reserved = meta1.getReservedWords();
    String[] meta2Reserved = meta2.getReservedWords();
    assertTrue( ( meta1Reserved.length + 1 ) == ( meta2Reserved.length ) ); // adds ERRORS
    assertEquals( "ERRORS", meta2Reserved[ meta2Reserved.length - 1 ] );
    assertFalse( meta2.supportsErrorHandlingOnBatchUpdates() );
  }


}
