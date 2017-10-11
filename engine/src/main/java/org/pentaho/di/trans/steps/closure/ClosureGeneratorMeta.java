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

package org.pentaho.di.trans.steps.closure;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
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

/*
 * Created on 19-Sep-2007
 *
 */
public class ClosureGeneratorMeta extends BaseStepMeta implements StepMetaInterface {
  private boolean rootIdZero;

  private String parentIdFieldName;
  private String childIdFieldName;
  private String distanceFieldName;

  public ClosureGeneratorMeta() {
    super();
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  @Override
  public Object clone() {
    ClosureGeneratorMeta retval = (ClosureGeneratorMeta) super.clone();
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      parentIdFieldName = XMLHandler.getTagValue( stepnode, "parent_id_field" );
      childIdFieldName = XMLHandler.getTagValue( stepnode, "child_id_field" );
      distanceFieldName = XMLHandler.getTagValue( stepnode, "distance_field" );
      rootIdZero = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "is_root_zero" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public void setDefault() {
  }

  @Override
  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // The output for the closure table is:
    //
    // - parentId
    // - childId
    // - distance
    //
    // Nothing else.
    //
    RowMetaInterface result = new RowMeta();
    ValueMetaInterface parentValueMeta = row.searchValueMeta( parentIdFieldName );
    if ( parentValueMeta != null ) {
      result.addValueMeta( parentValueMeta );
    }

    ValueMetaInterface childValueMeta = row.searchValueMeta( childIdFieldName );
    if ( childValueMeta != null ) {
      result.addValueMeta( childValueMeta );
    }

    ValueMetaInterface distanceValueMeta = new ValueMetaInteger( distanceFieldName );
    distanceValueMeta.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH );
    result.addValueMeta( distanceValueMeta );

    row.clear();
    row.addRowMeta( result );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "parent_id_field", parentIdFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "child_id_field", childIdFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "distance_field", distanceFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "is_root_zero", rootIdZero ) );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      parentIdFieldName = rep.getStepAttributeString( id_step, "parent_id_field" );
      childIdFieldName = rep.getStepAttributeString( id_step, "child_id_field" );
      distanceFieldName = rep.getStepAttributeString( id_step, "distance_field" );
      rootIdZero = rep.getStepAttributeBoolean( id_step, "is_root_zero" );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "parent_id_field", parentIdFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "child_id_field", childIdFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "distance_field", distanceFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "is_root_zero", rootIdZero );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    ValueMetaInterface parentValueMeta = prev.searchValueMeta( parentIdFieldName );
    if ( parentValueMeta != null ) {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_ERROR, "The fieldname of the parent id could not be found.",
          stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_OK, "The fieldname of the parent id could be found", stepMeta );
      remarks.add( cr );
    }

    ValueMetaInterface childValueMeta = prev.searchValueMeta( childIdFieldName );
    if ( childValueMeta != null ) {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_ERROR, "The fieldname of the child id could not be found.",
          stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_OK, "The fieldname of the child id could be found", stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ClosureGenerator( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new ClosureGeneratorData();
  }

  /**
   * @return the rootIdZero
   */
  public boolean isRootIdZero() {
    return rootIdZero;
  }

  /**
   * @param rootIdZero
   *          the rootIdZero to set
   */
  public void setRootIdZero( boolean rootIdZero ) {
    this.rootIdZero = rootIdZero;
  }

  /**
   * @return the parentIdFieldName
   */
  public String getParentIdFieldName() {
    return parentIdFieldName;
  }

  /**
   * @param parentIdFieldName
   *          the parentIdFieldName to set
   */
  public void setParentIdFieldName( String parentIdFieldName ) {
    this.parentIdFieldName = parentIdFieldName;
  }

  /**
   * @return the childIdFieldName
   */
  public String getChildIdFieldName() {
    return childIdFieldName;
  }

  /**
   * @param childIdFieldName
   *          the childIdFieldName to set
   */
  public void setChildIdFieldName( String childIdFieldName ) {
    this.childIdFieldName = childIdFieldName;
  }

  /**
   * @return the distanceFieldName
   */
  public String getDistanceFieldName() {
    return distanceFieldName;
  }

  /**
   * @param distanceFieldName
   *          the distanceFieldName to set
   */
  public void setDistanceFieldName( String distanceFieldName ) {
    this.distanceFieldName = distanceFieldName;
  }
}
