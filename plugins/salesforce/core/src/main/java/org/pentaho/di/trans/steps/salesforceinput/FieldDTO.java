/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.salesforceinput;

public class FieldDTO {

  String name;
  String field;
  boolean idlookup;
  String type;
  String length;
  String precision;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getField() {
    return field;
  }

  public void setField( String field ) {
    this.field = field;
  }

  public boolean isIdlookup() {
    return idlookup;
  }

  public void setIdlookup( boolean idlookup ) {
    this.idlookup = idlookup;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getLength() {
    return length;
  }

  public void setLength( String length ) {
    this.length = length;
  }

  public String getPrecision() {
    return precision;
  }

  public void setPrecision( String precision ) {
    this.precision = precision;
  }
}
