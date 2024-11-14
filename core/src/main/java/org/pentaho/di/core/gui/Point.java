/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.gui;

public class Point {
  public Point( int x, int y ) {
    this.x = x;
    this.y = y;
  }

  public int x;
  public int y;

  public void multiply( float factor ) {
    x = Math.round( x * factor );
    y = Math.round( y * factor );
  }
}
