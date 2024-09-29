/*!
 *
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
 *
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( propOrder = { "name", "type", "stringValue" } )
public class JobScheduleParam implements Serializable {

  private static final long serialVersionUID = -4214459740606299083L;

  private String name;
  private String type;
  private List<String> stringValue = new ArrayList<>();


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

  public List<String> getStringValue() {
    return stringValue;
  }

  public void setStringValue( List<String> value ) {
    this.stringValue = value;
  }

}
