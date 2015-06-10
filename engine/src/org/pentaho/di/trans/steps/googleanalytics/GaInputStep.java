/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.googleanalytics;

import java.io.IOException;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;

public class GaInputStep extends BaseStep implements StepInterface {

  private GaInputStepData data;
  private GaInputStepMeta meta;

  // private static Class<?> PKG = GaInputStep.class; // for i18n purposes

  public GaInputStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    meta = (GaInputStepMeta) smi;
    data = (GaInputStepData) sdi;

    if ( first ) {

      first = false;

      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // stores the indices where to look for the key fields in the input rows
      data.conversionMeta = new ValueMetaInterface[meta.getFeedField().length];

      for ( int i = 0; i < meta.getFeedField().length; i++ ) {

        // get output and from-string conversion format for each field
        ValueMetaInterface returnMeta = data.outputRowMeta.getValueMeta( i );

        ValueMetaInterface conversionMeta;
        conversionMeta = ValueMetaFactory.cloneValueMeta( returnMeta, ValueMetaInterface.TYPE_STRING );

        conversionMeta.setConversionMask( meta.getConversionMask()[i] );

        conversionMeta.setDecimalSymbol( "." ); // google analytics is en-US
        conversionMeta.setGroupingSymbol( null ); // google analytics uses no grouping symbol

        data.conversionMeta[i] = conversionMeta;

      }

    }

    // generate output row, make it correct size
    Object[] outputRow = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    List<String> entry;
    try {
        entry = getNextDataEntry();
    } catch (KettleException ex) {
        if (getStepMeta().isDoingErrorHandling()) {
            String tableId = meta.isUseCustomTableId()?environmentSubstitute(meta.getGaCustomTableId()):meta.getGaProfileTableId();
            putError(data.outputRowMeta, outputRow, 1, ex.toString(), tableId, "GA001");
            setOutputDone();
            return false;
        } else {
            throw ex;
        }
    }

    if ( entry != null && ( meta.getRowLimit() <= 0 || getLinesWritten() < meta.getRowLimit() ) ) { // another record to
                                                                                                    // process
      // fill the output fields with look up data
      for ( int i = 0; i < meta.getFeedField().length; i++ ) {

        String value = entry.get(i);
        String fieldName = environmentSubstitute( meta.getFeedField()[i] );
        String fieldType = meta.getFeedFieldType()[i];

        outputRow[i] = data.outputRowMeta.getValueMeta( i ).convertData( data.conversionMeta[i], value );

      }

      // copy row to possible alternate rowset(s)
      putRow( data.outputRowMeta, outputRow );

      // Some basic logging
      if ( checkFeedback( getLinesWritten() ) ) {
        if ( log.isBasic() ) {
          logBasic( "Linenr " + getLinesWritten() );
        }
      }
      return true;
    } else {
      setOutputDone();
      return false;
    }

  }

  protected Analytics.Data.Ga.Get getQuery(Analytics analyticsService) throws KettleException {

    String ids = meta.isUseCustomTableId() ? environmentSubstitute( meta.getGaCustomTableId() ) : meta
      .getGaProfileTableId();
    String startDate = environmentSubstitute( meta.getStartDate() );
    String endDate = environmentSubstitute( meta.getEndDate() );
    String dimensions = environmentSubstitute( meta.getDimensions() );
    if (!dimensions.matches("ga:.+")) {
        throw new KettleException("dimensions Invalid value ''. Values must match the following regular expression: 'ga:.+'");
    }
    String metrics = environmentSubstitute( meta.getMetrics() );
    if (!metrics.matches("ga:.+")) {
        throw new KettleException("metrics Invalid value ''. Values must match the following regular expression: 'ga:.+'");
    }

    Analytics.Data.Ga.Get query;
    try {
        query = analyticsService.data().ga().get(ids, startDate, endDate, metrics);
    } catch ( IOException e ) {
        throw new KettleException( e );
    }
    query.setDimensions(dimensions);

    if ( meta.isUseSegment() ) {
      if ( meta.isUseCustomSegment() ) {
        query.setSegment( environmentSubstitute( meta.getCustomSegment() ) );
      } else {
        query.setSegment( meta.getSegmentId() );
      }
    }

    if ( !Const.isEmpty( meta.getFilters() ) ) {
      query.setFilters( environmentSubstitute( meta.getFilters() ) );
    }
    if ( !Const.isEmpty( meta.getSort() ) ) {
      query.setSort( environmentSubstitute( meta.getSort() ) );
    }

    return query;

  }

  private List<String> getNextDataEntry() throws KettleException {

    // no query prepared yet?
    if ( data.query == null ) {

      String accessToken = environmentSubstitute( Encr.decryptPasswordOptionallyEncrypted( meta.getGaApiKey() ) );
      GoogleCredential credential = new GoogleCredential().setAccessToken(Encr.decryptPasswordOptionallyEncrypted( accessToken ));
      Analytics analyticsService = new Analytics.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
          .setApplicationName(meta.getGaAppName())
          .build();

      try {

        data.query = getQuery(analyticsService);
        // use default max results for now
        // data.query.setMaxResults(10000);
        data.feed = data.query.execute();
        data.entryIndex = 0;

      } catch ( IOException e ) {
        throw new KettleException( e );
      }

    } else if ( data.entryIndex >= data.feed.getRows().size() ) {
      // query is there, check whether we hit the last entry and re-query as necessary
      if ( data.feed.getQuery().getStartIndex() + data.entryIndex <= data.feed.getTotalResults() ) {
        // need to query for next page
        data.query.setStartIndex( data.feed.getQuery().getStartIndex() + data.entryIndex );

        try {

          data.feed = data.query.execute();
          data.entryIndex = 0;

        } catch ( IOException e ) {
          throw new KettleException( e );
        }

      }

    }

    List<List<String>> entries = data.feed.getRows();
    if ( data.entryIndex < entries.size() ) {
      return entries.get( data.entryIndex++ );
    } else {
      return null; // end of feed
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GaInputStepMeta) smi;
    data = (GaInputStepData) sdi;

    return super.init( smi, sdi );
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GaInputStepMeta) smi;
    data = (GaInputStepData) sdi;

    super.dispose( smi, sdi );
  }

}
