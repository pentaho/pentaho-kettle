/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.http;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves values from a database by calling database stored procedures or functions
 *
 * @author Matt
 * @since 26-apr-2003
 */
public class HTTP extends BaseStep implements StepInterface {
  private static Class<?> PKG = HTTPMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private HTTPMeta meta;
  private HTTPData data;

  public HTTP( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private Object[] execHttp( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
    if ( first ) {
      first = false;
      data.argnrs = new int[ meta.getArgumentField().length ];

      for ( int i = 0; i < meta.getArgumentField().length; i++ ) {
        data.argnrs[ i ] = rowMeta.indexOfValue( meta.getArgumentField()[ i ] );
        if ( data.argnrs[ i ] < 0 ) {
          logError( BaseMessages.getString( PKG, "HTTP.Log.ErrorFindingField" ) + meta.getArgumentField()[ i ] + "]" );
          throw new KettleStepException( BaseMessages.getString( PKG, "HTTP.Exception.CouldnotFindField", meta
            .getArgumentField()[ i ] ) );
        }
      }
    }

    return callHttpService( rowMeta, row );
  }

  private Object[] callHttpService( RowMetaInterface rowMeta, Object[] rowData ) throws KettleException {
    HttpClientManager.HttpClientBuilderFacade clientBuilder = HttpClientManager.getInstance().createBuilder();

    if ( data.realConnectionTimeout > -1 ) {
      clientBuilder.setConnectionTimeout( data.realConnectionTimeout );
    }
    if ( data.realSocketTimeout > -1 ) {
      clientBuilder.setSocketTimeout( data.realSocketTimeout );
    }
    if ( StringUtils.isNotBlank( data.realHttpLogin ) ) {
      clientBuilder.setCredentials( data.realHttpLogin, data.realHttpPassword );
    }
    if ( StringUtils.isNotBlank( data.realProxyHost ) ) {
      clientBuilder.setProxy( data.realProxyHost, data.realProxyPort );
    }

    CloseableHttpClient httpClient = clientBuilder.build();

    // Prepare HTTP get
    URI uri = null;
    try {
      URIBuilder uriBuilder = constructUrlBuilder( rowMeta, rowData );

      uri = uriBuilder.build();
      HttpGet method = new HttpGet( uri );

      // Add Custom HTTP headers
      if ( data.useHeaderParameters ) {
        for ( int i = 0; i < data.header_parameters_nrs.length; i++ ) {
          method.addHeader( data.headerParameters[ i ].getName(), data.inputRowMeta.getString( rowData,
            data.header_parameters_nrs[ i ] ) );
          if ( isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "HTTPDialog.Log.HeaderValue",
              data.headerParameters[ i ].getName(), data.inputRowMeta
                .getString( rowData, data.header_parameters_nrs[ i ] ) ) );
          }
        }
      }

      Object[] newRow = null;
      if ( rowData != null ) {
        newRow = rowData.clone();
      }
      // Execute request
      CloseableHttpResponse httpResponse = null;
      try {
        // used for calculating the responseTime
        long startTime = System.currentTimeMillis();

        // Preemptive authentication
        if ( StringUtils.isNotBlank( data.realProxyHost ) ) {
          HttpHost target = new HttpHost( data.realProxyHost, data.realProxyPort, "http" );
          // Create AuthCache instance
          AuthCache authCache = new BasicAuthCache();
          // Generate BASIC scheme object and add it to the local
          // auth cache
          BasicScheme basicAuth = new BasicScheme();
          authCache.put( target, basicAuth );
          // Add AuthCache to the execution context
          HttpClientContext localContext = HttpClientContext.create();
          localContext.setAuthCache( authCache );
          httpResponse = httpClient.execute( target, method, localContext );
        } else {
          httpResponse = httpClient.execute( method );
        }
        // calculate the responseTime
        long responseTime = System.currentTimeMillis() - startTime;
        if ( log.isDetailed() ) {
          log.logDetailed( BaseMessages.getString( PKG, "HTTP.Log.ResponseTime", responseTime, uri ) );
        }
        int statusCode = requestStatusCode( httpResponse );
        // The status code
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "HTTP.Log.ResponseStatusCode", "" + statusCode ) );
        }

        String body;
        switch ( statusCode ) {
          case HttpURLConnection.HTTP_UNAUTHORIZED:
            throw new KettleStepException( BaseMessages
              .getString( PKG, "HTTP.Exception.Authentication", data.realUrl ) );
          case -1:
            throw new KettleStepException( BaseMessages
              .getString( PKG, "HTTP.Exception.IllegalStatusCode", data.realUrl ) );
          case HttpURLConnection.HTTP_NO_CONTENT:
            body = "";
            break;
          default:
            HttpEntity entity = httpResponse.getEntity();
            if ( entity != null ) {
              body = EntityUtils.toString( entity );
            } else {
              body = "";
            }
            break;
        }

        Header[] headers = searchForHeaders( httpResponse );

        JSONObject json = new JSONObject();
        for ( Header header : headers ) {
          Object previousValue = json.get( header.getName() );
          if ( previousValue == null ) {
            json.put( header.getName(), header.getValue() );
          } else if ( previousValue instanceof List ) {
            List<String> list = (List<String>) previousValue;
            list.add( header.getValue() );
          } else {
            ArrayList<String> list = new ArrayList<String>();
            list.add( (String) previousValue );
            list.add( header.getValue() );
            json.put( header.getName(), list );
          }
        }
        String headerString = json.toJSONString();

        int returnFieldsOffset = rowMeta.size();
        if ( !Utils.isEmpty( meta.getFieldName() ) ) {
          newRow = RowDataUtil.addValueData( newRow, returnFieldsOffset, body );
          returnFieldsOffset++;
        }

        if ( !Utils.isEmpty( meta.getResultCodeFieldName() ) ) {
          newRow = RowDataUtil.addValueData( newRow, returnFieldsOffset, new Long( statusCode ) );
          returnFieldsOffset++;
        }
        if ( !Utils.isEmpty( meta.getResponseTimeFieldName() ) ) {
          newRow = RowDataUtil.addValueData( newRow, returnFieldsOffset, new Long( responseTime ) );
          returnFieldsOffset++;
        }
        if ( !Utils.isEmpty( meta.getResponseHeaderFieldName() ) ) {
          newRow = RowDataUtil.addValueData( newRow, returnFieldsOffset, headerString );
        }

      } finally {
        if ( httpResponse != null ) {
          httpResponse.close();
        }
        // Release current connection to the connection pool once you are done
        method.releaseConnection();
      }
      return newRow;
    } catch ( UnknownHostException uhe ) {
      throw new KettleException( BaseMessages.getString( PKG, "HTTP.Error.UnknownHostException", uhe.getMessage() ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "HTTP.Log.UnableGetResult", uri ), e );
    }
  }

  private URIBuilder constructUrlBuilder( RowMetaInterface outputRowMeta, Object[] row ) throws KettleValueException,
    KettleException {
    URIBuilder uriBuilder;
    try {
      String baseUrl = data.realUrl;
      if ( meta.isUrlInField() ) {
        // get dynamic url
        baseUrl = outputRowMeta.getString( row, data.indexOfUrlField );
      }

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "HTTP.Log.Connecting", baseUrl ) );
      }

      uriBuilder = new URIBuilder( baseUrl ); // the base URL with variable substitution
      List<NameValuePair> queryParams = uriBuilder.getQueryParams();

      for ( int i = 0; i < data.argnrs.length; i++ ) {
        String key = meta.getArgumentParameter()[ i ];
        String value = outputRowMeta.getString( row, data.argnrs[ i ] );
        BasicNameValuePair basicNameValuePair = new BasicNameValuePair( key, value );
        queryParams.add( basicNameValuePair );
      }
      if ( !queryParams.isEmpty() ) {
        uriBuilder.setParameters( queryParams );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "HTTP.Log.UnableCreateUrl" ), e );
    }
    return uriBuilder;
  }

  protected int requestStatusCode( HttpResponse httpResponse ) {
    return httpResponse.getStatusLine().getStatusCode();
  }

  protected InputStreamReader openStream( String encoding, HttpResponse httpResponse ) throws Exception {
    if ( !Utils.isEmpty( encoding ) ) {
      return new InputStreamReader( httpResponse.getEntity().getContent(), encoding );
    } else {
      return new InputStreamReader( httpResponse.getEntity().getContent() );
    }
  }

  protected Header[] searchForHeaders( CloseableHttpResponse response ) {
    return response.getAllHeaders();
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (HTTPMeta) smi;
    data = (HTTPData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first ) {
      data.outputRowMeta = getInputRowMeta().clone();
      data.inputRowMeta = getInputRowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( meta.isUrlInField() ) {
        if ( Utils.isEmpty( meta.getUrlField() ) ) {
          logError( BaseMessages.getString( PKG, "HTTP.Log.NoField" ) );
          throw new KettleException( BaseMessages.getString( PKG, "HTTP.Log.NoField" ) );
        }

        // cache the position of the field
        if ( data.indexOfUrlField < 0 ) {
          String realUrlfieldName = environmentSubstitute( meta.getUrlField() );
          data.indexOfUrlField = getInputRowMeta().indexOfValue( realUrlfieldName );
          if ( data.indexOfUrlField < 0 ) {
            // The field is unreachable !
            logError( BaseMessages.getString( PKG, "HTTP.Log.ErrorFindingField", realUrlfieldName ) );
            throw new KettleException( BaseMessages.getString( PKG, "HTTP.Exception.ErrorFindingField",
              realUrlfieldName ) );
          }
        }
      } else {
        data.realUrl = environmentSubstitute( meta.getUrl() );
      }

      // check for headers
      int nrHeaders = meta.getHeaderField().length;
      if ( nrHeaders > 0 ) {
        data.useHeaderParameters = true;
      }

      data.header_parameters_nrs = new int[ nrHeaders ];
      data.headerParameters = new NameValuePair[ nrHeaders ];

      // get the headers
      for ( int i = 0; i < nrHeaders; i++ ) {
        int fieldIndex = data.inputRowMeta.indexOfValue( meta.getHeaderField()[ i ] );
        if ( fieldIndex < 0 ) {
          logError( BaseMessages.getString( PKG,
            "HTTP.Exception.ErrorFindingField" ) + meta.getHeaderField()[ i ] + "]" );
          throw new KettleStepException( BaseMessages.getString( PKG, "HTTP.Exception.ErrorFindingField", meta
            .getHeaderField()[ i ] ) );
        }

        data.header_parameters_nrs[ i ] = fieldIndex;
        data.headerParameters[ i ] =
          new BasicNameValuePair( environmentSubstitute( meta.getHeaderParameter()[ i ] ),
            data.outputRowMeta.getString( r,
              data.header_parameters_nrs[ i ] ) );
      }

    } // end if first

    try {
      Object[] outputRowData = execHttp( getInputRowMeta(), r ); // add new values to the row
      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

      if ( checkFeedback( getLinesRead() ) ) {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "HTTP.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "HTTP.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "HTTP001" );
      }
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (HTTPMeta) smi;
    data = (HTTPData) sdi;

    if ( super.init( smi, sdi ) ) {
      // get authentication settings once
      data.realProxyHost = environmentSubstitute( meta.getProxyHost() );
      data.realProxyPort = Const.toInt( environmentSubstitute( meta.getProxyPort() ), 8080 );
      data.realHttpLogin = environmentSubstitute( meta.getHttpLogin() );
      data.realHttpPassword = Utils.resolvePassword( variables, meta.getHttpPassword() );

      data.realSocketTimeout = Const.toInt( environmentSubstitute( meta.getSocketTimeout() ), -1 );
      data.realConnectionTimeout = Const.toInt( environmentSubstitute( meta.getSocketTimeout() ), -1 );

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (HTTPMeta) smi;
    data = (HTTPData) sdi;

    super.dispose( smi, sdi );
  }

}
