/*******************************************************************************
 * Copyright (c) 2011, 2015 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import org.eclipse.swt.internal.widgets.WidgetTreeVisitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;


class ClearLayoutBuffersVisitor implements WidgetTreeVisitor {

  @Override
  public boolean visit( Widget widget ) {
    if( widget instanceof Composite ) {
      Composite composite = ( Composite )widget;
      composite.changed( composite.getChildren() );
    }
    return true;
  }

}
