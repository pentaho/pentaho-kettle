/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.namedconfig.model;

import java.io.Serializable;
import java.util.List;

public class Property implements Serializable {

  private static final long serialVersionUID = -583451939799938402L;
  
  private String name;
  private Object value;
  
  private String type; // eg java.lang.Integer
  private String uiType;
  private List<Object> defaultValues; // depending on the uiType, we might render a dropdown with defaults
  
  public Property() {
  }
  
  public Property( String name, Object value ) {
    this();
    this.name = name;
    this.value = value;
    setValue( value );
  }

  public Property( String name, Object value, String uiType ) {
    this( name, value );
    this.uiType = uiType;
  }  

  public Property( String name, Object value, String uiType, String type ) {
    this( name, value, uiType );
    this.type = type;
  }  
  
  public Property( String name, Object value, String uiType, String type, List<Object> defaultValues ) {
    this( name, value, uiType, type );
    this.defaultValues = defaultValues;
  }  
  
  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Object getValue() {
    return value;
  }

  public void setValue( Object value ) {
    this.value = value;
    this.type = value.getClass().getName();
  }
  
  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }
  
  public String getUiType() {
    return uiType;
  }

  public void setUiType( String uiType ) {
    this.uiType = uiType;
  }

  public List<Object> getDefaultValues() {
    return defaultValues;
  }

  public void setDefaultValues( List<Object> defaultValues ) {
    this.defaultValues = defaultValues;
  }  
  
}
