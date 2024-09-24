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
