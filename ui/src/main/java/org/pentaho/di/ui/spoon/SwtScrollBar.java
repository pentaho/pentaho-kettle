/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
