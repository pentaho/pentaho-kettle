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

import java.util.Random;
import java.util.UUID;

import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class TextFileFieldLoadSaveValidator implements FieldLoadSaveValidator<TextFileField> {
  Random rand = new Random();

  @Override
  public TextFileField getTestObject() {
    String name = UUID.randomUUID().toString();
    int type =
        ValueMetaFactory.getIdForValueMeta( ValueMetaFactory.getValueMetaNames()[rand.nextInt( ValueMetaFactory
            .getValueMetaNames().length )] );
    String format = UUID.randomUUID().toString();
    int length = Math.abs( rand.nextInt() );
    int precision = Math.abs( rand.nextInt() );
    String currencySymbol = UUID.randomUUID().toString();
    String decimalSymbol = UUID.randomUUID().toString();
    String groupSymbol = UUID.randomUUID().toString();
    String nullString = UUID.randomUUID().toString();

    return new TextFileField( name, type, format, length, precision, currencySymbol, decimalSymbol, groupSymbol,
        nullString );
  }

  @Override
  public boolean validateTestObject( TextFileField testObject, Object actual ) {
    if ( !( actual instanceof TextFileField ) || testObject.compare( actual ) != 0 ) {
      return false;
    }
    TextFileField act = (TextFileField) actual;
    if ( testObject.getName().equals( act.getName() )
        && testObject.getType() == act.getType()
        && testObject.getFormat().equals( act.getFormat() )
        && testObject.getLength() == act.getLength()
        && testObject.getPrecision() == act.getPrecision()
        && testObject.getCurrencySymbol().equals( act.getCurrencySymbol() )
        && testObject.getDecimalSymbol().equals( act.getDecimalSymbol() )
        && testObject.getGroupingSymbol().equals( act.getGroupingSymbol() )
        && testObject.getNullString().equals( act.getNullString() ) ) {
      return true;
    } else {
      return false;
    }
  }
}
