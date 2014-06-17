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

package org.pentaho.di.trans.steps.monetdbbulkloader;

import org.junit.Ignore;
import org.junit.Test;

public class MonetDBBulkLoaderTest {

  static final String DB = "pentaho-instaview";
  static final int PORT = 50000;
  static final String USER = "monetdb";
  static final String PASSWORD = "monetdb";
  static final String HOST = "localhost";

  // not a real unit test: ignore it. depends on monetdb running and a specific table existing in it.
  // was created to help test/verify the transition away from mclient process to MapiSocket API.
  @Ignore
  @Test
  public void testExecuteQuery() throws Exception {

    String query = "SELECT * FROM instaview_20121031113843842";
    MonetDBBulkLoader.executeSql( query, HOST, PORT, USER, PASSWORD, DB );

    query = "SELECT * FROM badtable";
    MonetDBBulkLoader.executeSql( query, HOST, PORT, USER, PASSWORD, DB );

  }

}
