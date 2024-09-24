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

package org.pentaho.di.core.gui;

public class Rectangle {
  public int x;
  public int y;
  public int width;
  public int height;

  /**
   * @param x
   * @param y
   * @param width
   * @param height
   */
  public Rectangle( int x, int y, int width, int height ) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public boolean contains( int x2, int y2 ) {
    return x2 >= x && x2 <= x + width && y2 >= y && y2 <= y + height;
  }

}
