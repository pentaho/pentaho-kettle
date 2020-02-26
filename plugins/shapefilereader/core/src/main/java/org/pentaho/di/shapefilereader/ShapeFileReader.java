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

/*
 * Created on 4-apr-2003
 *
 */

package org.pentaho.di.shapefilereader;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.gis.shapefiles.Shape;
import org.pentaho.gis.shapefiles.ShapeFile;
import org.pentaho.gis.shapefiles.ShapeInterface;
import org.pentaho.gis.shapefiles.ShapePoint;
import org.pentaho.gis.shapefiles.ShapePolyLine;
import org.pentaho.gis.shapefiles.ShapePolyLineM;
import org.pentaho.gis.shapefiles.ShapePolygon;


public class ShapeFileReader extends BaseStep implements StepInterface {
  private ShapeFileReaderMeta meta;
  private ShapeFileReaderData data;

  public ShapeFileReader( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
  }

  public synchronized boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleStepException {
    meta = (ShapeFileReaderMeta) smi;
    data = (ShapeFileReaderData) sdi;
    int partnr;

    boolean retval = true;

    if ( data.shapeNr >= data.shapeFile.getNrShapes() ) {
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this );
    }

    // building new row
    Object[] outputRow = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    int outputIndex;

    // getting shape # data.shapeNr from shapefile

