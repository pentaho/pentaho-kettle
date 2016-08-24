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

/*
 * Created on 07-juli-04
 *
 * @author Matt
 *
 */

public class ShapePolygon extends Shape implements ShapeInterface {
  public int nrparts;
  public int nrpoints;
  public int nrmeasures;

  public double box_minx, box_miny, box_maxx, box_maxy;
  public int[] part_starts;
  public ShapePoint[] point;

  /*
                                                           Byte
      Position  Field       Value      Type     Number     Order
      -------------------------------------------------------------------
      Byte 0    Shape Type  23         Integer  1          Little
      Byte 4    Box         Box        Double   4          Little
      Byte 36   NumParts    NumParts   Integer  1          Little
      Byte 40   NumPoints   NumPoints  Integer  1          Little
      Byte 44   Parts       Parts      Integer  NumParts   Little
      Byte X    Points      Points     Point    NumPoints  Little

      Note: X = 44 + (4 * NumParts)
  */

  public ShapePolygon( byte[] content ) {
    super( Shape.SHAPE_TYPE_POLYGON );

    int pos;

    box_minx = Converter.getDoubleLittle( content, 4 );
    box_miny = Converter.getDoubleLittle( content, 12 );
    box_maxx = Converter.getDoubleLittle( content, 20 );
    box_maxy = Converter.getDoubleLittle( content, 28 );

    nrparts = Converter.getIntegerLittle( content, 36 );
    nrpoints = Converter.getIntegerLittle( content, 40 );

    // int pos_parts = 44;
    // int pos_points = pos_parts + (nrparts*4);
    // int pos_mmin   = pos_points + (16*nrpoints);
    // int pos_mmax   = pos_mmin + 8;
    // int pos_marray = pos_mmax + 16;
    // int tot_length = pos_marray + (8*nrpoints);
    //System.out.println("\n\nlength="+content.length+", nrparts="+nrparts+", nrpoints="+nrpoints+", pos_marray="+pos_marray+", totlen="+tot_length);


    part_starts = new int[nrparts];
    point = new ShapePoint[nrpoints];

    // Determine the starting point for the parts...
    for ( int i = 0; i < nrparts; i++ ) {
      pos = 44 + i * 4;
      part_starts[i] = Converter.getIntegerLittle( content, pos );
    }

    for ( int i = 0; i < nrpoints; i++ ) {
      pos = ( 44 + nrparts * 4 ) + ( 16 * i );
      double pointx = Converter.getDoubleLittle( content, pos );
      double pointy = Converter.getDoubleLittle( content, pos + 8 );
      point[i] = new ShapePoint( pointx, pointy );
    }
  }

  public boolean contains( ShapePoint p ) {
    for ( int i = 0; i < nrpoints; i++ ) {
      if ( p.equals( point[i] ) ) {
        return true;
      }
    }
    return false;
  }
}
