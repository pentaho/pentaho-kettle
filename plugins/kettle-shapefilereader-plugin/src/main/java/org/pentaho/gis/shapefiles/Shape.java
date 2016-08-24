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

import org.pentaho.di.core.row.RowMetaInterface;


/**
 * Base class for the ESRI shapes
 *
 * @author Matt
 * @since 30-jun-2004
 *
 */
public class Shape {
  public static final int SHAPE_TYPE_NULL         =  0;    // : retval = "Null Shape"; break;
  public static final int SHAPE_TYPE_POINT        =  1;    // : retval = "Point"; break;
  public static final int SHAPE_TYPE_POLYLINE     =  3;    // : retval = "PolyLine"; break;
  public static final int SHAPE_TYPE_POLYGON      =  5;    // : retval = "Polygon"; break;
  public static final int SHAPE_TYPE_MULTIPOINT   =  8;    // : retval = "MultiPoint"; break;
  public static final int SHAPE_TYPE_POINTZ       = 11;    // : retval = "PointZ"; break;
  public static final int SHAPE_TYPE_POLYLINE_Z   = 13;    // : retval = "PolyLineZ"; break;
  public static final int SHAPE_TYPE_POLYGON_Z    = 15;    // : retval = "PolygonZ"; break;
  public static final int SHAPE_TYPE_MULTIPOINT_Z = 18;    // : retval = "MultiPointZ"; break;
  public static final int SHAPE_TYPE_POINT_M      = 21;    // : retval = "PointM"; break;
  public static final int SHAPE_TYPE_POLYLINE_M   = 23;    // : retval = "PolyLineM"; break;
  public static final int SHAPE_TYPE_POLYGON_M    = 25;    // : retval = "PolygonM"; break;
  public static final int SHAPE_TYPE_MULTIPOINT_M = 28;    // : retval = "MultiPointM"; break;
  public static final int SHAPE_TYPE_MULTIPATCH   = 31;    // : retval = "MultiPatch"; break;

  private Object[] dbfData;
  private RowMetaInterface dbfMeta;

  public static final String getEsriTypeDesc( int type ) {
    String retval = "Unknown";

    switch ( type ) {
      case SHAPE_TYPE_NULL:
        retval = "Null Shape";
        break;
      case SHAPE_TYPE_POINT:
        retval = "Point";
        break;
      case SHAPE_TYPE_POLYLINE:
        retval = "PolyLine";
        break;
      case SHAPE_TYPE_POLYGON:
        retval = "Polygon";
        break;
      case SHAPE_TYPE_MULTIPOINT:
        retval = "MultiPoint";
        break;
      case SHAPE_TYPE_POINTZ:
        retval = "PointZ";
        break;
      case SHAPE_TYPE_POLYLINE_Z:
        retval = "PolyLineZ";
        break;
      case SHAPE_TYPE_POLYGON_Z:
        retval = "PolygonZ";
        break;
      case SHAPE_TYPE_MULTIPOINT_Z:
        retval = "MultiPointZ";
        break;
      case SHAPE_TYPE_POINT_M:
        retval = "PointM";
        break;
      case SHAPE_TYPE_POLYLINE_M:
        retval = "PolyLineM";
        break;
      case SHAPE_TYPE_POLYGON_M:
        retval = "PolygonM";
        break;
      case SHAPE_TYPE_MULTIPOINT_M:
        retval = "MultiPointM";
        break;
      case SHAPE_TYPE_MULTIPATCH:
        retval = "MultiPatch";
        break;
      default:
        break;
    }

    return retval;
  }

  protected int type;

  public Shape( int type ) {
    setType( type );
    dbfData = null;
  }


  public int getType() {
    return type;
  }

  public String getTypeDesc() {
    return getEsriTypeDesc( type );
  }

  public void setType( int type ) {
    this.type = type;
  }

  /**
   * @return Returns the dbfData.
   */
  public Object[] getDbfData() {
    return dbfData;
  }

  /**
   * @param dbfData The dbfData to set.
   */
  public void setDbfData( Object[] dbfData ) {
    this.dbfData = dbfData;
  }

  public RowMetaInterface getDbfMeta() {
    return dbfMeta;
  }

  public void setDbfMeta( RowMetaInterface dbfMeta ) {
    this.dbfMeta = dbfMeta;
  }
}
