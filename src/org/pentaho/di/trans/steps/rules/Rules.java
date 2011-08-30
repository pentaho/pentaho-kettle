/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved.
 */

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
      this(new Hashtable<String, Object>(), false);
    }

    public Row(Map<String, Object> row) {
      this(row, false);
    }

    public Row(boolean external) {
      this(new Hashtable<String, Object>(), external);
    }

    public Row(Map<String, Object> row, boolean external) {
      this.row = row;
      this.external = external;
    }

    public Map<String, Object> getColumn() {
      return row;
    }

    public boolean isExternalSource() {
      return external;
    }

    public void addColumn(String columnName, Object value) {
      row.put(columnName, value);
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

    public Column(Boolean external) {
      this.external = external;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public Object getPayload() {
      return payload;
    }

    public void setPayload(Object payload) {
      this.payload = payload;
    }

    public void setExternalSource(Boolean external) {
      this.external = external;
    }

    public Boolean isExternalSource() {
      return external;
    }
  }
}
