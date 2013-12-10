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

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Verify the {@link BaseDatabaseMeta} can properly parse a script for individual statements
 *
 * @author Jordan Ganoff (jganoff@pentaho.com)
 *
 */
public class SQLStatementParserTest extends TestCase {
  /**
   * Simple concrete class specifically created to facilitate testing the base class.
   *
   */
  private class BaseDatabaseMetaForTest extends BaseDatabaseMeta {
    @Override
    public int[] getAccessTypeList() {
      return new int[0];
    }

    @Override
    public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
      boolean add_fieldname, boolean add_cr ) {
      return null;
    }

    @Override
    public String getDriverClass() {
      return "";
    }

    @Override
    public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
      return null;
    }

    @Override
    public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
      String pk, boolean semicolon ) {
      return null;
    }

    @Override
    public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk,
      boolean use_autoinc, String pk, boolean semicolon ) {
      return null;
    }

    @Override
    public String[] getUsedLibraries() {
      return new String[0];
    }
  }

  public void testParseStatements_simple() {
    BaseDatabaseMeta dbMeta = new BaseDatabaseMetaForTest();
    String sqlScript = "SELECT FROM table;";
    List<String> statements = dbMeta.parseStatements( sqlScript );
    assertEquals( 1, statements.size() );
    assertEquals( sqlScript, statements.get( 0 ) );
  }

  public void testParseStatements_simple_multiple() {
    BaseDatabaseMeta dbMeta = new BaseDatabaseMetaForTest();
    String statement1 = "SELECT * FROM table";
    String statement2 = "SELECT * FROM table2";
    String sqlScript = statement1 + ";" + Const.CR + statement2 + "; ";
    List<String> statements = dbMeta.parseStatements( sqlScript );
    assertEquals( 2, statements.size() );
    assertEquals( statement1, statements.get( 0 ) );
    assertEquals( statement2, statements.get( 1 ) );
  }

  public void testParseStatements_appostrophy_in_backticks() throws KettleFileException {
    BaseDatabaseMeta dbMeta = new BaseDatabaseMetaForTest();
    String sqlScript = "CREATE TABLE sfdcom_test ( `Rep's Scoring` VARCHAR(255) );";
    List<String> statements = dbMeta.parseStatements( sqlScript );
    assertEquals( 1, statements.size() );
    assertEquals( sqlScript, statements.get( 0 ) );
  }
}
