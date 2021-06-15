/*******************************************************************************
 * Copyright (c) 2007, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.controlkit;

import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher.Constraint;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.rap.rwt.theme.ControlThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;


public class ControlThemeAdapterImpl extends ThemeAdapter implements ControlThemeAdapter {

  @Override
  protected void configureMatcher( WidgetMatcher matcher ) {
    matcher.addStyle( "BORDER", SWT.BORDER );
    matcher.addState( "disabled", new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        if( widget instanceof Control ) {
          return !( ( Control )widget ).getEnabled();
        }
        return false;
      }
    });
  }

  @Override
  public BoxDimensions getBorder( Control control ) {
    return getCssBorder( getPrimaryElement( control ), control );
  }

  @Override
  public BoxDimensions getPadding( Control control ) {
    return getCssBoxDimensions( getPrimaryElement( control ), "padding", control ).dimensions;
  }

  @Override
  public Color getForeground( Control control ) {
    return getCssColor( getPrimaryElement( control ), "color", control );
  }

  @Override
  public Color getBackground( Control control ) {
    return getCssColor( getPrimaryElement( control ), "background-color", control );
  }

  @Override
  public Font getFont( Control control ) {
    return getCssFont( getPrimaryElement( control ), "font", control );
  }

}
