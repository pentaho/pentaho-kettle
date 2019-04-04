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
