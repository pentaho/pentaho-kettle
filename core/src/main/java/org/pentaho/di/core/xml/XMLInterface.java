/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
