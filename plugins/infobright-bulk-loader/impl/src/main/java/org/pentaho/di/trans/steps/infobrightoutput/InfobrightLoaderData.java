/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.infobrightoutput;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.infobright.etl.model.BrighthouseRecord;
import com.infobright.etl.model.DataFormat;
import com.infobright.io.InfobrightNamedPipeLoader;
import com.infobright.logging.EtlLogger;

/**
 * @author geoffrey.falk@infobright.com
 */
public class InfobrightLoaderData extends BaseStepData implements StepDataInterface {

  private Database db; // only for initial use and the loader.
  private String[] requiredFields;
  InfobrightNamedPipeLoader loader;
  BrighthouseRecord record;
  public RowMetaInterface requiredRowMeta;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface insertRowMeta;

  /**
   * Default constructor. Does nothing special.
   */
  public InfobrightLoaderData() {
    super();
  }

  void databaseSetup( InfobrightLoaderMeta meta, InfobrightLoader step ) throws KettleException {

    db = new Database( step, meta.getDatabaseMeta() );
    db.connect();

    // FIXME: This will fail if the first row of the table contains a value that
    // cannot be read by Java. For example, a DATE field that contains the value
    // '0000-00-00'. In this case, the Kettle error message will misleadingly say
    // that the table doesn't exist. There doesn't seem to be any workaround.
    // See Pentaho JIRA: PDI-2117.
    //
    requiredRowMeta = meta.getRequiredFields( step );
    requiredFields = requiredRowMeta.getFieldNames();

    try {
      // once the loader is built, this db connection cannot be used by this thread anymore.
      // the loader is using it and any other uses of the connection will block.
      if ( meta.getInfobrightProductType() == null ) {
        meta.setDataFormat( DataFormat.TXT_VARIABLE ); // default for ICE
      }
      DataFormat dataFormat = DataFormat.valueForDisplayName( meta.getInfobrightProductType() );
      int agentPort = meta.getAgentPort();
      Charset charset = meta.getCharset();
      Connection conn = db.getConnection();
      String tableName =
        meta
          .getDatabaseMeta().getQuotedSchemaTableCombination(
            step.environmentSubstitute( meta.getSchemaName() ),
            step.environmentSubstitute( meta.getTableName() ) );
      EtlLogger logger = new KettleEtlLogger( step );
      loader = new InfobrightNamedPipeLoader( tableName, conn, logger, dataFormat, charset, agentPort );
      loader.setTimeout( 30 );
      String debugFile = meta.getDebugFile();
      if ( debugFile != null ) {
        OutputStream debugOutputStream = new FileOutputStream( debugFile );
        loader.setDebugOutputStream( debugOutputStream );
      }
      record = loader.createRecord( false ); // TODO set to true to support error path
      loader.start();

    } catch ( Exception e ) {
      db.close();
      db = null;
      if ( loader != null ) {
        try {
          loader.killQuery();
        } catch ( SQLException e1 ) {
          throw new KettleDatabaseException( e1 );
        }
      }
      throw new KettleDatabaseException( e );
    }
  }

  String[] getRequiredFields() {
    return requiredFields;
  }

  void dispose() throws Exception {
    try {
      if ( loader != null ) {
        loader.stop();
      }
      loader = null;
    } finally {
      if ( db != null ) {
        db.close();
      }
      db = null;
    }
  }
}
