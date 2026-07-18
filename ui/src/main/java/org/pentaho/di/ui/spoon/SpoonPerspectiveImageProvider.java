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


package org.pentaho.di.ui.spoon;

public interface SpoonPerspectiveImageProvider extends SpoonPerspective {

  /**
   * Get the path to the perspective's icon, can be within a jar (classLoader) or
   * file-system.
   * 
   * @return the path to the perspective's icon
   */
  public String getPerspectiveIconPath();

}
