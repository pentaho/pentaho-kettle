/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
