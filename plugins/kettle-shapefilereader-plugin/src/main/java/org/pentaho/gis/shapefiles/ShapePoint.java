/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.gis.shapefiles;

import java.awt.geom.Point2D;

/*
 * Created on 13-mei-04
 *
 * @author Matt
 *
 */

public class ShapePoint extends Shape implements ShapeInterface {
  public double x, y;

  /*
                                                             Byte
    Position      Field       Value      Type     Number     Order
    ---------------------------------------------------------------
    Byte 0        Shape Type  23         Integer  1          Little
    Byte 4        Box         Box        Double   4          Little
    Byte 36       NumParts    NumParts   Integer  1          Little
    Byte 40       NumPoints   NumPoints  Integer  1          Little
    Byte 44       Parts       Parts      Integer  NumParts   Little
    Byte X        Points      Points     Point    NumPoints  Little
    Byte Y*       Mmin        Mmin       Double   1          Little
    Byte Y + 8*   Mmax        Mmax       Double   1          Little
    Byte Y + 16*  Marray      Marray     Double   NumPoints  Little

    Note: X = 44 + (4 * NumParts), Y = X + (16 * NumPoints)
    * optional

  **/

  public ShapePoint( byte[] content ) {
    super( Shape.SHAPE_TYPE_POINT );

    x = Converter.getDoubleLittle( content,  4 );
    y = Converter.getDoubleLittle( content, 12 );
  }

  public ShapePoint( double x, double y ) {
    super( Shape.SHAPE_TYPE_POINT );
    this.x = x;
    this.y = y;
  }

  // X & Y can be "slighly" different when working whith doubles.
  // Therefor, we calculate the distance between the 2 points
  // If the distance is smaller then 0.0001 we consider them equal!
  //
  public boolean equals( ShapePoint p ) {
    return getDistance( p ) < 0.0001;
  }

  public double getDistance( ShapePoint p ) {
    return ( ( p.x - x ) * ( p.x - x ) )
           + ( ( p.y - y ) * ( p.y - y ) );
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }

  public Point2D.Double getPoint2D() {
    return new Point2D.Double( x, y );
  }
}
