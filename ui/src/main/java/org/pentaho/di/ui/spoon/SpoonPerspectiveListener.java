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
