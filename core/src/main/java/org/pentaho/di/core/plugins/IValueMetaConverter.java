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

package org.pentaho.di.core.plugins;

import org.pentaho.di.core.row.value.ValueMetaConversionException;

import java.text.SimpleDateFormat;

/**
 * Created by tkafalas on 12/4/2017.
 */
public interface IValueMetaConverter {
  /**
   *
   * @param sourceValueMetaType The source ValueMeta Type defined in the ValueMetaInterface
   * @param targetValueMetaType The target ValueMeta Type defined in the ValueMetaInterface
   * @param value the source value to be converted
   * @return An object representing the value converted to targetMetaType.  This value is suitable to use for
   */
  public Object convertFromSourceToTargetDataType( int sourceValueMetaType, int targetValueMetaType, Object value )
    throws ValueMetaConversionException;

  public void setDatePattern( SimpleDateFormat datePattern );


}
