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

package org.pentaho.di.resource;

public interface ResourceXmlPropertyEmitterInterface {

  /**
   * Allows injection of additional relevant properties in the to-xml of the Resource Reference.
   *
   * @param ref
   *          The Resource Reference Holder (a step, or a job entry)
   * @param indention
   *          If -1, then no indenting, otherwise, it's the indent level to indent the XML strings
   * @return String of injected XML
   */
  public String getExtraResourceProperties( ResourceHolderInterface ref, int indention );

}
