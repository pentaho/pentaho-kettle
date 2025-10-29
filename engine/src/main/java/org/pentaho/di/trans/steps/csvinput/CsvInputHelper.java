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

package org.pentaho.di.trans.steps.csvinput;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.common.CsvInputAwareStep;
import org.pentaho.di.trans.steps.fileinput.text.BufferedInputStreamReader;
import org.pentaho.di.trans.steps.fileinput.text.CsvFileImportProcessor;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class for Csv Input step actions, extracted from CsvInput.
 * Handles only action methods related to metadata extraction.
 */
public class CsvInputHelper extends BaseStepHelper implements CsvInputAwareStep {

  public static final String STEP_NAME = "stepName";
  public static final String ERROR = "error";
  private static final String GET_FIELDS = "getFields";
  private TransMeta transMeta;
  public CsvInputHelper() {
    super();
  }

  public void setTransMeta(TransMeta transMeta) {
    this.transMeta = transMeta;
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    setTransMeta( transMeta );
    JSONObject response = new JSONObject();
    try {
      if ( GET_FIELDS.equals( method ) ) {
        response = getFieldsAction( queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR, ex.getMessage() );
    }
    return response;
  }
  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getFieldsAction(Map<String, String> queryParams )
          throws KettleException, JsonProcessingException {
    return populateMeta( queryParams );
  }

  // This method contains code extracted from textfileinput/TextFileCSVImportProgressDialog class
  // to get Fields data and Fields summary statistics
  private JSONObject populateMeta(Map<String, String> queryParams ) throws KettleException, JsonProcessingException {
    JSONObject response = new JSONObject();
    String isSampleSummary = queryParams.get( "isSampleSummary" );
    int samples = Integer.parseInt( Objects.toString( queryParams.get( "noOfFields" ), "0" ) );

    StepMeta stepMeta = getTransMeta().findStep( queryParams.get( STEP_NAME ) );
    CsvInputAwareMeta csvInputAwareMeta = (CsvInputAwareMeta) stepMeta.getStepMetaInterface();
    final InputStream inputStream = getInputStream( csvInputAwareMeta );
    final BufferedInputStreamReader reader = getBufferedReader( csvInputAwareMeta, inputStream );
    CsvInputMeta meta = (CsvInputMeta) stepMeta.getStepMetaInterface();
    String[] fieldNames = getFieldNames( csvInputAwareMeta );
    meta.setFields( fieldNames );

    CsvFileImportProcessor processor =
            new CsvFileImportProcessor( meta, transMeta, reader, samples, Boolean.parseBoolean( isSampleSummary ) );
    String summary = processor.analyzeFile( true );

    response.put( "fields", convertFieldsToJsonArray( processor.getInputFieldsDto() ) );
    response.put( "summary", summary );
    return response;
  }

  @Override
  public InputStream getInputStream( final CsvInputAwareMeta meta ) {
    InputStream inputStream = null;
    try {
      FileObject fileObject = meta.getHeaderFileObject( getTransMeta() );
      inputStream = KettleVFS.getInputStream( fileObject );
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( "FileInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return inputStream;
  }

  @Override
  public LogChannelInterface logChannel() {
    return log;
  }

  @Override
  public TransMeta getTransMeta() {
    return this.transMeta;
  }

}
