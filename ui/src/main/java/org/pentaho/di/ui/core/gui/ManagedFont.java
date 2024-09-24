/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.core.gui;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Class to keep track of which font is a system font (managed by the OS) and which is not.
 *
 * @author Matt
 * @since 2006-06-15
 *
 */
public class ManagedFont {
  private Font font;
  private boolean systemFont;

  /**
   * @param font
   *          The font
   * @param systemFont
   *          true if this is a system font and doesn't need to be disposed off
   */
  public ManagedFont( Font font, boolean systemFont ) {
    this.font = font;
    this.systemFont = systemFont;
  }

  /**
   * Create a new managed font by using fontdata
   *
   * @param display
   *          the display to use
   * @param fontData
   *          The fontdata to create the font with.
   */
  public ManagedFont( Display display, FontData fontData ) {
    this.font = new Font( display, fontData );
    this.systemFont = false;
  }

  /**
   * Free the managed resource if it hasn't already been done and if this is not a system font
   *
   */
  public void dispose() {
    // System color and already disposed off colors don't need to be disposed!
    if ( !systemFont && !font.isDisposed() ) {
      font.dispose();
    }
  }

  /**
   * @return Returns the font.
   */
  public Font getFont() {
    return font;
  }

  /**
   * @return true if this is a system font.
   */
  public boolean isSystemFont() {
    return systemFont;
  }

  /**
   * @param font
   *          the font to set
   */
  public void setFont( Font font ) {
    this.font = font;
  }

  /**
   * @param systemFont
   *          the systemFont to set
   */
  public void setSystemFont( boolean systemFont ) {
    this.systemFont = systemFont;
  }
}