    // Get the shape from the shapefile
    //
    ShapeInterface si = data.shapeFile.getShape( data.shapeNr );
    switch ( si.getType() ) {
      case Shape.SHAPE_TYPE_POLYLINE_M:
        // PolyLimeM";
        ShapePolyLineM eplm = (ShapePolyLineM) si;
        partnr = 0;
        for ( int j = 0; j < eplm.nrpoints; j++ ) {
          // PolyLimeM, point #"+j;
          for ( int k = 0; k < eplm.nrparts; k++ ) {
            if ( j == eplm.part_starts[ k ] ) {
              partnr++;
            }
          }

          outputIndex = 0;

          // adding the basics";
          // Add the basics...
          // The filename...
          outputRow[ outputIndex++ ] = meta.getShapeFilename();

          // The file type
          outputRow[ outputIndex++ ] = data.shapeFile.getFileHeader().getShapeTypeDesc();

          // The shape nr
          outputRow[ outputIndex++ ] = new Long( data.shapeNr + 1 );

          // The part nr
          outputRow[ outputIndex++ ] = new Long( partnr );

          // The nr of parts
          outputRow[ outputIndex++ ] = new Long( eplm.nrparts );

          // The point nr
          outputRow[ outputIndex++ ] = new Long( j + 1 );

          // The nr of points
          outputRow[ outputIndex++ ] = new Long( eplm.nrpoints );

          // The X coordinate
          outputRow[ outputIndex++ ] = new Double( eplm.point[ j ].x );

          // The Y coordinate
          outputRow[ outputIndex++ ] = new Double( eplm.point[ j ].y );

          // The measure
          outputRow[ outputIndex++ ] = new Double( eplm.measures[ j ] );

          // The Values in the DBF file...
          // PolyLimeM, point #"+j+", add dbf data";
          Object[] dbfData = si.getDbfData();
          RowMetaInterface dbfMeta = si.getDbfMeta();
          for ( int d = 0; d < dbfMeta.size(); d++ ) {
            outputRow[ outputIndex++ ] = dbfData[ d ];
          }

          linesInput++;

          // Put it out to the rest of the world...
          try {
            putRow( data.outputRowMeta, data.outputRowMeta.cloneRow( outputRow ) );
          } catch ( KettleValueException e ) {
            throw new KettleStepException( "Unable to clone row", e );
          }
        }
        break;
      case Shape.SHAPE_TYPE_POLYGON:
        // ShapePolygon";
        ShapePolygon eplg = (ShapePolygon) si;
        partnr = 0;
        for ( int j = 0; j < eplg.nrpoints; j++ ) {
          // PolyLime, point #"+j;
          for ( int k = 0; k < eplg.nrparts; k++ ) {
            if ( j == eplg.part_starts[ k ] ) {
              partnr++;
            }
          }

          outputIndex = 0;

          // adding the basics";
          // Add the basics...
          // The filename...
          outputRow[ outputIndex++ ] = meta.getShapeFilename();

          // The file type
          outputRow[ outputIndex++ ] = data.shapeFile.getFileHeader().getShapeTypeDesc();

          // The shape nr
          outputRow[ outputIndex++ ] = new Long( data.shapeNr + 1 );

          // The part nr
          outputRow[ outputIndex++ ] = new Long( partnr );

          // The nr of parts
          outputRow[ outputIndex++ ] = new Long( eplg.nrparts );

          // The point nr
          outputRow[ outputIndex++ ] = new Long( j + 1 );

          // The nr of points
          outputRow[ outputIndex++ ] = new Long( eplg.nrpoints );

          // The X coordinate
          outputRow[ outputIndex++ ] = new Double( eplg.point[ j ].x );

          // The Y coordinate
          outputRow[ outputIndex++ ] = new Double( eplg.point[ j ].y );

          // The measure
          outputRow[ outputIndex++ ] = new Double( 0.0 );

          // The Values in the DBF file...
          // PolyLime, point #"+j+", add dbf data";
          //
          Object[] dbfData = si.getDbfData();
          RowMetaInterface dbfMeta = si.getDbfMeta();
          for ( int d = 0; d < dbfMeta.size(); d++ ) {
            outputRow[ outputIndex++ ] = dbfData[ d ];
          }
          linesInput++;

          // Put it out to the rest of the world...
          try {
            putRow( data.outputRowMeta, data.outputRowMeta.cloneRow( outputRow ) );
          } catch ( KettleValueException e ) {
            throw new KettleStepException( "Unable to clone row", e );
          }
        }
        break;

      case Shape.SHAPE_TYPE_POLYLINE:
        // PolyLime";
        ShapePolyLine epl = (ShapePolyLine) si;
        partnr = 0;
        for ( int j = 0; j < epl.nrpoints; j++ ) {
          // PolyLime, point #"+j;
          for ( int k = 0; k < epl.nrparts; k++ ) {
            if ( j == epl.part_starts[ k ] ) {
              partnr++;
            }
          }

          outputIndex = 0;

          // adding the basics";
          // Add the basics...
          // The filename...
          outputRow[ outputIndex++ ] = meta.getShapeFilename();

          // The file type
          outputRow[ outputIndex++ ] = data.shapeFile.getFileHeader().getShapeTypeDesc();

          // The shape nr
          outputRow[ outputIndex++ ] = new Long( data.shapeNr + 1 );

          // The part nr
          outputRow[ outputIndex++ ] = new Long( partnr );

          // The nr of parts
          outputRow[ outputIndex++ ] = new Long( epl.nrparts );

          // The point nr
          outputRow[ outputIndex++ ] = new Long( j + 1 );

          // The nr of points
          outputRow[ outputIndex++ ] = new Long( epl.nrpoints );

          // The X coordinate
          outputRow[ outputIndex++ ] = new Double( epl.point[ j ].x );

          // The Y coordinate
          outputRow[ outputIndex++ ] = new Double( epl.point[ j ].y );

          // The measure
          outputRow[ outputIndex++ ] = new Double( 0.0 );

          // The Values in the DBF file...
          // PolyLime, point #"+j+", add dbf data";
          //
          Object[] dbfData = si.getDbfData();
          RowMetaInterface dbfMeta = si.getDbfMeta();
          for ( int d = 0; d < dbfMeta.size(); d++ ) {
            outputRow[ outputIndex++ ] = dbfData[ d ];
          }
          linesInput++;

          // Put it out to the rest of the world...
          try {
            putRow( data.outputRowMeta, data.outputRowMeta.cloneRow( outputRow ) );
          } catch ( KettleValueException e ) {
            throw new KettleStepException( "Unable to clone row", e );
          }
        }
        break;
      case Shape.SHAPE_TYPE_POINT:
        // Point";
        ShapePoint ep = (ShapePoint) si;

        // Add the basics...

        outputIndex = 0;

        // The filename...
        outputRow[ outputIndex++ ] = meta.getShapeFilename();

        // The file type
        outputRow[ outputIndex++ ] = data.shapeFile.getFileHeader().getShapeTypeDesc();

        // The shape nr
        outputRow[ outputIndex++ ] = new Long( data.shapeNr );

        // The part nr
        outputRow[ outputIndex++ ] = new Long( 0L );

        // The nr of parts
        outputRow[ outputIndex++ ] = new Long( 0L );

        // The point nr
        outputRow[ outputIndex++ ] = new Long( 0L );

        // The nr of points
        outputRow[ outputIndex++ ] = new Long( 0L );

        // The X coordinate
        outputRow[ outputIndex++ ] = new Double( ep.x );

        // The Y coordinate
        outputRow[ outputIndex++ ] = new Double( ep.y );

        // The measure
        outputRow[ outputIndex++ ] = new Double( 0.0 );

        // The Values in the DBF file...
        // PolyLimeM, point #"+data.shapeNr+", add dbf data";
        //
        Object[] dbfData = si.getDbfData();
        RowMetaInterface dbfMeta = si.getDbfMeta();
        for ( int d = 0; d < dbfMeta.size(); d++ ) {
          outputRow[ outputIndex++ ] = dbfData[ d ];
        }

        linesInput++;

        // Put it out to the rest of the world...
        try {
          putRow( data.outputRowMeta, data.outputRowMeta.cloneRow( outputRow ) );
        } catch ( KettleValueException e ) {
          throw new KettleStepException( "Unable to clone row", e );
        }
        break;
      default:
        System.out.println(
          "Unable to parse shape type [" + Shape.getEsriTypeDesc( si.getType() ) + "] : not yet implemented." );
        throw new KettleStepException(
          "Unable to parse shape type [" + Shape.getEsriTypeDesc( si.getType() ) + "] : not yet implemented." );
    }

