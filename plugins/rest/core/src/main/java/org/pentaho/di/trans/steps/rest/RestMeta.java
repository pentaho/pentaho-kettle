/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.rest;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
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
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.di.core.annotations.Step;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author Samatar
 * @since 16-jan-2011
 *
 */
@Step( id = "Rest", image = "REST.svg", i18nPackageName = "org.pentaho.di.trans.step.Rest",
  documentationUrl = "mk-95pdia003/pdi-transformation-steps/rest-client-step", name = "Rest.name",
  description = "Rest.description", categoryDescription = "Rest.category" )
public class RestMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = RestMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] APPLICATION_TYPES = new String[] {
    "TEXT PLAIN", "XML", "JSON", "OCTET STREAM", "XHTML", "FORM URLENCODED", "ATOM XML", "SVG XML", "TEXT XML" };
  public static final String APPLICATION_TYPE_TEXT_PLAIN = "TEXT PLAIN";
  public static final String APPLICATION_TYPE_XML = "XML";
  public static final String APPLICATION_TYPE_JSON = "JSON";
  public static final String APPLICATION_TYPE_OCTET_STREAM = "OCTET STREAM";
  public static final String APPLICATION_TYPE_XHTML = "XHTML";
  public static final String APPLICATION_TYPE_FORM_URLENCODED = "FORM URLENCODED";
  public static final String APPLICATION_TYPE_ATOM_XML = "ATOM XML";
  public static final String APPLICATION_TYPE_SVG_XML = "SVG XML";
  public static final String APPLICATION_TYPE_TEXT_XML = "TEXT XML";

  private String applicationType;

  public static final String[] HTTP_METHODS = new String[] { "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH"};

  public static final String HTTP_METHOD_GET = "GET";
  public static final String HTTP_METHOD_POST = "POST";
  public static final String HTTP_METHOD_PUT = "PUT";
  public static final String HTTP_METHOD_DELETE = "DELETE";
  public static final String HTTP_METHOD_HEAD = "HEAD";
  public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
  public static final String HTTP_METHOD_PATCH = "PATCH";

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

  @Deprecated
  public void allocate( int nrheaders, int nrparamers ) {
    allocate( nrheaders, nrparamers, 0 );
  }

  public void allocate( int nrheaders, int nrparamers, int nrmatrixparameters ) {
    headerField = new String[nrheaders];
    headerName = new String[nrheaders];
    parameterField = new String[nrparamers];
    parameterName = new String[nrparamers];
    matrixParameterField = new String[nrmatrixparameters];
    matrixParameterName = new String[nrmatrixparameters];
  }

  @Override
  public Object clone() {
    RestMeta retval = (RestMeta) super.clone();

    int nrheaders = headerName.length;
    int nrparameters = parameterField.length;
    int nrmatrixparameters = matrixParameterField.length;

    retval.allocate( nrheaders, nrparameters, nrmatrixparameters );
    System.arraycopy( headerField, 0, retval.headerField, 0, nrheaders );
    System.arraycopy( headerName, 0, retval.headerName, 0, nrheaders );
    System.arraycopy( parameterField, 0, retval.parameterField, 0, nrparameters );
    System.arraycopy( parameterName, 0, retval.parameterName, 0, nrparameters );
    System.arraycopy( matrixParameterField, 0, retval.matrixParameterField, 0, nrmatrixparameters );
    System.arraycopy( matrixParameterName, 0, retval.matrixParameterName, 0, nrmatrixparameters );

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
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
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
    retval.append( "    " ).append( XMLHandler.addTagValue( "applicationType", applicationType ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "method", method ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "url", url ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "urlInField", urlInField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dynamicMethod", dynamicMethod ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "methodFieldName", methodFieldName ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "urlField", urlField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "bodyField", bodyField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "httpLogin", httpLogin ) );
    retval.append( "    " ).append(
        XMLHandler.addTagValue( "httpPassword", Encr.encryptPasswordIfNotUsingVariables( httpPassword ) ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "proxyHost", proxyHost ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "proxyPort", proxyPort ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "preemptive", preemptive ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "trustStoreFile", trustStoreFile ) );
    retval.append( "    " ).append(
        XMLHandler.addTagValue( "trustStorePassword", Encr.encryptPasswordIfNotUsingVariables( trustStorePassword ) ) );

    retval.append( "    <headers>" ).append( Const.CR );
    for ( int i = 0, len = ( headerName != null ? headerName.length : 0 ); i < len; i++ ) {
      retval.append( "      <header>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", headerField[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", headerName[i] ) );
      retval.append( "        </header>" ).append( Const.CR );
    }
    retval.append( "      </headers>" ).append( Const.CR );

    retval.append( "    <parameters>" ).append( Const.CR );
    for ( int i = 0, len = ( parameterName != null ? parameterName.length : 0 ); i < len; i++ ) {
      retval.append( "      <parameter>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", parameterField[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", parameterName[i] ) );
      retval.append( "        </parameter>" ).append( Const.CR );
    }
    retval.append( "      </parameters>" ).append( Const.CR );

    retval.append( "    <matrixParameters>" ).append( Const.CR );
    for ( int i = 0, len = ( matrixParameterName != null ? matrixParameterName.length : 0 ); i < len; i++ ) {
      retval.append( "      <matrixParameter>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", matrixParameterField[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", matrixParameterName[i] ) );
      retval.append( "        </matrixParameter>" ).append( Const.CR );
    }
    retval.append( "      </matrixParameters>" ).append( Const.CR );

    retval.append( "    <result>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "name", fieldName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "code", resultCodeFieldName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "response_time", responseTimeFieldName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "response_header", responseHeaderFieldName ) );
    retval.append( "      </result>" ).append( Const.CR );

    return retval.toString();
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      applicationType = XMLHandler.getTagValue( stepnode, "applicationType" );
      method = XMLHandler.getTagValue( stepnode, "method" );
      url = XMLHandler.getTagValue( stepnode, "url" );
      urlInField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "urlInField" ) );
      methodFieldName = XMLHandler.getTagValue( stepnode, "methodFieldName" );

      dynamicMethod = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "dynamicMethod" ) );
      urlField = XMLHandler.getTagValue( stepnode, "urlField" );
      bodyField = XMLHandler.getTagValue( stepnode, "bodyField" );
      httpLogin = XMLHandler.getTagValue( stepnode, "httpLogin" );
      httpPassword = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "httpPassword" ) );

      proxyHost = XMLHandler.getTagValue( stepnode, "proxyHost" );
      proxyPort = XMLHandler.getTagValue( stepnode, "proxyPort" );
      preemptive = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "preemptive" ) );

      trustStoreFile = XMLHandler.getTagValue( stepnode, "trustStoreFile" );
      trustStorePassword =
          Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "trustStorePassword" ) );

      Node headernode = XMLHandler.getSubNode( stepnode, "headers" );
      int nrheaders = XMLHandler.countNodes( headernode, "header" );
      Node paramnode = XMLHandler.getSubNode( stepnode, "parameters" );
      int nrparameters = XMLHandler.countNodes( paramnode, "parameter" );
      Node matrixparamnode = XMLHandler.getSubNode( stepnode, "matrixParameters" );
      int nrmatrixparameters = XMLHandler.countNodes( matrixparamnode, "matrixParameter" );

      allocate( nrheaders, nrparameters, nrmatrixparameters );
      for ( int i = 0; i < nrheaders; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( headernode, "header", i );
        headerField[i] = XMLHandler.getTagValue( anode, "field" );
        headerName[i] = XMLHandler.getTagValue( anode, "name" );
      }
      for ( int i = 0; i < nrparameters; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( paramnode, "parameter", i );
        parameterField[i] = XMLHandler.getTagValue( anode, "field" );
        parameterName[i] = XMLHandler.getTagValue( anode, "name" );
      }
      for ( int i = 0; i < nrmatrixparameters; i++ ) {
        Node anode = XMLHandler.getSubNodeByNr( matrixparamnode, "matrixParameter", i );
        matrixParameterField[i] = XMLHandler.getTagValue( anode, "field" );
        matrixParameterName[i] = XMLHandler.getTagValue( anode, "name" );
      }

      fieldName = XMLHandler.getTagValue( stepnode, "result", "name" ); // Optional, can be null
      resultCodeFieldName = XMLHandler.getTagValue( stepnode, "result", "code" ); // Optional, can be null
      responseTimeFieldName = XMLHandler.getTagValue( stepnode, "result", "response_time" ); // Optional, can be null
      responseHeaderFieldName = XMLHandler.getTagValue( stepnode, "result", "response_header" ); // Optional, can be null
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "RestMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      applicationType = rep.getStepAttributeString( id_step, "applicationType" );
      method = rep.getStepAttributeString( id_step, "method" );
      url = rep.getStepAttributeString( id_step, "url" );
      urlInField = rep.getStepAttributeBoolean( id_step, "urlInField" );

      methodFieldName = rep.getStepAttributeString( id_step, "methodFieldName" );
      dynamicMethod = rep.getStepAttributeBoolean( id_step, "dynamicMethod" );
      urlField = rep.getStepAttributeString( id_step, "urlField" );
      bodyField = rep.getStepAttributeString( id_step, "bodyField" );
      httpLogin = rep.getStepAttributeString( id_step, "httpLogin" );
      httpPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "httpPassword" ) );

      proxyHost = rep.getStepAttributeString( id_step, "proxyHost" );
      proxyPort = rep.getStepAttributeString( id_step, "proxyPort" );

      trustStoreFile = rep.getStepAttributeString( id_step, "trustStoreFile" );
      trustStorePassword =
          Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "trustStorePassword" ) );

      preemptive = rep.getStepAttributeBoolean( id_step, "preemptive" );
      int nrheaders = rep.countNrStepAttributes( id_step, "header_field" );
      int nrparams = rep.countNrStepAttributes( id_step, "parameter_field" );
      int nrmatrixparams = rep.countNrStepAttributes( id_step, "matrix_parameter_field" );
      allocate( nrheaders, nrparams, nrmatrixparams );

      for ( int i = 0; i < nrheaders; i++ ) {
        headerField[i] = rep.getStepAttributeString( id_step, i, "header_field" );
        headerName[i] = rep.getStepAttributeString( id_step, i, "header_name" );
      }
      for ( int i = 0; i < nrparams; i++ ) {
        parameterField[i] = rep.getStepAttributeString( id_step, i, "parameter_field" );
        parameterName[i] = rep.getStepAttributeString( id_step, i, "parameter_name" );
      }
      for ( int i = 0; i < nrmatrixparams; i++ ) {
        matrixParameterField[i] = rep.getStepAttributeString( id_step, i, "matrix_parameter_field" );
        matrixParameterName[i] = rep.getStepAttributeString( id_step, i, "matrix_parameter_name" );
      }

      fieldName = rep.getStepAttributeString( id_step, "result_name" );
      resultCodeFieldName = rep.getStepAttributeString( id_step, "result_code" );
      responseTimeFieldName = rep.getStepAttributeString( id_step, "response_time" );
      responseHeaderFieldName = rep.getStepAttributeString( id_step, "response_header" );
    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "RestMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "applicationType", applicationType );
      rep.saveStepAttribute( id_transformation, id_step, "method", method );
      rep.saveStepAttribute( id_transformation, id_step, "url", url );
      rep.saveStepAttribute( id_transformation, id_step, "methodFieldName", methodFieldName );

      rep.saveStepAttribute( id_transformation, id_step, "dynamicMethod", dynamicMethod );
      rep.saveStepAttribute( id_transformation, id_step, "urlInField", urlInField );
      rep.saveStepAttribute( id_transformation, id_step, "urlField", urlField );
      rep.saveStepAttribute( id_transformation, id_step, "bodyField", bodyField );
      rep.saveStepAttribute( id_transformation, id_step, "httpLogin", httpLogin );
      rep.saveStepAttribute( id_transformation, id_step, "httpPassword", Encr
        .encryptPasswordIfNotUsingVariables( httpPassword ) );

      rep.saveStepAttribute( id_transformation, id_step, "proxyHost", proxyHost );
      rep.saveStepAttribute( id_transformation, id_step, "proxyPort", proxyPort );

      rep.saveStepAttribute( id_transformation, id_step, "trustStoreFile", trustStoreFile );
      rep.saveStepAttribute( id_transformation, id_step, "trustStorePassword", Encr
          .encryptPasswordIfNotUsingVariables( trustStorePassword ) );

      rep.saveStepAttribute( id_transformation, id_step, "preemptive", preemptive );
      for ( int i = 0; i < headerName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "header_field", headerField[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "header_name", headerName[i] );
      }
      for ( int i = 0; i < parameterField.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "parameter_field", parameterField[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "parameter_name", parameterName[i] );
      }
      for ( int i = 0; i < matrixParameterField.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "matrix_parameter_field", matrixParameterField[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "matrix_parameter_name", matrixParameterName[i] );
      }
      rep.saveStepAttribute( id_transformation, id_step, "result_name", fieldName );
      rep.saveStepAttribute( id_transformation, id_step, "result_code", resultCodeFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "response_time", responseTimeFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "response_header", responseHeaderFieldName );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "RestMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
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
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "RestMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "RestMeta.CheckResult.NoInpuReceived" ), stepMeta );
    }
    remarks.add( cr );

    // check Url
    if ( urlInField ) {
      if ( Utils.isEmpty( urlField ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.UrlfieldMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.UrlfieldOk" ), stepMeta );
      }

    } else {
      if ( Utils.isEmpty( url ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.UrlMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages
            .getString( PKG, "RestMeta.CheckResult.UrlOk" ), stepMeta );
      }
    }
    remarks.add( cr );

    // Check method
    if ( dynamicMethod ) {
      if ( Utils.isEmpty( methodFieldName ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.MethodFieldMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.MethodFieldOk" ), stepMeta );
      }

    } else {
      if ( Utils.isEmpty( method ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RestMeta.CheckResult.MethodMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
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
