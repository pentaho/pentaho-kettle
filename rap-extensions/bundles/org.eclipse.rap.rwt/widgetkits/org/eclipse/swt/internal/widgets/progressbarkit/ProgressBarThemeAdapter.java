/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.progressbarkit;

import org.eclipse.rap.rwt.internal.theme.WidgetMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.ProgressBar;


public class ProgressBarThemeAdapter extends ControlThemeAdapterImpl {

  @Override
  protected void configureMatcher( WidgetMatcher matcher ) {
    matcher.addStyle( "HORIZONTAL", SWT.HORIZONTAL );
    matcher.addStyle( "VERTICAL", SWT.VERTICAL );
  }

  public int getProgressBarWidth( ProgressBar progressBar ) {
    return getCssDimension( "ProgressBar", "width", progressBar );
  }

}
