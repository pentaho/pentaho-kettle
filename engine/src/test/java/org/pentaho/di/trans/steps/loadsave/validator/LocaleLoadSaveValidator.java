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
