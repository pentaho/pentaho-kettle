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

package org.pentaho.di.trans.steps.tableexists;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

/**
 * Check if a table exists in a Database *
 *
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class TableExists extends BaseDatabaseStep implements StepInterface {
  private static Class<?> PKG = TableExistsMeta.class; // for i18n purposes, needed by Translator2!!

  private TableExistsMeta meta;
  private TableExistsData data;

  public TableExists( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    meta = (TableExistsMeta) smi;
    data = (TableExistsData) sdi;

    boolean sendToErrorRow = false;
    String errorMessage = null;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    boolean tablexists = false;
    try {
      if ( first ) {
        first = false;
        data.outputRowMeta = getInputRowMeta().clone();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

        // Check is tablename field is provided
        if ( Utils.isEmpty( meta.getDynamicTablenameField() ) ) {
          logError( BaseMessages.getString( PKG, "TableExists.Error.TablenameFieldMissing" ) );
          throw new KettleException( BaseMessages.getString( PKG, "TableExists.Error.TablenameFieldMissing" ) );
        }

        // cache the position of the field
        if ( data.indexOfTablename < 0 ) {
          data.indexOfTablename = getInputRowMeta().indexOfValue( meta.getDynamicTablenameField() );
          if ( data.indexOfTablename < 0 ) {
            // The field is unreachable !
            logError( BaseMessages.getString( PKG, "TableExists.Exception.CouldnotFindField" )
              + "[" + meta.getDynamicTablenameField() + "]" );
            throw new KettleException( BaseMessages.getString(
              PKG, "TableExists.Exception.CouldnotFindField", meta.getDynamicTablenameField() ) );
          }
        }
      } // End If first

      // get tablename
      String tablename = getInputRowMeta().getString( r, data.indexOfTablename );

      // Check if table exists on the specified connection
      tablexists = data.db.checkTableExists( data.realSchemaname, tablename );

      Object[] outputRowData = RowDataUtil.addValueData( r, getInputRowMeta().size(), tablexists );

      // add new values to the row.
      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "TableExists.LineNumber", getLinesRead()
          + " : " + getInputRowMeta().getString( r ) ) );
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "TableExists.ErrorInStepRunning" + " : " + e.getMessage() ) );
        throw new KettleStepException( BaseMessages.getString( PKG, "TableExists.Log.ErrorInStep" ), e );
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "TableExistsO01" );
      }
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TableExistsMeta) smi;
    data = (TableExistsData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( Utils.isEmpty( meta.getResultFieldName() ) ) {
        logError( BaseMessages.getString( PKG, "TableExists.Error.ResultFieldMissing" ) );
        return false;
      }
      if ( !Utils.isEmpty( meta.getSchemaname() ) ) {
        data.realSchemaname = environmentSubstitute( meta.getSchemaname() );
      }
      return true;
    }
    return false;
  }

  @Override
  protected Class<?> getPKG() {
    return PKG;
  }

}
