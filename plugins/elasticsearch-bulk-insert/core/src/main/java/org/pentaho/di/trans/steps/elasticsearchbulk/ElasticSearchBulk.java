/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.elasticsearchbulk;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchTimeoutException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.DocWriteRequest.OpType;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.elasticsearchbulk.ElasticSearchBulkMeta.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Does bulk insert of data into ElasticSearch
 *
 * @author webdetails
 * @since 16-02-2011
 */
public class ElasticSearchBulk extends BaseStep implements StepInterface {

  private static final String INSERT_ERROR_CODE = null;
  private static Class<?> PKG = ElasticSearchBulkMeta.class; // for i18n
  private ElasticSearchBulkMeta meta;
  private ElasticSearchBulkData data;

  TransportClient tc;

  private Client client;
  private String index;
  private String type;

  BulkRequestBuilder currentRequest;

  private int batchSize = 2;

  private boolean isJsonInsert = false;
  private int jsonFieldIdx = 0;

  private String idOutFieldName = null;
  private Integer idFieldIndex = null;

  private Long timeout = null;
  private TimeUnit timeoutUnit = TimeUnit.MILLISECONDS;

  // private long duration = 0L;
  private int numberOfErrors = 0;

  private List<IndexRequestBuilder> requestsBuffer;

  private boolean stopOnError = true;
  private boolean useOutput = true;

  private Map<String, String> columnsToJson;
  private boolean hasFields;

  private IndexRequest.OpType opType = org.elasticsearch.action.DocWriteRequest.OpType.CREATE;

  public ElasticSearchBulk( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                            Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    Object[] rowData = getRow();
    if ( rowData == null ) {
      if ( currentRequest != null && currentRequest.numberOfActions() > 0 ) {
        // didn't fill a whole batch
        processBatch( false );
      }
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      setupData();
      currentRequest = client.prepareBulk();
      requestsBuffer = new ArrayList<IndexRequestBuilder>( this.batchSize );
      initFieldIndexes();
    }

    try {
      data.inputRowBuffer[data.nextBufferRowIdx++] = rowData;
      return indexRow( data.inputRowMeta, rowData ) || !stopOnError;
    } catch ( KettleStepException e ) {
      throw e;
    } catch ( Exception e ) {
      rejectAllRows( e.getLocalizedMessage() );
      String msg = BaseMessages.getString( PKG, "ElasticSearchBulk.Log.Exception", e.getLocalizedMessage() );
      logError( msg );
      throw new KettleStepException( msg, e );
    }
  }

  /**
   * Initialize <code>this.data</code>
   *
   * @throws KettleStepException
   */
  private void setupData() throws KettleStepException {
    data.nextBufferRowIdx = 0;
    data.inputRowMeta = getInputRowMeta().clone(); // only available after first getRow();
    data.inputRowBuffer = new Object[batchSize][];
    data.outputRowMeta = data.inputRowMeta.clone();
    meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
  }

  private void initFieldIndexes() throws KettleStepException {
    if ( isJsonInsert ) {
      Integer idx = getFieldIdx( data.inputRowMeta, environmentSubstitute( meta.getJsonField() ) );
      if ( idx != null ) {
        jsonFieldIdx = idx.intValue();
      } else {
        throw new KettleStepException( BaseMessages.getString( PKG, "ElasticSearchBulk.Error.NoJsonField" ) );
      }
    }

    idOutFieldName = environmentSubstitute( meta.getIdOutField() );

    if ( StringUtils.isNotBlank( meta.getIdInField() ) ) {
      idFieldIndex = getFieldIdx( data.inputRowMeta, environmentSubstitute( meta.getIdInField() ) );
      if ( idFieldIndex == null ) {
        throw new KettleStepException( BaseMessages.getString( PKG, "ElasticSearchBulk.Error.InvalidIdField" ) );
      }
    } else {
      idFieldIndex = null;
    }
  }

  private static Integer getFieldIdx( RowMetaInterface rowMeta, String fieldName ) {
    if ( fieldName == null ) {
      return null;
    }

    for ( int i = 0; i < rowMeta.size(); i++ ) {
      String name = rowMeta.getValueMeta( i ).getName();
      if ( fieldName.equals( name ) ) {
        return i;
      }
    }
    return null;
  }

  /**
   * @param rowMeta The metadata for the row to be indexed
   * @param row     The data for the row to be indexed
   */

