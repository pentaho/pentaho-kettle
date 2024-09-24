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

package org.pentaho.di.trans.steps.fieldsplitter;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.injection.InjectionTypeConverter;
import org.pentaho.di.core.row.value.ValueMetaString;

/**
 * Converter for trim types.
 */
public class TrimTypeConverter extends InjectionTypeConverter {
  @Override
  public int string2intPrimitive( String v ) throws KettleValueException {
    return ValueMetaString.getTrimTypeByCode( v );
  }
}
