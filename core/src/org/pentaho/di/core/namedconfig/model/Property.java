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

import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

@MetaStoreElementType( name = "Property", description = "A Property" )
public class Property implements Serializable {

  private static final long serialVersionUID = -583451939799938402L;
  
  @MetaStoreAttribute
  private String propertyName;
  @MetaStoreAttribute
  private String displayName;
  @MetaStoreAttribute
  private String propertyValue;
  
  @MetaStoreAttribute
  private String type; // eg java.lang.Integer
  @MetaStoreAttribute
  private String uiType;
  //@fMetfaStoreAttributex
  private List<String> defaultValues; // depending on the uiType, we might render a dropdown with defaults
  
  public Property() {
  }
  
  public Property( String propertyName, String propertyValue ) {
    this();
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
    setPropertyValue( propertyValue );
  }

  public Property( String propertyName, String propertyValue, String uiType ) {
    this( propertyName, propertyValue );
    this.uiType = uiType;
  }  

  public Property( String propertyName, String propertyValue, String uiType, String type ) {
    this( propertyName, propertyValue, uiType );
    this.type = type;
  }  
  
  public Property( String propertyName, String displayName, String propertyValue, String uiType, String type, List<String> defaultValues ) {
    this( propertyName, propertyValue, uiType, type );
    this.displayName = displayName;
    this.defaultValues = defaultValues;
  }  
  
  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName( String propertyName ) {
    this.propertyName = propertyName;
  }

  public String getDisplayName() {
    return displayName == null || displayName.equals("") ? getPropertyName() : displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }  
  
  public String getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue( String propertyValue ) {
    this.propertyValue = propertyValue;
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

  public List<String> getDefaultValues() {
    return defaultValues;
  }

  public void setDefaultValues( List<String> defaultValues ) {
    this.defaultValues = defaultValues;
  }  
  
}
