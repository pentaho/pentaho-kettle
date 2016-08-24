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
