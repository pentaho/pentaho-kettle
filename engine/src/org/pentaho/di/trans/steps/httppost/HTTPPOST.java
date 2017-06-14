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

package org.pentaho.di.trans.steps.httppost;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONObject;
import org.pentaho.di.cluster.SlaveConnectionManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
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
 * Make a HTTP Post call
 *
 * @author Samatar
 * @since 15-jan-2009
 *
 */

public class HTTPPOST extends BaseStep implements StepInterface {

  private static Class<?> PKG = HTTPPOSTMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final String CONTENT_TYPE = "Content-type";
  private static final String CONTENT_TYPE_TEXT_XML = "text/xml";

  private HTTPPOSTMeta meta;
  private HTTPPOSTData data;

  public HTTPPOST( StepMeta stepMeta, StepDataInterface stepDataInterface,
                   int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private Object[] callHTTPPOST( Object[] rowData ) throws KettleException {
    // get dynamic url ?
    if ( meta.isUrlInField() ) {
      data.realUrl = data.inputRowMeta.getString( rowData, data.indexOfUrlField );
    }

    FileInputStream fis = null;
    try {
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "HTTPPOST.Log.ConnectingToURL", data.realUrl ) );
      }

      // Prepare HTTP POST
      //
      HttpClient httpPostClient = SlaveConnectionManager.getInstance().createHttpClient();
      PostMethod post = new PostMethod( data.realUrl );
      // post.setFollowRedirects(false);

      // Set timeout
      if ( data.realConnectionTimeout > -1 ) {
        httpPostClient.getHttpConnectionManager().getParams().setConnectionTimeout( data.realConnectionTimeout );
      }
      if ( data.realSocketTimeout > -1 ) {
        httpPostClient.getHttpConnectionManager().getParams().setSoTimeout( data.realSocketTimeout );
      }

      if ( !Utils.isEmpty( data.realHttpLogin ) ) {
        httpPostClient.getParams().setAuthenticationPreemptive( true );
        Credentials defaultcreds = new UsernamePasswordCredentials( data.realHttpLogin, data.realHttpPassword );
        httpPostClient.getState().setCredentials( AuthScope.ANY, defaultcreds );
      }

