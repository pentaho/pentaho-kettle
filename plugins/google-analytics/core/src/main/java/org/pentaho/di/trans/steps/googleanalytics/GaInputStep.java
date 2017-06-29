/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.services.analytics.Analytics;


public class GaInputStep extends BaseStep implements StepInterface {

  private static Class<?> PKG = GaInputStepMeta.class; // for i18n purposes

  private GaInputStepData data;
  private GaInputStepMeta meta;

  private Analytics analytics;
  private String accountName;

  public GaInputStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    meta = (GaInputStepMeta) smi;
    data = (GaInputStepData) sdi;

    if ( first ) {

      first = false;

      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // stores the indices where to look for the key fields in the input rows
      data.conversionMeta = new ValueMetaInterface[ meta.getFieldsCount() ];

      for ( int i = 0; i < meta.getFieldsCount(); i++ ) {

        // get output and from-string conversion format for each field
        ValueMetaInterface returnMeta = data.outputRowMeta.getValueMeta( i );

        ValueMetaInterface conversionMeta;

        conversionMeta = ValueMetaFactory.cloneValueMeta( returnMeta, ValueMetaInterface.TYPE_STRING );
        conversionMeta.setConversionMask( meta.getConversionMask()[ i ] );
        conversionMeta.setDecimalSymbol( "." ); // google analytics is en-US
        conversionMeta.setGroupingSymbol( null ); // google analytics uses no grouping symbol

        data.conversionMeta[ i ] = conversionMeta;
      }
    }

    // generate output row, make it correct size
    Object[] outputRow = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    List<String> entry = getNextDataEntry();

