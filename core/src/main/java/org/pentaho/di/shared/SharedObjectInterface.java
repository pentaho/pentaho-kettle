/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.shared;

import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

public interface SharedObjectInterface<T extends SharedObjectInterface> {

  static final String OBJECT_ID = "object_id";

  /**
   * @deprecated
   *
   */
  @Deprecated
  public void setShared( boolean shared );

  /**
   * @deprecated
   *
   */
  @Deprecated
  public boolean isShared();

  public String getName();

  public String getXML() throws KettleException;

  public Date getChangedDate();

  /**
   * Wrapper method for clone() so clone() can be called from interface
   * @return Object
   */
  public T makeClone();

  public Node toNode() throws KettleException;

}
