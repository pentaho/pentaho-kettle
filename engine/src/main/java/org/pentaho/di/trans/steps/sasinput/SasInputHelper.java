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

package org.pentaho.di.trans.steps.sasinput;

import java.io.File;

import org.eobjects.sassy.SasColumnType;
import org.eobjects.sassy.SasReader;
import org.eobjects.sassy.SasReaderCallback;
import org.eobjects.sassy.SasReaderException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;

/**
 * This file helps us to read a SAS7BAT file
 *
 * @author matt
 * @since 4.3
 * @since 07-OCT-2011
 */
public class SasInputHelper {

  private String filename;
  private RowMetaInterface rowMeta;

  private SasReader sasReader;

  /**
   * @param filename
   *          The SAS7BAT filename
   */
  public SasInputHelper( final String filename ) throws KettleException {
    this.filename = filename;

    sasReader = new SasReader( new File( filename ) );

    // Determine the row layout of the file ...
    //
    try {
      rowMeta = new RowMeta();
      sasReader.read( new SasReaderCallback() {
        public void column( int index, String name, String label, SasColumnType type, int length ) {
          int kettleType = ValueMetaInterface.TYPE_NONE;
          int kettleLength;
          switch ( type ) {
            case CHARACTER:
              kettleType = ValueMetaInterface.TYPE_STRING;
              kettleLength = length;
              break;
            case NUMERIC:
              kettleType = ValueMetaInterface.TYPE_NUMBER;
              kettleLength = -1;
              break;
            default:
              throw new RuntimeException( "Unhandled SAS data type encountered: " + type );
          }
          try {
            ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta( name, kettleType );
            valueMeta.setLength( kettleLength );
            valueMeta.setComments( label );
            rowMeta.addValueMeta( valueMeta );
          } catch ( Exception e ) {
            throw new SasReaderException( "Unable to create new value meta type", e );
          }
        }

        public boolean readData() {
          return false;
        }

        public boolean row( int rowNumber, Object[] rowData ) {
          return true;
        }
      } );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to determine the layout of SAS7BAT file '" + filename + "'", e );
    }

  }

  @Override
  public String toString() {
    return filename;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @return the rowMeta
   */
  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }
}
