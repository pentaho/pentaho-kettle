/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.injection;

import org.apache.commons.lang.StringUtils;

/**
 * An extension to the {@link DefaultInjectionTypeConverter} that converts null and empty values to -1 for
 * number/int type variables. This ensures that 0's aren't inserted for field values that are meant to remain empty.
 * This assumes that the step dialog knows to treat field values set to -1 as "empty".
 */
public class NullNumberConverter extends DefaultInjectionTypeConverter {

  @Override
  public int string2intPrimitive( final String v ) {
    return StringUtils.isBlank( v ) ? -1 : Integer.parseInt( v );
  }

  @Override
  public int integer2intPrimitive( final Long v ) {
    return v == null ? -1 : v.intValue();
  }

  @Override
  public int number2intPrimitive( final Double v ) {
    return v == null ? -1 : Math.round( v.floatValue() );
  }
}
