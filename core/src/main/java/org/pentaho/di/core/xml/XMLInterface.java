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

package org.pentaho.di.core.xml;

import org.pentaho.di.core.exception.KettleException;

/**
 * Implementing classes of this interface know how to express themselves using XML They also can construct themselves
 * using XML.
 *
 * @author Matt
 * @since 29-jan-2004
 */
public interface XMLInterface {
  /**
   * Describes the Object implementing this interface as XML
   *
   * @return the XML string for this object
   * @throws KettleException
   *           in case there is an encoding problem.
   */
  public String getXML() throws KettleException;

}
