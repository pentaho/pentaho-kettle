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
import org.pentaho.di.trans.steps.common.CsvInputAwareHelper;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.fileinput.text.BufferedInputStreamReader;
import org.pentaho.di.trans.steps.fileinput.text.CsvFileImportProcessor;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class for CsvInput step providing UI-triggered action support.
 */
public class CsvInputHelper extends BaseStepHelper implements CsvInputAwareHelper {

  private static final String GET_FIELDS = "getFields";

  private final CsvInputMeta csvInputMeta;

  public CsvInputHelper( CsvInputMeta csvInputMeta ) {
    this.csvInputMeta = csvInputMeta;
  }

  /**
   * Handles step-specific actions for CsvInput.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta,
                                         Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( method.equalsIgnoreCase( GET_FIELDS ) ) {
      response = getFields( transMeta, queryParams );
    } else {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the fields from the CSV file and returns them as a JSON object.
   * This method contains code extracted from textfileinput/TextFileCSVImportProgressDialog class
   * to get Fields data and Fields summary statistics.
   *
   * @param transMeta   The transformation metadata associated with the step.
   * @param queryParams A map of query parameters for the action.
   * @return A JSON object containing the fields and summary.
   */
  public JSONObject getFields( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      response = populateMeta( transMeta, queryParams );
    } catch ( Exception ex ) {
      log.logError( "Error getting fields: ", ex.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Populates metadata by analyzing the CSV file.
   *
   * @param transMeta   The transformation metadata.
   * @param queryParams Query parameters containing sample settings.
   * @return A JSON object with fields and summary information.
   * @throws KettleException         If an error occurs during processing.
   * @throws JsonProcessingException If JSON conversion fails.
   */
  private JSONObject populateMeta( TransMeta transMeta, Map<String, String> queryParams )
    throws KettleException, JsonProcessingException {
    JSONObject response = new JSONObject();
    String isSampleSummary = queryParams.get( "isSampleSummary" );
    int samples = Integer.parseInt( Objects.toString( queryParams.get( "noOfFields" ), "0" ) );

    CsvInputAwareMeta csvInputAwareMeta = csvInputMeta;
    final InputStream inputStream = getInputStream( transMeta, csvInputAwareMeta );
    final BufferedInputStreamReader reader = getBufferedReader( transMeta, csvInputAwareMeta, inputStream );
    String[] fieldNames = getFieldNames( transMeta, csvInputAwareMeta );
    csvInputMeta.setFields( fieldNames );

    CsvFileImportProcessor processor =
      new CsvFileImportProcessor( csvInputMeta, transMeta, reader, samples, Boolean.parseBoolean( isSampleSummary ) );
    String summary = processor.analyzeFile( true );

    response.put( "fields", convertFieldsToJsonArray( processor.getInputFieldsDto() ) );
    response.put( "summary", summary );
    return response;
  }

  /**
   * Retrieves the input stream for the CSV file.
   *
   * @param transMeta The transformation metadata.
   * @param meta      The CSV input metadata.
   * @return The input stream for the file.
   */
  public InputStream getInputStream( TransMeta transMeta, final CsvInputAwareMeta meta ) {
    InputStream inputStream = null;
    try {
      FileObject fileObject = meta.getHeaderFileObject( transMeta );
      inputStream = KettleVFS.getInputStream( fileObject );
    } catch ( final Exception e ) {
      log.logError( BaseMessages.getString( "FileInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return inputStream;
  }

  @Override
  public LogChannelInterface logChannel() {
    return log;
  }
}

