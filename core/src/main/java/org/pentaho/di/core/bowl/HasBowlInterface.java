/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.bowl;

public interface HasBowlInterface {
  /**
   * @return the bowl
   */
  public Bowl getBowl();

  /**
   * @param bowl
   *          the bowl to set
   */
  public void setBowl( Bowl bowl );

}
