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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleSQLException;

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
  public void testLikePatternMatching() {
    try {
      ThinUtil.like( "foo", null );
      fail( "Null pattern should not be allowed" );
    } catch ( IllegalArgumentException e ) {
      assertNotNull( e );
    }

    assertTrue( "Exact Matching", ThinUtil.like( "foobar", "foobar" ) );

    assertTrue( "_ Matching", ThinUtil.like( "foobar", "f__b_r" ) );
    assertTrue( "* Matching", ThinUtil.like( "foobar", "foo%" ) );

    assertTrue( "Regex Escaping", ThinUtil.like( "foo\\*?[]()bar", "%\\*?[]()%" ) );

    assertFalse( "False Match", ThinUtil.like( "foo", "bar" ) );
  }
}
