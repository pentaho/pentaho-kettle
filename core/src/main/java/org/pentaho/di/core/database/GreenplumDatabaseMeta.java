/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

/**
 * Contains PostgreSQL specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class GreenplumDatabaseMeta extends PostgreSQLDatabaseMeta implements DatabaseInterface {
  @Override
  public String[] getReservedWords() {
    String[] newWords = new String[] { "ERRORS" };
    String[] pgWords = super.getReservedWords();
    String[] gpWords = new String[ pgWords.length + newWords.length ];

    System.arraycopy( pgWords, 0, gpWords, 0, pgWords.length );
    System.arraycopy( newWords, 0, gpWords, pgWords.length, newWords.length );

    return gpWords;
  }

  @Override
  public boolean supportsErrorHandlingOnBatchUpdates() {
    return false;
  }
}
