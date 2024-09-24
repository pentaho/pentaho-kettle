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
public class SQLStatementParserIT extends TestCase {
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
