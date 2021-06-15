/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.theme;

import java.io.Serializable;


/**
 * Represents a set of dimensions that apply to the four edges of a widget, e.g. padding or border
 * widths.
 *
 * @since 3.0
 */
public class BoxDimensions implements Serializable {

  /**
   * the value for the upper edge
   */
  public final int top;
  /**
   * the value for the right edge
   */
  public final int right;
  /**
   * the value for the lower edge
   */
  public final int bottom;
  /**
   * the value for the left edge
   */
  public final int left;

  /**
   * Creates an immutable instance of BoxDimensions. Values are specified in the same order as known
   * from CSS box model, clock-wise, starting at the top.
   *
   * @param top the value for the upper edge
   * @param right the value for the right edge
   * @param bottom the value for the lower edge
   * @param left the value for the left edge
   */
  public BoxDimensions( int top, int right, int bottom, int left ) {
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + bottom;
    result = prime * result + left;
    result = prime * result + right;
    result = prime * result + top;
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if( this == obj ) {
      return true;
    }
    if( obj == null ) {
      return false;
    }
    if( getClass() != obj.getClass() ) {
      return false;
    }
    BoxDimensions other = ( BoxDimensions )obj;
    if( bottom != other.bottom ) {
      return false;
    }
    if( left != other.left ) {
      return false;
    }
    if( right != other.right ) {
      return false;
    }
    if( top != other.top ) {
      return false;
    }
    return true;
  }

}
