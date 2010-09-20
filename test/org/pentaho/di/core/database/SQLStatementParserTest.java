/* Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
package org.pentaho.di.core.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;

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
      return null;
    }
  };

  public void testParseStatements_simple() {
    BaseDatabaseMeta dbMeta = new BaseDatabaseMetaForTest();
    String sqlScript = "SELECT FROM table;"; //$NON-NLS-1$
    List<String> statements = dbMeta.parseStatements(sqlScript);
    assertEquals(1, statements.size());
    assertEquals(sqlScript, statements.get(0));
  }

  public void testParseStatements_simple_multiple() {
    BaseDatabaseMeta dbMeta = new BaseDatabaseMetaForTest();
    String statement1 = "SELECT * FROM table"; //$NON-NLS-1$
    String statement2 = "SELECT * FROM table2"; //$NON-NLS-1$
    String sqlScript = statement1 + ";" + Const.CR + statement2 + "; "; //$NON-NLS-1$ //$NON-NLS-2$
    List<String> statements = dbMeta.parseStatements(sqlScript);
    assertEquals(2, statements.size());
    assertEquals(statement1, statements.get(0));
    assertEquals(statement2, statements.get(1));
  }

  public void testParseStatements_appostrophy_in_backticks() throws KettleFileException {
    BaseDatabaseMeta dbMeta = new BaseDatabaseMetaForTest();
    String sqlScript = "CREATE TABLE sfdcom_test ( `Rep's Scoring` VARCHAR(255) );"; //$NON-NLS-1$
    List<String> statements = dbMeta.parseStatements(sqlScript);
    assertEquals(1, statements.size());
    assertEquals(sqlScript, statements.get(0));
  }
}
