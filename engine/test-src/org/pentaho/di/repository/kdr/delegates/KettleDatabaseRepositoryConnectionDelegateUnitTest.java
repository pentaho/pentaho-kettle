/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.repository.kdr.delegates;

import org.junit.Test;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate
  .createIdsWithValuesQuery;

/**
 */
public class KettleDatabaseRepositoryConnectionDelegateUnitTest {

  @Test
  public void createIdsWithsValueQuery() {
    final String table = "table";
    final String id = "id";
    final String lookup = "lookup";
    final String expectedTemplate = format( "select %s from %s where %s in ", id, table, lookup ) + "(%s)";

    assertTrue( format( expectedTemplate, "?" ).equalsIgnoreCase( createIdsWithValuesQuery(table, id, lookup, 1 ) ) );
    assertTrue( format( expectedTemplate, "?,?" ).equalsIgnoreCase( createIdsWithValuesQuery( table, id, lookup, 2 ) ) );
  }
}
