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

package org.pentaho.di.core;

import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.UndoInterface;

public interface AddUndoPositionInterface {
  public void addUndoPosition( UndoInterface undoInterface, Object[] obj, int[] pos, Point[] prev, Point[] curr );
}
