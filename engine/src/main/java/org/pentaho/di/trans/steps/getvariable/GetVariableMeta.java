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

package org.pentaho.di.trans.steps.getvariable;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
 * Created on 05-aug-2003
 */
@InjectionSupported( localizationPrefix = "GetVariable.Injection.", groups = { "FIELDS" } )
public class GetVariableMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = GetVariableMeta.class; // for i18n purposes, needed by Translator2!!

  @InjectionDeep
  private FieldDefinition[] fieldDefinitions;

  public GetVariableMeta() {
    super(); // allocate BaseStepMeta
  }

  public FieldDefinition[] getFieldDefinitions() {
    return fieldDefinitions;
  }

  public void setFieldDefinitions( FieldDefinition[] fieldDefinitions ) {
    this.fieldDefinitions = fieldDefinitions;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int count ) {
    fieldDefinitions = new FieldDefinition[count];
    for ( int i = 0; i < fieldDefinitions.length; i++ ) {
      fieldDefinitions[i] = new FieldDefinition();
    }
  }

  @Override
  public Object clone() {
    GetVariableMeta retval = (GetVariableMeta) super.clone();

    int count = fieldDefinitions.length;

    retval.allocate( count );
    for ( int i = 0; i < count; i++ ) {
      retval.getFieldDefinitions()[i] = fieldDefinitions[i].clone();
    }
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int count = XMLHandler.countNodes( fields, "field" );

      allocate( count );

      for ( int i = 0; i < count; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldDefinitions[i].setFieldName( XMLHandler.getTagValue( fnode, "name" ) );
        fieldDefinitions[i].setVariableString( XMLHandler.getTagValue( fnode, "variable" ) );
        fieldDefinitions[i].setFieldType(
          ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) ) );
        fieldDefinitions[i].setFieldFormat( XMLHandler.getTagValue( fnode, "format" ) );
        fieldDefinitions[i].setCurrency( XMLHandler.getTagValue( fnode, "currency" ) );
        fieldDefinitions[i].setDecimal( XMLHandler.getTagValue( fnode, "decimal" ) );
        fieldDefinitions[i].setGroup( XMLHandler.getTagValue( fnode, "group" ) );
        fieldDefinitions[i].setFieldLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
        fieldDefinitions[i].setFieldPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
        fieldDefinitions[i].setTrimType(
          ValueMetaString.getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );

        // Backward compatibility
        //
        if ( fieldDefinitions[i].getFieldType() == ValueMetaInterface.TYPE_NONE ) {
          fieldDefinitions[i].setFieldType( ValueMetaInterface.TYPE_STRING );
        }
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to read step information from XML", e );
    }
  }

  @Override
  public void setDefault() {
    int count = 0;

    allocate( count );

    for ( int i = 0; i < count; i++ ) {
      fieldDefinitions[i].setFieldName( "field" + i );
      fieldDefinitions[i].setVariableString( "" );
    }
  }

  @Override
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Determine the maximum length...
    //
    int length = -1;
    for ( int i = 0; i < fieldDefinitions.length; i++ ) {
      String variableString = fieldDefinitions[i].getVariableString();
      if ( variableString != null ) {
        String string = space.environmentSubstitute( variableString );
        if ( string.length() > length ) {
          length = string.length();
        }
      }
    }

    RowMetaInterface row = new RowMeta();
    for ( int i = 0; i < fieldDefinitions.length; i++ ) {
      ValueMetaInterface v;
      try {
        v = ValueMetaFactory.createValueMeta( fieldDefinitions[i].getFieldName(), fieldDefinitions[i].getFieldType() );
      } catch ( KettlePluginException e ) {
        throw new KettleStepException( e );
      }
      int fieldLength = fieldDefinitions[i].getFieldLength();
      if ( fieldLength < 0 ) {
        v.setLength( length );
      } else {
        v.setLength( fieldLength );
      }
      int fieldPrecision = fieldDefinitions[i].getFieldPrecision();
      if ( fieldPrecision >= 0 ) {
        v.setPrecision( fieldPrecision );
      }
      v.setConversionMask( fieldDefinitions[i].getFieldFormat() );
      v.setGroupingSymbol( fieldDefinitions[i].getGroup() );
      v.setDecimalSymbol( fieldDefinitions[i].getDecimal() );
      v.setCurrencySymbol( fieldDefinitions[i].getCurrency() );
      v.setTrimType( fieldDefinitions[i].getTrimType() );
      v.setOrigin( name );

      row.addValueMeta( v );
    }

    inputRowMeta.mergeRowMeta( row, name );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < fieldDefinitions.length; i++ ) {
      String fieldName = fieldDefinitions[i].getFieldName();
      if ( fieldName != null && fieldName.length() != 0 ) {
        retval.append( "      <field>" ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldName ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "variable",
          fieldDefinitions[i].getVariableString() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "type",
          ValueMetaFactory.getValueMetaName( fieldDefinitions[i].getFieldType() ) ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "format", fieldDefinitions[i].getFieldFormat() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "currency", fieldDefinitions[i].getCurrency() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", fieldDefinitions[i].getDecimal() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "group", fieldDefinitions[i].getGroup() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "length", fieldDefinitions[i].getFieldLength() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "precision", fieldDefinitions[i]
            .getFieldPrecision() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "trim_type",
          ValueMetaString.getTrimTypeCode( fieldDefinitions[i].getTrimType() ) ) );

        retval.append( "      </field>" ).append( Const.CR );
      }
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldDefinitions[i].setFieldName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        fieldDefinitions[i].setVariableString( rep.getStepAttributeString( id_step, i, "field_variable" ) );
        fieldDefinitions[i].setFieldType(
          ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) ) );

        fieldDefinitions[i].setFieldFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        fieldDefinitions[i].setCurrency( rep.getStepAttributeString( id_step, i, "field_currency" ) );
        fieldDefinitions[i].setDecimal( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
        fieldDefinitions[i].setGroup( rep.getStepAttributeString( id_step, i, "field_group" ) );
        fieldDefinitions[i].setFieldLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
        fieldDefinitions[i].setFieldPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
        fieldDefinitions[i].setTrimType(
          ValueMetaString.getTrimTypeByCode( rep.getStepAttributeString( id_step, i, "field_trim_type" ) ) );

        // Backward compatibility
        //
        int fieldType = fieldDefinitions[i].getFieldType();
        if ( fieldType == ValueMetaInterface.TYPE_NONE ) {
          fieldDefinitions[i].setFieldType( ValueMetaInterface.TYPE_STRING );
        }
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {

      for ( int i = 0; i < fieldDefinitions.length; i++ ) {
        String fieldName = fieldDefinitions[i].getFieldName();
        if ( fieldName != null && fieldName.length() != 0 ) {
          rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_variable", fieldDefinitions[i]
              .getVariableString() );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_type",
            ValueMetaFactory.getValueMetaName( fieldDefinitions[i].getFieldType() ) );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_format", fieldDefinitions[i].getFieldFormat() );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", fieldDefinitions[i].getCurrency() );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", fieldDefinitions[i].getDecimal() );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_group", fieldDefinitions[i].getGroup() );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_length", fieldDefinitions[i].getFieldLength() );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", fieldDefinitions[i]
              .getFieldPrecision() );
          rep.saveStepAttribute( id_transformation, id_step, i, "field_trim_type",
            ValueMetaString.getTrimTypeCode( fieldDefinitions[i].getTrimType() ) );
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }

  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    // See if we have input streams leading to this step!
    int nrRemarks = remarks.size();
    for ( int i = 0; i < fieldDefinitions.length; i++ ) {
      if ( Utils.isEmpty( fieldDefinitions[i].getVariableString() ) ) {
        CheckResult cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "GetVariableMeta.CheckResult.VariableNotSpecified", fieldDefinitions[i].getFieldName() ), stepMeta );
        remarks.add( cr );
      }
    }
    if ( remarks.size() == nrRemarks ) {
      CheckResult cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "GetVariableMeta.CheckResult.AllVariablesSpecified" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new GetVariable( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new GetVariableData();
  }

  public static class FieldDefinition implements Cloneable {

    @Injection( name = "FIELDNAME", group = "FIELDS" )
    private String fieldName;
    @Injection( name = "VARIABLE", group = "FIELDS" )
    private String variableString;

    @Injection( name = "FIELDTYPE", group = "FIELDS" )
    private int fieldType;

    @Injection( name = "FIELDFORMAT", group = "FIELDS" )
    private String fieldFormat;
    @Injection( name = "FIELDLENGTH", group = "FIELDS" )
    private int fieldLength;
    @Injection( name = "FIELDPRECISION", group = "FIELDS" )
    private int fieldPrecision;

    @Injection( name = "CURRENCY", group = "FIELDS" )
    private String currency;
    @Injection( name = "DECIMAL", group = "FIELDS" )
    private String decimal;
    @Injection( name = "GROUP", group = "FIELDS" )
    private String group;

    @Injection( name = "TRIMTYPE", group = "FIELDS" )
    private int trimType;

    /**
     * @return Returns the fieldName.
     */
    public String getFieldName() {
      return fieldName;
    }

    /**
     * @param fieldName
     *          The fieldName to set.
     */
    public void setFieldName( String fieldName ) {
      this.fieldName = fieldName;
    }

    /**
     * @return Returns the strings containing variables.
     */
    public String getVariableString() {
      return variableString;
    }

    /**
     * @param variableString
     *          The variable strings to set.
     */
    public void setVariableString( String variableString ) {
      this.variableString = variableString;
    }

    /**
     * @return the field type (ValueMetaInterface.TYPE_*)
     */
    public int getFieldType() {
      return fieldType;
    }

    /**
     * @param fieldType
     *          the field type to set (ValueMetaInterface.TYPE_*)
     */
    public void setFieldType( int fieldType ) {
      this.fieldType = fieldType;
    }

    /**
     * @return the fieldFormat
     */
    public String getFieldFormat() {
      return fieldFormat;
    }

    /**
     * @param fieldFormat
     *          the fieldFormat to set
     */
    public void setFieldFormat( String fieldFormat ) {
      this.fieldFormat = fieldFormat;
    }

    /**
     * @return the fieldLength
     */
    public int getFieldLength() {
      return fieldLength;
    }

    /**
     * @param fieldLength
     *          the fieldLength to set
     */
    public void setFieldLength( int fieldLength ) {
      this.fieldLength = fieldLength;
    }

    /**
     * @return the fieldPrecision
     */
    public int getFieldPrecision() {
      return fieldPrecision;
    }

    /**
     * @param fieldPrecision
     *          the fieldPrecision to set
     */
    public void setFieldPrecision( int fieldPrecision ) {
      this.fieldPrecision = fieldPrecision;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
      return currency;
    }

    /**
     * @param currency
     *          the currency to set
     */
    public void setCurrency( String currency ) {
      this.currency = currency;
    }

    /**
     * @return the decimal
     */
    public String getDecimal() {
      return decimal;
    }

    /**
     * @param decimal
     *          the decimal to set
     */
    public void setDecimal( String decimal ) {
      this.decimal = decimal;
    }

    /**
     * @return the group
     */
    public String getGroup() {
      return group;
    }

    /**
     * @param group
     *          the group to set
     */
    public void setGroup( String group ) {
      this.group = group;
    }

    /**
     * @return the trimType
     */
    public int getTrimType() {
      return trimType;
    }

    /**
     * @param trimType
     *          the trimType to set
     */
    public void setTrimType( int trimType ) {
      this.trimType = trimType;
    }

    @Override
    public FieldDefinition clone() {
      try {
        return (FieldDefinition) super.clone();
      } catch ( CloneNotSupportedException e ) {
        throw new RuntimeException( e );
      }
    }
  }
}
