/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.ValueMetaAndData;

public class ThinUtilTest {
  @Test
  public void testFindClauseNullOrEmptyString() throws KettleSQLException {
    assertNull( ThinUtil.findClause( null, null ) );
    assertNull( ThinUtil.findClause( "", null ) );
  }

  @Test
  public void testFindSelectFromNotFound() throws KettleSQLException {
    assertNull( ThinUtil.findClause( "Select * From Test", "WHERE" ) );
  }

  @Test
  public void testFindSelectFromFound() throws KettleSQLException {
    assertEquals( "*", ThinUtil.findClause( "Select * From Test", "SELECT", "FROM" ) );
  }

  @Test
  public void testFindClauseSkipsChars() throws KettleSQLException {
    assertNull( ThinUtil.findClause( "'Select' * From Test", "SELECT", "FROM" ) );
  }

  @Test
  public void testAttemptDateValueExtraction() throws Exception {
    ValueMetaAndData timestamp = ThinUtil.attemptDateValueExtraction( "TIMESTAMP '2014-01-01 00:00:00'" );
    ValueMetaAndData date = ThinUtil.attemptDateValueExtraction( "DATE '2014-01-01'" );

    assertNotNull( timestamp );
    assertEquals( "2014-01-01 00:00:00", timestamp.toString() );

    assertNotNull( date );
    assertEquals( "2014-01-01", date.toString() );
  }
}
