/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.checksum;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 30-06-2008
 *
 * @author Samatar Hassan
 */
@Step( id = "CheckSum", i18nPackageName = "org.pentaho.di.trans.steps.checksum", name = "CheckSum.Name",
    description = "CheckSum.Description",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform" )
@InjectionSupported( localizationPrefix = "CheckSum.Injection.", groups = { "FIELDS" }  )
public class CheckSumMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = CheckSumMeta.class; // for i18n purposes, needed by Translator2!!

  //
  // XML tags
  //

  protected static final String XML_TAG_CHECKSUMTYPE = "checksumtype";
  protected static final String XML_TAG_FIELD_SEPARATOR_STRING = "fieldSeparatorString";
  protected static final String XML_TAG_RESULTFIELD_NAME = "resultfieldName";
  protected static final String XML_TAG_RESULT_TYPE = "resultType";
  protected static final String XML_TAG_COMPATIBILITY_MODE = "compatibilityMode";
  protected static final String XML_TAG_OLD_CHECKSUM_BEHAVIOUR = "oldChecksumBehaviour";
  protected static final String XML_TAG_EVALUATION_METHOD = "evaluationMethod";
  protected static final String XML_TAG_FIELDS = "fields";
  protected static final String XML_TAG_FIELD = "field";
  protected static final String XML_TAG_NAME = "name";
  protected static final String XML_TAG_FIELD_NAME = "field_name";

  //
  // Checksum types
  //

  public static final String TYPE_CRC32 = "CRC32";
  public static final String TYPE_ADLER32 = "ADLER32";
  public static final String TYPE_MD5 = "MD5";
  public static final String TYPE_SHA1 = "SHA-1";
  public static final String TYPE_SHA256 = "SHA-256";

  public static final String[] checksumtypeCodes = { TYPE_CRC32, TYPE_ADLER32, TYPE_MD5, TYPE_SHA1, TYPE_SHA256 };
  public static final String[] checksumtypeDescs = {
    BaseMessages.getString( PKG, "CheckSumMeta.Type.CRC32" ),
    BaseMessages.getString( PKG, "CheckSumMeta.Type.ADLER32" ),
    BaseMessages.getString( PKG, "CheckSumMeta.Type.MD5" ),
    BaseMessages.getString( PKG, "CheckSumMeta.Type.SHA1" ),
    BaseMessages.getString( PKG, "CheckSumMeta.Type.SHA256" ) };

  //
  // Result types
  //

  public static final String[] resultTypeCode = { "string", "hexadecimal", "binary" };
  public static final int result_TYPE_STRING = 0;
  public static final int result_TYPE_HEXADECIMAL = 1;
  public static final int result_TYPE_BINARY = 2;

  private static final String[] resultTypeDesc = {
    BaseMessages.getString( PKG, "CheckSumMeta.ResultType.String" ),
    BaseMessages.getString( PKG, "CheckSumMeta.ResultType.Hexadecimal" ),
    BaseMessages.getString( PKG, "CheckSumMeta.ResultType.Binary" ) };

  //
  // Evaluation methods
  //

  protected static final String[] EVALUATION_METHOD_CODES =
    { Const.KETTLE_CHECKSUM_EVALUATION_METHOD_BYTES, Const.KETTLE_CHECKSUM_EVALUATION_METHOD_PENTAHO_STRINGS,
      Const.KETTLE_CHECKSUM_EVALUATION_METHOD_NATIVE_STRINGS };

  public static final int EVALUATION_METHOD_BYTES = 0;
  public static final int EVALUATION_METHOD_PENTAHO_STRINGS = 1;
  public static final int EVALUATION_METHOD_NATIVE_STRINGS = 2;
  public static final int DEFAULT_EVALUATION_METHOD = EVALUATION_METHOD_BYTES;

  protected static final String[] EVALUATION_METHOD_DESCS = {
    BaseMessages.getString( PKG, "CheckSumMeta.EvaluationMethod.Bytes" ),
    BaseMessages.getString( PKG, "CheckSumMeta.EvaluationMethod.PentahoStrings" ),
    BaseMessages.getString( PKG, "CheckSumMeta.EvaluationMethod.NativeStrings" ) };

  /** by which fields to display? */
  @Injection( name = "FIELD_NAME", group = "FIELDS" )
  private String[] fieldName;

  @Injection( name = "RESULT_FIELD"  )
  private String resultfieldName;

  @Injection( name = "TYPE" )
  private String checksumtype;

  @Injection( name = "COMPATIBILITY_MODE" )
  private boolean compatibilityMode;

  /**
   * @deprecated use {@link #evaluationMethod} instead
   */
  @Injection( name = "OLD_CHECKSUM_BEHAVIOR" )
  @Deprecated
  private boolean oldChecksumBehaviour;

  @Injection( name = "FIELD_SEPARATOR_STRING" )
  private String fieldSeparatorString;

  /** result type */
  @Injection( name = "RESULT_TYPE" )
  private int resultType;

  @Injection( name = "EVALUATION_METHOD" )
  private int evaluationMethod;

  public CheckSumMeta() {
    super(); // allocate BaseStepMeta

    allocate( 0 );
  }

  //
  // Checksum types
  //

  public void setCheckSumType( int i ) {
    checksumtype = checksumtypeCodes[i];
  }

  public int getTypeByDesc() {
    if ( checksumtype == null ) {
      return 0;
    }
    for ( int i = 0; i < checksumtypeCodes.length; i++ ) {
      if ( checksumtype.equals( checksumtypeCodes[i] ) ) {
        return i;
      }
    }
    return 0;
  }

  public String getCheckSumType() {
    return checksumtype;
  }

  public String[] getChecksumtypeDescs() {
    return checksumtypeDescs;
  }

  public String[] getResultTypeDescs() {
    return resultTypeDesc;
  }

  //
  // Result types
  //

  public int getResultType() {
    return resultType;
  }

  public String getResultTypeDesc( int i ) {
    if ( i < 0 || i >= resultTypeDesc.length ) {
      return resultTypeDesc[0];
    }
    return resultTypeDesc[i];
  }

  public int getResultTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < resultTypeDesc.length; i++ ) {
      if ( resultTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getResultTypeByCode( tt );
  }

  private int getResultTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < resultTypeCode.length; i++ ) {
      if ( resultTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void setResultType( int resultType ) {
    this.resultType = resultType;
  }

  //
  // Evaluation methods
  //

  /**
   * <p>Returns the currently set evaluation method.</p>
   *
   * @return the current evaluation method
   * @see #DEFAULT_EVALUATION_METHOD
   * @see #EVALUATION_METHOD_BYTES
   * @see #EVALUATION_METHOD_PENTAHO_STRINGS
   * @see #EVALUATION_METHOD_NATIVE_STRINGS
   */
  public int getEvaluationMethod() {
    return evaluationMethod;
  }

  /**
   * <p>Sets the evaluation method.</p>
   *
   * @param evaluationMethod the new evaluation method
   * @see #DEFAULT_EVALUATION_METHOD
   * @see #EVALUATION_METHOD_BYTES
   * @see #EVALUATION_METHOD_PENTAHO_STRINGS
   * @see #EVALUATION_METHOD_NATIVE_STRINGS
   */
  public void setEvaluationMethod( int evaluationMethod ) {
    this.evaluationMethod = evaluationMethod;
  }

  /**
   * <p>Returns an array with the descriptions for the available evaluation methods.</p>
   *
   * @return array with the descriptions for the available evaluation methods
   */
  public static String[] getEvaluationMethodDescs() {
    return EVALUATION_METHOD_DESCS;
  }

  /**
   * <p>Get the description of the given evaluation method.</p>
   *
   * @param evaluationMethodIndex the evaluation method for which the description is wanted
   * @return the description of the given evaluation method
   */
  public static String getEvaluationMethodDesc( int evaluationMethodIndex ) {
    if ( evaluationMethodIndex < 0 || evaluationMethodIndex >= EVALUATION_METHOD_DESCS.length ) {
      evaluationMethodIndex = DEFAULT_EVALUATION_METHOD;
    }

    return EVALUATION_METHOD_DESCS[ evaluationMethodIndex ];
  }


  /**
   * <p>Get the code of the given evaluation method.</p>
   *
   * @param evaluationMethodIndex the evaluation method for which the code is wanted
   * @return the code of the given evaluation method
   */
  public static String getEvaluationMethodCode( int evaluationMethodIndex ) {
    if ( evaluationMethodIndex < 0 || evaluationMethodIndex >= EVALUATION_METHOD_CODES.length ) {
      evaluationMethodIndex = DEFAULT_EVALUATION_METHOD;
    }

    return EVALUATION_METHOD_CODES[ evaluationMethodIndex ];
  }

  /**
   * <p>Get the Evaluation Method corresponding to the given description.</p>
   * <p><b>Important:</b> descriptions vary on the language, as so, results may be unpredictable when mixing them!</p>
   * <p>If the description is unknown, {@link #getEvaluationMethodByCode(String)} will be used to see if the given
   * value
   * corresponds to a known code.</p>
   * <p>If the code is also unknown, the default Evaluation Method ({@value #DEFAULT_EVALUATION_METHOD}) will be
   * returned.</p>
   *
   * @param evaluationMethodDesc the description of the Evaluation Method to find
   * @return the Evaluation Method corresponding to the given description (or code if it's an unknown description); if
   * both description and code are unknown, the default Evaluation Method ({@value #DEFAULT_EVALUATION_METHOD}) will be
   * returned
   * @see #getEvaluationMethodByCode(String)
   * @see #DEFAULT_EVALUATION_METHOD
   * @see #EVALUATION_METHOD_BYTES
   * @see #EVALUATION_METHOD_PENTAHO_STRINGS
   * @see #EVALUATION_METHOD_NATIVE_STRINGS
   */
  public int getEvaluationMethodByDesc( String evaluationMethodDesc ) {
    if ( null != evaluationMethodDesc ) {
      for ( int i = 0; i < EVALUATION_METHOD_DESCS.length; ++i ) {
        if ( EVALUATION_METHOD_DESCS[ i ].equalsIgnoreCase( evaluationMethodDesc ) ) {
          return i;
        }
      }
      // If this fails, try to match using the code.
      return getEvaluationMethodByCode( evaluationMethodDesc );
    }

    return DEFAULT_EVALUATION_METHOD;
  }

  /**
   * <p>Get the Evaluation Method corresponding to the given code.</p>
   * <p>If the code is not known, the default Evaluation Method ({@value #DEFAULT_EVALUATION_METHOD}) will be
   * returned.</p>
   *
   * @param evaluationMethodCode the code of the Evaluation Method whose code we want
   * @return the Evaluation Method corresponding to the given code if it's an unknown description; if the description is
   * not known, the default Evaluation Method ({@value #DEFAULT_EVALUATION_METHOD}) will be returned
   * @see #DEFAULT_EVALUATION_METHOD
   * @see #EVALUATION_METHOD_BYTES
   * @see #EVALUATION_METHOD_PENTAHO_STRINGS
   * @see #EVALUATION_METHOD_NATIVE_STRINGS
   */
  public int getEvaluationMethodByCode( String evaluationMethodCode ) {
    if ( null != evaluationMethodCode ) {
      for ( int i = 0; i < EVALUATION_METHOD_CODES.length; ++i ) {
        if ( EVALUATION_METHOD_CODES[ i ].equalsIgnoreCase( evaluationMethodCode ) ) {
          return i;
        }
      }
    }

    return DEFAULT_EVALUATION_METHOD;
  }

  /**
   * @return Returns the resultfieldName.
   */
  public String getResultFieldName() {
    return resultfieldName;
  }

  /**
   * @param resultfieldName
   *          The resultfieldName to set.
   */
  public void setResultFieldName( String resultfieldName ) {
    this.resultfieldName = resultfieldName;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    CheckSumMeta retval = (CheckSumMeta) super.clone();

    int nrfields = fieldName.length;

    retval.allocate( nrfields );
    System.arraycopy( fieldName, 0, retval.fieldName, 0, nrfields );
    return retval;
  }

  public void allocate( int nrfields ) {
    fieldName = new String[nrfields];
  }

  /**
   * @return Returns the fieldName.
   */
  public String[] getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName
   *          The fieldName to set.
   */
  public void setFieldName( String[] fieldName ) {
    this.fieldName = fieldName;
  }

  public String getFieldSeparatorString() {
    return fieldSeparatorString;
  }

  public void setFieldSeparatorString( String fieldSeparatorString ) {
    this.fieldSeparatorString = fieldSeparatorString;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      checksumtype = XMLHandler.getTagValue( stepnode, XML_TAG_CHECKSUMTYPE );
      resultfieldName = XMLHandler.getTagValue( stepnode, XML_TAG_RESULTFIELD_NAME );
      resultType = getResultTypeByCode( Const.NVL( XMLHandler.getTagValue( stepnode, XML_TAG_RESULT_TYPE ), "" ) );
      compatibilityMode = parseCompatibilityMode( XMLHandler.getTagValue( stepnode, XML_TAG_COMPATIBILITY_MODE ) );
      oldChecksumBehaviour =
        parseOldChecksumBehaviour( XMLHandler.getTagValue( stepnode, XML_TAG_OLD_CHECKSUM_BEHAVIOUR ) );
      String evalMet = XMLHandler.getTagValue( stepnode, XML_TAG_EVALUATION_METHOD );
      if ( null == evalMet ) {
        evalMet = Const.getEnvironmentVariable( Const.KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD,
          Const.KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT );
      }
      this.evaluationMethod = getEvaluationMethodByCode( evalMet );
      setFieldSeparatorString( XMLHandler.getTagValue( stepnode, XML_TAG_FIELD_SEPARATOR_STRING ) );

      Node fields = XMLHandler.getSubNode( stepnode, XML_TAG_FIELDS );
      int nrfields = XMLHandler.countNodes( fields, XML_TAG_FIELD );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, XML_TAG_FIELD, i );
        fieldName[ i ] = XMLHandler.getTagValue( fnode, XML_TAG_NAME );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  private boolean parseCompatibilityMode( String compatibilityMode ) {
    if ( compatibilityMode == null ) {
      return true; // It was previously not saved
    } else {
      return Boolean.parseBoolean( compatibilityMode ) || "Y".equalsIgnoreCase( compatibilityMode );
    }
  }

  private boolean parseOldChecksumBehaviour( String oldChecksumBehaviour ) {
    if ( oldChecksumBehaviour == null ) {
      return true; // It was previously not saved
    } else {
      return Boolean.parseBoolean( oldChecksumBehaviour ) || "Y".equalsIgnoreCase( oldChecksumBehaviour );
    }
  }

  private static String getResultTypeCode( int i ) {
    if ( i < 0 || i >= resultTypeCode.length ) {
      return resultTypeCode[0];
    }
    return resultTypeCode[i];
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );
    retval.append( "      " ).append( XMLHandler.addTagValue( XML_TAG_CHECKSUMTYPE, checksumtype ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( XML_TAG_RESULTFIELD_NAME, resultfieldName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( XML_TAG_RESULT_TYPE, getResultTypeCode( resultType ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( XML_TAG_COMPATIBILITY_MODE, compatibilityMode ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( XML_TAG_OLD_CHECKSUM_BEHAVIOUR, oldChecksumBehaviour ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( XML_TAG_EVALUATION_METHOD, getEvaluationMethodCode( evaluationMethod ) ) );
    if ( getFieldSeparatorString() != null ) {
      retval.append( "      " ).append( XMLHandler.addTagValue( XML_TAG_FIELD_SEPARATOR_STRING, getFieldSeparatorString() ) );
    }

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < fieldName.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( XML_TAG_NAME, fieldName[i] ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void setDefault() {
    resultfieldName = null;
    checksumtype = checksumtypeCodes[0];
    resultType = result_TYPE_HEXADECIMAL;
    fieldSeparatorString = null;
    evaluationMethod = DEFAULT_EVALUATION_METHOD;

    allocate( 0 );
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      checksumtype = rep.getStepAttributeString( id_step, XML_TAG_CHECKSUMTYPE );

      resultfieldName = rep.getStepAttributeString( id_step, XML_TAG_RESULTFIELD_NAME );
      resultType = getResultTypeByCode( Const.NVL( rep.getStepAttributeString( id_step, XML_TAG_RESULT_TYPE ), "" ) );
      compatibilityMode = parseCompatibilityMode( rep.getStepAttributeString( id_step, XML_TAG_COMPATIBILITY_MODE ) );
      oldChecksumBehaviour = parseOldChecksumBehaviour( rep.getStepAttributeString( id_step,
        XML_TAG_OLD_CHECKSUM_BEHAVIOUR ) );
      String evalMet = rep.getStepAttributeString( id_step, XML_TAG_EVALUATION_METHOD );
      if ( null == evalMet ) {
        evalMet = Const.getEnvironmentVariable( Const.KETTLE_DEFAULT_CHECKSUM_EVALUATION_METHOD,
          Const.KETTLE_CHECKSUM_EVALUATION_METHOD_DEFAULT );
      }
      evaluationMethod = getEvaluationMethodByCode( evalMet );
      setFieldSeparatorString( rep.getStepAttributeString( id_step, XML_TAG_FIELD_SEPARATOR_STRING ) );

      int nrfields = rep.countNrStepAttributes( id_step, XML_TAG_FIELD_NAME );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[ i ] = rep.getStepAttributeString( id_step, i, XML_TAG_FIELD_NAME );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, XML_TAG_CHECKSUMTYPE, checksumtype );

      rep.saveStepAttribute( id_transformation, id_step, XML_TAG_RESULTFIELD_NAME, resultfieldName );
      rep.saveStepAttribute( id_transformation, id_step, XML_TAG_RESULT_TYPE, getResultTypeCode( resultType ) );
      rep.saveStepAttribute( id_transformation, id_step, XML_TAG_COMPATIBILITY_MODE, compatibilityMode );
      rep.saveStepAttribute( id_transformation, id_step, XML_TAG_OLD_CHECKSUM_BEHAVIOUR, oldChecksumBehaviour );
      rep.saveStepAttribute( id_transformation, id_step, XML_TAG_EVALUATION_METHOD, getEvaluationMethodCode( evaluationMethod ) );

      if ( getFieldSeparatorString() != null ) {
        rep.saveStepAttribute( id_transformation, id_step, XML_TAG_FIELD_SEPARATOR_STRING, getFieldSeparatorString() );
      }

      for ( int i = 0; i < fieldName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, XML_TAG_FIELD_NAME, fieldName[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Output field (String)
    if ( !Utils.isEmpty( resultfieldName ) ) {
      ValueMetaInterface v = null;
      if ( checksumtype.equals( TYPE_CRC32 ) || checksumtype.equals( TYPE_ADLER32 ) ) {
        v = new ValueMetaInteger( space.environmentSubstitute( resultfieldName ) );
      } else {
        switch ( resultType ) {
          case result_TYPE_BINARY:
            v = new ValueMetaBinary( space.environmentSubstitute( resultfieldName ) );
            break;
          default:
            v = new ValueMetaString( space.environmentSubstitute( resultfieldName ) );
            break;
        }
      }
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    if ( Utils.isEmpty( resultfieldName ) ) {
      error_message = BaseMessages.getString( PKG, "CheckSumMeta.CheckResult.ResultFieldMissing" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
    } else {
      error_message = BaseMessages.getString( PKG, "CheckSumMeta.CheckResult.ResultFieldOK" );
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, error_message, stepMeta );
    }
    remarks.add( cr );

    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "CheckSumMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "CheckSumMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      boolean error_found = false;
      error_message = "";

      // Starting from selected fields in ...
      for ( int i = 0; i < fieldName.length; i++ ) {
        int idx = prev.indexOfValue( fieldName[i] );
        if ( idx < 0 ) {
          error_message += "\t\t" + fieldName[i] + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message = BaseMessages.getString( PKG, "CheckSumMeta.CheckResult.FieldsFound", error_message );

        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        if ( fieldName.length > 0 ) {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "CheckSumMeta.CheckResult.AllFieldsFound" ), stepMeta );
          remarks.add( cr );
        } else {
          cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
              PKG, "CheckSumMeta.CheckResult.NoFieldsEntered" ), stepMeta );
          remarks.add( cr );
        }
      }
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "CheckSumMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "CheckSumMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }

    if ( isCompatibilityMode() ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
        PKG, "CheckSumMeta.CheckResult.CompatibilityModeWarning" ), stepMeta );
      remarks.add( cr );

      if ( TYPE_SHA256.equals( getCheckSumType() ) ) {
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "CheckSumMeta.CheckResult.CompatibilityModeSHA256Error" ), stepMeta );
        remarks.add( cr );
      }
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new CheckSum( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new CheckSumData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * @deprecated update to non-compatibility mode
   * @return the Compatibility Mode
   */
  @Deprecated
  public boolean isCompatibilityMode() {
    return compatibilityMode;
  }

  /**
   * <p>While setter and getter works as expected, this information is no longer used.</p>
   * <p>Both methods are kept for compatibility only.</p>
   *
   * @return the current value
   * @see #setOldChecksumBehaviour(boolean)
   * @deprecated use {@link #getEvaluationMethod()} instead
   */
  @Deprecated
  public boolean isOldChecksumBehaviour() {
    return oldChecksumBehaviour;
  }

  /**
   * @deprecated Update to non-compatibility mode
   * @param compatibilityMode
   */
  @Deprecated
  public void setCompatibilityMode( boolean compatibilityMode ) {
    this.compatibilityMode = compatibilityMode;
  }

  /**
   * <p>While setter and getter works as expected, this information is no longer used.</p>
   * <p>Both methods are kept for compatibility only.</p>
   *
   * @param oldChecksumBehaviour the new value
   * @see #isOldChecksumBehaviour()
   * @deprecated use {@link #setEvaluationMethod(int)} instead
   */
  @Deprecated
  public void setOldChecksumBehaviour( boolean oldChecksumBehaviour ) {
    this.oldChecksumBehaviour = oldChecksumBehaviour;
  }
}