  private boolean indexRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
    try {

      IndexRequestBuilder requestBuilder = client.prepareIndex( index, type );
      requestBuilder.setOpType( this.opType );

      if ( idFieldIndex != null ) {
        requestBuilder.setId( "" + row[idFieldIndex] ); // "" just in case field isn't string
      }

      if ( isJsonInsert ) {
        addSourceFromJsonString( row, requestBuilder );
      } else {
        addSourceFromRowFields( requestBuilder, rowMeta, row );
      }

      currentRequest.add( requestBuilder );
      requestsBuffer.add( requestBuilder );

      if ( currentRequest.numberOfActions() >= batchSize ) {
        return processBatch( true );
      } else {
        return true;
      }

    } catch ( KettleStepException e ) {
      throw e;
    } catch ( NoNodeAvailableException e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "ElasticSearchBulkDialog.Error.NoNodesFound" ) );
    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "ElasticSearchBulk.Log.Exception", e
              .getLocalizedMessage() ), e );
    }
  }

  /**
   * @param row
   * @param requestBuilder
   */
  private void addSourceFromJsonString( Object[] row, IndexRequestBuilder requestBuilder ) throws KettleStepException {
    Object jsonString = row[jsonFieldIdx];
    if ( jsonString instanceof byte[] ) {
      requestBuilder.setSource( (byte[]) jsonString, XContentType.JSON );
    } else if ( jsonString instanceof String ) {
      requestBuilder.setSource( (String) jsonString, XContentType.JSON );
    } else {
      throw new KettleStepException( BaseMessages.getString( "ElasticSearchBulk.Error.NoJsonFieldFormat" ) );
    }
  }

  /**
   * @param requestBuilder
   * @param rowMeta
   * @param row
   * @throws IOException
   */
  private void addSourceFromRowFields( IndexRequestBuilder requestBuilder, RowMetaInterface rowMeta, Object[] row )
          throws IOException {
    XContentBuilder jsonBuilder = XContentFactory.jsonBuilder().startObject();

    for ( int i = 0; i < rowMeta.size(); i++ ) {
      if ( idFieldIndex != null && i == idFieldIndex ) { // skip id
        continue;
      }

      ValueMetaInterface valueMeta = rowMeta.getValueMeta( i );
      String name = hasFields ? columnsToJson.get( valueMeta.getName() ) : valueMeta.getName();
      Object value = row[i];
      if ( value instanceof Date && value.getClass() != Date.class ) {
        Date subDate = (Date) value;
        // create a genuine Date object, or jsonBuilder will not recognize it
        value = new Date( subDate.getTime() );
      }
      if ( StringUtils.isNotBlank( name ) ) {
        jsonBuilder.field( name, value );
      }
    }

    jsonBuilder.endObject();
    requestBuilder.setSource( jsonBuilder );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ElasticSearchBulkMeta) smi;
    data = (ElasticSearchBulkData) sdi;

    if ( super.init( smi, sdi ) ) {

      try {

        numberOfErrors = 0;

        initFromMeta();
        initClient();

        return true;

      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "ElasticSearchBulk.Log.ErrorOccurredDuringStepInitialize" )
                + e.getMessage() );
      }
      return true;
    }
    return false;
  }

  private void initFromMeta() {
    index = environmentSubstitute( meta.getIndex() );
    type = environmentSubstitute( meta.getType() );
    batchSize = meta.getBatchSizeInt( this );
    try {
      timeout = Long.parseLong( environmentSubstitute( meta.getTimeOut() ) );
    } catch ( NumberFormatException e ) {
      timeout = null;
    }
    timeoutUnit = meta.getTimeoutUnit();
    isJsonInsert = meta.isJsonInsert();
    useOutput = meta.isUseOutput();
    stopOnError = meta.isStopOnError();

    columnsToJson = meta.getFieldsMap();
    this.hasFields = columnsToJson.size() > 0;

    this.opType =
            StringUtils.isNotBlank( meta.getIdInField() ) && meta.isOverWriteIfSameId() ? OpType.INDEX : OpType.CREATE;

  }

  private boolean processBatch( boolean makeNew ) throws KettleStepException {


    ActionFuture<BulkResponse> actionFuture = currentRequest.execute();
    boolean responseOk = false;

    BulkResponse response = null;
    try {
      if ( timeout != null && timeoutUnit != null ) {
        response = actionFuture.actionGet( timeout, timeoutUnit );
      } else {
        response = actionFuture.actionGet();
      }
    } catch ( ElasticsearchException e ) {
      String msg = BaseMessages.getString( PKG, "ElasticSearchBulk.Error.BatchExecuteFail", e.getLocalizedMessage() );
      if ( e instanceof ElasticsearchTimeoutException ) {
        msg = BaseMessages.getString( PKG, "ElasticSearchBulk.Error.Timeout" );
      }
      logError( msg );
      rejectAllRows( msg );
    }

    if ( response != null ) {
      responseOk = handleResponse( response );
      requestsBuffer.clear();
    } else { // have to assume all failed
      numberOfErrors += currentRequest.numberOfActions();
      setErrors( numberOfErrors );
    }
    // duration += response.getTookInMillis(); //just in trunk..

    if ( makeNew ) {
      currentRequest = client.prepareBulk();
      data.nextBufferRowIdx = 0;
      data.inputRowBuffer = new Object[batchSize][];
    } else {
      currentRequest = null;
      data.inputRowBuffer = null;
    }

    return responseOk;
  }

  /**
   * @param response
   * @return <code>true</code> if no errors
   */
  private boolean handleResponse( BulkResponse response ) {

    boolean hasErrors = response.hasFailures();

    if ( hasErrors ) {
      logError( response.buildFailureMessage() );
    }

    int errorsInBatch = 0;

    if ( hasErrors || useOutput ) {
      for ( BulkItemResponse item : response ) {
        if ( item.isFailed() ) {
          // log
          logDetailed( item.getFailureMessage() );
          errorsInBatch++;
          if ( getStepMeta().isDoingErrorHandling() ) {
            rejectRow( item.getItemId(), item.getFailureMessage() );
          }
        } else if ( useOutput ) {
          if ( idOutFieldName != null ) {
            addIdToRow( item.getId(), item.getItemId() );
          }
          echoRow( item.getItemId() );
        }
      }
    }

    numberOfErrors += errorsInBatch;
    setErrors( numberOfErrors );
    int linesOK = currentRequest.numberOfActions() - errorsInBatch;

    if ( useOutput ) {
      setLinesOutput( getLinesOutput() + linesOK );
    } else {
      setLinesWritten( getLinesWritten() + linesOK );
    }

    return !hasErrors;
  }

  private void addIdToRow( String id, int rowIndex ) {

    data.inputRowBuffer[rowIndex] =
            RowDataUtil.resizeArray( data.inputRowBuffer[rowIndex], getInputRowMeta().size() + 1 );
    data.inputRowBuffer[rowIndex][getInputRowMeta().size()] = id;

  }

  /**
   * Send input row to output
   *
   * @param rowIndex
   */
  private void echoRow( int rowIndex ) {
    try {

      putRow( data.outputRowMeta, data.inputRowBuffer[rowIndex] );

    } catch ( KettleStepException e ) {
      logError( e.getLocalizedMessage() );
    } catch ( ArrayIndexOutOfBoundsException e ) {
      logError( e.getLocalizedMessage() );
    }
  }

  /**
   * Send input row to error.
   *
   * @param index
   * @param errorMsg
   */
  private void rejectRow( int index, String errorMsg ) {
    try {

      putError( getInputRowMeta(), data.inputRowBuffer[index], 1, errorMsg, null, INSERT_ERROR_CODE );

    } catch ( KettleStepException e ) {
      logError( e.getLocalizedMessage() );
    } catch ( ArrayIndexOutOfBoundsException e ) {
      logError( e.getLocalizedMessage() );
    }
  }

  private void rejectAllRows( String errorMsg ) {
    for ( int i = 0; i < data.nextBufferRowIdx; i++ ) {
      rejectRow( i, errorMsg );
    }
  }

  private void initClient() throws UnknownHostException {


    Settings.Builder settingsBuilder = Settings.builder();
    settingsBuilder.put( Settings.Builder.EMPTY_SETTINGS );
    meta.getSettingsMap().entrySet().stream().forEach( ( s ) -> settingsBuilder.put( s.getKey(),
            environmentSubstitute( s.getValue() ) ) );

    PreBuiltTransportClient tClient = new PreBuiltTransportClient( settingsBuilder.build() );

    for ( Server server : meta.getServers() ) {
      tClient.addTransportAddress( new TransportAddress(
              InetAddress.getByName( environmentSubstitute( server.getAddress() ) ),
              server.getPort() ) );
    }

    client = tClient;

    /** With the upgrade to elasticsearch 6.3.0, removed the NodeBuilder,
     *  which was removed from the elasticsearch 5.0 API, see:
     *  https://www.elastic.co/guide/en/elasticsearch/reference/5.0/breaking_50_java_api_changes
     *  .html#_nodebuilder_removed
     */

  }

  private void disposeClient() {

    if ( client != null ) {
      client.close();
    }


  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ElasticSearchBulkMeta) smi;
    data = (ElasticSearchBulkData) sdi;
    try {
      disposeClient();
    } catch ( Exception e ) {
      logError( e.getLocalizedMessage(), e );
    }
    super.dispose( smi, sdi );
  }
}
