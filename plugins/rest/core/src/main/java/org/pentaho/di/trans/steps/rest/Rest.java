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


package org.pentaho.di.trans.steps.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.uri.UriComponent;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.util.HttpClientManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * @author Samatar
 * @since 16-jan-2011
 */
public class Rest extends BaseStep implements StepInterface {
  private static Class<?> PKG = RestMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String HEADER_CONTENT_TYPE = "Content-Type";

  private RestMeta meta;
  private RestData data;

  public Rest( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  protected Object[] callRest( Object[] rowData ) throws KettleException {
    try ( Client client = getClient( rowData ) ) {
      WebTarget target = buildRequest( client, rowData );
      return invokeRequest( target, rowData );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.CanNotReadURL", data.realUrl ), e );
    }
  }

  protected Client getClient( Object[] rowData ) throws KettleException {
    // get dynamic url ?
    if ( meta.isUrlInField() ) {
      data.realUrl = data.inputRowMeta.getString( rowData, data.indexOfUrlField );
    }

    // get dynamic method?
    if ( meta.isDynamicMethod() ) {
      data.method = data.inputRowMeta.getString( rowData, data.indexOfMethod );
      if ( Utils.isEmpty( data.method ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.MethodMissing" ) );
      }
    }

    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "Rest.Log.ConnectingToURL", data.realUrl ) );
    }

    // Register a custom StringMessageBodyWriter to solve PDI-17423
    ClientBuilder clientBuilder = ClientBuilder.newBuilder();
    clientBuilder.withConfig( data.config ).property( HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true );

    if ( meta.isIgnoreSsl() || !Utils.isEmpty( data.trustStoreFile ) ) {
      clientBuilder.sslContext( data.sslContext );
      clientBuilder.hostnameVerifier( ( s1, s2 ) -> true );
    }

    Client client = clientBuilder.build();
    if ( data.basicAuthentication != null ) {
      client.register( data.basicAuthentication );
    }

