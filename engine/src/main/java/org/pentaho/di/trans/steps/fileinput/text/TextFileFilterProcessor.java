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


package org.pentaho.di.trans.steps.fileinput.text;

import org.pentaho.di.core.variables.VariableSpace;

/**
 * Processor of Filters. Kind of inversion principle, and to make unit testing easier.
 *
 * @author Sven Boden
 */
public class TextFileFilterProcessor {

  /** The filters to process */
  private TextFileFilter[] filters;
  private String[] filtersString;
  private boolean stopProcessing;

  /**
   * @param filters
   *          The filters to process
   */
  public TextFileFilterProcessor( TextFileFilter[] filters, VariableSpace space ) {
    this.filters = filters;
    this.stopProcessing = false;

    if ( filters.length == 0 ) {
      // This makes processing faster in case there are no filters.
      filters = null;
    } else {
      filtersString = new String[filters.length];
      for ( int f = 0; f < filters.length; f++ ) {
        filtersString[f] = space.environmentSubstitute( filters[f].getFilterString() );
      }
    }
  }

  public boolean doFilters( String line ) {
    if ( filters == null ) {
      return true;
    }

    boolean filterOK = true; // if false: skip this row
    boolean positiveMode = false;
    boolean positiveMatchFound = false;

    // If we have at least one positive filter, we enter positiveMode
    // Negative filters will always take precedence, meaning that the line
    // is skipped if one of them is found

    for ( int f = 0; f < filters.length && filterOK; f++ ) {
      TextFileFilter filter = filters[f];
      String filterString = filtersString[f];
      if ( filter.isFilterPositive() ) {
        positiveMode = true;
      }

      if ( filterString != null && filterString.length() > 0 ) {
        int from = filter.getFilterPosition();
        if ( from >= 0 ) {
          int to = from + filterString.length();
          if ( line.length() >= from && line.length() >= to ) {
            String sub = line.substring( filter.getFilterPosition(), to );
            if ( sub.equalsIgnoreCase( filterString ) ) {
              if ( filter.isFilterPositive() ) {
                positiveMatchFound = true;
              } else {
                filterOK = false; // skip this one!
              }
            }
          }
        } else { // anywhere on the line
          int idx = line.indexOf( filterString );
          if ( idx >= 0 ) {
            if ( filter.isFilterPositive() ) {
              positiveMatchFound = true;
            } else {
              filterOK = false; // skip this one!
            }
          }
        }

        if ( !filterOK ) {
          boolean isFilterLastLine = filter.isFilterLastLine();
          if ( isFilterLastLine ) {
            stopProcessing = true;
          }
        }
      }
    }

    // Positive mode and no match found? Discard the line
    if ( filterOK && positiveMode && !positiveMatchFound ) {
      filterOK = false;
    }

    return filterOK;
  }

  /**
   * Was processing requested to be stopped. Can only be true when doFilters was false.
   *
   * @return == true: processing should stop, == false: processing should continue.
   */
  public boolean isStopProcessing() {
    return stopProcessing;
  }
}
