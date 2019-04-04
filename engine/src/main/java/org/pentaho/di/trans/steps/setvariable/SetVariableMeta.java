/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.setvariable;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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

/**
 * Sets environment variables based on content in certain fields of a single input row.
 *
 * Created on 27-apr-2006
 */
public class SetVariableMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SetVariableMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int VARIABLE_TYPE_JVM = 0;
  public static final int VARIABLE_TYPE_PARENT_JOB = 1;
  public static final int VARIABLE_TYPE_GRAND_PARENT_JOB = 2;
  public static final int VARIABLE_TYPE_ROOT_JOB = 3;

  private static final String[] variableTypeCode = { "JVM", "PARENT_JOB", "GP_JOB", "ROOT_JOB" };
  private static final String[] variableTypeDesc = {
    "Valid in the Java Virtual Machine", "Valid in the parent job", "Valid in the grand-parent job",
    "Valid in the root job" };

  private String[] fieldName;
  private String[] variableName;
  private int[] variableType;
  private String[] defaultValue;

  private boolean usingFormatting;

  public SetVariableMeta() {
    super(); // allocate BaseStepMeta
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

  /**
   * @param fieldValue
   *          The fieldValue to set.
   */
  public void setVariableName( String[] fieldValue ) {
    this.variableName = fieldValue;
  }

  /**
   * @return Returns the fieldValue.
   */
  public String[] getVariableName() {
    return variableName;
  }

  /**
   * @return Returns the local variable flag: true if this variable is only valid in the parents job.
   */
  public int[] getVariableType() {
    return variableType;
  }

  /**
   * @return Returns the defaultValue.
   */
  public String[] getDefaultValue() {
    return defaultValue;
  }

  /**
   * @param defaultValue
   *          The defaultValue to set.
   */
  public void setDefaultValue( String[] defaultValue ) {
    this.defaultValue = defaultValue;
  }

  /**
   * @param variableType
   *          The variable type, see also VARIABLE_TYPE_...
   * @return the variable type code for this variable type
   */
  public static final String getVariableTypeCode( int variableType ) {
    return variableTypeCode[variableType];
  }

  /**
   * @param variableType
   *          The variable type, see also VARIABLE_TYPE_...
   * @return the variable type description for this variable type
   */
  public static final String getVariableTypeDescription( int variableType ) {
    return variableTypeDesc[variableType];
  }

  /**
   * @param variableType
   *          The code or description of the variable type
   * @return The variable type
   */
  public static final int getVariableType( String variableType ) {
    for ( int i = 0; i < variableTypeCode.length; i++ ) {
      if ( variableTypeCode[i].equalsIgnoreCase( variableType ) ) {
        return i;
      }
    }
    for ( int i = 0; i < variableTypeDesc.length; i++ ) {
      if ( variableTypeDesc[i].equalsIgnoreCase( variableType ) ) {
        return i;
      }
    }
    return VARIABLE_TYPE_JVM;
  }

  /**
   * @param localVariable
   *          The localVariable to set.
   */
  public void setVariableType( int[] localVariable ) {
    this.variableType = localVariable;
  }

  public static final String[] getVariableTypeDescriptions() {
    return variableTypeDesc;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int count ) {
    fieldName = new String[count];
    variableName = new String[count];
    variableType = new int[count];
    defaultValue = new String[count];
  }

  public Object clone() {
    SetVariableMeta retval = (SetVariableMeta) super.clone();

    int count = fieldName.length;

    retval.allocate( count );
    System.arraycopy( fieldName, 0, retval.fieldName, 0, count );
    System.arraycopy( variableName, 0, retval.variableName, 0, count );
    System.arraycopy( variableType, 0, retval.variableType, 0, count );
    System.arraycopy( defaultValue, 0, retval.defaultValue, 0, count );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int count = XMLHandler.countNodes( fields, "field" );

      allocate( count );

      for ( int i = 0; i < count; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldName[i] = XMLHandler.getTagValue( fnode, "field_name" );
        variableName[i] = XMLHandler.getTagValue( fnode, "variable_name" );
        variableType[i] = getVariableType( XMLHandler.getTagValue( fnode, "variable_type" ) );
        defaultValue[i] = XMLHandler.getTagValue( fnode, "default_value" );
      }

      // Default to "N" for backward compatibility
      //
      usingFormatting = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_formatting" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "SetVariableMeta.RuntimeError.UnableToReadXML.SETVARIABLE0004" ), e );
    }
  }

  public void setDefault() {
    int count = 0;

    allocate( count );

    for ( int i = 0; i < count; i++ ) {
      fieldName[i] = "field" + i;
      variableName[i] = "";
      variableType[i] = VARIABLE_TYPE_JVM;
      defaultValue[i] = "";
    }

    usingFormatting = true;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 150 );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < fieldName.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field_name", fieldName[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "variable_name", variableName[i] ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "variable_type", getVariableTypeCode( variableType[i] ) ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "default_value", defaultValue[i] ) );
      retval.append( "        </field>" ).append( Const.CR );
    }
    retval.append( "      </fields>" ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.addTagValue( "use_formatting", usingFormatting ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        variableName[i] = rep.getStepAttributeString( id_step, i, "variable_name" );
        variableType[i] = getVariableType( rep.getStepAttributeString( id_step, i, "variable_type" ) );
        defaultValue[i] = rep.getStepAttributeString( id_step, i, "default_value" );
      }

      usingFormatting = rep.getStepAttributeBoolean( id_step, 0, "use_formatting", false );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SetVariableMeta.RuntimeError.UnableToReadRepository.SETVARIABLE0005" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      for ( int i = 0; i < fieldName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", Utils.isEmpty( fieldName[i] )
          ? "" : fieldName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "variable_name", variableName[i] );
        rep.saveStepAttribute(
          id_transformation, id_step, i, "variable_type", getVariableTypeCode( variableType[i] ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "default_value", defaultValue[i] );
      }

      rep.saveStepAttribute( id_transformation, id_step, 0, "use_formatting", usingFormatting );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "SetVariableMeta.RuntimeError.UnableToSaveRepository.SETVARIABLE0006", "" + id_step ), e );
    }

  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "SetVariableMeta.CheckResult.NotReceivingFieldsFromPreviousSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SetVariableMeta.CheckResult.ReceivingFieldsFromPreviousSteps", "" + prev.size() ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SetVariableMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SetVariableMeta.CheckResult.NotReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new SetVariable( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new SetVariableData();
  }

  /**
   * @return the usingFormatting
   */
  public boolean isUsingFormatting() {
    return usingFormatting;
  }

  /**
   * @param usingFormatting
   *          the usingFormatting to set
   */
  public void setUsingFormatting( boolean usingFormatting ) {
    this.usingFormatting = usingFormatting;
  }
}
