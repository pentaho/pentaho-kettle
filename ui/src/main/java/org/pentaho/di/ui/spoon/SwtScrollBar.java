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

package org.pentaho.di.ui.spoon;

import org.eclipse.swt.widgets.ScrollBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.ScrollBarInterface;

public class SwtScrollBar implements ScrollBarInterface {

  private ScrollBar scrollBar;

  /**
   * @param scrollBar
   */
  public SwtScrollBar( ScrollBar scrollBar ) {
    this.scrollBar = scrollBar;
  }

  public int getSelection() {
    if ( Const.isRunningOnWebspoonMode() ) {
      return Math.round( scrollBar.getSelection() / scrollBar.getMaximum() );
    } else {
      return scrollBar.getSelection();
    }
  }

  public void setThumb( int thumb ) {
    scrollBar.setThumb( thumb );
  }
}
