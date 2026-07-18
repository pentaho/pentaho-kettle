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


package org.pentaho.di.core.row.value;

/**
 * Created by tkafalas on 12/5/2017.
 */
public class ValueMetaConversionException extends Exception {
  private static final long serialVersionUID = 1L;

  public ValueMetaConversionException( String errorMessage, Exception e ) {
    super( errorMessage, e );
  }
}
