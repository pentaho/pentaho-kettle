/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.textkit;

import org.eclipse.rap.rwt.internal.theme.Size;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher.Constraint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


public class TextThemeAdapter extends ControlThemeAdapterImpl {

  @Override
  protected void configureMatcher( WidgetMatcher matcher ) {
    super.configureMatcher( matcher );
    matcher.addStyle( "SINGLE", SWT.SINGLE );
    matcher.addStyle( "MULTI", SWT.MULTI );
    matcher.addState( "read-only", new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        Text text = ( Text )widget;
        return !text.getEditable();
      }
    });
  }

  public Size getSearchIconImageSize( Control control ) {
    return getCssImageSize( "Text-Search-Icon", "background-image", control );
  }

  public int getSearchIconSpacing( Control control ) {
    return Math.max( 0, getCssDimension( "Text-Search-Icon", "spacing", control ) );
  }

  public Size getCancelIconImageSize( Control control ) {
    return getCssImageSize( "Text-Cancel-Icon", "background-image", control );
  }

  public int getCancelIconSpacing( Control control ) {
    return Math.max( 0, getCssDimension( "Text-Cancel-Icon", "spacing", control ) );
  }

}
