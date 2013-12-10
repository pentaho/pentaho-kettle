/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.junit.Test;

/**
 * User: RFellows Date: 3/4/13
 */
public class MonetDBDatabaseMetaTest {

  @Test
  public void testGetSafeFieldname() throws Exception {
    MonetDBDatabaseMeta meta = new MonetDBDatabaseMeta();
    String expected = "hello_world";
    String fieldname = "hello world";
    String result = meta.getSafeFieldname( fieldname );

    assertEquals( expected, result );
  }

  @Test
  public void testGetSafeFieldname_beginingWithNumber() throws Exception {
    MonetDBDatabaseMeta meta = new MonetDBDatabaseMeta();
    String expected = "_2B";
    String fieldname = "2B";
    String result = meta.getSafeFieldname( fieldname );

    assertEquals( expected, result );
  }

  @Test
  public void testGetSafeFieldname_reservedWord() throws Exception {
    MonetDBDatabaseMeta meta = new MonetDBDatabaseMeta();
    String expected = "drop_";
    String fieldname = "drop";
    String result = meta.getSafeFieldname( fieldname );

    assertEquals( expected, result );
  }

  @Test
  public void testGetSafeFieldname_nonAlphaNumericChars() throws Exception {
    MonetDBDatabaseMeta meta = new MonetDBDatabaseMeta();
    String expected = "what_the_";
    String fieldname = "what the *#&@(@!?";
    String result = meta.getSafeFieldname( fieldname );

    assertEquals( expected, result );
  }
}
