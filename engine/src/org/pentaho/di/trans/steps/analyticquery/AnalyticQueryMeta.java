/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.analyticquery;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @author ngoodman
 * @since 27-jan-2009
 */
@InjectionSupported( localizationPrefix = "AnalyticQuery.Injection." )
public class AnalyticQueryMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = AnalyticQuery.class; // for i18n purposes, needed by Translator2!!

  public static final int TYPE_FUNCT_LEAD = 0;
  public static final int TYPE_FUNCT_LAG = 1;

  public static final String[] typeGroupCode = /* WARNING: DO NOT TRANSLATE THIS. WE ARE SERIOUS, DON'T TRANSLATE! */
  { "LEAD", "LAG", };

  public static final String[] typeGroupLongDesc = {
    BaseMessages.getString( PKG, "AnalyticQueryMeta.TypeGroupLongDesc.LEAD" ),
    BaseMessages.getString( PKG, "AnalyticQueryMeta.TypeGroupLongDesc.LAG" ) };

  /** Fields to partition by ie, CUSTOMER, PRODUCT */
  @Injection( name = "GROUP_FIELDS" )
  private String[] groupField;

  @InjectionDeep(prefix="OUTPUT")
  private OutputField[] outputFields;

  /** END arrays are one for each configured analytic function */

  public AnalyticQueryMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the groupField.
   */
  public String[] getGroupField() {
    return groupField;
  }

  /**
   * @param groupField
   *          The groupField to set.
   */
  public void setGroupField( String[] groupField ) {
    this.groupField = groupField;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int sizegroup, int nrfields ) {
    groupField = new String[sizegroup];
    outputFields = new OutputField[nrfields];
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {

      Node groupn = XMLHandler.getSubNode( stepnode, "group" );
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );

      int sizegroup = XMLHandler.countNodes( groupn, "field" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( sizegroup, nrfields );

      for ( int i = 0; i < sizegroup; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( groupn, "field", i );
        groupField[i] = XMLHandler.getTagValue( fnode, "name" );
      }
      for ( int i = 0; i < nrfields; i++ ) {
        OutputField of = new OutputField();
        outputFields[i] = of;
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        of.setAggregateField( XMLHandler.getTagValue( fnode, "aggregate" ) );
        of.setSubjectField( XMLHandler.getTagValue( fnode, "subject" ) );
        of.setAggregateType( getType( XMLHandler.getTagValue( fnode, "type" ) ) );

        of.setValueField( Integer.parseInt( XMLHandler.getTagValue( fnode, "valuefield" ) ) );
      }

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "AnalyticQueryMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public static final int getType( String desc ) {
    for ( int i = 0; i < typeGroupCode.length; i++ ) {
      if ( typeGroupCode[i].equalsIgnoreCase( desc ) ) {
        return i;
      }
    }
    for ( int i = 0; i < typeGroupLongDesc.length; i++ ) {
      if ( typeGroupLongDesc[i].equalsIgnoreCase( desc ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final String getTypeDesc( int i ) {
    if ( i < 0 || i >= typeGroupCode.length ) {
      return null;
    }
    return typeGroupCode[i];
  }

  public static final String getTypeDescLong( int i ) {
    if ( i < 0 || i >= typeGroupLongDesc.length ) {
      return null;
    }
    return typeGroupLongDesc[i];
  }

  public void setDefault() {

    int sizegroup = 0;
    int nrfields = 0;

    allocate( sizegroup, nrfields );
  }

  public OutputField[] getOutputFields() {
    return outputFields;
  }

  public void getFields( RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // re-assemble a new row of metadata
    //
    RowMetaInterface fields = new RowMeta();

    // Add existing values
    fields.addRowMeta( r );

    // add analytic values
    for ( int i = 0; i < outputFields.length; i++ ) {
      OutputField of = outputFields[i];

      int index_of_subject = -1;
      index_of_subject = r.indexOfValue( of.getSubjectField() );

      // if we found the subjectField in the RowMetaInterface, and we should....
      if ( index_of_subject > -1 ) {
        ValueMetaInterface vmi = r.getValueMeta( index_of_subject ).clone();
        vmi.setOrigin( origin );
        vmi.setName( of.getAggregateField() );
        fields.addValueMeta( r.size() + i, vmi );
      } else {
        // we have a condition where the subjectField can't be found from the rowMetaInterface
        StringBuilder sbfieldNames = new StringBuilder();
        String[] fieldNames = r.getFieldNames();
        for ( int j = 0; j < fieldNames.length; j++ ) {
          sbfieldNames.append( "[" + fieldNames[j] + "]" + ( j < fieldNames.length - 1 ? ", " : "" ) );
        }
        throw new KettleStepException( BaseMessages.getString( PKG, "AnalyticQueryMeta.Exception.SubjectFieldNotFound",
            getParentStepMeta().getName(), of.getSubjectField(), sbfieldNames.toString() ) );
      }
    }

    r.clear();
    // Add back to Row Meta
    r.addRowMeta( fields );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( "      <group>" ).append( Const.CR );
    for ( int i = 0; i < groupField.length; i++ ) {
      retval.append( "        <field>" ).append( Const.CR );
      retval.append( "          " ).append( XMLHandler.addTagValue( "name", groupField[i] ) );
      retval.append( "        </field>" ).append( Const.CR );
    }
    retval.append( "      </group>" ).append( Const.CR );

    retval.append( "      <fields>" ).append( Const.CR );
    for ( int i = 0; i < outputFields.length; i++ ) {
      retval.append( "        <field>" ).append( Const.CR );
      retval.append( "          " ).append( XMLHandler.addTagValue( "aggregate", outputFields[i]
          .getAggregateField() ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "subject", outputFields[i].getSubjectField() ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "type", getTypeDesc( outputFields[i]
          .getAggregateType() ) ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "valuefield", outputFields[i].getValueField() ) );
      retval.append( "        </field>" ).append( Const.CR );
    }
    retval.append( "      </fields>" ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {

      int groupsize = rep.countNrStepAttributes( id_step, "group_name" );
      int nrvalues = rep.countNrStepAttributes( id_step, "aggregate_name" );

      allocate( groupsize, nrvalues );

      for ( int i = 0; i < groupsize; i++ ) {
        groupField[i] = rep.getStepAttributeString( id_step, i, "group_name" );
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        OutputField of = new OutputField();
        outputFields[i] = of;
        of.setAggregateField( rep.getStepAttributeString( id_step, i, "aggregate_name" ) );
        of.setSubjectField( rep.getStepAttributeString( id_step, i, "aggregate_subject" ) );
        of.setAggregateType( getType( rep.getStepAttributeString( id_step, i, "aggregate_type" ) ) );
        of.setValueField( (int) rep.getStepAttributeInteger( id_step, i, "aggregate_value_field" ) );
      }

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "AnalyticQueryMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {

      for ( int i = 0; i < groupField.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "group_name", groupField[i] );
      }

      for ( int i = 0; i < outputFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "aggregate_name", outputFields[i].getAggregateField() );
        rep.saveStepAttribute( id_transformation, id_step, i, "aggregate_subject", outputFields[i].getSubjectField() );
        rep.saveStepAttribute( id_transformation, id_step, i, "aggregate_type", getTypeDesc( outputFields[i]
            .getAggregateType() ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "aggregate_value_field", outputFields[i]
            .getValueField() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "AnalyticQueryMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "AnalyticQueryMeta.CheckResult.ReceivingInfoOK" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "AnalyticQueryMeta.CheckResult.NoInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new AnalyticQuery( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new AnalyticQueryData();
  }

  public TransformationType[] getSupportedTransformationTypes() {
    return new TransformationType[] { TransformationType.Normal, };
  }

  public static class OutputField {
    /** Name of OUTPUT fieldname "MYNEWLEADFUNCTION" */
    @Injection( name = "AGGREGATE_FIELD" )
    private String aggregateField;
    /** Name of the input fieldname it operates on "ORDERTOTAL" */
    @Injection( name = "SUBJECT_FIELD" )
    private String subjectField;
    /** Aggregate type (LEAD/LAG, etc) */
    @Injection( name = "AGGREGATE_TYPE" )
    private int aggregateType;
    /** Offset "N" of how many rows to go forward/back */
    @Injection( name = "VALUE_FIELD" )
    private int valueField;
    

    /**
     * @return Returns the aggregateField.
     */
    public String getAggregateField() {
      return aggregateField;
    }

    /**
     * @param aggregateField
     *          The aggregateField to set.
     */
    public void setAggregateField( String aggregateField ) {
      this.aggregateField = aggregateField;
    }

    /**
     * @return Returns the aggregateTypes.
     */
    public int getAggregateType() {
      return aggregateType;
    }

    /**
     * @param aggregateType
     *          The aggregateType to set.
     */
    public void setAggregateType( int aggregateType ) {
      this.aggregateType = aggregateType;
    }

    /**
     * @return Returns the subjectField.
     */
    public String getSubjectField() {
      return subjectField;
    }

    /**
     * @param subjectField
     *          The subjectField to set.
     */
    public void setSubjectField( String subjectField ) {
      this.subjectField = subjectField;
    }

    /**
     * @return Returns the valueField.
     */
    public int getValueField() {
      return valueField;
    }

    /**
     * @param The
     *          valueField to set.
     */
    public void setValueField( int valueField ) {
      this.valueField = valueField;
    }

  }
}
