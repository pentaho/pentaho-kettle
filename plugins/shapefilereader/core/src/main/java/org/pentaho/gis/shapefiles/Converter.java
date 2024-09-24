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

public class Converter {

  public static final int getIntegerLittle( byte[] b, int pos ) {
    int[] bi = new int[4];
    for ( int i = 0; i < 4; i++ ) {
      bi[i] = b[pos + i];
      if ( bi[i] < 0 ) {
        bi[i] += 256;
      }
    }

    return ( bi[0] ) + ( bi[1] << 8 ) + ( bi[2] << 16 ) + ( bi[3] << 24 );
  }

  public static final int getIntegerBig( byte[] b, int pos ) {
    int[] bi = new int[4];
    for ( int i = 0; i < 4; i++ ) {
      bi[i] = b[pos + i];
      if ( bi[i] < 0 ) {
        bi[i] += 256;
      }
    }

    return ( bi[3] ) + ( bi[2] << 8 ) + ( bi[1] << 16 ) + ( bi[0] << 24 );
  }

  public static final long getLongLittle( byte[] b, int pos ) {
    long[] bi = new long[8];
    for ( int i = 0; i < 8; i++ ) {
      bi[i] = b[pos + i];
      if ( bi[i] < 0 ) {
        bi[i] += 256;
      }
    }

    return ( bi[0] )
           + ( bi[1] << 8 )
           + ( bi[2] << 16 )
           + ( bi[3] << 24 )
           + ( bi[4] << 32 )
           + ( bi[5] << 40 )
           + ( bi[6] << 48 )
           + ( bi[7] << 56 );
  }

  public static final long getLongBig( byte[] b, int pos ) {
    long[] bi = new long[8];
    for ( int i = 0; i < 8; i++ ) {
      bi[i] = b[pos + i];
      if ( bi[i] < 0 ) {
        bi[i] += 256;
      }
    }

    return ( bi[7] )
           + ( bi[6] << 8 )
           + ( bi[5] << 16 )
           + ( bi[4] << 24 )
           + ( bi[3] << 32 )
           + ( bi[2] << 40 )
           + ( bi[1] << 48 )
           + ( bi[0] << 56 );
  }

  // Convert ESRI double
  public static final double getDoubleLittle( byte[] b, int pos ) {
    return Double.longBitsToDouble( getLongLittle( b, pos ) );
  }

  // Convert ESRI double
  public static final double getDoubleBig( byte[] b, int pos ) {
    return Double.longBitsToDouble( getLongBig( b, pos ) );
  }
}
