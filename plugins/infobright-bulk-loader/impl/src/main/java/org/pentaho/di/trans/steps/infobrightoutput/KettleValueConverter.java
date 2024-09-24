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
