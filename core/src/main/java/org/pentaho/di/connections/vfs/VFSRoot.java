/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs;

import java.util.Date;

/**
 * Created by bmorrise on 2/27/19.
 */
public class VFSRoot {

  public VFSRoot( String name, Date modifiedDate ) {
    this.name = name;
    this.modifiedDate = modifiedDate;
  }

  private String name;
  private Date modifiedDate;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate( Date modifiedDate ) {
    this.modifiedDate = modifiedDate;
  }
}
