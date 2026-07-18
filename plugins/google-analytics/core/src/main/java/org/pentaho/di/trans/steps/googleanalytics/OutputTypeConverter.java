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



package org.pentaho.di.trans.steps.googleanalytics;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.injection.InjectionTypeConverter;
import org.pentaho.di.core.row.value.ValueMetaFactory;

/**
 * Converter for output types.
 */
public class OutputTypeConverter extends InjectionTypeConverter {
  @Override
  public int string2intPrimitive( String v ) throws KettleValueException {
    return ValueMetaFactory.getIdForValueMeta( v );
  }
}