      HostConfiguration hostConfiguration = new HostConfiguration();
      if ( !Utils.isEmpty( data.realProxyHost ) ) {
        hostConfiguration.setProxy( data.realProxyHost, data.realProxyPort );
      }
      // Specify content type and encoding
      // If content encoding is not explicitly specified
      // ISO-8859-1 is assumed by the POSTMethod
      if ( !data.contentTypeHeaderOverwrite ) { // can be overwritten now
        if ( Utils.isEmpty( data.realEncoding ) ) {
          post.setRequestHeader( CONTENT_TYPE, CONTENT_TYPE_TEXT_XML );
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.HeaderValue", CONTENT_TYPE, CONTENT_TYPE_TEXT_XML ) );
          }
        } else {
          post.setRequestHeader( CONTENT_TYPE, CONTENT_TYPE_TEXT_XML + "; " + data.realEncoding );
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.HeaderValue", CONTENT_TYPE, CONTENT_TYPE_TEXT_XML
                + "; " + data.realEncoding ) );
          }
        }
      }

      // HEADER PARAMETERS
      if ( data.useHeaderParameters ) {
        // set header parameters that we want to send
        for ( int i = 0; i < data.header_parameters_nrs.length; i++ ) {
          post.addRequestHeader( data.headerParameters[i].getName(), data.inputRowMeta.getString( rowData,
              data.header_parameters_nrs[i] ) );
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.HeaderValue", data.headerParameters[i].getName(),
                data.inputRowMeta.getString( rowData, data.header_parameters_nrs[i] ) ) );
          }
        }
      }

      // BODY PARAMETERS
      if ( data.useBodyParameters ) {
        // set body parameters that we want to send
        for ( int i = 0; i < data.body_parameters_nrs.length; i++ ) {
          data.bodyParameters[i].setValue( data.inputRowMeta.getString( rowData, data.body_parameters_nrs[i] ) );
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.BodyValue", data.bodyParameters[i].getName(),
                data.inputRowMeta.getString( rowData, data.body_parameters_nrs[i] ) ) );
          }
        }
        String bodyParams = getRequestBodyParamsAsStr( data.bodyParameters, data.realEncoding );
        post.setRequestEntity( new StringRequestEntity( bodyParams, CONTENT_TYPE_TEXT_XML, "US-ASCII" ) );
      }

      // QUERY PARAMETERS
      if ( data.useQueryParameters ) {
        for ( int i = 0; i < data.query_parameters_nrs.length; i++ ) {
          data.queryParameters[i].setValue( data.inputRowMeta.getString( rowData, data.query_parameters_nrs[i] ) );
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.QueryValue", data.queryParameters[i].getName(),
                data.inputRowMeta.getString( rowData, data.query_parameters_nrs[i] ) ) );
          }
        }
        post.setQueryString( data.queryParameters );
      }

      // Set request entity?
      if ( data.indexOfRequestEntity >= 0 ) {
        String tmp = data.inputRowMeta.getString( rowData, data.indexOfRequestEntity );
        // Request content will be retrieved directly
        // from the input stream
        // Per default, the request content needs to be buffered
        // in order to determine its length.
        // Request body buffering can be avoided when
        // content length is explicitly specified

        if ( meta.isPostAFile() ) {
          File input = new File( tmp );
          fis = new FileInputStream( input );
          post.setRequestEntity( new InputStreamRequestEntity( fis, input.length() ) );
        } else {
          byte[] bytes;
          if ( ( data.realEncoding != null ) && ( data.realEncoding.length() > 0 ) ) {
            bytes = tmp.getBytes( data.realEncoding );
          } else {
            bytes = tmp.getBytes();
          }
          post.setRequestEntity( new InputStreamRequestEntity( new ByteArrayInputStream( bytes ), bytes.length ) );
        }
      }

      // Execute request
      //
      InputStreamReader inputStreamReader = null;
      Object[] newRow = null;
      if ( rowData != null ) {
        newRow = rowData.clone();
      }
      try {
        // used for calculating the responseTime
        long startTime = System.currentTimeMillis();

        // Execute the POST method
        int statusCode = requestStatusCode( post, hostConfiguration, httpPostClient );

        // calculate the responseTime
        long responseTime = System.currentTimeMillis() - startTime;

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "HTTPPOST.Log.ResponseTime", responseTime, data.realUrl ) );
        }

        // Display status code
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.ResponseCode", String.valueOf( statusCode ) ) );
        }
        String body = null;
        String headerString = null;
        if ( statusCode != -1 ) {
          if ( statusCode == 204 ) {
            body = "";
          } else {
            // if the response is not 401: HTTP Authentication required
            if ( statusCode != 401 ) {

              Header[] headers = searchForHeaders( post );
              // Use request encoding if specified in component to avoid strange response encodings
              // See PDI-3815
              String encoding = data.realEncoding;

              // Try to determine the encoding from the Content-Type value
              //
              if ( Utils.isEmpty( encoding ) ) {
                String contentType = post.getResponseHeader( "Content-Type" ).getValue();
                if ( contentType != null && contentType.contains( "charset" ) ) {
                  encoding = contentType.replaceFirst( "^.*;\\s*charset\\s*=\\s*", "" ).replace( "\"", "" ).trim();
                }
              }
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
                  list.add( (String) header.getValue() );
                  json.put( header.getName(), list );
                }
              }
              headerString = json.toJSONString();

              // Get the response, but only specify encoding if we've got one
              // otherwise the default charset ISO-8859-1 is used by HttpClient
              inputStreamReader = openStream( encoding, post );

              StringBuilder bodyBuffer = new StringBuilder();

              int c;
              while ( ( c = inputStreamReader.read() ) != -1 ) {
                bodyBuffer.append( (char) c );
              }
              inputStreamReader.close();

              // Display response
              body = bodyBuffer.toString();

              if ( isDebug() ) {
                logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.ResponseBody", body ) );
              }
            } else { // the status is a 401
              throw new KettleStepException( BaseMessages.getString( PKG, "HTTPPOST.Exception.Authentication",
                  data.realUrl ) );

            }
          }
        }
        int returnFieldsOffset = data.inputRowMeta.size();
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
        if ( inputStreamReader != null ) {
          inputStreamReader.close();
        }
        // Release current connection to the connection pool once you are done
        post.releaseConnection();
        if ( data.realcloseIdleConnectionsTime > -1 ) {
          httpPostClient.getHttpConnectionManager().closeIdleConnections( data.realcloseIdleConnectionsTime );
        }
      }
      return newRow;
    } catch ( UnknownHostException uhe ) {
      throw new KettleException( BaseMessages.getString( PKG,
              "HTTPPOST.Error.UnknownHostException", uhe.getMessage() ) );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "HTTPPOST.Error.CanNotReadURL", data.realUrl ), e );

    } finally {
      if ( fis != null ) {
        BaseStep.closeQuietly( fis );
      }
    }
  }

  protected int requestStatusCode( PostMethod post, HostConfiguration hostConfiguration, HttpClient httpPostClient ) throws IOException {
    return httpPostClient.executeMethod( hostConfiguration, post );
  }

  protected InputStreamReader openStream( String encoding, PostMethod post ) throws Exception {
    if ( Utils.isEmpty( encoding ) ) {
      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.Encoding", "ISO-8859-1" ) );
      }
      return new InputStreamReader( post.getResponseBodyAsStream() );
    } else {
      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "HTTPPOST.Log.Encoding", encoding ) );
      }
      return new InputStreamReader( post.getResponseBodyAsStream(), encoding );
    }

  }

  protected Header[] searchForHeaders( PostMethod post ) {
    return post.getResponseHeaders();
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (HTTPPOSTMeta) smi;
    data = (HTTPPOSTData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...
      setOutputDone();
      return false;
    }
    if ( first ) {
      first = false;
      data.inputRowMeta = getInputRowMeta();
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( meta.isUrlInField() ) {
        if ( Utils.isEmpty( meta.getUrlField() ) ) {
          logError( BaseMessages.getString( PKG, "HTTPPOST.Log.NoField" ) );
          throw new KettleException( BaseMessages.getString( PKG, "HTTPPOST.Log.NoField" ) );
        }

        // cache the position of the field
        if ( data.indexOfUrlField < 0 ) {
          String realUrlfieldName = environmentSubstitute( meta.getUrlField() );
          data.indexOfUrlField = data.inputRowMeta.indexOfValue( ( realUrlfieldName ) );
          if ( data.indexOfUrlField < 0 ) {
            // The field is unreachable !
            logError( BaseMessages.getString( PKG, "HTTPPOST.Log.ErrorFindingField", realUrlfieldName ) );
            throw new KettleException( BaseMessages.getString( PKG, "HTTPPOST.Exception.ErrorFindingField",
                realUrlfieldName ) );
          }
        }
      } else {
        data.realUrl = environmentSubstitute( meta.getUrl() );
      }
      // set body parameters
      int nrargs = meta.getArgumentField().length;
      if ( nrargs > 0 ) {
        data.useBodyParameters = false;
        data.useHeaderParameters = false;
        data.contentTypeHeaderOverwrite = false;
        int nrheader = 0;
        int nrbody = 0;
        for ( int i = 0; i < nrargs; i++ ) {  // split into body / header
          if ( meta.getArgumentHeader()[i] ) {
            data.useHeaderParameters = true; // at least one header parameter
            nrheader++;
          } else {
            data.useBodyParameters = true; // at least one body parameter
            nrbody++;
          }
        }
        data.header_parameters_nrs = new int[nrheader];
        data.headerParameters = new NameValuePair[nrheader];
        data.body_parameters_nrs = new int[nrbody];
        data.bodyParameters = new NameValuePair[nrbody];
        int posHeader = 0;
        int posBody = 0;
        for ( int i = 0; i < nrargs; i++ ) {
          int fieldIndex = data.inputRowMeta.indexOfValue( meta.getArgumentField()[i] );
          if ( fieldIndex < 0 ) {
            logError( BaseMessages.getString( PKG, "HTTPPOST.Log.ErrorFindingField" ) + meta.getArgumentField()[i]
                + "]" );
            throw new KettleStepException( BaseMessages.getString( PKG, "HTTPPOST.Exception.CouldnotFindField", meta
                .getArgumentField()[i] ) );
          }
          if ( meta.getArgumentHeader()[i] ) {
            data.header_parameters_nrs[posHeader] = fieldIndex;
            data.headerParameters[posHeader] =
                new NameValuePair( environmentSubstitute( meta.getArgumentParameter()[i] ), data.outputRowMeta
                    .getString( r, data.header_parameters_nrs[posHeader] ) );
            posHeader++;
            if ( CONTENT_TYPE.equalsIgnoreCase( meta.getArgumentParameter()[i] ) ) {
              data.contentTypeHeaderOverwrite = true; // Content-type will be overwritten
            }
          } else {
            data.body_parameters_nrs[posBody] = fieldIndex;
            data.bodyParameters[posBody] =
                new NameValuePair( environmentSubstitute( meta.getArgumentParameter()[i] ), data.outputRowMeta
                    .getString( r, data.body_parameters_nrs[posBody] ) );
            posBody++;
          }
        }
      }
      // set query parameters
      int nrQuery = meta.getQueryField().length;
      if ( nrQuery > 0 ) {
        data.useQueryParameters = true;
        data.query_parameters_nrs = new int[nrQuery];
        data.queryParameters = new NameValuePair[nrQuery];
        for ( int i = 0; i < nrQuery; i++ ) {
          data.query_parameters_nrs[i] = data.inputRowMeta.indexOfValue( meta.getQueryField()[i] );
          if ( data.query_parameters_nrs[i] < 0 ) {
            logError( BaseMessages.getString( PKG, "HTTPPOST.Log.ErrorFindingField" ) + meta.getQueryField()[i] + "]" );
            throw new KettleStepException( BaseMessages.getString( PKG, "HTTPPOST.Exception.CouldnotFindField", meta
                .getQueryField()[i] ) );
          }
          data.queryParameters[i] =
              new NameValuePair( environmentSubstitute( meta.getQueryParameter()[i] ), data.outputRowMeta.getString( r,
                  data.query_parameters_nrs[i] ) );
        }
      }
      // set request entity?
      if ( !Utils.isEmpty( meta.getRequestEntity() ) ) {
        data.indexOfRequestEntity = data.inputRowMeta.indexOfValue( environmentSubstitute( meta.getRequestEntity() ) );
        if ( data.indexOfRequestEntity < 0 ) {
          throw new KettleStepException( BaseMessages.getString( PKG,
              "HTTPPOST.Exception.CouldnotFindRequestEntityField", meta.getRequestEntity() ) );
        }
      }
      data.realEncoding = environmentSubstitute( meta.getEncoding() );
    } // end if first

    try {
      Object[] outputRowData = callHTTPPOST( r );
      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

      if ( checkFeedback( getLinesRead() ) ) {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "HTTPPOST.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "HTTPPOST.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        logError( Const.getStackTracker( e ) );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }

      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "HTTPPOST001" );
      }

    }

    return true;
  }

  private String getRequestBodyParamsAsStr( NameValuePair[] pairs, String charset ) throws KettleException {
    StringBuffer buf = new StringBuffer();
    try {
      for ( int i = 0; i < pairs.length; ++i ) {
        NameValuePair pair = pairs[i];
        if ( pair.getName() != null ) {
          if ( i > 0 ) {
            buf.append( "&" );
          }

          buf.append( URLEncoder.encode( pair.getName(), charset ) );
          buf.append( "=" );
          if ( pair.getValue() != null ) {
            buf.append( URLEncoder.encode( pair.getValue(), charset ) );
          }
        }
      }
      return buf.toString();
    } catch ( UnsupportedEncodingException e ) {
      throw new KettleException( e.getMessage(), e.getCause() );
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (HTTPPOSTMeta) smi;
    data = (HTTPPOSTData) sdi;

    if ( super.init( smi, sdi ) ) {
      // get authentication settings once
      data.realProxyHost = environmentSubstitute( meta.getProxyHost() );
      data.realProxyPort = Const.toInt( environmentSubstitute( meta.getProxyPort() ), 8080 );
      data.realHttpLogin = environmentSubstitute( meta.getHttpLogin() );
      data.realHttpPassword = Utils.resolvePassword( variables, meta.getHttpPassword() );

      data.realSocketTimeout = Const.toInt( environmentSubstitute( meta.getSocketTimeout() ), -1 );
      data.realConnectionTimeout = Const.toInt( environmentSubstitute( meta.getSocketTimeout() ), -1 );
      data.realcloseIdleConnectionsTime =
              Const.toInt( environmentSubstitute( meta.getCloseIdleConnectionsTime() ), -1 );

      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (HTTPPOSTMeta) smi;
    data = (HTTPPOSTData) sdi;

    super.dispose( smi, sdi );
  }
}
