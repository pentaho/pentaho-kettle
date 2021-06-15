/*******************************************************************************
 * Copyright (c) 2007, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import org.eclipse.rap.rwt.internal.RWTProperties;


public final class UITestUtil {

  static boolean enabled;

  static {
    String property = System.getProperty( RWTProperties.ENABLE_UI_TESTS );
    enabled = Boolean.valueOf( property ).booleanValue();
  }

  public static boolean isEnabled() {
    return enabled;
  }

  private UITestUtil() {
    // prevent instantiation
  }

}