    // Next shape please!
    data.shapeNr++;

    if ( ( linesInput % Const.ROWS_UPDATE ) == 0 ) {
      logBasic( "linenr " + linesInput );
    }

    return retval;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ShapeFileReaderMeta) smi;
    data = (ShapeFileReaderData) sdi;

    if ( super.init( meta, data ) ) {
      if ( meta.getShapeFilename() == null
        || meta.getDbfFilename() == null
        || meta.getShapeFilename().length() == 0
        || meta.getDbfFilename().length() == 0
      ) {
        logError( "We need both a shapefile and a DBF file." );
        return false;
      }

      if ( StringUtils.isBlank( meta.getEncoding() ) ) {
        meta.setEncoding( getTransMeta().getVariable( "ESRI.encoding" ) );
      }

      String shapeFilename = environmentSubstitute( meta.getShapeFilename() );
      if ( shapeFilename.startsWith( "file:" ) ) {
        shapeFilename = shapeFilename.substring( 5 );
      }

      String dbFilename = environmentSubstitute( meta.getDbfFilename() );
      if ( dbFilename.startsWith( "file:" ) ) {
        dbFilename = dbFilename.substring( 5 );
      }

      data.shapeFile = new ShapeFile( log, shapeFilename, dbFilename, meta.getEncoding() );
      try {
        data.shapeFile.readFile();
      } catch ( Exception e ) {
        logError( "Unable to read shapefile [" + shapeFilename + "] because of an error: " + e.toString() );
        return false;
      }

      data.shapeNr = 0;
    }
    return true;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ShapeFileReaderMeta) smi;
    data = (ShapeFileReaderData) sdi;

    super.dispose( smi, sdi );
  }

  //
  // Run is were the action happens!
  //
  //
  public void run() {
    logBasic( "Starting to run..." );

    try {
      while ( processRow( meta, data ) ) {
        if ( isStopped() ) {
          break;
        }
      }
    } catch ( Exception e ) {
      logError( "Unexpected error", e );
      setErrors( 1 );
      stopAll();
    } finally {
      dispose( meta, data );
      logBasic( "Finished, processed " + linesInput + " rows, written " + linesWritten + " lines." );
      markStop();
    }
  }
}
