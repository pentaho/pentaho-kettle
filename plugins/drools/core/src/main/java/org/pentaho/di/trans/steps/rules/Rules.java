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

package org.pentaho.di.trans.steps.rules;

import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author cboyden
 *
 */

public class Rules {

  public static class Row {
    private Map<String, Object> row;

    private Boolean external;

    public Row() {
      this( new Hashtable<String, Object>(), false );
    }

    public Row( Map<String, Object> row ) {
      this( row, false );
    }

    public Row( boolean external ) {
      this( new Hashtable<String, Object>(), external );
    }

    public Row( Map<String, Object> row, boolean external ) {
      this.row = row;
      this.external = external;
    }

    public Map<String, Object> getColumn() {
      return row;
    }

    public boolean isExternalSource() {
      return external;
    }

    public void addColumn( String columnName, Object value ) {
      row.put( columnName, value );
    }
  }

  public static class Column {
    private String name;

    private String type;

    private Object payload;

    private Boolean external;

    public Column() {
      this.external = false;
    }

    public Column( Boolean external ) {
      this.external = external;
    }

    public Column( String name, String type, Object payload  ) {
      this();
      this.name = name;
      this.type = type;
      this.payload = payload;
    }

    public Column( Boolean external, String name, String type, Object payload  ) {
      this( external );
      this.name = name;
      this.type = type;
      this.payload = payload;
    }

    public String getName() {
      return name;
    }

    public void setName( String name ) {
      this.name = name;
    }

    public String getType() {
      return type;
    }

    public void setType( String type ) {
      this.type = type;
    }

    public Object getPayload() {
      return payload;
    }

    public void setPayload( Object payload ) {
      this.payload = payload;
    }

    public void setExternalSource( Boolean external ) {
      this.external = external;
    }

    public Boolean isExternalSource() {
      return external;
    }
  }
}
