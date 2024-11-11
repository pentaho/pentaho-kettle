/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.spoon;

/**
 * Implementations can be registered with SpoonPerspectives to receive notification of state changes.
 *
 * @author nbaker
 *
 */
public interface SpoonPerspectiveListener {
  void onActivation();

  void onDeactication();
}
