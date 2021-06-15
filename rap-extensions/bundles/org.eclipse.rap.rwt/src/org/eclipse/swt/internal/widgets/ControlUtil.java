/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.widgets.Control;


public class ControlUtil {

  public static IControlAdapter getControlAdapter( Control control ) {
    return control.getAdapter( IControlAdapter.class );
  }
  
  private ControlUtil() {
    // prevent instantiation
  }
}
