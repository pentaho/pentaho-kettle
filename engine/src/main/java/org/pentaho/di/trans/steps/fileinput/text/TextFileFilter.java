/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.fileinput.text;

import org.pentaho.di.core.injection.Injection;

public class TextFileFilter implements Cloneable {
  /** The position of the occurrence of the filter string to check at */
  @Injection( name = "FILTER_POSITION", group = "FILTERS" )
  private int filterPosition;

  /** The string to filter on */
  @Injection( name = "FILTER_STRING", group = "FILTERS" )
  private String filterString;

  /** True if we want to stop when we reach a filter line */
  @Injection( name = "FILTER_LAST_LINE", group = "FILTERS" )
  private boolean filterLastLine;

  /** True if we want to match only this lines */
  @Injection( name = "FILTER_POSITIVE", group = "FILTERS" )
  private boolean filterPositive;

  /**
   * @param filterPosition
   *          The position of the occurrence of the filter string to check at
   * @param filterString
   *          The string to filter on
   * @param filterLastLine
   *          True if we want to stop when we reach a filter string on the specified position False if you just want to
   *          skip the line.
   * @param filterPositive
   *          True if we want to get only lines that match this string
   *
   */
  public TextFileFilter( int filterPosition, String filterString, boolean filterLastLine, boolean filterPositive ) {
    this.filterPosition = filterPosition;
    this.filterString = filterString;
    this.filterLastLine = filterLastLine;
    this.filterPositive = filterPositive;
  }

  public TextFileFilter() {
  }

  public Object clone() {
    try {
      Object retval = super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  /**
   * @return Returns the filterLastLine.
   */
  public boolean isFilterLastLine() {
    return filterLastLine;
  }

  /**
   * @param filterLastLine
   *          The filterLastLine to set.
   */
  public void setFilterLastLine( boolean filterLastLine ) {
    this.filterLastLine = filterLastLine;
  }

  /**
   * @return Returns the filterPositive.
   */
  public boolean isFilterPositive() {
    return filterPositive;
  }

  /**
   * @param filterPositive
   *          The filterPositive to set.
   */
  public void setFilterPositive( boolean filterPositive ) {
    this.filterPositive = filterPositive;
  }

  /**
   * @return Returns the filterPosition.
   */
  public int getFilterPosition() {
    return filterPosition;
  }

  /**
   * @param filterPosition
   *          The filterPosition to set.
   */
  public void setFilterPosition( int filterPosition ) {
    this.filterPosition = filterPosition;
  }

  /**
   * @return Returns the filterString.
   */
  public String getFilterString() {
    return filterString;
  }

  /**
   * @param filterString
   *          The filterString to set.
   */
  public void setFilterString( String filterString ) {
    this.filterString = filterString;
  }
}
