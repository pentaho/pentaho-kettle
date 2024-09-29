/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.xbaseinput.XBase;


/*
 * Created on 30-jun-2004
 *
 * @author Matt
 *
 */

public class ShapeFile {
  private List<ShapeInterface> shapes;
  private ShapeFileHeader fileheader;
  private LogChannelInterface log;

  private String dbfFilename;
  private String shapeFilename;
  private String encoding;

  public ShapeFile( LogChannelInterface log, String name ) {
    this.log = log;
    dbfFilename = name + ".dbf";
    shapeFilename = name + ".shp";

    shapes = new ArrayList<ShapeInterface>();
  }

  public ShapeFile( LogChannelInterface log, String shapeFilename, String dbfFilename ) {
    this.log = log;
    this.shapeFilename = shapeFilename;
    this.dbfFilename = dbfFilename;

    shapes = new ArrayList<ShapeInterface>();
  }

  public ShapeFile( LogChannelInterface log, String shapeFilename, String dbfFilename, String encoding ) {
    this.log = log;
    this.shapeFilename = shapeFilename;
    this.dbfFilename = dbfFilename;
    this.encoding = encoding;

    shapes = new ArrayList<ShapeInterface>();
  }

  /**
   * @return Returns the shapeFilename.
   */
  public String getShapeFilename() {
    return shapeFilename;
  }

  /**
   * @return Returns the dbfFilename.
   */
  public String getDbfFilename() {
    return dbfFilename;
  }

  public void readFile() throws GisException, KettleException {
    File file = new File( shapeFilename );
    try {
      // Open shape file & DBF file...
      DataInputStream dis = new DataInputStream( new FileInputStream( file ) );

      XBase xbase = new XBase( log, dbfFilename );
      xbase.open(); // throws exception now

      //Set encoding
      if ( StringUtils.isNotBlank( encoding ) ) {
        xbase.getReader().setCharactersetName( encoding );
      }

      // First determine the meta-data for this dbf file...
      RowMetaInterface fields = xbase.getFields();

      // Read the header data...
      byte[] header = new byte[100];
      dis.read( header );
      fileheader = new ShapeFileHeader( header );
      int id = 0;
      while ( dis.available() > 0 ) {
        // Read the record header to see the length of the next shape...
        byte[] record_header = new byte[8];
        dis.read( record_header );
        ShapeRecordHeader erh = new ShapeRecordHeader( record_header );

        // Read the actual content of the shape
        if ( erh.length <= dis.available() ) {
          byte[] content = new byte[erh.length];
          dis.read( content );

          // Determine the shape type...
          int btype = Converter.getIntegerLittle( content, 0 );
          ShapeInterface esi = null;

          switch ( btype ) {
            case Shape.SHAPE_TYPE_NULL:
              esi = new ShapeNull( content );
              break;
            case Shape.SHAPE_TYPE_POINT:
              esi = new ShapePoint( content );
              break;
            case Shape.SHAPE_TYPE_POLYLINE:
              esi = new ShapePolyLine( content );
              break;
            case Shape.SHAPE_TYPE_POLYGON:
              esi = new ShapePolygon( content );
              break;
            case Shape.SHAPE_TYPE_POLYLINE_M:
              esi = new ShapePolyLineM( content );
              break;
            default:
              throw new GisException( "shape type : " + btype + " not recognized! (" + Shape.getEsriTypeDesc( btype ) + ")" );
          }

          // Get a row from the associated DBF file...
          Object[] row = xbase.getRow( fields );
          if ( row != null ) {
            esi.setDbfData( row );
            esi.setDbfMeta( xbase.getFields() );
          }

          shapes.add( esi );
          id++;
        }
      }

      dis.close();
      xbase.close();
    } catch ( IOException e ) {
      throw new GisException( "Error reading shape file", e );
    }
  }

  public int getNrShapes() {
    return shapes.size();
  }

  public ShapeInterface getShape( int i ) {
    return shapes.get( i );
  }

  public void addShape( ShapeInterface esi ) {
    shapes.add( esi );
  }

  public ShapeFileHeader getFileHeader() {
    return fileheader;
  }

  public String getFilename() {
    return shapeFilename;
  }
}
