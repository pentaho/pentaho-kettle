/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
