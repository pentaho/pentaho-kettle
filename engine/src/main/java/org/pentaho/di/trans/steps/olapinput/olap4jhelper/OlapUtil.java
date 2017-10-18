/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.olapinput.olap4jhelper;

import org.olap4j.CellSet;

public class OlapUtil {

  public static CellDataSet cellSet2Matrix( final CellSet cellSet ) {
    if ( cellSet == null ) {
      return null;
    }
    final CellSetFormatter pcsf = new CellSetFormatter();

    final Matrix matrix = pcsf.format( cellSet );
    final CellDataSet cds = new CellDataSet( matrix.getMatrixWidth(), matrix.getMatrixHeight() );

    int z = 0;
    final AbstractBaseCell[][] bodyvalues =
      new AbstractBaseCell[matrix.getMatrixHeight() - matrix.getOffset()][matrix.getMatrixWidth()];
    for ( int y = matrix.getOffset(); y < matrix.getMatrixHeight(); y++ ) {

      for ( int x = 0; x < matrix.getMatrixWidth(); x++ ) {
        bodyvalues[z][x] = matrix.get( x, y );
      }
      z++;
    }

    cds.setCellSetBody( bodyvalues );

    final AbstractBaseCell[][] headervalues = new AbstractBaseCell[matrix.getOffset()][matrix.getMatrixWidth()];
    for ( int y = 0; y < matrix.getOffset(); y++ ) {
      for ( int x = 0; x < matrix.getMatrixWidth(); x++ ) {
        headervalues[y][x] = matrix.get( x, y );
      }
    }
    cds.setCellSetHeaders( headervalues );
    cds.setOffset( matrix.getOffset() );
    return cds;

  }
}
