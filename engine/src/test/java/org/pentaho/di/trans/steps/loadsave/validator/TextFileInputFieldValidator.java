/*! ******************************************************************************
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

import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

@SuppressWarnings( "deprecation" )
public class TextFileInputFieldValidator implements FieldLoadSaveValidator<TextFileInputField> {
  private static final Random rand = new Random();

  @Override public TextFileInputField getTestObject() {
    TextFileInputField rtn = new TextFileInputField( UUID.randomUUID().toString(), rand.nextInt(), rand.nextInt() );
    rtn.setType( rand.nextInt( 7 ) );
    rtn.setCurrencySymbol( UUID.randomUUID().toString() );
    rtn.setDecimalSymbol( UUID.randomUUID().toString() );
    rtn.setFormat( UUID.randomUUID().toString() );
    rtn.setGroupSymbol( UUID.randomUUID().toString() );
    rtn.setIfNullValue( UUID.randomUUID().toString() );
    rtn.setIgnored( rand.nextBoolean() );
    rtn.setNullString( UUID.randomUUID().toString() );
    rtn.setPrecision( rand.nextInt( 9 ) );
    rtn.setRepeated( rand.nextBoolean() );
    rtn.setTrimType( rand.nextInt( ValueMetaString.trimTypeDesc.length ) );
    return rtn;
  }

  @Override
  public boolean validateTestObject( TextFileInputField testObject, Object actual ) {
    if ( !( actual instanceof TextFileInputField ) ) {
      return false;
    }

    TextFileInputField another = (TextFileInputField) actual;
    return new EqualsBuilder()
      .append( testObject.getName(), another.getName() )
      .append( testObject.getTypeDesc(), another.getTypeDesc() )
      .append( testObject.getFormat(), another.getFormat() )
      .append( testObject.getCurrencySymbol(), another.getCurrencySymbol() )
      .append( testObject.getDecimalSymbol(), another.getDecimalSymbol() )
      .append( testObject.getGroupSymbol(), another.getGroupSymbol() )
      .append( testObject.getLength(), another.getLength() )
      .append( testObject.getPrecision(), another.getPrecision() )
      .append( testObject.getTrimTypeCode(), another.getTrimTypeCode() )
      // These fields not universally serialized, so don't check for them
      // TextFileInputMeta serializes some that CsvInputMeta doesn't. Given
      // they're both deprecated, ignore these.
      //.append( testObject.isRepeated(), another.isRepeated() )
      //.append( testObject.getNullString(), another.getNullString() )
      //.append( testObject.getIfNullValue(), another.getIfNullValue() )
      //.append( testObject.getPosition(), another.getPosition() )
      .isEquals();


  }


}
