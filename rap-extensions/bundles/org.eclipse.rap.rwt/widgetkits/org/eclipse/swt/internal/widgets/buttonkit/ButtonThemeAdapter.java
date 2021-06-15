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
package org.eclipse.swt.internal.widgets.buttonkit;

import org.eclipse.rap.rwt.internal.theme.Size;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Button;


public class ButtonThemeAdapter extends ControlThemeAdapterImpl {

  private static final Size ZERO = new Size( 0, 0 );

  @Override
  protected void configureMatcher( WidgetMatcher matcher ) {
    super.configureMatcher( matcher );
    matcher.addStyle( "FLAT", SWT.FLAT );
    matcher.addStyle( "ARROW", SWT.ARROW );
    matcher.addStyle( "PUSH", SWT.PUSH );
    matcher.addStyle( "TOGGLE", SWT.TOGGLE );
    matcher.addStyle( "CHECK", SWT.CHECK );
    matcher.addStyle( "RADIO", SWT.RADIO );
    matcher.addStyle( "UP", SWT.UP );
    matcher.addStyle( "DOWN", SWT.DOWN );
    matcher.addStyle( "LEFT", SWT.LEFT );
    matcher.addStyle( "RIGHT", SWT.RIGHT );
  }

  public int getSpacing( Button button ) {
    return getCssDimension( "Button", "spacing", button );
  }

  public int getCheckSpacing( Button button ) {
    return getCssDimension( "Button", "spacing", button );
  }

  public Size getCheckSize( Button button ) {
    if( ( button.getStyle() & SWT.RADIO ) != 0) {
      return getCssImageSize( "Button-RadioIcon", "background-image", button );
    }
    if( ( button.getStyle() & SWT.CHECK ) != 0) {
      return getCssImageSize( "Button-CheckIcon", "background-image", button );
    }
    return ZERO;
  }

  public Size getArrowSize( Button button ) {
    if( ( button.getStyle() & SWT.ARROW ) != 0) {
      return getCssImageSize( "Button-ArrowIcon", "background-image", button );
    }
    return ZERO;
  }

}
