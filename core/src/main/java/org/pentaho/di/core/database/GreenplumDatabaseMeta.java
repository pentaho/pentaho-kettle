/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
