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

package org.pentaho.di.core.spreadsheet;

public enum KCellType {
  EMPTY( "Empty" ), BOOLEAN( "Boolean" ), BOOLEAN_FORMULA( "Boolean formula" ), DATE( "Date" ), DATE_FORMULA(
    "Date formula" ), LABEL( "Label" ), STRING_FORMULA( "String formula" ), NUMBER( "Number" ), NUMBER_FORMULA(
    "Number formula" );

  private String description;

  private KCellType( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
