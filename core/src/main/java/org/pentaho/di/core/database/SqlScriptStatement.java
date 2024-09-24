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

public class SqlScriptStatement {
  private String statement;
  private int fromIndex;
  private int toIndex;
  private boolean query;

  private boolean complete;
  private boolean ok;
  private String loggingText;

  /**
   * @param statement
   * @param fromIndex
   * @param toIndex
   * @param query
   */
  public SqlScriptStatement( String statement, int fromIndex, int toIndex, boolean query ) {
    this.statement = statement;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
    this.query = query;
  }

  @Override
  public String toString() {
    return statement;
  }

  /**
   * @return the statement
   */
  public String getStatement() {
    return statement;
  }

  /**
   * @param statement
   *          the statement to set
   */
  public void setStatement( String statement ) {
    this.statement = statement;
  }

  /**
   * @return the fromIndex
   */
  public int getFromIndex() {
    return fromIndex;
  }

  /**
   * @param fromIndex
   *          the fromIndex to set
   */
  public void setFromIndex( int fromIndex ) {
    this.fromIndex = fromIndex;
  }

  /**
   * @return the toIndex
   */
  public int getToIndex() {
    return toIndex;
  }

  /**
   * @param toIndex
   *          the toIndex to set
   */
  public void setToIndex( int toIndex ) {
    this.toIndex = toIndex;
  }

  /**
   * @return the query
   */
  public boolean isQuery() {
    return query;
  }

  /**
   * @param query
   *          the query to set
   */
  public void setQuery( boolean query ) {
    this.query = query;
  }

  /**
   * @return the ok
   */
  public boolean isOk() {
    return ok;
  }

  /**
   * @param ok
   *          the ok to set
   */
  public void setOk( boolean ok ) {
    this.ok = ok;
  }

  /**
   * @return the loggingText
   */
  public String getLoggingText() {
    return loggingText;
  }

  /**
   * @param loggingText
   *          the loggingText to set
   */
  public void setLoggingText( String loggingText ) {
    this.loggingText = loggingText;
  }

  /**
   * @return the complete
   */
  public boolean isComplete() {
    return complete;
  }

  /**
   * @param complete
   *          the complete to set
   */
  public void setComplete( boolean complete ) {
    this.complete = complete;
  }

}
