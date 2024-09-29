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

package org.pentaho.di.core;

import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SQLStatementTest {
  @Test
  public void testClass() throws KettleException {
    final String name = "stepName";
    final DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    final String sql = "sql string";
    final String error = "error";

    SQLStatement statement = new SQLStatement( name, dbMeta, sql );
    assertSame( name, statement.getStepname() );
    assertSame( dbMeta, statement.getDatabase() );
    assertTrue( statement.hasSQL() );
    assertSame( sql, statement.getSQL() );
    statement.setStepname( null );
    assertNull( statement.getStepname() );
    statement.setDatabase( null );
    assertNull( statement.getDatabase() );
    statement.setSQL( null );
    assertNull( statement.getSQL() );
    assertFalse( statement.hasSQL() );
    assertFalse( statement.hasError() );
    statement.setError( error );
    assertTrue( statement.hasError() );
    assertSame( error, statement.getError() );
  }
}
