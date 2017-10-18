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
