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

package org.pentaho.di.trans.steps.setvalueconstant;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
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
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

@InjectionSupported( localizationPrefix = "SetValueConstant.Injection.", groups = { "FIELDS", "OPTIONS" } )
public class SetValueConstantMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SetValueConstantMeta.class; // for i18n purposes, needed by Translator2!!

  @InjectionDeep
  private List<Field> fields = new ArrayList<>();

  public Field getField( int i ) {
    return fields.get( i );
  }

  public List<Field> getFields() {
    return fields;
  }

  public void setFields( List<Field> fields ) {
    this.fields = fields;
  }

  @Injection( name = "USE_VARIABLE", group = "OPTIONS" )
  private boolean usevar;

  public SetValueConstantMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
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
      List<Field> fieldList = new ArrayList<>();
      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        Field field = new Field();
        field.setFieldName( XMLHandler.getTagValue( fnode, "name" ) );
        field.setReplaceValue( XMLHandler.getTagValue( fnode, "value" ) );
        field.setReplaceMask( XMLHandler.getTagValue( fnode, "mask" ) );
        String emptyString = XMLHandler.getTagValue( fnode, "set_empty_string" );
        field.setEmptyString( !Utils.isEmpty( emptyString ) && "Y".equalsIgnoreCase( emptyString ) );
        fieldList.add( field );
      }
      setFields( fieldList );
    } catch ( Exception e ) {
      throw new KettleXMLException( "It was not possible to load the metadata for this step from XML", e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();
    retval.append( "   " + XMLHandler.addTagValue( "usevar", usevar ) );
    retval.append( "    <fields>" + Const.CR );
    fields.forEach( field -> {
      retval.append( "      <field>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", field.getFieldName() ) );
      retval.append( "        " + XMLHandler.addTagValue( "value", field.getReplaceValue() ) );
      retval.append( "        " + XMLHandler.addTagValue( "mask", field.getReplaceMask() ) );
      retval.append( "        " + XMLHandler.addTagValue( "set_empty_string", field.isEmptyString() ) );
      retval.append( "        </field>" + Const.CR );
    } );
    retval.append( "      </fields>" + Const.CR );

    return retval.toString();
  }

  public void setDefault() {
    usevar = false;
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      usevar = rep.getStepAttributeBoolean( id_step, "usevar" );
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );
      List<Field> fieldList = new ArrayList<>();
      for ( int i = 0; i < nrfields; i++ ) {
        Field field = new Field();
        field.setFieldName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field.setReplaceValue( rep.getStepAttributeString( id_step, i, "replace_value" ) );
        field.setReplaceMask( rep.getStepAttributeString( id_step, i, "replace_mask" ) );
        field.setEmptyString( rep.getStepAttributeBoolean( id_step, i, "set_empty_string", false ) );
        fieldList.add( field );
      }
      setFields( fieldList );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "usevar", usevar );
      for ( int i = 0; i < fields.size(); i++ ) {
        Field field = fields.get( i );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getFieldName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "replace_value", field.getReplaceValue() );
        rep.saveStepAttribute( id_transformation, id_step, i, "replace_mask", field.getReplaceMask() );
        rep.saveStepAttribute( id_transformation, id_step, i, "set_empty_string", field.isEmptyString() );
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
      for ( int i = 0; i < fields.size(); i++ ) {
        int idx = prev.indexOfValue( fields.get( i ).getFieldName() );
        if ( idx < 0 ) {
          error_message += "\t\t" + fields.get( i ).getFieldName() + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
          BaseMessages.getString( PKG, "SetValueConstantMeta.CheckResult.FieldsFound", error_message );

        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        if ( Utils.isEmpty( fields ) ) {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
              PKG, "SetValueConstantMeta.CheckResult.NoFieldsEntered" ), stepMeta );
        } else {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "SetValueConstantMeta.CheckResult.AllFieldsFound" ), stepMeta );
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

  public static class Field {

    @Injection( name = "FIELD_NAME", group = "FIELDS" )
    private String fieldName;

    @Injection( name = "REPLACE_VALUE", group = "FIELDS" )
    private String replaceValue;

    @Injection( name = "REPLACE_MASK", group = "FIELDS" )
    private String replaceMask;

    @Injection( name = "EMPTY_STRING", group = "FIELDS" )
    private boolean setEmptyString;

    public String getFieldName() {
      return fieldName;
    }

    public void setFieldName( String fieldName ) {
      this.fieldName = fieldName;
    }

    public String getReplaceValue() {
      return replaceValue;
    }

    public void setReplaceValue( String replaceValue ) {
      this.replaceValue = replaceValue;
    }

    public String getReplaceMask() {
      return replaceMask;
    }

    public void setReplaceMask( String replaceMask ) {
      this.replaceMask = replaceMask;
    }

    public boolean isEmptyString() {
      return setEmptyString;
    }

    public void setEmptyString( boolean setEmptyString ) {
      this.setEmptyString = setEmptyString;
    }

    @Override
    public boolean equals( Object obj ) {
      return fieldName.equals( ((Field) obj).getFieldName() )
              && replaceValue.equals( ((Field) obj).getReplaceValue() )
              && replaceMask.equals( ((Field) obj).getReplaceMask() )
              && setEmptyString == ((Field) obj).isEmptyString();
    }
  }
}
