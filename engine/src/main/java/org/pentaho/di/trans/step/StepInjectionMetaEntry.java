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

package org.pentaho.di.trans.step;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.value.ValueMetaFactory;

/**
 * This is a single metadata attribute for step metadata injection.
 *
 * @author matt
 *
 */
public class StepInjectionMetaEntry implements Cloneable {
  private String key;
  private Object value;
  private String description;
  private int valueType;

  private List<StepInjectionMetaEntry> details;

  /**
   * @param key
   * @param value
   * @param valueType
   * @param description
   */
  public StepInjectionMetaEntry( String key, Object value, int valueType, String description ) {
    this.key = key;
    this.value = value;
    this.valueType = valueType;
    this.description = description;

    details = new ArrayList<StepInjectionMetaEntry>();
  }

  @Override
  public String toString() {
    return "{"
      + key + ":" + ValueMetaFactory.getValueMetaName( valueType )
      + ( value == null ? "" : "(" + value.toString() + ")" )
      + "}";
  }

  @Override
  public StepInjectionMetaEntry clone() {
    try {
      return (StepInjectionMetaEntry) super.clone();
    } catch ( CloneNotSupportedException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * @param key
   * @param valueType
   * @param description
   */
  public StepInjectionMetaEntry( String key, int valueType, String description ) {
    this( key, null, valueType, description );
  }

  /**
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * @param key
   *          the key to set
   */
  public void setKey( String key ) {
    this.key = key;
  }

  /**
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue( Object value ) {
    this.value = value;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * @return the valueType This is the data type (see ValueMetaInterface) of the value. If the value is NONE (0) this is
   *         a list of values.
   */
  public int getValueType() {
    return valueType;
  }

  /**
   * @param valueType
   *          the valueType to set
   */
  public void setValueType( int valueType ) {
    this.valueType = valueType;
  }

  /**
   * @return the details : In case the data type of this entry is NONE (0) you can specify a list of entries. For
   *         example, for a step with a list of entries (filenames, fields, keys, etc) you can specify the list of
   *         metadata entries with this list.
   */
  public List<StepInjectionMetaEntry> getDetails() {
    return details;
  }

  /**
   * @param details
   *          the details to set. In case the data type of this entry is NONE (0) you can specify a list of entries. For
   *          example, for a step with a list of entries (filenames, fields, keys, etc) you can specify the list of
   *          metadata entries with this list.
   */
  public void setDetails( List<StepInjectionMetaEntry> details ) {
    this.details = details;
  }

}
