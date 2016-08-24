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
 * Created on 30-jun-2004
 *
 * @author Matt
 *
 * Contains the information in the header of the Esri Shape file...
 *
 */

public class ShapeRecordHeader {
  public int number;
  public int length;

  /*
    Position  Field           Value           Type     Order
    --------------------------------------------------------
    Byte 0    Record Number   Record Number   Integer  Big
    Byte 4    Content Length  Content Length  Integer  Big

  */

  public ShapeRecordHeader( byte[] header ) {
    number = Converter.getIntegerBig( header,  0 );

    // The length is in 16-bit words: nr of bytes x 2!!
    length = Converter.getIntegerBig( header,  4 ) * 2;
  }
}
