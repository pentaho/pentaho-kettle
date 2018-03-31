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

package org.pentaho.di.trans.steps.formula;

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
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
 * Contains the meta-data for the Formula step: calculates ad-hoc formula's Powered by Pentaho's "libformula"
 *
 * Created on 22-feb-2007
 */

public class FormulaMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = FormulaMeta.class; // for i18n purposes, needed by Translator2!!

  /** The formula calculations to be performed */
  private FormulaMetaFunction[] formula;

  public FormulaMeta() {
    super(); // allocate BaseStepMeta
  }

  public FormulaMetaFunction[] getFormula() {
    return formula;
  }

  public void setFormula( FormulaMetaFunction[] calcTypes ) {
    this.formula = calcTypes;
  }

  public void allocate( int nrCalcs ) {
    formula = new FormulaMetaFunction[nrCalcs];
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    int nrCalcs = XMLHandler.countNodes( stepnode, FormulaMetaFunction.XML_TAG );
    allocate( nrCalcs );
    for ( int i = 0; i < nrCalcs; i++ ) {
      Node calcnode = XMLHandler.getSubNodeByNr( stepnode, FormulaMetaFunction.XML_TAG, i );
      formula[i] = new FormulaMetaFunction( calcnode );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    if ( formula != null ) {
      for ( int i = 0; i < formula.length; i++ ) {
        retval.append( "       " + formula[i].getXML() + Const.CR );
      }
    }

    return retval.toString();
  }

  public boolean equals( Object obj ) {
    if ( obj != null && ( obj.getClass().equals( this.getClass() ) ) ) {
      FormulaMeta m = (FormulaMeta) obj;
      return ( getXML() == m.getXML() );
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode( formula );
  }

  public Object clone() {
    FormulaMeta retval = (FormulaMeta) super.clone();
    if ( formula != null ) {
      retval.allocate( formula.length );
      for ( int i = 0; i < formula.length; i++ ) {
        //CHECKSTYLE:Indentation:OFF
        retval.getFormula()[i] = (FormulaMetaFunction) formula[i].clone();
      }
    } else {
      retval.allocate( 0 );
    }
    return retval;
  }

  public void setDefault() {
    formula = new FormulaMetaFunction[0];
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    int nrCalcs = rep.countNrStepAttributes( id_step, "field_name" );
    allocate( nrCalcs );
    for ( int i = 0; i < nrCalcs; i++ ) {
      formula[i] = new FormulaMetaFunction( rep, id_step, i );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    for ( int i = 0; i < formula.length; i++ ) {
      formula[i].saveRep( rep, metaStore, id_transformation, id_step, i );
    }
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    for ( int i = 0; i < formula.length; i++ ) {
      FormulaMetaFunction fn = formula[i];
      if ( Utils.isEmpty( fn.getReplaceField() ) ) {
        // Not replacing a field.
        if ( !Utils.isEmpty( fn.getFieldName() ) ) {
          // It's a new field!

          try {
            ValueMetaInterface v = ValueMetaFactory.createValueMeta( fn.getFieldName(), fn.getValueType() );
            v.setLength( fn.getValueLength(), fn.getValuePrecision() );
            v.setOrigin( name );
            row.addValueMeta( v );
          } catch ( Exception e ) {
            throw new KettleStepException( e );
          }
        }
      } else {
        // Replacing a field
        int index = row.indexOfValue( fn.getReplaceField() );
        if ( index < 0 ) {
          throw new KettleStepException( "Unknown field specified to replace with a formula result: ["
            + fn.getReplaceField() + "]" );
        }
        // Change the data type etc.
        //
        ValueMetaInterface v = row.getValueMeta( index ).clone();
        v.setLength( fn.getValueLength(), fn.getValuePrecision() );
        v.setOrigin( name );
        row.setValueMeta( index, v ); // replace it
      }
    }
  }

  /**
   * Checks the settings of this step and puts the findings in a remarks List.
   *
   * @param remarks
   *          The list to put the remarks in @see org.pentaho.di.core.CheckResult
   * @param stepMeta
   *          The stepMeta to help checking
   * @param prev
   *          The fields coming from the previous step
   * @param input
   *          The input step names
   * @param output
   *          The output step names
   * @param info
   *          The fields that are used as information by the step
   */
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
          PKG, "FormulaMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FormulaMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FormulaMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "FormulaMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new Formula( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new FormulaData();
  }

}
