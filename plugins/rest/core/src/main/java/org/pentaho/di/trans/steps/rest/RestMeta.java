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

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepHelperInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author Samatar
 * @since 16-jan-2011
 *
 */
@Step( id = "Rest", image = "REST.svg", i18nPackageName = "org.pentaho.di.trans.step.Rest",
  documentationUrl = "pdi-transformation-steps-reference-overview/rest-client-step", name = "Rest.name",
  description = "Rest.description", categoryDescription = "Rest.category" )
public class RestMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = RestMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String TAG_APPLICATION_TYPE = "applicationType";
  private static final String TAG_BODY_FIELD = "bodyField";
  private static final String TAG_CODE = "code";
  private static final String TAG_DYNAMIC_METHOD = "dynamicMethod";
  private static final String TAG_FIELD = "field";
  private static final String TAG_HEADER = "header";
  private static final String TAG_HEADER_FIELD = "header_field";
  private static final String TAG_HEADER_NAME = "header_name";
  private static final String TAG_HEADERS = "headers";
  private static final String TAG_HTTP_LOGIN = "httpLogin";
  private static final String TAG_HTTP_PASSWORD = "httpPassword";
  private static final String TAG_IGNORE_SSL = "ignoreSsl";
  private static final String TAG_MATRIX_PARAMETER = "matrixParameter";
  private static final String TAG_MATRIX_PARAMETER_FIELD = "matrix_parameter_field";
  private static final String TAG_MATRIX_PARAMETER_NAME = "matrix_parameter_name";
  private static final String TAG_MATRIX_PARAMETERS = "matrixParameters";
  private static final String TAG_METHOD = "method";
  private static final String TAG_METHOD_FIELD_NAME = "methodFieldName";
  private static final String TAG_NAME = "name";
  private static final String TAG_PARAMETER = "parameter";
  private static final String TAG_PARAMETER_FIELD = "parameter_field";
  private static final String TAG_PARAMETER_NAME = "parameter_name";
  private static final String TAG_PARAMETERS = "parameters";
  private static final String TAG_PREEMPTIVE = "preemptive";
  private static final String TAG_PROXY_HOST = "proxyHost";
  private static final String TAG_PROXY_PORT = "proxyPort";
  private static final String TAG_RESPONSE_HEADER = "response_header";
  private static final String TAG_RESPONSE_TIME = "response_time";
  private static final String TAG_RESULT = "result";
  private static final String TAG_RESULT_CODE = "result_code";
  private static final String TAG_RESULT_NAME = "result_name";
  private static final String TAG_TRUST_STORE_FILE = "trustStoreFile";
  private static final String TAG_TRUST_STORE_PASSWORD = "trustStorePassword";
  private static final String TAG_URL = "url";
  private static final String TAG_URL_FIELD = "urlField";
  private static final String TAG_URL_IN_FIELD = "urlInField";
  private static final String TAG_SPACES8 = "        ";
  private static final String TAG_SPACES6 = "      ";
  private static final String TAG_SPACES4 = "    ";

  public static final String APPLICATION_TYPE_TEXT_PLAIN = "TEXT PLAIN";
  public static final String APPLICATION_TYPE_XML = "XML";
  public static final String APPLICATION_TYPE_JSON = "JSON";
  public static final String APPLICATION_TYPE_OCTET_STREAM = "OCTET STREAM";
  public static final String APPLICATION_TYPE_XHTML = "XHTML";
  public static final String APPLICATION_TYPE_FORM_URLENCODED = "FORM URLENCODED";
  public static final String APPLICATION_TYPE_ATOM_XML = "ATOM XML";
  public static final String APPLICATION_TYPE_SVG_XML = "SVG XML";
  public static final String APPLICATION_TYPE_TEXT_XML = "TEXT XML";
  public static final String[] APPLICATION_TYPES =
    new String[] { APPLICATION_TYPE_TEXT_PLAIN, APPLICATION_TYPE_XML, APPLICATION_TYPE_JSON,
      APPLICATION_TYPE_OCTET_STREAM, APPLICATION_TYPE_XHTML, APPLICATION_TYPE_FORM_URLENCODED,
      APPLICATION_TYPE_ATOM_XML, APPLICATION_TYPE_SVG_XML, APPLICATION_TYPE_TEXT_XML };

  private String applicationType;

  public static final String HTTP_METHOD_GET = "GET";
  public static final String HTTP_METHOD_POST = "POST";
  public static final String HTTP_METHOD_PUT = "PUT";
  public static final String HTTP_METHOD_DELETE = "DELETE";
  public static final String HTTP_METHOD_HEAD = "HEAD";
  public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
  public static final String HTTP_METHOD_PATCH = "PATCH";
  public static final String[] HTTP_METHODS =
    new String[] { HTTP_METHOD_GET, HTTP_METHOD_POST, HTTP_METHOD_PUT, HTTP_METHOD_DELETE, HTTP_METHOD_HEAD,
      HTTP_METHOD_OPTIONS, HTTP_METHOD_PATCH };

  /** URL / service to be called */
  private String url;
  private boolean urlInField;
  private String urlField;

  /** headers name */
  private String[] headerField;
  private String[] headerName;

  /** Query parameters name */
  private String[] parameterField;
  private String[] parameterName;

  /** Matrix parameters name */
  private String[] matrixParameterField;
  private String[] matrixParameterName;

  /** function result: new value name */
  private String fieldName;
  private String resultCodeFieldName;
  private String responseTimeFieldName;
  private String responseHeaderFieldName;

  /** proxy **/
  private String proxyHost;
  private String proxyPort;
  private String httpLogin;
  private String httpPassword;
  private boolean preemptive;

  /** Body fieldname **/
  private String bodyField;

  /** HTTP Method **/
  private String method;
  private boolean dynamicMethod;
  private String methodFieldName;

  /** Trust store **/
  private String trustStoreFile;
  private String trustStorePassword;

  private boolean ignoreSsl;
  public RestMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the method.
   */
  public String getMethod() {
    return method;
  }

  /**
   * @param value
   *          The method to set.
   */
  public void setMethod( String value ) {
    this.method = value;
  }

  /**
   * @return Returns the bodyField.
   */
  public String getBodyField() {
    return bodyField;
  }

  /**
   * @param value
   *          The bodyField to set.
   */
  public void setBodyField( String value ) {
    this.bodyField = value;
  }

  /**
   * @return Returns the headerName.
   */
  public String[] getHeaderName() {
    return headerName;
  }

  /**
   * @param value
   *          The headerName to set.
   */
  public void setHeaderName( String[] value ) {
    this.headerName = value;
  }

  /**
   * @return Returns the parameterField.
   */
  public String[] getParameterField() {
    return parameterField;
  }

  /**
   * @param value
   *          The parameterField to set.
   */
  public void setParameterField( String[] value ) {
    this.parameterField = value;
  }

  /**
   * @return Returns the parameterName.
   */
  public String[] getParameterName() {
    return parameterName;
  }

  /**
   * @param value
   *          The parameterName to set.
   */
  public void setParameterName( String[] value ) {
    this.parameterName = value;
  }

  /**
   * @return Returns the matrixParameterField.
   */
  public String[] getMatrixParameterField() {
    return matrixParameterField;
  }

  /**
   * @param value
   *          The matrixParameterField to set.
   */
  public void setMatrixParameterField( String[] value ) {
    this.matrixParameterField = value;
  }

  /**
   * @return Returns the matrixParameterName.
   */
  public String[] getMatrixParameterName() {
    return matrixParameterName;
  }

  /**
   * @param value
   *          The matrixParameterName to set.
   */
  public void setMatrixParameterName( String[] value ) {
    this.matrixParameterName = value;
  }

  /**
   * @return Returns the headerField.
   */
  public String[] getHeaderField() {
    return headerField;
  }

  /**
   * @param value
   *          The headerField to set.
   */
  public void setHeaderField( String[] value ) {
    this.headerField = value;
  }

  /**
   * @return Returns the procedure.
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param procedure
   *          The procedure to set.
   */
  public void setUrl( String procedure ) {
    this.url = procedure;
  }

  /**
   * @return Is the url coded in a field?
   */
  public boolean isUrlInField() {
    return urlInField;
  }

  /**
   * @param urlInField
   *          Is the url coded in a field?
   */
  public void setUrlInField( boolean urlInField ) {
    this.urlInField = urlInField;
  }

  /**
   * @return Is preemptive?
   */
  public boolean isPreemptive() {
    return preemptive;
  }

  /**
   * @param preemptive
   *          Ispreemptive?
   */
  public void setPreemptive( boolean preemptive ) {
    this.preemptive = preemptive;
  }

  /**
   * @return Is the method defined in a field?
   */
  public boolean isDynamicMethod() {
    return dynamicMethod;
  }

  /**
   * @param dynamicMethod
   *          If the method is defined in a field?
   */
  public void setDynamicMethod( boolean dynamicMethod ) {
    this.dynamicMethod = dynamicMethod;
  }

  /**
   * @return methodFieldName
   */
  public String getMethodFieldName() {
    return methodFieldName;
  }

  /**
   * @param methodFieldName
   */
  public void setMethodFieldName( String methodFieldName ) {
    this.methodFieldName = methodFieldName;
  }

  /**
   * @return The field name that contains the url.
   */
  public String getUrlField() {
    return urlField;
  }

  /**
   * @param urlField
   *          name of the field that contains the url
   */
  public void setUrlField( String urlField ) {
    this.urlField = urlField;
  }

  /**
   * @return Returns the resultName.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @param resultName
   *          The resultName to set.
   */
  public void setFieldName( String resultName ) {
    this.fieldName = resultName;
  }

  public boolean isIgnoreSsl() {
    return ignoreSsl;
  }

  public void setIgnoreSsl(boolean ignoreSsl) {
    this.ignoreSsl = ignoreSsl;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  /**
   * Allocate internal structures according to the number of headers and parameters.
   *
   * @param nrHeaders    number of headers
   * @param nrParameters number of parameters
   * @deprecated use #allocate(int nrHeaders, int nrParameters, int nrMatrixParameters) instead
   */
  @Deprecated
  public void allocate( int nrHeaders, int nrParameters ) {
    allocate( nrHeaders, nrParameters, 0 );
  }

  /**
   * Allocate internal structures according to the given components' sizes
   *
   * @param nrHeaders number of headers
   * @param nrParameters number of parameters
   * @param nrMatrixParameters number of matrix parameters
   */
  public void allocate( int nrHeaders, int nrParameters, int nrMatrixParameters ) {
    headerField = new String[ nrHeaders ];
    headerName = new String[ nrHeaders ];
    parameterField = new String[ nrParameters ];
    parameterName = new String[ nrParameters ];
    matrixParameterField = new String[ nrMatrixParameters ];
    matrixParameterName = new String[ nrMatrixParameters ];
  }

  @Override
  public Object clone() {
    RestMeta retval = (RestMeta) super.clone();

    int nrHeaders = headerName.length;
    int nrParameters = parameterField.length;
    int nrMatrixParameters = matrixParameterField.length;

    retval.allocate( nrHeaders, nrParameters, nrMatrixParameters );
    System.arraycopy( headerField, 0, retval.headerField, 0, nrHeaders );
    System.arraycopy( headerName, 0, retval.headerName, 0, nrHeaders );
    System.arraycopy( parameterField, 0, retval.parameterField, 0, nrParameters );
    System.arraycopy( parameterName, 0, retval.parameterName, 0, nrParameters );
    System.arraycopy( matrixParameterField, 0, retval.matrixParameterField, 0, nrMatrixParameters );
    System.arraycopy( matrixParameterName, 0, retval.matrixParameterName, 0, nrMatrixParameters );

    return retval;
  }

  @Override
  public void setDefault() {
    allocate( 0, 0, 0 );

    this.fieldName = "result";
    this.resultCodeFieldName = "";
    this.responseTimeFieldName = "";
    this.responseHeaderFieldName = "";
    this.method = HTTP_METHOD_GET;
    this.dynamicMethod = false;
    this.methodFieldName = null;
    this.preemptive = false;
    this.trustStoreFile = null;
    this.trustStorePassword = null;
    this.applicationType = APPLICATION_TYPE_TEXT_PLAIN;
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info,
    StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( !Utils.isEmpty( fieldName ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( fieldName ) );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }

    if ( !Utils.isEmpty( resultCodeFieldName ) ) {
      ValueMetaInterface v =
        new ValueMetaInteger( space.environmentSubstitute( resultCodeFieldName ) );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }
    if ( !Utils.isEmpty( responseTimeFieldName ) ) {
      ValueMetaInterface v =
        new ValueMetaInteger( space.environmentSubstitute( responseTimeFieldName ) );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }
    String headerFieldName = space.environmentSubstitute( responseHeaderFieldName );
    if ( !Utils.isEmpty( headerFieldName ) ) {
      ValueMetaInterface v =
        new ValueMetaString( headerFieldName );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_APPLICATION_TYPE, applicationType ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_METHOD, method ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_URL, url ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_URL_IN_FIELD, urlInField ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_DYNAMIC_METHOD, dynamicMethod ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_METHOD_FIELD_NAME, methodFieldName ) );

    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_URL_FIELD, urlField ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_BODY_FIELD, bodyField ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_HTTP_LOGIN, httpLogin ) );
    retval.append( TAG_SPACES4 ).append(
        XMLHandler.addTagValue( TAG_HTTP_PASSWORD, Encr.encryptPasswordIfNotUsingVariables( httpPassword ) ) );

    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_PROXY_HOST, proxyHost ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_PROXY_PORT, proxyPort ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_PREEMPTIVE, preemptive ) );

    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_TRUST_STORE_FILE, trustStoreFile ) );
    retval.append( TAG_SPACES4 ).append( XMLHandler.addTagValue( TAG_IGNORE_SSL, ignoreSsl ) );
    retval.append( TAG_SPACES4 ).append(
        XMLHandler.addTagValue( TAG_TRUST_STORE_PASSWORD, Encr.encryptPasswordIfNotUsingVariables( trustStorePassword ) ) );

    retval.append( "    <headers>" ).append( Const.CR );
    for ( int i = 0, len = ( headerName != null ? headerName.length : 0 ); i < len; i++ ) {
      retval.append( "      <header>" ).append( Const.CR );
      retval.append( TAG_SPACES8 ).append( XMLHandler.addTagValue( TAG_FIELD, headerField[i] ) );
      retval.append( TAG_SPACES8 ).append( XMLHandler.addTagValue( TAG_NAME, headerName[i] ) );
      retval.append( "        </header>" ).append( Const.CR );
    }
    retval.append( "      </headers>" ).append( Const.CR );

    retval.append( "    <parameters>" ).append( Const.CR );
    for ( int i = 0, len = ( parameterName != null ? parameterName.length : 0 ); i < len; i++ ) {
      retval.append( "      <parameter>" ).append( Const.CR );
      retval.append( TAG_SPACES8 ).append( XMLHandler.addTagValue( TAG_FIELD, parameterField[i] ) );
      retval.append( TAG_SPACES8 ).append( XMLHandler.addTagValue( TAG_NAME, parameterName[i] ) );
      retval.append( "        </parameter>" ).append( Const.CR );
    }
    retval.append( "      </parameters>" ).append( Const.CR );

    retval.append( "    <matrixParameters>" ).append( Const.CR );
    for ( int i = 0, len = ( matrixParameterName != null ? matrixParameterName.length : 0 ); i < len; i++ ) {
      retval.append( "      <matrixParameter>" ).append( Const.CR );
      retval.append( TAG_SPACES8 ).append( XMLHandler.addTagValue( TAG_FIELD, matrixParameterField[i] ) );
      retval.append( TAG_SPACES8 ).append( XMLHandler.addTagValue( TAG_NAME, matrixParameterName[i] ) );
      retval.append( "        </matrixParameter>" ).append( Const.CR );
    }
    retval.append( "      </matrixParameters>" ).append( Const.CR );

    retval.append( "    <result>" ).append( Const.CR );
    retval.append( TAG_SPACES6 ).append( XMLHandler.addTagValue( TAG_NAME, fieldName ) );
    retval.append( TAG_SPACES6 ).append( XMLHandler.addTagValue( TAG_CODE, resultCodeFieldName ) );
    retval.append( TAG_SPACES6 ).append( XMLHandler.addTagValue( TAG_RESPONSE_TIME, responseTimeFieldName ) );
    retval.append( TAG_SPACES6 ).append( XMLHandler.addTagValue( TAG_RESPONSE_HEADER, responseHeaderFieldName ) );
    retval.append( "      </result>" ).append( Const.CR );

    return retval.toString();
  }

  private void readData( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      applicationType = XMLHandler.getTagValue( stepnode, TAG_APPLICATION_TYPE );
      method = XMLHandler.getTagValue( stepnode, TAG_METHOD );
      url = XMLHandler.getTagValue( stepnode, TAG_URL );
      urlInField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_URL_IN_FIELD ) );
      methodFieldName = XMLHandler.getTagValue( stepnode, TAG_METHOD_FIELD_NAME );

      dynamicMethod = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_DYNAMIC_METHOD ) );
      urlField = XMLHandler.getTagValue( stepnode, TAG_URL_FIELD );
      bodyField = XMLHandler.getTagValue( stepnode, TAG_BODY_FIELD );
      httpLogin = XMLHandler.getTagValue( stepnode, TAG_HTTP_LOGIN );
      httpPassword = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, TAG_HTTP_PASSWORD ) );

      proxyHost = XMLHandler.getTagValue( stepnode, TAG_PROXY_HOST );
      proxyPort = XMLHandler.getTagValue( stepnode, TAG_PROXY_PORT );
      preemptive = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_PREEMPTIVE ) );

      ignoreSsl = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_IGNORE_SSL ) );
      trustStoreFile = XMLHandler.getTagValue( stepnode, TAG_TRUST_STORE_FILE );
      trustStorePassword =
          Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, TAG_TRUST_STORE_PASSWORD ) );

      Node headerNode = XMLHandler.getSubNode( stepnode, TAG_HEADERS );
      int nrHeaders = XMLHandler.countNodes( headerNode, TAG_HEADER );
      Node paramNode = XMLHandler.getSubNode( stepnode, TAG_PARAMETERS );
      int nrParameters = XMLHandler.countNodes( paramNode, TAG_PARAMETER );
      Node matrixParametersNode = XMLHandler.getSubNode( stepnode, TAG_MATRIX_PARAMETERS );
      int nrMatrixParameters = XMLHandler.countNodes( matrixParametersNode, TAG_MATRIX_PARAMETER );

      allocate( nrHeaders, nrParameters, nrMatrixParameters );
      for ( int i = 0; i < nrHeaders; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( headerNode, TAG_HEADER, i );
        headerField[i] = XMLHandler.getTagValue( anode, TAG_FIELD );
        headerName[i] = XMLHandler.getTagValue( anode, TAG_NAME );
      }
      for ( int i = 0; i < nrParameters; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( paramNode, TAG_PARAMETER, i );
        parameterField[i] = XMLHandler.getTagValue( anode, TAG_FIELD );
        parameterName[i] = XMLHandler.getTagValue( anode, TAG_NAME );
      }
      for ( int i = 0; i < nrMatrixParameters; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( matrixParametersNode, TAG_MATRIX_PARAMETER, i );
        matrixParameterField[i] = XMLHandler.getTagValue( anode, TAG_FIELD );
        matrixParameterName[i] = XMLHandler.getTagValue( anode, TAG_NAME );
      }

      fieldName = XMLHandler.getTagValue( stepnode, TAG_RESULT, TAG_NAME ); // Optional, can be null
      resultCodeFieldName = XMLHandler.getTagValue( stepnode, TAG_RESULT, TAG_CODE ); // Optional, can be null
      responseTimeFieldName = XMLHandler.getTagValue( stepnode, TAG_RESULT, TAG_RESPONSE_TIME ); // Optional, can be null
      responseHeaderFieldName = XMLHandler.getTagValue( stepnode, TAG_RESULT, TAG_RESPONSE_HEADER ); // Optional, can be null
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "RestMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases ) throws KettleException {
    try {
      applicationType = rep.getStepAttributeString( idStep, TAG_APPLICATION_TYPE );
      method = rep.getStepAttributeString( idStep, TAG_METHOD );
      url = rep.getStepAttributeString( idStep, TAG_URL );
      urlInField = rep.getStepAttributeBoolean( idStep, TAG_URL_IN_FIELD );

      methodFieldName = rep.getStepAttributeString( idStep, TAG_METHOD_FIELD_NAME );
      dynamicMethod = rep.getStepAttributeBoolean( idStep, TAG_DYNAMIC_METHOD );
      urlField = rep.getStepAttributeString( idStep, TAG_URL_FIELD );
      bodyField = rep.getStepAttributeString( idStep, TAG_BODY_FIELD );
      httpLogin = rep.getStepAttributeString( idStep, TAG_HTTP_LOGIN );
      httpPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( idStep, TAG_HTTP_PASSWORD ) );

      proxyHost = rep.getStepAttributeString( idStep, TAG_PROXY_HOST );
      proxyPort = rep.getStepAttributeString( idStep, TAG_PROXY_PORT );

      trustStoreFile = rep.getStepAttributeString( idStep, TAG_TRUST_STORE_FILE );
      ignoreSsl = "Y".equalsIgnoreCase( rep.getStepAttributeString( idStep, TAG_IGNORE_SSL ) );
      trustStorePassword =
          Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( idStep, TAG_TRUST_STORE_PASSWORD ) );

      preemptive = rep.getStepAttributeBoolean( idStep, TAG_PREEMPTIVE );
      int nrHeaders = rep.countNrStepAttributes( idStep, TAG_HEADER_FIELD );
      int nrParams = rep.countNrStepAttributes( idStep, TAG_PARAMETER_FIELD );
      int nrMatrixParams = rep.countNrStepAttributes( idStep, TAG_MATRIX_PARAMETER_FIELD );
      allocate( nrHeaders, nrParams, nrMatrixParams );

      for ( int i = 0; i < nrHeaders; i++ ) {
        headerField[i] = rep.getStepAttributeString( idStep, i, TAG_HEADER_FIELD );
        headerName[i] = rep.getStepAttributeString( idStep, i, TAG_HEADER_NAME );
      }
      for ( int i = 0; i < nrParams; i++ ) {
        parameterField[i] = rep.getStepAttributeString( idStep, i, TAG_PARAMETER_FIELD );
        parameterName[i] = rep.getStepAttributeString( idStep, i, TAG_PARAMETER_NAME );
      }
      for ( int i = 0; i < nrMatrixParams; i++ ) {
        matrixParameterField[i] = rep.getStepAttributeString( idStep, i, TAG_MATRIX_PARAMETER_FIELD );
        matrixParameterName[i] = rep.getStepAttributeString( idStep, i, TAG_MATRIX_PARAMETER_NAME );
      }

      fieldName = rep.getStepAttributeString( idStep, TAG_RESULT_NAME );
      resultCodeFieldName = rep.getStepAttributeString( idStep, TAG_RESULT_CODE );
      responseTimeFieldName = rep.getStepAttributeString( idStep, TAG_RESPONSE_TIME );
      responseHeaderFieldName = rep.getStepAttributeString( idStep, TAG_RESPONSE_HEADER );
    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "RestMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep ) throws KettleException {
    try {
      rep.saveStepAttribute( idTransformation, idStep, TAG_APPLICATION_TYPE, applicationType );
      rep.saveStepAttribute( idTransformation, idStep, TAG_METHOD, method );
      rep.saveStepAttribute( idTransformation, idStep, TAG_URL, url );
      rep.saveStepAttribute( idTransformation, idStep, TAG_METHOD_FIELD_NAME, methodFieldName );

      rep.saveStepAttribute( idTransformation, idStep, TAG_DYNAMIC_METHOD, dynamicMethod );
      rep.saveStepAttribute( idTransformation, idStep, TAG_URL_IN_FIELD, urlInField );
      rep.saveStepAttribute( idTransformation, idStep, TAG_URL_FIELD, urlField );
      rep.saveStepAttribute( idTransformation, idStep, TAG_BODY_FIELD, bodyField );
      rep.saveStepAttribute( idTransformation, idStep, TAG_HTTP_LOGIN, httpLogin );
      rep.saveStepAttribute( idTransformation, idStep, TAG_HTTP_PASSWORD, Encr
        .encryptPasswordIfNotUsingVariables( httpPassword ) );

      rep.saveStepAttribute( idTransformation, idStep, TAG_PROXY_HOST, proxyHost );
      rep.saveStepAttribute( idTransformation, idStep, TAG_PROXY_PORT, proxyPort );

      rep.saveStepAttribute( idTransformation, idStep, TAG_IGNORE_SSL, ignoreSsl );
      rep.saveStepAttribute( idTransformation, idStep, TAG_TRUST_STORE_FILE, trustStoreFile );
      rep.saveStepAttribute( idTransformation, idStep, TAG_TRUST_STORE_PASSWORD, Encr
          .encryptPasswordIfNotUsingVariables( trustStorePassword ) );

      rep.saveStepAttribute( idTransformation, idStep, TAG_PREEMPTIVE, preemptive );
      for ( int i = 0; i < headerName.length; i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_HEADER_FIELD, headerField[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_HEADER_NAME, headerName[i] );
      }
      for ( int i = 0; i < parameterField.length; i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_PARAMETER_FIELD, parameterField[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_PARAMETER_NAME, parameterName[i] );
      }
      for ( int i = 0; i < matrixParameterField.length; i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_MATRIX_PARAMETER_FIELD, matrixParameterField[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, TAG_MATRIX_PARAMETER_NAME, matrixParameterName[i] );
      }
      rep.saveStepAttribute( idTransformation, idStep, TAG_RESULT_NAME, fieldName );
      rep.saveStepAttribute( idTransformation, idStep, TAG_RESULT_CODE, resultCodeFieldName );
      rep.saveStepAttribute( idTransformation, idStep, TAG_RESPONSE_TIME, responseTimeFieldName );
      rep.saveStepAttribute( idTransformation, idStep, TAG_RESPONSE_HEADER, responseHeaderFieldName );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "RestMeta.Exception.UnableToSaveStepInfo" )
        + idStep, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "RestMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "RestMeta.CheckResult.NoInpuReceived" ), stepMeta );
    }
    remarks.add( cr );

    // check Url
    if ( urlInField ) {
      if ( Utils.isEmpty( urlField ) ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.UrlfieldMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.UrlfieldOk" ), stepMeta );
      }

    } else {
      if ( Utils.isEmpty( url ) ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.UrlMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages
            .getString( PKG, "RestMeta.CheckResult.UrlOk" ), stepMeta );
      }
    }
    remarks.add( cr );

    // Check method
    if ( dynamicMethod ) {
      if ( Utils.isEmpty( methodFieldName ) ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.MethodFieldMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.MethodFieldOk" ), stepMeta );
      }

    } else {
      if ( Utils.isEmpty( method ) ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.MethodMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.MethodOk" ), stepMeta );
      }
    }
    remarks.add( cr );

  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new Rest( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new RestData();
  }

  @Override
  public StepHelperInterface getStepHelperInterface() {
    return new RestHelper();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * @return the resultCodeFieldName
   */
  public String getResultCodeFieldName() {
    return resultCodeFieldName;
  }

  /**
   * @param resultCodeFieldName
   *          the resultCodeFieldName to set
   */
  public void setResultCodeFieldName( String resultCodeFieldName ) {
    this.resultCodeFieldName = resultCodeFieldName;
  }

  /**
   * Setter
   *
   * @param proxyHost
   */
  public void setProxyHost( String proxyHost ) {
    this.proxyHost = proxyHost;
  }

  /**
   * Getter
   *
   * @return
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * Setter
   *
   * @param proxyPort
   */
  public void setProxyPort( String proxyPort ) {
    this.proxyPort = proxyPort;
  }

  /**
   * Getter
   *
   * @return
   */
  public String getProxyPort() {
    return this.proxyPort;
  }

  /**
   * Setter
   *
   * @param applicationType
   */
  public void setApplicationType( String applicationType ) {
    this.applicationType = applicationType;
  }

  /**
   * Getter
   *
   * @return
   */
  public String getApplicationType() {
    return applicationType;
  }

  /**
   * Setter
   *
   * @param httpLogin
   */
  public void setHttpLogin( String httpLogin ) {
    this.httpLogin = httpLogin;
  }

  /**
   * Getter
   *
   * @return
   */
  public String getHttpLogin() {
    return httpLogin;
  }

  /**
   * Setter
   *
   * @param httpPassword
   */
  public void setHttpPassword( String httpPassword ) {
    this.httpPassword = httpPassword;
  }

  /**
   *
   * @return
   */
  public String getHttpPassword() {
    return httpPassword;
  }

  /**
   * Setter
   *
   * @param trustStoreFile
   */
  public void setTrustStoreFile( String trustStoreFile ) {
    this.trustStoreFile = trustStoreFile;
  }

  /**
   *
   * @return trustStoreFile
   */
  public String getTrustStoreFile() {
    return trustStoreFile;
  }

  /**
   * Setter
   *
   * @param trustStorePassword
   */
  public void setTrustStorePassword( String trustStorePassword ) {
    this.trustStorePassword = trustStorePassword;
  }

  /**
   *
   * @return trustStorePassword
   */
  public String getTrustStorePassword() {
    return trustStorePassword;
  }

  public String getResponseTimeFieldName() {
    return responseTimeFieldName;
  }

  public void setResponseTimeFieldName( String responseTimeFieldName ) {
    this.responseTimeFieldName = responseTimeFieldName;
  }
  public String getResponseHeaderFieldName() {
    return responseHeaderFieldName;
  }

  public void setResponseHeaderFieldName( String responseHeaderFieldName ) {
    this.responseHeaderFieldName = responseHeaderFieldName;
  }

  public static boolean isActiveBody( String method ) {
    if ( Utils.isEmpty( method ) ) {
      return false;
    }
    return ( method.equals( HTTP_METHOD_POST ) || method.equals( HTTP_METHOD_PUT ) || method.equals( HTTP_METHOD_PATCH ) );
  }

  public static boolean isActiveParameters( String method ) {
    if ( Utils.isEmpty( method ) ) {
      return false;
    }
    return ( method.equals( HTTP_METHOD_POST ) || method.equals( HTTP_METHOD_PUT )
      || method.equals( HTTP_METHOD_PATCH ) || method.equals( HTTP_METHOD_DELETE ) );
  }
}
