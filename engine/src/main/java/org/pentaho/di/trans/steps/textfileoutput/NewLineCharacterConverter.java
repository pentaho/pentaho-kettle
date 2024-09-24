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

package org.pentaho.di.trans.steps.textfileoutput;

import org.pentaho.di.core.injection.InjectionTypeConverter;

/**
 * Converter for string representations of new line characters
 */
public class NewLineCharacterConverter extends InjectionTypeConverter {

  @Override
  public String string2string( String v ) {
    if ( v == null ) {
      return null;
    } else {
      v = v.replace( "\\r", "\r" );
      v = v.replace( "\\n", "\n" );
      return v;
    }
  }
}
