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
