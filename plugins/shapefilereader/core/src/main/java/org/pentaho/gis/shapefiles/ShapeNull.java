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


package org.pentaho.gis.shapefiles;

/*
 * Created on 13-mei-04
 *
 * @author Matt
 *
 */

public class ShapeNull extends Shape implements ShapeInterface {
  public ShapeNull( byte[] content ) {
    super( Shape.SHAPE_TYPE_NULL );
  }

  public ShapeNull() {
    super( Shape.SHAPE_TYPE_NULL );
  }

  // X & Y can be "slightly" different when working with doubles.
  // Therefore, we calculate the distance between the 2 points
  // If the distance is smaller then 0.0001 we consider them equal!
  //
  public boolean equals( ShapeNull p ) {
    return false;
  }

  @Override
  public String toString() {
    return getTypeDesc();
  }
}
