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

package org.pentaho.di.trans.steps.setvalueconstant;

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
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class SetValueConstantMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SetValueConstantMeta.class; // for i18n purposes, needed by Translator2!!

  /** which fields to display? */
  private String[] fieldName;

  /** by which value we replace */
  private String[] replaceValue;

  private String[] replaceMask;

  /** Flag : set empty string **/
  private boolean[] setEmptyString;

  private boolean usevar;

  public SetValueConstantMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    SetValueConstantMeta retval = (SetValueConstantMeta) super.clone();

    int nrfields = fieldName.length;
    retval.allocate( nrfields );
    System.arraycopy( fieldName, 0, retval.fieldName, 0, nrfields );
    System.arraycopy( replaceValue, 0, retval.replaceValue, 0, nrfields );
    System.arraycopy( replaceMask, 0, retval.replaceMask, 0, nrfields );
    System.arraycopy( setEmptyString, 0, retval.setEmptyString, 0, nrfields );

    return retval;
  }

  public void allocate( int nrfields ) {
    fieldName = new String[nrfields];
    replaceValue = new String[nrfields];
    replaceMask = new String[nrfields];
    setEmptyString = new boolean[nrfields];
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
   * @return Returns the replaceValue.
   */
  public String[] getReplaceValue() {
    return replaceValue;
  }

  /**
   * @param fieldName
   *          The replaceValue to set.
   */
  public void setReplaceValue( String[] replaceValue ) {
    this.replaceValue = replaceValue;
  }

  /**
   * @return Returns the replaceMask.
   */
  public String[] getReplaceMask() {
    return replaceMask;
  }

  /**
   * @param replaceMask
   *          The replaceMask to set.
   */
  public void setReplaceMask( String[] replaceMask ) {
    this.replaceMask = replaceMask;
  }

  /**
   * @deprecated use {@link #isEmptyString()} instead
   * @return the setEmptyString
   */
  @Deprecated
  public boolean[] isSetEmptyString() {
    return isEmptyString();
  }

  public boolean[] isEmptyString() {
    return setEmptyString;
  }

  /**
   * @param setEmptyString
   *          the setEmptyString to set
   */
  public void setEmptyString( boolean[] setEmptyString ) {
    this.setEmptyString = setEmptyString;
  }

  public void setUseVars( boolean usevar ) {
    this.usevar = usevar;
  }

  public boolean isUseVars() {
    return usevar;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      usevar = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "usevar" ) );
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );
      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        fieldName[i] = XMLHandler.getTagValue( fnode, "name" );
        replaceValue[i] = XMLHandler.getTagValue( fnode, "value" );
        replaceMask[i] = XMLHandler.getTagValue( fnode, "mask" );
        String emptyString = XMLHandler.getTagValue( fnode, "set_empty_string" );
        setEmptyString[i] = !Utils.isEmpty( emptyString ) && "Y".equalsIgnoreCase( emptyString );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "It was not possible to load the metadata for this step from XML", e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "   " + XMLHandler.addTagValue( "usevar", usevar ) );
    retval.append( "    <fields>" + Const.CR );
    for ( int i = 0; i < fieldName.length; i++ ) {
      retval.append( "      <field>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", fieldName[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "value", replaceValue[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "mask", replaceMask[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "set_empty_string", setEmptyString[i] ) );
      retval.append( "        </field>" + Const.CR );
    }
    retval.append( "      </fields>" + Const.CR );

    return retval.toString();
  }

  public void setDefault() {
    int nrfields = 0;
    allocate( nrfields );
    for ( int i = 0; i < nrfields; i++ ) {
      fieldName[i] = "field" + i;
      replaceValue[i] = "value" + i;
      replaceMask[i] = "mask" + i;
      setEmptyString[i] = false;
    }
    usevar = false;
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      usevar = rep.getStepAttributeBoolean( id_step, "usevar" );
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );
      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        replaceValue[i] = rep.getStepAttributeString( id_step, i, "replace_value" );
        replaceMask[i] = rep.getStepAttributeString( id_step, i, "replace_mask" );
        setEmptyString[i] = rep.getStepAttributeBoolean( id_step, i, "set_empty_string", false );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "usevar", usevar );
      for ( int i = 0; i < fieldName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "replace_value", replaceValue[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "replace_mask", replaceMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "set_empty_string", setEmptyString[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "SetValueConstantMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SetValueConstantMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      String error_message = "";
      boolean error_found = false;

      // Starting from selected fields in ...
      for ( int i = 0; i < fieldName.length; i++ ) {
        int idx = prev.indexOfValue( fieldName[i] );
        if ( idx < 0 ) {
          error_message += "\t\t" + fieldName[i] + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
          BaseMessages.getString( PKG, "SetValueConstantMeta.CheckResult.FieldsFound", error_message );

        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        if ( fieldName.length > 0 ) {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "SetValueConstantMeta.CheckResult.AllFieldsFound" ), stepMeta );
        } else {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
              PKG, "SetValueConstantMeta.CheckResult.NoFieldsEntered" ), stepMeta );
        }
        remarks.add( cr );
      }

    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SetValueConstantMeta.CheckResult.StepRecevingData2" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SetValueConstantMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
    }
    remarks.add( cr );
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new SetValueConstant( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new SetValueConstantData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }
}
