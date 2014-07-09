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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.sql.SQL;

public class SQLTest {
  @Test
  public void testExample1() throws KettleSQLException {
    String select = "A, B, C";
    String from = "Step";
    SQL sql = new SQL( "SELECT " + select + " FROM " + from );
    assertEquals( select, sql.getSelectClause() );
    assertEquals( from, sql.getServiceName() );
    assertNull( sql.getWhereClause() );
    assertNull( sql.getGroupClause() );
    assertNull( sql.getHavingClause() );
    assertNull( sql.getOrderClause() );
  }

  @Test
  public void testExample2() throws KettleSQLException {
    String select = "A, B, C";
    String from = "Step";
    String where = "D > 6 AND E = 'abcd'";
    SQL sql = new SQL( "SELECT " + select + " FROM " + from + " WHERE " + where );
    assertEquals( select, sql.getSelectClause() );
    assertEquals( from, sql.getServiceName() );
    assertEquals( where, sql.getWhereClause() );
    assertNull( sql.getGroupClause() );
    assertNull( sql.getHavingClause() );
    assertNull( sql.getOrderClause() );
  }

  @Test
  public void testExample3() throws KettleSQLException {
    String select = "A, B, C";
    String from = "Step";
    String order = "B, A, C";
    SQL sql = new SQL( "SELECT " + select + " FROM " + from + " ORDER BY " + order );
    assertEquals( select, sql.getSelectClause() );
    assertEquals( from, sql.getServiceName() );
    assertNull( sql.getWhereClause() );
    assertNull( sql.getGroupClause() );
    assertNull( sql.getHavingClause() );
    assertEquals( order, sql.getOrderClause() );
  }

  @Test
  public void testExample4() throws KettleSQLException {
    String select = "A, B, sum(C)";
    String from = "Step";
    String where = "D > 6 AND E = 'abcd'";
    String group = "A, B";
    String having = "sum(C) > 100";
    String order = "sum(C) DESC";
    SQL sql =
        new SQL( "SELECT " + select + " FROM " + from + " WHERE " + where + " GROUP BY " + group + " HAVING " + having
            + " ORDER BY " + order );
    assertEquals( select, sql.getSelectClause() );
    assertEquals( from, sql.getServiceName() );
    assertEquals( where, sql.getWhereClause() );
    assertEquals( group, sql.getGroupClause() );
    assertEquals( having, sql.getHavingClause() );
    assertEquals( order, sql.getOrderClause() );
  }

  @Test
  public void testWhereInColumnIndexPDI12347() throws KettleSQLException {
    String select = "whereDoYouLive, good, fine";
    String from = "testingABC";
    SQL sql = new SQL( "SELECT " + select + " FROM " + from );
    assertEquals( select, sql.getSelectClause() );
    assertEquals( from, sql.getServiceName() );
    assertNull( sql.getWhereClause() );
    assertNull( sql.getGroupClause() );
    assertNull( sql.getHavingClause() );
    assertNull( sql.getOrderClause() );
  }
}
