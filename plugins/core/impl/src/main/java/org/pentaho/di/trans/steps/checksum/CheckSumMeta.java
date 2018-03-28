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

package org.pentaho.di.trans.steps.checksum;

import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
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
public class CheckSumMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = CheckSumMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String TYPE_CRC32 = "CRC32";
  public static final String TYPE_ADLER32 = "ADLER32";
  public static final String TYPE_MD5 = "MD5";
  public static final String TYPE_SHA1 = "SHA-1";
  public static final String TYPE_SHA256 = "SHA-256";

  public static String[] checksumtypeCodes = { TYPE_CRC32, TYPE_ADLER32, TYPE_MD5, TYPE_SHA1, TYPE_SHA256 };
  public static String[] checksumtypeDescs = {
    BaseMessages.getString( PKG, "CheckSumDialog.Type.CRC32" ),
    BaseMessages.getString( PKG, "CheckSumDialog.Type.ADLER32" ),
    BaseMessages.getString( PKG, "CheckSumDialog.Type.MD5" ),
    BaseMessages.getString( PKG, "CheckSumDialog.Type.SHA1" ),
    BaseMessages.getString( PKG, "CheckSumDialog.Type.SHA256" ) };

  /**
   * The result type description
   */
  public static final String[] resultTypeDesc = {
    BaseMessages.getString( PKG, "CheckSumDialog.ResultType.String" ),
    BaseMessages.getString( PKG, "CheckSumDialog.ResultType.Hexadecimal" ),
    BaseMessages.getString( PKG, "CheckSumDialog.ResultType.Binary" ) };

  /**
   * The result type codes
   */
  public static final String[] resultTypeCode = { "string", "hexadecimal", "binay" };
  public static final int result_TYPE_STRING = 0;
  public static final int result_TYPE_HEXADECIMAL = 1;
  public static final int result_TYPE_BINARY = 2;

  /** by which fields to display? */
  private String[] fieldName;

  private String resultfieldName;

  private String checksumtype;

  private boolean compatibilityMode;
  private boolean oldChecksumBehaviour;

  /** result type */
  private int resultType;

  public CheckSumMeta() {
    super(); // allocate BaseStepMeta
  }

  // TODO Deprecate one of these setCheckSumType methods
  public void setCheckSumType( int i ) {
    checksumtype = checksumtypeCodes[i];
  }

  public void setCheckSumType( String type ) {
    if ( Arrays.asList( checksumtypeCodes ).contains( type ) ) {
      checksumtype = type;
    } else {
      checksumtype = checksumtypeCodes[0];
    }
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

  public int getResultType() {
    return resultType;
  }

  public static String getResultTypeDesc( int i ) {
    if ( i < 0 || i >= resultTypeDesc.length ) {
      return resultTypeDesc[0];
    }
    return resultTypeDesc[i];
  }

  public static int getResultTypeByDesc( String tt ) {
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

  private static int getResultTypeByCode( String tt ) {
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

  /**
   * @return Returns the resultfieldName.
   */
  public String getResultFieldName() {
    return resultfieldName;
  }

  /**
   * @param resultName
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

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      checksumtype = XMLHandler.getTagValue( stepnode, "checksumtype" );
      resultfieldName = XMLHandler.getTagValue( stepnode, "resultfieldName" );
      resultType = getResultTypeByCode( Const.NVL( XMLHandler.getTagValue( stepnode, "resultType" ), "" ) );
      compatibilityMode = parseCompatibilityMode( XMLHandler.getTagValue( stepnode, "compatibilityMode" ) );
      oldChecksumBehaviour = parseOldChecksumBehaviour( XMLHandler.getTagValue( stepnode, "oldChecksumBehaviour" ) );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        fieldName[i] = XMLHandler.getTagValue( fnode, "name" );
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
    retval.append( "      " ).append( XMLHandler.addTagValue( "checksumtype", checksumtype ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "resultfieldName", resultfieldName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "resultType", getResultTypeCode( resultType ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compatibilityMode", compatibilityMode ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "oldChecksumBehaviour", oldChecksumBehaviour ) );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < fieldName.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldName[i] ) );
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
    int nrfields = 0;

    allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      fieldName[i] = "field" + i;
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      checksumtype = rep.getStepAttributeString( id_step, "checksumtype" );

      resultfieldName = rep.getStepAttributeString( id_step, "resultfieldName" );
      resultType = getResultTypeByCode( Const.NVL( rep.getStepAttributeString( id_step, "resultType" ), "" ) );
      compatibilityMode = parseCompatibilityMode( rep.getStepAttributeString( id_step, "compatibilityMode" ) );
      oldChecksumBehaviour = parseOldChecksumBehaviour( rep.getStepAttributeString( id_step, "oldChecksumBehaviour" ) );

      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[i] = rep.getStepAttributeString( id_step, i, "field_name" );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "checksumtype", checksumtype );

      rep.saveStepAttribute( id_transformation, id_step, "resultfieldName", resultfieldName );
      rep.saveStepAttribute( id_transformation, id_step, "resultType", getResultTypeCode( resultType ) );
      rep.saveStepAttribute( id_transformation, id_step, "compatibilityMode", compatibilityMode );
      rep.saveStepAttribute( id_transformation, id_step, "oldChecksumBehaviour", oldChecksumBehaviour );

      for ( int i = 0; i < fieldName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName[i] );
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
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
    } else {
      error_message = BaseMessages.getString( PKG, "CheckSumMeta.CheckResult.ResultFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
    }
    remarks.add( cr );

    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "CheckSumMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
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

        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        if ( fieldName.length > 0 ) {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "CheckSumMeta.CheckResult.AllFieldsFound" ), stepMeta );
          remarks.add( cr );
        } else {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
              PKG, "CheckSumMeta.CheckResult.NoFieldsEntered" ), stepMeta );
          remarks.add( cr );
        }
      }

    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "CheckSumMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "CheckSumMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }

    if ( isCompatibilityMode() ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
        PKG, "CheckSumMeta.CheckResult.CompatibilityModeWarning" ), stepMeta );
      remarks.add( cr );
    }

    if ( isCompatibilityMode() && getCheckSumType() == TYPE_SHA256 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
        PKG, "CheckSumMeta.CheckResult.CompatibilityModeSHA256Error" ), stepMeta );
      remarks.add( cr );
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

  public void setOldChecksumBehaviour( boolean oldChecksumBehaviour ) {
    this.oldChecksumBehaviour = oldChecksumBehaviour;
  }
}
