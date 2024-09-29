/* ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Locale;
import java.util.Random;

import org.pentaho.di.core.util.Utils;

/**
 * @author Andrey Khayrutdinov
 */
public class LocaleLoadSaveValidator implements FieldLoadSaveValidator<Locale> {
  @Override public Locale getTestObject() {
    Locale[] availableLocales = Locale.getAvailableLocales();

    Locale random = availableLocales[ new Random().nextInt( availableLocales.length ) ];
    if ( Utils.isEmpty( random.toString() ) || random.toString().matches( "(\\w)*#.*" ) ) {
      // locales with '#', like 'sr_rs_#latn', are not restored properly
      return Locale.US;
    } else {
      return random;
    }
  }

  @Override public boolean validateTestObject( Locale testObject, Object actual ) {
    return testObject.equals( actual );
  }
}
