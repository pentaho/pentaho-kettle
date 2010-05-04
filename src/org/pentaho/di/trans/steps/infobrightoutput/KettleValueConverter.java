/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
  
  public KettleValueConverter(ValueMetaInterface meta) {
    this.meta = meta;
  }

  //@Override
  public BigDecimal getBigNumber(Object object) throws ValueConverterException {
    try {
      return meta.getBigNumber(object); 
    } catch (KettleValueException kve) {
      throw new ValueConverterException(kve);
    }
  }

  //@Override
  public byte[] getBinary(Object object) throws ValueConverterException {
    try {
      return meta.getBinary(object); 
    } catch (KettleValueException kve) {
      throw new ValueConverterException(kve);
    }
  }

  //@Override
  public byte[] getBinaryString(Object object) throws ValueConverterException {
    try {
      return meta.getBinaryString(object); 
    } catch (KettleValueException kve) {
      throw new ValueConverterException(kve);
    }
  }

  //@Override
  public Boolean getBoolean(Object object) throws ValueConverterException {
    try {
      return meta.getBoolean(object); 
    } catch (KettleValueException kve) {
      throw new ValueConverterException(kve);
    }
  }

  //@Override
  public Date getDate(Object object) throws ValueConverterException {
    try {
      return meta.getDate(object); 
    } catch (KettleValueException kve) {
      throw new ValueConverterException(kve);
    }
  }

  //@Override
  public Long getInteger(Object object) throws ValueConverterException {
    try {
      return meta.getInteger(object); 
    } catch (KettleValueException kve) {
      throw new ValueConverterException(kve);
    }
  }

  //@Override
  public Double getNumber(Object object) throws ValueConverterException {
    try {
      return meta.getNumber(object); 
    } catch (KettleValueException kve) {
      throw new ValueConverterException(kve);
    }
  }

  //@Override
  public String getString(Object object) throws ValueConverterException {
    try {
      return meta.getString(object); 
    } catch (KettleValueException kve) {
      throw new ValueConverterException(kve);
    }
  }

}
