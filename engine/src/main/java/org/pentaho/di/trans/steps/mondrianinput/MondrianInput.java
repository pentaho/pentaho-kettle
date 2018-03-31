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

package org.pentaho.di.trans.steps.mondrianinput;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads information from a database table by using freehand SQL
 *
 * @author Matt
 * @since 8-apr-2003
 */
public class MondrianInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = MondrianInputMeta.class; // for i18n purposes, needed by Translator2!!
  private MondrianInputMeta meta;
  private MondrianData data;

  public MondrianInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( first ) {
      // we just got started
      first = false;
      String mdx = meta.getSQL();
      if ( meta.isVariableReplacementActive() ) {
        mdx = environmentSubstitute( meta.getSQL() );
      }

      String catalog = environmentSubstitute( meta.getCatalog() );
      data.mondrianHelper = new MondrianHelper( meta.getDatabaseMeta(), catalog, mdx, this );
      data.mondrianHelper.setRole( meta.getRole() );
      data.mondrianHelper.openQuery();
      data.mondrianHelper.createRectangularOutput();

      data.outputRowMeta = data.mondrianHelper.getOutputRowMeta().clone(); //

      data.rowNumber = 0;
    }

    if ( data.rowNumber >= data.mondrianHelper.getRows().size() ) {
      setOutputDone(); // signal end to receiver(s)
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "MondrianInputMessageDone" ) );
      }
      data.mondrianHelper.close();
      return false; // end of data or error.
    }

    List<Object> row = data.mondrianHelper.getRows().get( data.rowNumber++ );
    Object[] outputRowData = RowDataUtil.allocateRowData( row.size() );
    for ( int i = 0; i < row.size(); i++ ) {
      outputRowData[i] = row.get( i );
    }

    putRow( data.outputRowMeta, outputRowData );
    // PDI-14120 request
    if ( checkFeedback( getLinesOutput() ) ) {
      if ( log.isBasic() ) {
        logBasic( "linenr " + getLinesOutput() ); // Not nls-ized because none of the linenr messages are at this time
      }
    }
    return true;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    super.dispose( smi, sdi );
    data.mondrianHelper.close(); // For safety sake in case the processing of rows is interrupted.
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MondrianInputMeta) smi;
    data = (MondrianData) sdi;

    if ( super.init( smi, sdi ) ) {
      return true;
    }

    return false;
  }

}