    if ( entry != null && ( meta.getRowLimit() <= 0 || getLinesWritten() < meta.getRowLimit() ) ) { // another record to
      // fill the output fields with look up data
      for ( int i = 0, j = 0; i < meta.getFieldsCount(); i++ ) {
        String fieldName = environmentSubstitute( meta.getFeedField()[ i ] );
        Object dataObject;
        String type = environmentSubstitute( meta.getFeedFieldType()[ i ] );

        // We handle fields differently depending on whether its a Dimension/Metric, Data Source Property, or
        // Data Source Field. Also the API doesn't exactly match the concepts anymore (see individual comments below),
        // so there is quite a bit of special processing.
        if ( GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_PROPERTY.equals( type ) ) {
          // Account name has to be handled differently, it's in the Accounts API not Profiles API
          if ( GaInputStepMeta.PROPERTY_DATA_SOURCE_ACCOUNT_NAME.equals( fieldName ) ) {
            // We expect a single account name, and already fetched it during init
            dataObject = accountName;
          } else {
            dataObject = data.feed.getProfileInfo().get( removeClassifier( fieldName ) );
          }
        } else if ( GaInputStepMeta.FIELD_TYPE_DATA_SOURCE_FIELD.equals( type ) ) {
          // Get tableId or tableName
          if ( GaInputStepMeta.FIELD_DATA_SOURCE_TABLE_ID.equals( fieldName ) ) {
            dataObject = data.feed.getProfileInfo().get( removeClassifier( fieldName ) );
          } else {
            // We only have two Data Source Fields and they're hard-coded, so we handle tableName in this else-clause
            // since tableId was done in the if-clause. We have to handle the two differently because tableName is
            // actually the profile name in this version (v3) of the Google Analytics API.
            dataObject = data.feed.getProfileInfo().getProfileName();
          }
        } else if ( GaInputStepMeta.DEPRECATED_FIELD_TYPE_CONFIDENCE_INTERVAL.equals( type ) ) {
          dataObject = null;
          if ( log.isRowLevel() ) {
            logRowlevel( BaseMessages.getString( PKG, "GoogleAnalytics.Warn.FieldTypeNotSupported",
              GaInputStepMeta.DEPRECATED_FIELD_TYPE_CONFIDENCE_INTERVAL ) );
          }
        } else {
          // Assume it's a Dimension or Metric, we've covered the rest of the cases above.
          dataObject = entry.get( j++ );
        }
        outputRow[ i ] = data.outputRowMeta.getValueMeta( i ).convertData( data.conversionMeta[ i ], dataObject );
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

  protected Analytics.Data.Ga.Get getQuery( Analytics analytics ) {

    Analytics.Data dataApi = analytics.data();
    Analytics.Data.Ga.Get query;

    try {
      String metrics = environmentSubstitute( meta.getMetrics() );
      if ( Utils.isEmpty( metrics ) ) {
        logError( BaseMessages.getString( PKG, "GoogleAnalytics.Error.NoMetricsSpecified.Message" ) );
        return null;
      }
      query = dataApi.ga().get(
        meta.isUseCustomTableId() ? environmentSubstitute( meta.getGaCustomTableId() ) : meta.getGaProfileTableId(),
        //ids
        environmentSubstitute( meta.getStartDate() ), // start date
        environmentSubstitute( meta.getEndDate() ), // end date
        metrics  // metrics
      );

      String dimensions = environmentSubstitute( meta.getDimensions() );
      if ( !Utils.isEmpty( dimensions ) ) {
        query.setDimensions( dimensions );
      }

      if ( meta.isUseSegment() ) {
        if ( meta.isUseCustomSegment() ) {
          query.setSegment( environmentSubstitute( meta.getCustomSegment() ) );
        } else {
          query.setSegment( meta.getSegmentId() );
        }
      }

      if ( !Utils.isEmpty( meta.getSamplingLevel() ) ) {
        query.setSamplingLevel( environmentSubstitute( meta.getSamplingLevel() ) );
      }

      if ( !Utils.isEmpty( meta.getFilters() ) && !Utils.isEmpty( environmentSubstitute( meta.getFilters() ) ) ) {
        query.setFilters( environmentSubstitute( meta.getFilters() ) );
      }
      if ( !Utils.isEmpty( meta.getSort() ) ) {
        query.setSort( environmentSubstitute( meta.getSort() ) );
      }

      return query;
    } catch ( IOException ioe ) {
      return null;
    }

  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GaInputStepMeta) smi;
    data = (GaInputStepData) sdi;

    if ( !super.init( smi, sdi ) ) {
      return false;
    }

    // Look for deprecated field types and log error(s) for them
    String[] types = environmentSubstitute( meta.getFeedFieldType() );
    if ( types != null ) {
      for ( String type : types ) {
        if ( GaInputStepMeta.DEPRECATED_FIELD_TYPE_CONFIDENCE_INTERVAL.equals( type ) ) {
          logError( BaseMessages.getString( PKG, "GoogleAnalytics.Warn.FieldTypeNotSupported",
            GaInputStepMeta.DEPRECATED_FIELD_TYPE_CONFIDENCE_INTERVAL ) );
        }
      }
    }

    String appName = environmentSubstitute( meta.getGaAppName() );
    String serviceAccount = environmentSubstitute( meta.getOAuthServiceAccount() );
    String OAuthKeyFile = environmentSubstitute( meta.getOAuthKeyFile() );

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.AppName.Label" ) + ": " + appName );
      logDetailed( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.OauthAccount.Label" ) + ": " + serviceAccount );
      logDetailed( BaseMessages.getString( PKG, "GoogleAnalyticsDialog.KeyFile.Label" ) + ": " + OAuthKeyFile );
    }

    try {
      // Create an Analytics object, and fetch what we can for later (account name, e.g.)
      analytics = GoogleAnalyticsApiFacade.createFor( appName, serviceAccount, OAuthKeyFile ).getAnalytics();
      // There is necessarily an account name associated with this, so any NPEs or other exceptions mean bail out
      accountName = analytics.management().accounts().list().execute().getItems().iterator().next().getName();
    } catch ( TokenResponseException tre ) {
      Exception exceptionToLog = tre;
      if ( tre.getDetails() != null && tre.getDetails().getError() != null ) {
        exceptionToLog = new IOException( BaseMessages.getString( PKG, "GoogleAnalytics.Error.OAuth2.Auth",
            tre.getDetails().getError() ), tre );
      }
      logError( BaseMessages.getString( PKG, "GoogleAnalytics.Error.AccessingGaApi" ), exceptionToLog );
      return false;
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "GoogleAnalytics.Error.AccessingGaApi" ), e );
      return false;
    }
    return true;
  }

  // made not private for testing purposes
  List<String> getNextDataEntry() throws KettleException {
    // no query prepared yet?
    if ( data.query == null ) {

      data.query = getQuery( analytics );
      // use default max results for now
      //data.query.setMaxResults( 10000 );

      if ( log.isDetailed() ) {
        logDetailed( "querying google analytics: " + data.query.buildHttpRequestUrl().toURI().toString() );
      }

      try {
        data.feed = data.query.execute();
        data.entryIndex = 0;

      } catch ( IOException e2 ) {
        throw new KettleException( e2 );
      }

    } else if ( data.feed != null
      // getItemsPerPage():
      //    Its value ranges from 1 to 10,000 with a value of 1000 by default, or otherwise
      //    specified by the max-results query parameter
      && data.entryIndex + 1 >= data.feed.getItemsPerPage() ) {
      try {
        // query is there, check whether we hit the last entry and re-query as necessary
        int startIndex = ( data.query.getStartIndex() == null ) ? 1 : data.query.getStartIndex();
        int totalResults = ( data.feed.getTotalResults() == null ) ? 0 : data.feed.getTotalResults();

        int newStartIndex = startIndex + data.entryIndex;
        if ( newStartIndex <= totalResults ) {
          // need to query for next page
          data.query.setStartIndex( newStartIndex );
          data.feed = data.query.execute();
          data.entryIndex = 0;
        }
      } catch ( IOException e2 ) {
        throw new KettleException( e2 );
      }
    }

    if ( data.feed != null ) {
      List<List<String>> entries = data.feed.getRows();
      if ( entries != null && data.entryIndex < entries.size() ) {
        return entries.get( data.entryIndex++ );
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (GaInputStepMeta) smi;
    data = (GaInputStepData) sdi;

    super.dispose( smi, sdi );
  }

  private String removeClassifier( String original ) {
    int colonIndex = original.indexOf( ":" );
    return original.substring( colonIndex + 1 );
  }
}
