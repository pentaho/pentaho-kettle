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

import org.pentaho.di.core.Const;

public class MSSQLServer2012NativeDatabaseMeta extends MSSQLServerNativeDatabaseMeta {
  /**
   * Get the SQL to get the next value of a sequence. (Oracle/PGSQL only)
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence. (Oracle/PGSQL only)
   */
  @Override
  public String getSQLNextSequenceValue( String sequenceName ) {
    return "SELECT NEXT VALUE FOR "+sequenceName;
  }

  /**
   * Get the current value of a database sequence
   *
   * @param sequenceName
   *          The sequence to check
   * @return The current value of a database sequence
   */
  @Override
  public String getSQLCurrentSequenceValue( String sequenceName ) {
    return "SELECT current_value FROM sys.sequences WHERE name = '"+sequenceName+"'";
  }

  /**
   * Check if a sequence exists.
   *
   * @param sequenceName
   *          The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  @Override
  public String getSQLSequenceExists( String sequenceName ) {
    return "SELECT OBJECT_ID('"+sequenceName+"','SO')";
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences() {
    return true;
  }

}
