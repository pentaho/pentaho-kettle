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

public class ShapeFileHeader {
  public int filecode;
  public int unused1, unused2, unused3, unused4, unused5;
  public int file_length;
  public int version;
  public int shapetype;
  public double bbox_xmin, bbox_ymin;
  public double bbox_xmax, bbox_ymax;
  public double bbox_zmin, bbox_zmax;
  public double bbox_mmin, bbox_mmax;

  /*
    Position   Field         Value        Type     Order
    -----------------------------------------------------
    Byte 0     File Code     9994         Integer  Big
    Byte 4     Unused        0            Integer  Big
    Byte 8     Unused        0            Integer  Big
    Byte 12    Unused        0            Integer  Big
    Byte 16    Unused        0            Integer  Big
    Byte 20    Unused        0            Integer  Big
    Byte 24    File Length   File Length  Integer  Big
    Byte 28    Version       1000         Integer  Little
    Byte 32    Shape Type    Shape Type   Integer  Little
    Byte 36    Bounding Box  Xmin         Double   Little
    Byte 44    Bounding Box  Ymin         Double   Little
    Byte 52    Bounding Box  Xmax         Double   Little
    Byte 60    Bounding Box  Ymax         Double   Little
    Byte 68*   Bounding Box  Zmin         Double   Little
    Byte 76*   Bounding Box  Zmax         Double   Little
    Byte 84*   Bounding Box  Mmin         Double   Little
    Byte 92*   Bounding Box  Mmax         Double   Little

    * Unused, with value 0.0, if not Measured or Z type
  */

  public ShapeFileHeader( byte[] header ) {
    filecode = Converter.getIntegerBig( header,  0 );
    unused1 = Converter.getIntegerBig( header,  4 );
    unused2 = Converter.getIntegerBig( header,  8 );
    unused3 = Converter.getIntegerBig( header, 12 );
    unused4 = Converter.getIntegerBig( header, 16 );
    unused5 = Converter.getIntegerBig( header, 20 );
    file_length = Converter.getIntegerBig( header, 24 );
    version = Converter.getIntegerLittle( header, 28 );
    shapetype = Converter.getIntegerLittle( header, 32 );

    bbox_xmin = Converter.getDoubleLittle( header, 36 );
    bbox_ymin = Converter.getDoubleLittle( header, 44 );
    bbox_xmax = Converter.getDoubleLittle( header, 52 );
    bbox_ymax = Converter.getDoubleLittle( header, 60 );
    bbox_zmin = Converter.getDoubleLittle( header, 68 );
    bbox_zmax = Converter.getDoubleLittle( header, 76 );
    bbox_mmin = Converter.getDoubleLittle( header, 84 );
    bbox_mmax = Converter.getDoubleLittle( header, 92 );
  }

  public int getShapeType() {
    return shapetype;
  }

  public String getShapeTypeDesc() {
    return Shape.getEsriTypeDesc( shapetype );
  }
}
