/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.theme;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;


/**
 * Theme adapter for {@link Control}s. The theme adapter provides a control
 * with information on characteristics of its representation which depend on the
 * current theme.
 *
 * @since 2.0
 */
public interface ControlThemeAdapter {

  /**
   * Returns the border of the specified control.
   *
   * @param control the control whose border is requested
   * @return the border
   * @since 3.0
   */
  BoxDimensions getBorder( Control control );

  /**
   * Returns the padding of the specified control.
   *
   * @param control the control whose padding is requested
   * @return the padding
   * @since 3.0
   */
  BoxDimensions getPadding( Control control );

  /**
   * Returns the default foreground color that the specified control will use to
   * draw if no user defined foreground color has been set using
   * {@link Control#setForeground(Color)}.
   * @param control the control whose foreground color is requested
   *
   * @return the foreground color
   */
  Color getForeground( Control control );

  /**
   * Returns the default background color that the specified control will use if
   * no user-defined background color has been set using
   * {@link Control#setBackground(Color)}.
   *
   * @param control the control whose background color is requested
   * @return the background color
   */
  Color getBackground( Control control );

  /**
   * Returns the default font that the specified control will use to paint
   * textual information when no user-defined font has been set using
   * {@link Control#setFont(Font)}.
   *
   * @param control the control whose font is requested
   * @return the font
   */
  Font getFont( Control control );

}
