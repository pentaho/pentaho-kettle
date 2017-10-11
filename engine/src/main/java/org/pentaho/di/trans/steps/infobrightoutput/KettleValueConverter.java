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

package org.pentaho.di.trans.steps.infobrightoutput;

import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.infobright.etl.model.ValueConverter;
import com.infobright.etl.model.ValueConverterException;

/**
 * Wraps a Kettle ValueMetaInterface in a portable way.
 *
 * @author geoffrey.falk@infobright.com
 */
public class KettleValueConverter implements ValueConverter {

  private final ValueMetaInterface meta;

  public KettleValueConverter( ValueMetaInterface meta ) {
    this.meta = meta;
  }

  // @Override
  public BigDecimal getBigNumber( Object object ) throws ValueConverterException {
    try {
      return meta.getBigNumber( object );
    } catch ( KettleValueException kve ) {
      throw new ValueConverterException( kve );
    }
  }

  // @Override
  public byte[] getBinary( Object object ) throws ValueConverterException {
    try {
      return meta.getBinary( object );
    } catch ( KettleValueException kve ) {
      throw new ValueConverterException( kve );
    }
  }

  // @Override
  public byte[] getBinaryString( Object object ) throws ValueConverterException {
    try {
      return meta.getBinaryString( object );
    } catch ( KettleValueException kve ) {
      throw new ValueConverterException( kve );
    }
  }

  // @Override
  public Boolean getBoolean( Object object ) throws ValueConverterException {
    try {
      return meta.getBoolean( object );
    } catch ( KettleValueException kve ) {
      throw new ValueConverterException( kve );
    }
  }

  // @Override
  public Date getDate( Object object ) throws ValueConverterException {
    try {
      return meta.getDate( object );
    } catch ( KettleValueException kve ) {
      throw new ValueConverterException( kve );
    }
  }

  // @Override
  public Long getInteger( Object object ) throws ValueConverterException {
    try {
      return meta.getInteger( object );
    } catch ( KettleValueException kve ) {
      throw new ValueConverterException( kve );
    }
  }

  // @Override
  public Double getNumber( Object object ) throws ValueConverterException {
    try {
      return meta.getNumber( object );
    } catch ( KettleValueException kve ) {
      throw new ValueConverterException( kve );
    }
  }

  // @Override
  public String getString( Object object ) throws ValueConverterException {
    try {
      return meta.getString( object );
    } catch ( KettleValueException kve ) {
      throw new ValueConverterException( kve );
    }
  }

}
