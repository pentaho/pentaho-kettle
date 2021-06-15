/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying material
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.expandbarkit;

import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.ExpandBar;


public class ExpandBarThemeAdapter extends ControlThemeAdapterImpl {

  public BoxDimensions getItemBorder( ExpandBar bar ) {
    return getCssBorder( "ExpandItem", bar );
  }

  public BoxDimensions getItemHeaderBorder( ExpandBar bar ) {
    return getCssBorder( "ExpandItem-Header", bar );
  }

}