    return client;
  }

  protected WebTarget buildRequest( Client client, Object[] rowData ) throws KettleException {
    // create a target object, which encapsulates a web resource for the client
    WebTarget target = client.target( data.realUrl );

    if ( data.useMatrixParams ) {
      // Add matrix parameters
      UriBuilder builder = target.getUriBuilder();
      for ( int i = 0; i < data.nrMatrixParams; i++ ) {
        String value = data.inputRowMeta.getString( rowData, data.indexOfMatrixParamFields[ i ] );
        if ( isDebug() ) {
          logDebug(
            BaseMessages.getString( PKG, "Rest.Log.matrixParameterValue", data.matrixParamNames[ i ], value ) );
        }
        builder = builder.matrixParam( data.matrixParamNames[ i ],
          UriComponent.encode( value, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED ) );
      }
      target = client.target( builder.build() );
    }

    if ( data.useParams ) {
      // Add query parameters
      for ( int i = 0; i < data.nrParams; i++ ) {
        String value = data.inputRowMeta.getString( rowData, data.indexOfParamFields[ i ] );
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "Rest.Log.queryParameterValue", data.paramNames[ i ], value ) );
        }
        target = target.queryParam( data.paramNames[ i ],
          UriComponent.encode( value, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED ) );
      }
    }

    if ( isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "Rest.Log.ConnectingToURL", target.getUri() ) );
    }

    return target;
  }

  private Object[] invokeRequest( WebTarget target, Object[] rowData ) throws KettleException {
    Object[] newRow = null;
    if ( rowData != null ) {
      newRow = rowData.clone();
    }

    // used for calculating the responseTime
    long startTime = System.currentTimeMillis();

    Invocation.Builder invocationBuilder = target.request();

    String contentType = calcContentType( rowData, invocationBuilder );

    String entityString = "";
    if ( data.useBody ) {
      // Set Http request entity
      entityString = Const.NVL( data.inputRowMeta.getString( rowData, data.indexOfBodyField ), "" );
      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "Rest.Log.BodyValue", entityString ) );
      }
    }

    Response response = getResponse( invocationBuilder, contentType, entityString );

    // Get response time
    long responseTime = System.currentTimeMillis() - startTime;
    if ( isDetailed() ) {
      logDetailed(
        BaseMessages.getString( PKG, "Rest.Log.ResponseTime", String.valueOf( responseTime ), data.realUrl ) );
    }

    // Get status
    int status = response.getStatus();
    // Display status code
    if ( isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "Rest.Log.ResponseCode", "" + status ) );
    }

    // Get Response
    String body;
    try {
      body = response.readEntity( String.class );
    } catch ( Exception ex ) {
      body = "";
    }

    // for output
    int returnFieldsOffset = data.inputRowMeta.size();
    // add response to output
    if ( !Utils.isEmpty( data.resultFieldName ) ) {
      newRow = RowDataUtil.addValueData( newRow, returnFieldsOffset, body );
      returnFieldsOffset++;
    }

    // add status to output
    if ( !Utils.isEmpty( data.resultCodeFieldName ) ) {
      newRow = RowDataUtil.addValueData( newRow, returnFieldsOffset, Long.valueOf( status ) );
      returnFieldsOffset++;
    }

    // add response time to output
    if ( !Utils.isEmpty( data.resultResponseFieldName ) ) {
      newRow = RowDataUtil.addValueData( newRow, returnFieldsOffset, Long.valueOf( responseTime ) );
      returnFieldsOffset++;
    }
    // add response header to output
    if ( !Utils.isEmpty( data.resultHeaderFieldName ) ) {
      newRow = RowDataUtil.addValueData( newRow, returnFieldsOffset, getHeaderFromResponse( response ) );
    }
    return newRow;
  }

  /**
   * Calculate content type from headers if any
   *
   * @param rowData
   * @param invocationBuilder
   * @return content type if any
   * @throws KettleValueException
   */
  private String calcContentType( Object[] rowData, Invocation.Builder invocationBuilder ) throws KettleValueException {
    String contentType = null;
    if ( data.useHeaders ) {
      // Add headers
      for ( int i = 0; i < data.nrheader; i++ ) {
        String value = data.inputRowMeta.getString( rowData, data.indexOfHeaderFields[ i ] );

        // unsure if an already set header will be returned to builder
        invocationBuilder.header( data.headerNames[ i ], value );
        if ( HEADER_CONTENT_TYPE.equals( data.headerNames[ i ] ) ) {
          contentType = value;
        }
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "Rest.Log.HeaderValue", data.headerNames[ i ], value ) );
        }
      }
    }
    return contentType;
  }

  /**
   * Invoke the request based on the method
   *
   * @param invocationBuilder
   * @param contentType
   * @param entityString
   * @return the response from the server
   * @throws KettleException in case the request could not be processed
   */
  private Response getResponse( Invocation.Builder invocationBuilder, String contentType, String entityString )
    throws KettleException {
    Response response;
    try {
      switch ( data.method ) {
        case RestMeta.HTTP_METHOD_GET -> response = invocationBuilder.get( Response.class );
        case RestMeta.HTTP_METHOD_POST -> response = invocationBuilder.post( getEntity( contentType, entityString ) );
        case RestMeta.HTTP_METHOD_PUT -> response = invocationBuilder.put( getEntity( contentType, entityString ) );
        case RestMeta.HTTP_METHOD_DELETE -> response = invocationBuilder.delete();
        case RestMeta.HTTP_METHOD_HEAD -> response = invocationBuilder.head();
        case RestMeta.HTTP_METHOD_OPTIONS -> response = invocationBuilder.options();
        case RestMeta.HTTP_METHOD_PATCH ->
          response = invocationBuilder.method( RestMeta.HTTP_METHOD_PATCH, getEntity( contentType, entityString ) );
        default -> throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.UnknownMethod", data.method ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Request could not be processed", e );
    }
    return response;
  }

  /**
   * Get entity for request using the given content type or the default one
   *
   * @param contentType  the content type
   * @param entityString the entity string
   * @return
   */
  private Entity<?> getEntity( String contentType, String entityString ) {
    Entity<?> entity;

    if ( null != contentType ) {
      entity = Entity.entity( entityString, contentType );
    } else {
      entity = Entity.entity( entityString, data.mediaType );
    }

    return entity;
  }

  /**
   * Get headers from response and return then as a json string
   *
   * @param response the response object from which to extract headers
   * @return json string representing the headers of the given response
   */
  private String getHeaderFromResponse( Response response ) {
    MultivaluedMap<String, Object> headers = searchForHeaders( response );
    JSONObject json = new JSONObject();

    for ( java.util.Map.Entry<String, List<Object>> entry : headers.entrySet() ) {
      String name = entry.getKey();
      List<Object> value = entry.getValue();
      if ( value.size() > 1 ) {
        json.put( name, value );
      } else {
        json.put( name, value.get( 0 ) );
      }
    }

    return json.toJSONString();
  }

  private void setConfig() throws KettleException {
    if ( data.config == null ) {
      data.config = new ClientConfig();
      data.config.connectorProvider( new ApacheConnectorProvider() );
      data.config.property( ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED );
      if ( !Utils.isEmpty( data.realProxyHost ) ) {
        // PROXY CONFIGURATION
        data.config.property( ClientProperties.PROXY_URI, "http://" + data.realProxyHost + ":" + data.realProxyPort );
        if ( !Utils.isEmpty( data.realHttpLogin ) && !Utils.isEmpty( data.realHttpPassword ) ) {
          data.config.property( ClientProperties.PROXY_USERNAME, data.realHttpLogin );
          data.config.property( ClientProperties.PROXY_PASSWORD, data.realHttpPassword );
        }
      } else {
        if ( !Utils.isEmpty( data.realHttpLogin ) ) {
          // Basic authentication
          data.basicAuthentication =
            HttpAuthenticationFeature.basicBuilder()
              .credentials( data.realHttpLogin, data.realHttpPassword )
              .build();
        }
      }
      // SSL TRUST STORE CONFIGURATION
      setSSLConfiguration( data );
    }
  }

  protected void setSSLConfiguration( RestData data ) throws KettleException {
    try {
      data.sslContext = HttpClientManager.getSslContext( meta.isIgnoreSsl(),
        getInputStream( data.trustStoreFile ),
        data.trustStorePassword );

    } catch ( NoSuchAlgorithmException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.NoSuchAlgorithm" ), e );
    } catch ( KeyStoreException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.KeyStoreException" ), e );
    } catch ( CertificateException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.CertificateException" ), e );
    } catch ( FileNotFoundException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.FileNotFound", data.trustStoreFile ), e );
    } catch ( IOException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.IOException" ), e );
    } catch ( KeyManagementException | UnrecoverableKeyException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Rest.Error.KeyManagementException" ), e );
    }
  }

  /**
   * Get an InputStream for the file with the given name.
   * If the file name is empty or null, returns null.
   *
   * @param fileName the file name to get InputStream from
   * @return InputStream for the given file, <code>null</code> if the given file name is empty or null
   * @throws KettleException if any error occurs while getting the InputStream
   */
  protected InputStream getInputStream( String fileName ) throws KettleException {
    InputStream inputStream = null;

    if ( !StringUtil.isEmpty( fileName ) ) {
      fileName = fileName.trim();
      if ( !StringUtil.isEmpty( fileName ) ) {
        inputStream = KettleVFS.getInstance( this.getTransMeta().getBowl() ).getInputStream( fileName );
      }
    }
    return inputStream;
  }

  protected MultivaluedMap<String, Object> searchForHeaders( Response response ) {
    return response.getHeaders();
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (RestMeta) smi;
    data = (RestData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!

    if ( r == null ) {
      // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      data.inputRowMeta = getInputRowMeta();
      data.outputRowMeta = data.inputRowMeta.clone();
      meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
        metaStore );

      // Let's set URL
      calcURL();

      // Check Method
      calcMethod();

      // set Headers
      calcHeaders();

      String substitutedMethod = environmentSubstitute( meta.getMethod() );

      // Set Parameters
      if ( RestMeta.isActiveParameters( substitutedMethod ) ) {
        calcParameters();
      }

      // Do we need to set body
      if ( RestMeta.isActiveBody( substitutedMethod ) ) {
        calcBody();
      }
    } // end if first

    try {
      Object[] outputRowData = callRest( r );
      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s)
      if ( isDetailed() && checkFeedback( getLinesRead() ) ) {
        logDetailed( BaseMessages.getString( PKG, "Rest.LineNumber" ) + getLinesRead() );
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, e.toString(), null, "Rest001" );
      } else {
        logError( BaseMessages.getString( PKG, "Rest.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        logError( Const.getStackTracker( e ) );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
    }
    return true;
  }

  /**
   * Calculate URL
   *
   * @throws KettleException
   */
  private void calcURL() throws KettleException {
    if ( meta.isUrlInField() ) {
      if ( Utils.isEmpty( meta.getUrlField() ) ) {
        logError( BaseMessages.getString( PKG, "Rest.Log.NoField" ) );
        throw new KettleException( BaseMessages.getString( PKG, "Rest.Log.NoField" ) );
      }
      // cache the position of the field
      if ( data.indexOfUrlField < 0 ) {
        String realUrlfieldName = environmentSubstitute( meta.getUrlField() );
        data.indexOfUrlField = data.inputRowMeta.indexOfValue( realUrlfieldName );
        if ( data.indexOfUrlField < 0 ) {
          // The field is unreachable !
          throw new KettleException(
            BaseMessages.getString( PKG, "Rest.Exception.ErrorFindingField", realUrlfieldName ) );
        }
      }
    } else {
      // Static URL
      data.realUrl = environmentSubstitute( meta.getUrl() );
    }
  }

  /**
   * Calculate method
   *
   * @throws KettleException
   */
  private void calcMethod() throws KettleException {
    if ( meta.isDynamicMethod() ) {
      String field = environmentSubstitute( meta.getMethodFieldName() );
      if ( Utils.isEmpty( field ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.MethodFieldMissing" ) );
      }
      data.indexOfMethod = data.inputRowMeta.indexOfValue( field );
      if ( data.indexOfMethod < 0 ) {
        // The field is unreachable !
        throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.ErrorFindingField", field ) );
      }
    }
  }

  /**
   * Calculate headers
   *
   * @throws KettleException
   */
  private void calcHeaders() throws KettleException {
    int nrArgs = meta.getHeaderName() == null ? 0 : meta.getHeaderName().length;
    if ( nrArgs > 0 ) {
      data.nrheader = nrArgs;
      data.indexOfHeaderFields = new int[ nrArgs ];
      data.headerNames = new String[ nrArgs ];
      for ( int i = 0; i < nrArgs; i++ ) {
        // split into body / header
        data.headerNames[ i ] = environmentSubstitute( meta.getHeaderName()[ i ] );
        String field = environmentSubstitute( meta.getHeaderField()[ i ] );
        if ( Utils.isEmpty( field ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.HeaderFieldEmpty" ) );
        }
        data.indexOfHeaderFields[ i ] = data.inputRowMeta.indexOfValue( field );
        if ( data.indexOfHeaderFields[ i ] < 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.ErrorFindingField", field ) );
        }
      }
      data.useHeaders = true;
    }
  }

  /**
   * Calculate the parameters
   *
   * @throws KettleException
   */
  private void calcParameters() throws KettleException {
    // Parameters
    int nrParams = meta.getParameterField() == null ? 0 : meta.getParameterField().length;
    if ( nrParams > 0 ) {
      data.nrParams = nrParams;
      data.paramNames = new String[ nrParams ];
      data.indexOfParamFields = new int[ nrParams ];
      for ( int i = 0; i < nrParams; i++ ) {
        data.paramNames[ i ] = environmentSubstitute( meta.getParameterName()[ i ] );
        String field = environmentSubstitute( meta.getParameterField()[ i ] );
        if ( Utils.isEmpty( field ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.ParamFieldEmpty" ) );
        }
        data.indexOfParamFields[ i ] = data.inputRowMeta.indexOfValue( field );
        if ( data.indexOfParamFields[ i ] < 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.ErrorFindingField", field ) );
        }
      }
      data.useParams = true;
    }

    int nrMatrixParams = meta.getMatrixParameterField() == null ? 0 : meta.getMatrixParameterField().length;
    if ( nrMatrixParams > 0 ) {
      data.nrMatrixParams = nrMatrixParams;
      data.matrixParamNames = new String[ nrMatrixParams ];
      data.indexOfMatrixParamFields = new int[ nrMatrixParams ];
      for ( int i = 0; i < nrMatrixParams; i++ ) {
        data.matrixParamNames[ i ] = environmentSubstitute( meta.getMatrixParameterName()[ i ] );
        String field = environmentSubstitute( meta.getMatrixParameterField()[ i ] );
        if ( Utils.isEmpty( field ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.MatrixParamFieldEmpty" ) );
        }
        data.indexOfMatrixParamFields[ i ] = data.inputRowMeta.indexOfValue( field );
        if ( data.indexOfMatrixParamFields[ i ] < 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.ErrorFindingField", field ) );
        }
      }
      data.useMatrixParams = true;
    }
  }

  /**
   * Calculate body
   *
   * @throws KettleException
   */
  private void calcBody() throws KettleException {
    String field = environmentSubstitute( meta.getBodyField() );
    if ( !Utils.isEmpty( field ) ) {
      data.indexOfBodyField = data.inputRowMeta.indexOfValue( field );
      if ( data.indexOfBodyField < 0 ) {
        throw new KettleException( BaseMessages.getString( PKG, "Rest.Exception.ErrorFindingField", field ) );
      }
      data.useBody = true;
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (RestMeta) smi;
    data = (RestData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.resultFieldName = environmentSubstitute( meta.getFieldName() );
      data.resultCodeFieldName = environmentSubstitute( meta.getResultCodeFieldName() );
      data.resultResponseFieldName = environmentSubstitute( meta.getResponseTimeFieldName() );
      data.resultHeaderFieldName = environmentSubstitute( meta.getResponseHeaderFieldName() );

      // get authentication settings once
      data.realProxyHost = environmentSubstitute( meta.getProxyHost() );
      data.realProxyPort = Const.toInt( environmentSubstitute( meta.getProxyPort() ), 8080 );
      data.realHttpLogin = environmentSubstitute( meta.getHttpLogin() );
      data.realHttpPassword =
        Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( meta.getHttpPassword() ) );

      if ( !meta.isDynamicMethod() ) {
        data.method = environmentSubstitute( meta.getMethod() );
        if ( Utils.isEmpty( data.method ) ) {
          logError( BaseMessages.getString( PKG, "Rest.Error.MethodMissing" ) );
          return false;
        }
      }

      data.trustStoreFile = environmentSubstitute( meta.getTrustStoreFile() );
      data.trustStorePassword =
        Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( meta.getTrustStorePassword() ) );

      calcMediaType();

      try {
        setConfig();
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "Rest.Error.Config" ), e );
        return false;
      }
      return true;
    }
    return false;
  }

  /**
   * Calculate media type based on the application type
   */
  private void calcMediaType() {
    String applicationType = Const.NVL( meta.getApplicationType(), "" );
    switch ( applicationType ) {
      case RestMeta.APPLICATION_TYPE_XML -> data.mediaType = MediaType.APPLICATION_XML_TYPE;
      case RestMeta.APPLICATION_TYPE_JSON -> data.mediaType = MediaType.APPLICATION_JSON_TYPE;
      case RestMeta.APPLICATION_TYPE_OCTET_STREAM -> data.mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
      case RestMeta.APPLICATION_TYPE_XHTML -> data.mediaType = MediaType.APPLICATION_XHTML_XML_TYPE;
      case RestMeta.APPLICATION_TYPE_FORM_URLENCODED -> data.mediaType = MediaType.APPLICATION_FORM_URLENCODED_TYPE;
      case RestMeta.APPLICATION_TYPE_ATOM_XML -> data.mediaType = MediaType.APPLICATION_ATOM_XML_TYPE;
      case RestMeta.APPLICATION_TYPE_SVG_XML -> data.mediaType = MediaType.APPLICATION_SVG_XML_TYPE;
      case RestMeta.APPLICATION_TYPE_TEXT_XML -> data.mediaType = MediaType.TEXT_XML_TYPE;
      default -> data.mediaType = MediaType.TEXT_PLAIN_TYPE;
    }
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (RestMeta) smi;
    data = (RestData) sdi;

    data.config = null;
    data.headerNames = null;
    data.indexOfHeaderFields = null;
    data.paramNames = null;

    super.dispose( smi, sdi );
  }
}
