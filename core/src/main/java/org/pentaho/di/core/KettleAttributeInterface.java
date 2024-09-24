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

package org.pentaho.di.core;

public interface KettleAttributeInterface {

  /**
   * @return the key for this attribute, usually the repository code.
   */
  public String getKey();

  /**
   * @return the xmlCode
   */
  public String getXmlCode();

  /**
   * @return the repCode
   */
  public String getRepCode();

  /**
   * @return the description
   */
  public String getDescription();

  /**
   * @return the tooltip
   */
  public String getTooltip();

  /**
   * @return the type
   */
  public int getType();

  /**
   * @return The parent interface.
   */
  public KettleAttributeInterface getParent();
}
